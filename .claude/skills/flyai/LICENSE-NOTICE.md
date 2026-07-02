# 来源与使用边界说明

本目录下的 `SKILL.md` 与 `references/*.md` 原文 vendor 自:

- 上游仓库:`https://github.com/alibaba-flyai/flyai-skill`
- 路径:`skills/flyai/`
- 许可证:MIT License,Copyright (c) 2026 alibaba-flyai
- Vendor 时间:随 VisePanda v0.2.6 迭代加入

## 这是什么

一个 Claude Code / OpenClaw 兼容的 Agent Skill,通过 `flyai-cli`
(npm 包 `@fly-ai/flyai-cli`)调用飞猪(阿里)官方 MCP 服务,提供机票、
火车票、酒店、景点门票等旅行数据的自然语言/结构化搜索。

## 使用边界(务必遵守)

- **仅供本仓库的开发/规划阶段使用**,例如:丰富 Explore/Tools 静态
  fallback 内容、验证行程文案的事实合理性、为未来真实 provider 设计数据
  模型时参考样例 JSON。
- **禁止**在任何 VisePanda 生产代码(`/api/*` 路由、`lib/**/*.ts`)中调用
  `flyai` CLI 或提取其内嵌的默认试用凭证发起请求。原因与生产集成路径,
  见 `docs/planning/flyai-skill-integration.md`。
- 生产级真实预订数据集成需要飞猪官方的服务端合作确认,在此之前不得
  绕过官方渠道自行接入。

## 如何在开发时使用

```bash
npm i -g @fly-ai/flyai-cli
flyai keyword-search --query "杭州三日游"
flyai search-hotel --dest-name "杭州" --poi-name "西湖"
```

每个子命令的精确参数,请查看对应的 `references/<command>.md`。
