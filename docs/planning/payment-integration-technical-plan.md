# 真实支付接入技术方案(对应 Issue #131)

日期:2026-07-07
性质:技术方案文档,本轮不写支付代码,不接 StoreKit/Google Play SDK
前置:Trip Pass / Human Task 商品结构已经是 iOS 端的 placeholder UI(PR #108,`MeView.SubscriptionPlanCard`)

## 0. 现状:纯 UI 占位,没有真实支付能力

当前 iOS `MeView` 已经有三个商品卡片和对应的 placeholder product id,点击后只弹出一条 "StoreKit purchase placeholder" 提示,不发生任何真实扣款:

- `visepanda.trip_pass.7day` —— 7-Day Trip Pass,$9.99 一次性
- `visepanda.trip_pass.14day` —— 14-Day Trip Pass
- `visepanda.human_task.single` —— Human Task,按次购买

这份文档规划"下一步真正接上 StoreKit/后端订单/人工工单"需要做的事,给后续实现提供依据,不要求本轮就做。

## 1. StoreKit(iOS)product id 规划

沿用 placeholder 阶段已经写死在代码里的 id,不要求上线前改名(改名意味着要在 App Store Connect 重新建商品,没有实际收益):

| Product ID | 类型 | 说明 |
|---|---|---|
| `visepanda.trip_pass.7day` | Non-Consumable(一次性) | 7天行程通行证,不是订阅,一次购买覆盖一次旅行窗口 |
| `visepanda.trip_pass.14day` | Non-Consumable | 14天行程通行证 |
| `visepanda.human_task.single` | Consumable(消耗型) | 单次人工任务请求,买一次用一次,可以重复购买 |

**关键决定:不用 Auto-Renewable Subscription**。这是延续操作者已经否决的"代付/月费订阅"商业化方向(见 `project_visepanda_monetization_direction` 相关决策)——Trip Pass 是"买一次覆盖一次旅行窗口",不是持续订阅,用 Apple 的 Non-Consumable 类型语义上更贴切,也避免续费纠纷和 App Store 订阅审核的额外要求。

## 2. Google Play(Android)product id 规划

Android 当前暂停派工,这里先把命名定下来,等 Android 恢复排期时不需要重新讨论:

| Product ID | Google Play Billing 类型 | 对应 iOS |
|---|---|---|
| `trip_pass_7day` | One-time product | `visepanda.trip_pass.7day` |
| `trip_pass_14day` | One-time product | `visepanda.trip_pass.14day` |
| `human_task_single` | Consumable | `visepanda.human_task.single` |

Google Play 的 product id 命名规范不支持点号分隔(只能用小写字母/数字/下划线/连字符),所以 Android 侧用下划线风格,不是直接照抄 iOS 的 id——这是两个商店各自的命名限制,不是不一致,后端订单表用一个内部统一的 `product_key` 字段做映射(见下一节),不依赖任一商店的 product id 字符串作为唯一标识。

## 3. 后端订单表草案

新增 Supabase 表 `purchase_orders`(草案,字段和约束仅供实现参考,实现时可以调整):

```sql
create table if not exists public.purchase_orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id),
  product_key text not null,        -- 内部统一 key,如 'trip_pass_7day'/'trip_pass_14day'/'human_task_single'
  platform text not null,           -- 'ios' | 'android'
  platform_product_id text not null, -- 该平台实际的 product id 字符串
  platform_transaction_id text not null, -- StoreKit transaction id / Google Play purchase token
  status text not null default 'pending', -- 'pending' | 'verified' | 'fulfilled' | 'refunded' | 'failed'
  amount_cents integer,
  currency text,
  purchased_at timestamptz,
  fulfilled_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint purchase_orders_platform_check check (platform in ('ios', 'android')),
  constraint purchase_orders_status_check check (status in ('pending', 'verified', 'fulfilled', 'refunded', 'failed'))
);

create unique index if not exists purchase_orders_platform_transaction_idx
  on public.purchase_orders (platform, platform_transaction_id);
```

**状态机**:`pending`(客户端本地记录已发起购买)→ `verified`(服务端验证过收据/购买令牌真实有效,防止客户端伪造"我买过了")→ `fulfilled`(履约完成——Trip Pass 是"标记这个时间窗口的功能已解锁",Human Task 是"工单已创建")。`refunded`/`failed` 是异常终态。

**收据验证**:iOS 用 App Store Server API 验证 transaction,Android 用 Google Play Developer API 验证 purchase token——这一步必须在服务端做,不能只信任客户端上报的"我买了"。

## 4. Restore Purchase 流程草案

1. 用户点击 "Restore Purchase"(iOS `MeView` 已有占位按钮)。
2. 客户端调用 StoreKit 的 `AppStore.sync()`(或对应 API)拉取该 Apple ID 下的历史交易。
3. 客户端把拉到的 transaction id 列表发给后端一个新端点(草案:`POST /api/purchases/restore`),后端逐个核对 `purchase_orders` 表里是否已有记录,没有的话补建(可能是换设备场景)。
4. 后端返回该用户当前有效的 Trip Pass/Human Task 额度,客户端据此更新 UI(比如"你已经有一个有效的 7天Pass,截止到 XX")。

Human Task(消耗型)一般不需要 Restore——买一次用一次,历史记录只是账务追踪,不代表"可以再用一次"。

## 5. Human Task 工单流草案

Trip Pass 是纯软件解锁(不需要人工介入),Human Task 需要真人处理,流程草案:

1. 用户购买 `human_task_single` 并选择任务类型(打电话确认营业/餐厅酒店沟通/预约协助/简单翻译转述——边界已经在 `MeView` 现有文案里写明,不承诺紧急医疗/法律签证结果/金钱代付)。
2. 支付验证通过后(`purchase_orders.status = 'verified'`),后端在一张新的 `human_task_tickets` 表(草案,不在本文档展开完整 schema)里创建一条工单,状态 `open`。
3. 人工(操作者本人或未来的客服团队)在某个后台界面(可以先是最简单的 Supabase Table Editor 手动处理,不需要专门开发一个后台管理系统)认领工单,完成后标记 `done`,填写结果摘要。
4. 客户端轮询或者(更好)通过现有的 Chat 消息机制把结果推给用户——工单完成时,给用户发一条 assistant 消息通知结果,复用现有的消息展示逻辑,不需要额外的通知系统。
5. `purchase_orders.fulfilled_at` 在工单标记完成时同步写入。

## 6. 本轮不做的事(明确边界)

- 不接入真实 StoreKit/Google Play Billing SDK。
- 不建 `purchase_orders`/`human_task_tickets` 表(草案供以后参考,不是本轮的 migration)。
- 不实现收据验证服务端逻辑。
- 不做工单后台管理界面。

真正开始接入时,建议先做 iOS + Trip Pass(最简单,没有人工介入),验证完整链路(购买→服务端验证→解锁)后再扩展到 Human Task 和 Android。
