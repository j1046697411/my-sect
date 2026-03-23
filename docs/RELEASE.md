# 发布流程

本文档描述项目发布的标准流程，确保所有发布步骤一致且可重复。

## 发布前检查清单

在创建版本标签之前，请完成以下检查项：

- [ ] 运行 `./gradlew check` 确保所有测试通过且代码检查无错误
- [ ] 更新 `CHANGELOG.md` 文件，添加新版本的变更记录
- [ ] 确认 `gradle.properties` 中的 `version.name` 已更新为新版本号
- [ ] 审查自上次发布以来的所有变更，确保无遗漏
- [ ] 在团队内部沟通即将发布的版本，通知相关人员
- [ ] 确认所有待发布的 PR 已合并到主分支
- [ ] 检查依赖版本是否有安全漏洞

## 打 Tag 流程

完成检查清单后，按以下步骤创建版本标签：

```bash
# 1. 确保本地主分支是最新的
git checkout main
git pull origin main

# 2. 创建附注标签
git tag -a v1.0.0 -m "Release v1.0.0"

# 3. 推送到远程仓库
git push origin v1.0.0
```

**注意**：请将 `v1.0.0` 替换为实际版本号。版本号遵循语义化版本规范。

## 创建 GitHub Release

标签推送后，GitHub 会自动触发 CI/CD 流水线。发布版本需要手动创建 GitHub Release：

### 方式一：使用 GitHub Web 界面

1. 访问项目的 Releases 页面：https://github.com/[owner]/[repo]/releases
2. 点击 "Draft a new release"
3. 选择刚推送的标签
4. 填写版本发布标题和说明
5. 链接到 `CHANGELOG.md` 中对应的版本章节
6. 点击 "Publish release"

### 方式二：使用 gh CLI

```bash
# 使用 gh CLI 创建 Release
gh release create v1.0.0 \
  --title "v1.0.0" \
  --notes "详见 CHANGELOG.md" \
  --target main
```

## 发布后步骤

发布完成后，执行以下操作：

- [ ] 验证 CI/CD 流水线成功运行
- [ ] 检查构建产物是否正确生成并发布
- [ ] 如需内部通知，发送版本发布公告
- [ ] 监控错误监控系统，确保新版本无异常
- [ ] 更新相关文档（如有 API 变更）

## 回滚流程

如发布后发现严重问题需要回滚：

```bash
# 删除本地标签
git tag -d v1.0.0

# 删除远程标签
git push origin --delete v1.0.0

# 如需重新发布，修复问题后从"打 Tag 流程"重新开始
```
