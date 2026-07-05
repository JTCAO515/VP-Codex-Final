# Trip.com 酒店预订对接调研

调研日期: 2026-07-05

## 结论

Trip.com/携程这条线本轮不建议直接实现酒店预订 API。

可公开自助使用的是 Affiliate Program: 生成可追踪酒店/机票等推广链接,订单完成后按 CPS 结算佣金。完整酒店查询、房态、价格、下单、支付、取消等 Open API 没有公开免申请文档,入口在 Trip.com Developers/Open platform 登录和联系流程之后,需要业务/合规审核。

因此 VisePanda 现阶段应继续保持 `BookingCandidate.status = "info-only"`。如果要进入真实可订酒店,需要操作者(项目方)先出面申请 Trip.com/携程合作账号和 API 权限,拿到正式文档、测试环境、合同和结算条款后再排实现。

## 公开入口

- Trip.com Affiliate Program: https://www.trip.com/partners
- Trip.com Developers: https://developers.trip.com/
- Trip.com Open platform: https://connect.trip.com/
- 携程加盟合作: https://pages.c-ctrip.com/cooperation/web/cooperation.html

Trip.com Developers 首页公开列出 Hotels、Flights、Trains、Tours & Tickets、Car Rentals、Business Travels 等方向,但页面提示需要登录查看更多信息。Open platform 公开流程为:提交资料、PCI/PII 合规审核、对接 connectivity team、提供所需文件、集成和测试。

## 准入门槛评估

### Affiliate 链接模式

门槛较低,适合先做跳转变现:

- 注册 Trip.com affiliate 账号
- 填写 profile details
- 在 dashboard 里生成酒店、航班或首页的 tracking link
- 用户点击链接并完成预订后产生佣金

这类模式不需要 VisePanda 处理酒店库存、房态、支付、取消或用户信用卡信息。代价是体验只能跳转到 Trip.com 完成,App 内不能保证实时价格或可订状态。

### Open API / 酒店预订模式

门槛较高,不适合在没有商务准入的情况下开工:

- 需要联系 Trip.com/携程或登录开发者平台
- 需要 PCI/PII 合规审核
- 需要提供合作方资料和所需文件
- 需要 connectivity team 介入
- 生产前通常需要集成、测试、验收

公开页面没有给出可匿名调用的酒店搜索/预订 API endpoint,也没有公开测试 key。不能为了推进代码而模拟一套字段。

## 酒店相关接口可见度

公开信息只能确认 Trip.com 有 Hotels 业务和面向合作伙伴的开发者入口;无法确认具体接口字段。

合理预期的后续 API 范围包括:

- 目的地/酒店搜索
- 酒店详情和图片
- 房型、房价、税费、取消政策
- 可订性校验
- 订单创建
- 支付或支付跳转
- 取消/退款
- 订单查询

这些字段必须以 Trip.com/携程正式文档为准。本轮不在代码里预埋类型,避免未来正式文档不一致时返工。

## 分佣和结算

公开 Affiliate 资料显示酒店佣金按完成订单阶梯计算:

- 0-199 单:5%
- 200-999 单:6%
- 1000+ 单:7%

结算公开说明:

- 通过 USD 或 HKD 银行转账
- 最低打款门槛为 200 USD 或 1500 HKD
- 佣金基于已完成行程;取消或 no-show 不结算
- 常见到账周期约 40-60 个工作日

Open API/B2B 预订模式的佣金、净价、账期、押金或授信条款未公开,需要商务合同确认。

## 是否有免申请只读查询 API

未发现 Trip.com/携程官方公开的免申请酒店只读查询 API。

Trip.com 页面可公开搜索酒店,Affiliate dashboard 可生成推广链接,但这不是面向服务端实时检索的开放 API。抓网页或逆向接口不符合项目稳定性和合规要求,不应采用。

## 后续方案草案

### 短期:Affiliate 跳转

拿到 affiliate tracking 配置后,可以把酒店 `BookingCandidate` 从纯 `info-only` 扩展为外链跳转:

- 仍由 Explore/Butler 展示本地/高德/编辑精选酒店信息
- CTA 使用 Trip.com affiliate deep link 或搜索页 tracking link
- 不展示实时可订价,只写“在 Trip.com 查看实时价格”
- 不处理支付和订单

这是最小可行商业化路径。

### 中期:正式 Open API

项目方申请并拿到正式权限后再实现:

- 服务端新增酒店 provider,密钥只在服务端
- 后端做 destination/date/occupancy 到 Trip.com 字段的映射
- 搜索结果必须显示价格时间戳和供应商
- 下单前必须二次 price/availability check
- 订单、取消、退款必须有明确状态机
- 移动端仍通过 VisePanda 后端,不直连 Trip.com API

## 风险

- 酒店预订涉及价格、房态、取消政策和支付,不能用静态数据近似。
- 若进入 merchant/API 预订模式,PCI/PII 合规和客服责任会显著增加。
- Affiliate 跳转体验较轻,但用户离开 VisePanda 完成交易,转化和订单状态不可完全掌控。

## Sources

- Trip.com Developers: https://developers.trip.com/
- Trip.com Open platform: https://connect.trip.com/
- Trip.com Affiliate guide: https://www.trip.com/ask/questions/trip.com-affliate-program.html
- 携程加盟合作: https://pages.c-ctrip.com/cooperation/web/cooperation.html
