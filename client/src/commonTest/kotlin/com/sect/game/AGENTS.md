# 测试知识库

**类型**: 单元测试
**范围**: `client/src/commonTest/kotlin/com/sect/game/`

---

## OVERVIEW

kotlin-test 框架，TDD 开发，覆盖率目标 ≥ 80%。

## 测试结构

```
commonTest/kotlin/com/sect/game/
├── domain/                # 领域层测试
│   ├── entity/            # DiscipleTest, SectTest
│   ├── AttributesTest.kt
│   ├── RealmTest.kt
│   └── ...
├── goap/                  # GOAP 系统测试
│   ├── core/              # WorldStateTest, ConditionTest
│   ├── planner/           # AStarPlannerTest
│   ├── executor/          # ActionExecutorTest
│   ├── registry/          # ActionRegistryTest
│   ├── goals/             # GoalFactoryImplTest, 各目标测试
│   └── actions/           # ActionTest
├── engine/
│   └── GameEngineTest.kt
└── mvi/
    └── GameContainerTest.kt
```

## 测试命名

**类名**: `<被测类>Test`
**方法名**: `<方法名>_<场景>_<预期结果>` (英文)

```kotlin
@Test
fun cultivate_exhaustedDisciple_fails() { ... }
```

## 测试模式

### AAA 模式
```kotlin
@Test
fun test() {
    // Given
    val disciple = createTestDisciple()
    // When
    val result = disciple.cultivate()
    // Then
    assertTrue(result.isFailure)
}
```

### Result<T> 断言
```kotlin
assertTrue(result.isSuccess)
val newDisciple = result.getOrNull()!!
```

### 异步测试
```kotlin
@Test
fun testContainer_StartGame_TransitionsToPlaying() = runTest {
    container.accept(GameIntent.StartGame("测试宗门"))
    delay(200)
    assertIs<GameState.Playing>(container.state.value)
}
```

## 运行命令

```bash
./gradlew test                    # 所有测试
./gradlew jvmTest                 # JVM 测试
./gradlew koverReport             # 覆盖率报告
```
