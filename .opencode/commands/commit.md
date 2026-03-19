---
description: 提交修改并修复 CI 问题
agent: build
---

提交所有修改到 Git，并检查修复 CI 问题：

1. 运行 `git status` 查看修改状态
2. 运行 `git diff` 查看具体改动
3. 分析改动内容，确定 commit message type（feat/fix/refactor/docs/chore）
4. 运行 `git add <files>` 暂存修改
5. 运行 `git commit -m "<type>: <description>"` 提交
6. 运行 `git push` 推送到远程

如果 CI 失败需要修复：

7. 运行 `gh run list --workflow=ci.yml -L 1` 查看最新 CI 状态
8. 运行 `gh run view <run-id> --log-failed` 获取失败日志
9. 分析错误信息，识别问题类型：
   - 编译错误：检查语法、类型、依赖
   - 测试失败：运行 `./gradlew test` 复现
   - 配置错误：检查 workflow 文件
10. 修复问题
11. 本地验证：`./gradlew compileKotlinJvm`（不需要 Android SDK）
12. 提交并推送修复

如果需要创建 PR：
```
gh pr create --title "<title>" --body "<body>"
```

如果 PR 已存在且需要启用 auto-merge：
```
gh pr merge <number> --auto --merge
```

**重要**：提交推送后立即结束，不要等待 CI 运行完成
