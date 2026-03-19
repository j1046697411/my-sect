# 架构决策记录 (ADR)

本目录包含项目的架构决策记录。

## ADR 列表

| 编号 | 标题 | 日期 | 状态 |
|------|------|------|------|
| [001](001-document-management-system.md) | Document Management System | 2026-03-19 | accepted |

## 什么是 ADR

架构决策记录 (Architecture Decision Record, ADR) 是一种记录重要架构决策及其背景的文档。每个 ADR 包含：

- **背景**：为什么需要做出这个决策
- **选项**：考虑过的备选方案
- **决策**：最终选择的方案
- **后果**：决策带来的影响

## 创建新的 ADR

1. 复制 `000-template.md` 作为新 ADR 的基础
2. 使用下一个连续的编号（如 002、003...）
3. 填写所有四个必需部分
4. 运行 `.github/scripts/update-adr-index.sh` 自动更新索引

## 参考

- [Michael Nygard 的 ADR 格式](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
