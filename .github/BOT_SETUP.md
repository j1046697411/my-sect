# GitHub Bot Setup Guide

This guide documents how to create a dedicated GitHub Bot user and configure it for automated workflows.

## Overview

Using a dedicated bot account instead of personal tokens provides:
- Better security isolation
- Clear audit trails
- Easier token rotation
- Principle of least privilege

## Step 1: Create Bot User

1. Create a new GitHub account for the bot (e.g., `your-org-bot`)
2. Use a dedicated email address (e.g., `bot@yourdomain.com`)
3. Enable two-factor authentication (2FA)
4. Do NOT add this account to any organization memberships unless required

## Step 2: Generate Personal Access Token (PAT)

1. Log in as the bot user
2. Navigate to **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
3. Click **Generate new token (classic)**
4. Configure the token:
   - **Note**: `DOC_BOT_TOKEN - Automated documentation workflows`
   - **Expiration**: Set to 90 days (rotate regularly)
   - **Scopes** (grant ONLY these):
     - ✅ `repo` - Full control of private repositories (required for repo access)
     - ✅ `workflow` - Update GitHub Action workflows
     - ❌ Do NOT grant: `admin`, `delete_repo`, `user`, `gist`, `notifications`

5. Click **Generate token**
6. **IMPORTANT**: Copy the token immediately (it won't be shown again)

## Step 3: Add Repository Secrets

Add the following secrets to your repository:

### Required Secrets

| Secret Name | Value | Purpose |
|-------------|-------|---------|
| `DOC_BOT_TOKEN` | Bot's PAT from Step 2 | Authentication for automated workflows |
| `OPENAI_API_KEY` | Your OpenAI API key | AI Agent functionality (if using AI features) |

### How to Add Secrets

1. Go to your repository on GitHub
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret:
   - **Name**: `DOC_BOT_TOKEN`
   - **Value**: Paste the bot's PAT
   - Click **Add secret**
5. Repeat for `OPENAI_API_KEY`

## Step 4: Verification

Test that the bot token works correctly:

```bash
# Test API access with curl
export TOKEN="your_bot_token_here"
export REPO="owner/repo-name"

# Verify token can read repository
curl -H "Authorization: token $TOKEN" \
     -H "Accept: application/vnd.github.v3+json" \
     https://api.github.com/repos/$REPO

# Verify workflow permissions
curl -H "Authorization: token $TOKEN" \
     -H "Accept: application/vnd.github.v3+json" \
     https://api.github.com/repos/$REPO/actions/workflows

# Test git operations (if needed)
git clone https://x-access-token:$TOKEN@github.com/$REPO.git
```

Expected output: JSON response with repository/workflow information (not 401/403 errors).

## Step 5: Use in GitHub Actions

Example workflow usage:

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
          # Your documentation commands here
          echo "Bot token configured successfully"
```

## Security Best Practices

### DO:
- ✅ Use a dedicated bot account
- ✅ Grant minimum required scopes
- ✅ Rotate tokens every 90 days
- ✅ Monitor bot account activity
- ✅ Use repository-scoped secrets (not organization-wide unless needed)

### DO NOT:
- ❌ Use personal tokens for automation
- ❌ Grant admin or delete_repo permissions
- ❌ Commit tokens to codebase (always use secrets)
- ❌ Share bot credentials via insecure channels
- ❌ Leave expired tokens active

## Token Rotation

When rotating the bot token:

1. Generate a new PAT following Step 2
2. Update the `DOC_BOT_TOKEN` secret in repository settings
3. Delete the old token from the bot account
4. Verify workflows still function correctly

## Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Token is invalid or expired - regenerate |
| 403 Forbidden | Missing required scope - check token permissions |
| Workflow fails | Verify secret name matches exactly (`DOC_BOT_TOKEN`) |
| Git push fails | Ensure `repo` scope is granted |

## References

- [GitHub Personal Access Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/using-secrets-in-actions)
- [GitHub API Authentication](https://docs.github.com/en/rest/overview/other-authentication-methods)
