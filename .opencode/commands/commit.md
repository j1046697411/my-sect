---
description: 提交修改到 Git 并创建 PR
agent: build
---

提交所有修改到 Git 并创建 PR：

1. 运行 `git status` 查看修改状态
2. 运行 `git fetch origin` 拉取远程最新状态
3. 检查当前分支是否已有 PR：运行 `gh pr list --head <branch-name>`
   - 如果有 PR：
     - 运行 `gh run list --workflow=ci.yml -L 1` 检查 CI 状态
     - 如果 CI 失败：运行 `gh run view <run-id> --log-failed` 获取失败日志
     - 运行 `gh api repos/<owner>/<repo>/pulls/<pr_number>/comments` 获取 PR 评论
     - 将评论和 CI 问题添加到上下文，理解需要修复的问题
     - 运行 `git pull origin <branch-name>` 拉取 PR 最新代码
4. 在开始开发前拉取主分支最新代码：
   - 运行 `git pull origin <main-branch>` 拉取主分支最新代码
   - 如果有冲突：解决冲突后继续
   - 这一步确保你基于最新代码进行开发，避免后续提交时发现大范围冲突
5. 分析是否有需要修复的问题：
   - 如果有 CI 失败：修复编译/配置问题
   - 如果有 PR 评论：按评论要求修复
   - 修复后运行 `./gradlew compileKotlinJvm` 验证
6. 如果有本地修改：
   - 运行 `git diff` 查看具体改动
   - 分析改动内容
   - 在本地运行 `./gradlew compileKotlinJvm` 验证编译（不需要 Android SDK）
   - 如果编译失败，修复问题后重新验证，直到通过
   - 运行 `./gradlew check` 运行所有检查（测试、lint 等）
   - 如果检查失败，修复问题后重新验证，直到通过
   - 确认所有修改已通过本地验证
   - 再次运行 `git pull origin <main-branch>` 拉取主分支最新代码（解决可能的冲突）
   - 运行 `git add <files>` 暂存修改
   - 运行 `git commit -m "<type>: <description>"` 提交
7. 如果没有 PR：运行 `gh pr create --title "<title>" --body "<body>"` 创建 PR
8. 如果已有 PR 或创建 PR 后：直接推送 `git push`
9. 推送后立即结束，不要等待 CI 运行完成

强制规则：
- 修复问题必须找到根本原因，禁止写代码绕过去（如 suppress 警告、删除测试、强制类型转换等）
- 如果暂时无法修复，保留原问题并说明原因，不能用临时方案掩盖

注意：
- commit message type：feat/fix/refactor/docs/chore
- 如果 PR 已存在且需要启用 auto-merge：运行 `gh pr merge <number> --auto --merge`
