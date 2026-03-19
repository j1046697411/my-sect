# Doc-Updater Agent Test Scenarios

测试 doc-updater 智能体的场景文档。

---

## 场景 1：新增智能体调用 / New Agent Call Added

**描述**: 当规则文件中添加了新的智能体调用时，应触发更新。

### 输入 / Input
```markdown
# 修改前的文件
## 可用智能体
- planner: 实现规划
- architect: 系统设计

# 修改后的文件
## 可用智能体
- planner: 实现规划
- architect: 系统设计
- code-reviewer: 代码审查
```

### 预期输出 / Expected Output
- doc-updater 检测到智能体列表变化
- 自动更新相关文档以反映新增的智能体
- 生成更新日志条目

### 实际输出 / Actual Output
_待测试时填写_

---

## 场景 2：仅注释修改 / Comment Modified Only

**描述**: 当仅修改注释内容时，不应触发更新。

### 输入 / Input
```markdown
# 修改前的文件
## 智能体使用
<!-- 这是智能体列表 -->
- planner: 实现规划

# 修改后的文件
## 智能体使用
<!-- 这是更新后的智能体列表注释 -->
- planner: 实现规划
```

### 预期输出 / Expected Output
- doc-updater 检测到仅有注释变化
- 不触发文档更新
- 返回"无需更新"状态

### 实际输出 / Actual Output
_待测试时填写_

---

## 场景 3：工作流文件修改 / Workflow File Modified

**描述**: 当工作流配置文件被修改时，应触发更新。

### 输入 / Input
```yaml
# 修改前的 .github/workflows/ci.yml
jobs:
  test:
    steps:
      - run: ./gradlew test

# 修改后的 .github/workflows/ci.yml
jobs:
  test:
    steps:
      - run: ./gradlew test
      - run: ./gradlew ktlintCheck
  deploy:
    steps:
      - run: ./gradlew deploy
```

### 预期输出 / Expected Output
- doc-updater 检测到工作流配置变化
- 更新 CI/CD 相关文档
- 同步更新构建命令表格

### 实际输出 / Actual Output
_待测试时填写_

---

## 场景 4：冲突检测 / Conflict Detected

**描述**: 当检测到人工编辑与自动更新冲突时，人工编辑优先。

### 输入 / Input
```markdown
# 自动更新准备写入
## 构建命令
| 命令 | 描述 |
|------|------|
| build | 完整构建 |

# 人工同时编辑的内容
## 构建命令
| 命令 | 描述 |
|------|------|
| build | 完整构建（包含测试）|
| quick-build | 快速构建（跳过测试）|
```

### 预期输出 / Expected Output
- doc-updater 检测到内容冲突
- 放弃自动更新
- 记录冲突日志
- 人工编辑内容保留优先

### 实际输出 / Actual Output
_待测试时填写_

---

## 测试执行记录 / Test Execution Log

| 场景 | 执行日期 | 执行者 | 结果 | 备注 |
|------|----------|--------|------|------|
| 1 | - | - | 待执行 | - |
| 2 | - | - | 待执行 | - |
| 3 | - | - | 待执行 | - |
| 4 | - | - | 待执行 | - |
