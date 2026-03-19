---
description: 检查 CI 状态并修复失败
agent: build
---

检查 CI 失败原因并尝试修复：

1. 运行 `gh run list --workflow=ci.yml -L 1` 查看最新 CI 状态
2. 如果失败，运行 `gh run view <run-id> --log-failed` 获取失败日志
3. 分析错误信息，识别问题类型：
   - 编译错误：检查语法、类型、依赖
   - 测试失败：运行 `./gradlew test` 复现
   - 配置错误：检查 workflow 文件
4. 修复问题
5. 本地验证：`./gradlew compileKotlinJvm`（不需要 Android SDK）
6. 提交并推送修复
