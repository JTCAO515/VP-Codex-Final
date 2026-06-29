# VisePanda 部署后手动操作指南（中文教程）

本教程面向没有数据库/后台经验的用户，按顺序照做即可，不需要写代码。

## 一、执行 `0002_trip_archive_and_share.sql` 数据库迁移

这一步是给已有的 Supabase 项目"追加"一段数据库设置，用来支持行程的"归档"状态和"分享链接"功能。如果不做这一步，归档和分享按钮点击后会报错。

1. 打开浏览器，登录 [supabase.com](https://supabase.com)，进入你之前为 VisePanda 创建的项目。
2. 在左侧菜单里找到并点击 **SQL Editor**（图标通常像一个终端/命令行符号）。
3. 点击右上角的 **New query**（新建查询），会打开一个空白的代码编辑区域。
4. 打开本项目仓库里的文件 `supabase/migrations/0002_trip_archive_and_share.sql`，用任意文本编辑器（比如记事本、VS Code）打开它，全选（Ctrl+A）并复制（Ctrl+C）里面的全部内容。
5. 回到 Supabase 的 SQL Editor 页面，点击刚才打开的空白编辑区域，粘贴（Ctrl+V）刚才复制的内容。
6. 确认粘贴的内容完整（开头应该是 `alter table` 或类似的 SQL 语句，不要漏掉开头或结尾）。
7. 点击编辑器右下角（或右上角，具体位置随 Supabase 版本可能略有不同）的 **Run** 按钮（绿色，通常带一个三角形播放图标）。
8. 如果下方出现绿色的 "Success" 提示，说明迁移执行成功。如果出现红色报错，请把报错内容截图发给开发者排查，不要重复点击 Run。
9. 这一步只需要执行一次。执行成功后，App 里的"Mark as Ready / Archive / Restore from archive"和"Get share link / Revoke share link"按钮就可以正常使用了。

## 二、在 Supabase 启用 Google 登录（本轮 v0.1.16 新增需求）

本轮把网站右上角的账号入口改成了一个图标 + 悬浮窗口，里面新增了"使用 Google 账号登录"的按钮。**邮箱+密码登录不需要额外设置，开箱即用**；但"使用 Google 登录"需要你在 Supabase 后台手动启用一次，否则用户点击该按钮会看到报错。

### 第一步：在 Google Cloud 创建一个 OAuth 客户端

1. 打开浏览器，访问 [Google Cloud Console](https://console.cloud.google.com/)，用你管理这个产品的 Google 账号登录。
2. 如果还没有项目，先在顶部点击项目下拉菜单，点击 **New Project**（新建项目），随便起个名字（比如 `VisePanda`），点击创建，等待几秒钟切换到这个新项目。
3. 在左侧菜单或顶部搜索框中找到 **APIs & Services**（API 和服务），点击进入。
4. 点击左侧的 **OAuth consent screen**（OAuth 同意屏幕）。
   - User Type 选择 **External**（外部），点击 Create。
   - App name 填 `VisePanda`，User support email 选你自己的邮箱。
   - 下面的 Developer contact information 也填你自己的邮箱。
   - 一路点击 **Save and Continue**，中间的 Scopes、Test users 页面可以先跳过不填，最后点击 **Back to Dashboard**。
5. 点击左侧的 **Credentials**（凭据），点击顶部的 **+ Create Credentials**，选择 **OAuth client ID**。
6. Application type 选择 **Web application**。
7. Name 随便填，比如 `VisePanda Web`。
8. 找到 **Authorized redirect URIs**（已获授权的重定向 URI），点击 **+ Add URI**，填入下面这个地址（把 `<你的项目ID>` 换成你自己 Supabase 项目的实际 ID，可以在 Supabase 项目设置页面的 Project URL 里看到，形如 `https://xxxxxxxx.supabase.co`）：
   ```
   https://<你的项目ID>.supabase.co/auth/v1/callback
   ```
9. 点击 **Create**。完成后会弹出一个窗口，显示 **Client ID** 和 **Client Secret** 两串字符，先不要关闭这个窗口，把这两个值复制保存到一个安全的地方（比如本地一个临时文本文件），下一步要用。

### 第二步：把 Client ID / Secret 填进 Supabase

1. 回到 [supabase.com](https://supabase.com)，进入你的 VisePanda 项目。
2. 在左侧菜单找到 **Authentication**（身份认证），点击进入。
3. 找到 **Providers**（登录方式）标签页，点击进入。
4. 在登录方式列表里找到 **Google**，点击展开。
5. 打开 **Enable Sign in with Google** 这个开关（设为开启）。
6. 把第一步保存的 **Client ID** 粘贴到 "Client ID" 输入框。
7. 把第一步保存的 **Client Secret** 粘贴到 "Client Secret" 输入框。
8. 点击 **Save**（保存）。
9. 完成后，回到网站，点击右上角账号图标打开悬浮窗口，点击 **Continue with Google** 按钮测试，应该会跳转到 Google 账号选择页面，选完账号后会自动跳回网站并显示已登录。

### 注意事项

- 如果只想先用邮箱密码登录，可以先跳过整个"第二步"，邮箱密码登录已经可以正常工作，不影响其他功能。
- Client Secret 是敏感信息，不要把它粘贴到聊天记录、公开文档或提交到代码仓库里；只能填在 Supabase 后台的对应输入框里。
- 如果生产环境域名（`go2china.space`）和 Supabase 项目地址不一致导致跳转后显示空白页或报错，检查一下浏览器地址栏跳转回来的网址是否和你预期的生产域名一致；如有问题可以把截图发给开发者排查。
