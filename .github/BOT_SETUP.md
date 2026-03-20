# GitHub Bot 配置指南

本指南说明如何创建专用的 GitHub Bot 用户并为其配置自动化工作流。

## 概述

使用专用 Bot 账户而非个人令牌可以带来：
- 更好的安全隔离
- 清晰的审计追踪
- 更简单的令牌轮换
- 最小权限原则

## 步骤 1：创建 Bot 用户

1. 为 Bot 创建一个新的 GitHub 账户（例如 `your-org-bot`）
2. 使用专用邮箱（例如 `bot@yourdomain.com`）
3. 启用双因素认证（2FA）
4. **不要**将此账户添加到任何组织成员资格中，除非需要

## 步骤 2：生成个人访问令牌（PAT）

1. 以 Bot 用户身份登录
2. 导航至 **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
3. 点击 **Generate new token (classic)**
4. 配置令牌：
   - **Note**: `DOC_BOT_TOKEN - Automated documentation workflows`
   - **Expiration**: 设置为 90 天（定期轮换）
   - **Scopes**（仅授予以下权限）：
     - ✅ `repo` - 完全控制私有仓库（仓库访问必需）
     - ✅ `workflow` - 更新 GitHub Action 工作流
     - ❌ **不要**授予：`admin`、`delete_repo`、`user`、`gist`、`notifications`

5. 点击 **Generate token**
6. **重要**：立即复制令牌（不会再显示）

## 步骤 3：添加仓库密钥

将以下密钥添加到您的仓库：

### 必需密钥

| 密钥名称 | 值 | 用途 |
|-------------|-------|---------|
| `DOC_BOT_TOKEN` | 步骤 2 中的 Bot PAT | 自动化工作流的身份验证 |
| `OPENAI_API_KEY` | 您的 OpenAI API 密钥 | AI 智能体功能（如果使用 AI 功能） |

### 如何添加密钥

1. 进入 GitHub 上的您的仓库
2. 导航至 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret**
4. 添加每个密钥：
   - **Name**: `DOC_BOT_TOKEN`
   - **Value**: 粘贴 Bot 的 PAT
   - 点击 **Add secret**
5. 对 `OPENAI_API_KEY` 重复上述步骤

## 步骤 4：验证

测试 Bot 令牌是否正常工作：

```bash
# 使用 curl 测试 API 访问
export TOKEN="your_bot_token_here"
export REPO="owner/repo-name"

# 验证令牌可以读取仓库
curl -H "Authorization: token $TOKEN" \
     -H "Accept: application/vnd.github.v3+json" \
     https://api.github.com/repos/$REPO

# 验证工作流权限
curl -H "Authorization: token $TOKEN" \
     -H "Accept: application/vnd.github.v3+json" \
     https://api.github.com/repos/$REPO/actions/workflows

# 测试 git 操作（如果需要）
git clone https://x-access-token:$TOKEN@github.com/$REPO.git
```

预期输出：包含仓库/工作流信息的 JSON 响应（不是 401/403 错误）。

## 步骤 5：在 GitHub Actions 中使用

示例工作流用法：

```yaml
name: Documentation Update
on: [push]

jobs:
  update-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          token: ${{ secrets.DOC_BOT_TOKEN }}
      
      - name: Run documentation workflow
        env:
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
        run: |
          # 您的文档命令在这里
          echo "Bot token configured successfully"
```

## 安全最佳实践

### 应该做：
- ✅ 使用专用 Bot 账户
- ✅ 授予最小必需权限
- ✅ 每 90 天轮换令牌
- ✅ 监控 Bot 账户活动
- ✅ 使用仓库范围的密钥（除非需要，否则不要使用组织范围的）

### 不应该做：
- ❌ 使用个人令牌进行自动化
- ❌ 授予 admin 或 delete_repo 权限
- ❌ 将令牌提交到代码库（始终使用密钥）
- ❌ 通过不安全渠道共享 Bot 凭据
- ❌ 保留过期的令牌

## 令牌轮换

轮换 Bot 令牌时：

1. 按照步骤 2 生成新的 PAT
2. 在仓库设置中更新 `DOC_BOT_TOKEN` 密钥
3. 从 Bot 账户中删除旧令牌
4. 验证工作流仍然正常运作

## 故障排除

| 问题 | 解决方案 |
|-------|----------|
| 401 Unauthorized | 令牌无效或已过期 - 重新生成 |
| 403 Forbidden | 缺少必需权限 - 检查令牌权限 |
| Workflow 失败 | 验证密钥名称完全匹配（`DOC_BOT_TOKEN`） |
| Git push 失败 | 确保已授予 `repo` 权限 |

## 参考

- [GitHub 个人访问令牌](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
- [GitHub Actions 密钥](https://docs.github.com/en/actions/security-guides/using-secrets-in-actions)
- [GitHub API 身份验证](https://docs.github.com/en/rest/overview/other-authentication-methods)
