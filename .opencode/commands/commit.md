---
description: 提交所有修改到 Git
agent: build
---

提交所有当前修改到 Git：

1. 运行 `git status` 查看修改状态
2. 运行 `git diff` 查看具体改动
3. 分析改动内容，确定 commit message type（feat/fix/refactor/docs/chore）
4. 运行 `git add <files>` 暂存修改
5. 运行 `git commit -m "<type>: <description>"` 提交
6. 运行 `git push` 推送到远程

如果需要创建 PR，运行：
```
gh pr create --title "<title>" --body "<body>"
```

如果 PR 已存在且需要启用 auto-merge：
```
gh pr merge <number> --auto --merge
```
