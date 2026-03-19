---
description: 提交修改到 Git 并创建 PR
agent: build
---

提交所有修改到 Git 并创建 PR：

1. 运行 `git status` 查看修改状态
2. 如果有修改：
   - 运行 `git diff` 查看具体改动
   - 分析改动内容
   - 在本地运行 `./gradlew compileKotlinJvm` 验证编译（不需要 Android SDK）
   - 如果编译失败，修复问题后重新验证，直到通过
   - 运行 `git add <files>` 暂存修改
   - 运行 `git commit -m "<type>: <description>"` 提交
 3. 如果没有修改：
   - 运行 `git fetch origin` 拉取远程最新状态
   - 运行 `git pull origin <branch-name>` 拉取 PR 最新代码
   - 运行 `gh pr list --head <branch-name>` 获取 PR 编号
   - 运行 `gh api repos/<owner>/<repo>/pulls/<pr_number>/comments` 获取 PR 评论
   - 将评论内容添加到上下文，理解需要修复的问题
   - 运行 `git status` 再次确认是否有修改需要提交
4. 检查当前分支是否已有 PR：运行 `gh pr list --head <branch-name>`
   - 如果没有 PR：运行 `gh pr create --title "<title>" --body "<body>"` 创建 PR
   - 如果已有 PR：直接推送 `git push`
5. 推送后立即结束，不要等待 CI 运行完成

注意：
- commit message type：feat/fix/refactor/docs/chore
- 如果 PR 已存在且需要启用 auto-merge：运行 `gh pr merge <number> --auto --merge`
