# 宗门修真录 - 架构规划文档 v2.0

> **文档版本**: v2.0 (初稿)  
> **创建日期**: 2026-02-28  
> **架构模式**: Clean Architecture + FlowMVI + 模块化设计  
> **技术栈**: Kotlin 1.9.20 + Compose Multiplatform + Kodein + Coroutines

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [技术栈选型](#2-技术栈选型)
3. [架构设计原则](#3-架构设计原则)
4. [模块化设计](#4-模块化设计)
5. [核心架构设计](#5-核心架构设计)
6. [AI 系统设计](#6-ai-系统设计)
7. [数据存储方案](#7-数据存储方案)
8. [7 周开发计划](#8-7-周开发计划)
9. [风险评估](#9-风险评估)
10. [附录](#10-附录)

---

## 1. 项目概述

### 1.1 游戏定位

**宗门修真录**是一款基于 Kotlin Multiplatform 的东方玄幻文字修真游戏，玩家扮演宗门掌门或弟子，通过管理宗门、培养弟子、参与试炼等方式，体验修真世界的成长与发展。

### 1.2 核心特色

- **宗门管理**: 资源分配、设施建设、弟子培养
- **AI 角色系统**: 100-200 个 AI 弟子具有自主决策能力（行为树）
- **修炼系统**: 境界提升、功法学习、丹药炼制
- **多平台支持**: JVM（桌面）、Android（移动）、Web（未来）
- **纯文字界面**: Compose Multiplatform 渲染，低配置要求

### 1.3 技术目标

| 目标 | 指标 | 说明 |
|------|------|------|
| 操作响应时间 | <1 秒 | 菜单操作、状态更新 |
| 游戏启动时间 | <5 秒 | 冷启动（含资源加载） |
| 内存占用 | <200MB | 稳定运行，100-200 弟子场景 |
| CPU 使用率 | <10% | 单线程，AI 决策时<50% |
| 存档大小 | <10MB | 大型存档（200 弟子） |
| 存档/读档时间 | <1 秒 | 异步序列化 |

---

## 2. 技术栈选型

### 2.1 核心技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| **Kotlin** | 1.9.20 | 主要开发语言 | 多平台支持、协程、类型安全 |
| **Compose Multiplatform** | 1.5.10 | UI 框架 | 声明式 UI、跨平台、现代化 |
| **FlowMVI** | 最新版本 | 状态管理 | 单向数据流、可测试性、与 Flow 集成 |
| **Kodein** | 7.30 | 依赖注入 | 轻量级、多平台支持、易上手 |
| **Kotlinx Coroutines** | 1.7.3 | 异步处理 | 协程、Flow、结构化并发 |
| **Kotlinx Serialization** | 1.6.0 | JSON 序列化 | 多平台、性能优秀、类型安全 |

### 2.2 多平台目标

| 平台 | 优先级 | 说明 |
|------|--------|------|
| **JVM (桌面)** | P0 | 主要目标，完整功能 |
| **Android** | P0 | 移动端，触摸优化 |
| **Web** | P2 | 未来扩展，浏览器运行 |
| **iOS** | P2 | 未来扩展，移动端补充 |

### 2.3 构建工具

```toml
# libs.versions.toml (核心配置)
[versions]
kotlin = "1.9.20"
kotlinx-coroutines = "1.7.3"
kotlinx-serialization = "1.6.0"
compose = "1.5.10"
kodein = "7.30"
android-gradle = "8.1.4"

[libraries]
# FlowMVI - 直接使用最新快照版本
flowmvi-core = { module = "pro.respawn:flowmvi-core", version = "latest.release" }

# Kodein DI
kodein-di = { module = "org.kodein.di:kodein-di", version.ref = "kodein" }
kodein-di-framework = { module = "org.kodein.di:kodein-di-framework", version.ref = "kodein" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
android-application = { id = "com.android.application", version.ref = "android-gradle" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## 3. 架构设计原则

### 3.1 Clean Architecture 分层

```
┌─────────────────────────────────────────────────────────────┐
│                    展示层 (Presentation)                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Compose UI  │  │  ViewModel  │  │  UI State (Flow)    │  │
│  │   (View)    │◄─│  (MVI)      │◄─│  StateFlow/SharedFlow│  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────┬───────────────────────────────┘
                              │ Intent (用户操作)
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        领域层 (Domain)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Entities   │  │  Use Cases  │  │  Repository Interfaces│ │
│  │  (纯 Kotlin)│  │  (业务逻辑) │  │  (抽象定义)          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              核心子系统 (纯 Kotlin 业务逻辑)              ││
│  │  • WorldSimulator (世界模拟)  • AiDecisionEngine       ││
│  │  • CultivationSystem (修炼)   • CombatSystem           ││
│  │  • SectManager (宗门管理)     • EventBus               ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────┬───────────────────────────────┘
                              │ 依赖倒置
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        数据层 (Data)                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Repositories│  │ Data Sources│  │  DTOs + Mappers     │  │
│  │ (实现)      │  │ • JSON 文件  │  │  (序列化/反序列化)   │  │
│  │             │  │ • 资源配置   │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 架构原则

1. **单向依赖**: 数据层 → 领域层 → 展示层，严禁反向依赖
2. **领域纯净**: 领域层不依赖任何框架（Room、Retrofit 等）
3. **依赖倒置**: 数据层通过接口实现领域层定义的 Repository
4. **模块独立**: Feature 模块之间不直接依赖，通过领域层接口和事件通信
5. **数据不可变**: 领域模型使用 data class + copy() 方法修改状态

---

## 4. 模块化设计

### 4.1 模块总览（16 个模块）

| 模块名 | 类型 | 职责 | 依赖数 |
|--------|------|------|--------|
| **core-domain** | Core | 领域模型、用例、仓储接口 | 0 |
| **core-data** | Core | 数据层抽象、Repository 基类 | 1 |
| **core-presentation** | Core | MVI 基类、主题、通用组件 | 1 |
| **core-navigation** | Core | 导航系统、路由定义 | 1 |
| **common-ui** | Common | 通用 UI 组件、动画 | 1 |
| **common-resources** | Common | 共享资源（图片、字符串） | 0 |
| **data-serializer** | Data | 序列化配置、版本管理 | 1 |
| **data-datasource** | Data | 数据源实现（本地 DB/文件） | 2 |
| **data-repository** | Data | 仓储实现、缓存、同步 | 3 |
| **feature-sect** | Feature | 宗门管理（建筑/职位/资源） | 3 |
| **feature-cultivation** | Feature | 修炼系统（境界/突破/功法） | 3 |
| **feature-ai** | Feature | AI 行为树、决策引擎 | 1 |
| **feature-combat** | Feature | 战斗系统（回合制/技能） | 2 |
| **feature-social** | Feature | 社交系统（师徒/交易） | 2 |
| **feature-map** | Feature | 世界地图、探索、秘境 | 3 |
| **feature-event** | Feature | 事件系统（随机/天劫） | 2 |
| **app-desktop** | App | 桌面应用入口 | 13 |
| **app-android** | App | Android 应用入口 | 13 |

### 4.2 模块依赖关系

```
依赖方向：App → Feature → Core → Data

                    app-desktop / app-android
                              │
              ┌───────────────┴───────────────┐
              │                               │
      ┌───────▼────────┐            ┌────────▼────────┐
      │  Feature 模块   │            │   Core 模块     │
      │  (7 个独立)     │            │   (4 个)        │
      │               │            │               │
      │ • sect        │            │ • domain      │
      │ • cultivation │            │ • data        │
      │ • ai          │            │ • presentation│
      │ • combat      │            │ • navigation  │
      │ • social      │            │               │
      │ • map         │            │ ┌───────────┐ │
      │ • event       │            │ │ Common    │ │
      └───────┬───────┘            │ │ • ui      │ │
              │                    │ │ • resources││
      ┌───────▼────────┐           │ └───────────┘ │
      │  Data 模块      │           └───────────────┘
      │  (3 个)         │
      │               │
      │ • serializer  │
      │ • datasource  │
      │ • repository  │
      └───────────────┘
```

### 4.3 模块职责详解

#### Core 模块（基础设施）

**core-domain** - 领域层核心
```
职责:
- 定义领域模型（Entity、Value Object）
- 定义业务用例（Use Case）
- 定义仓储接口（Repository Interface）
- 定义领域事件（Domain Event）

关键文件:
- model/Cultivator.kt       # 弟子实体
- model/Sect.kt             # 宗门实体
- usecase/CultivateUseCase.kt
- repository/CultivatorRepository.kt
- event/EventBus.kt
```

**core-presentation** - 展示层抽象
```
职责:
- 定义 MVI 基类（BaseViewModel, BaseIntent, BaseState, BaseEffect）
- 定义主题系统（Theme, Color, Typography）
- 提供通用 UI 工具

关键文件:
- mvi/BaseViewModel.kt
- mvi/Intent.kt, State.kt, Effect.kt
- theme/Theme.kt
```

**core-navigation** - 导航系统
```
职责:
- 定义路由（Route）
- 实现导航器（Navigator）
- 处理深链接

关键文件:
- Route.kt                  # 密封类定义所有路由
- Navigator.kt              # 导航状态管理
```

#### Feature 模块（业务功能）

每个 Feature 模块采用统一的 MVI 结构：
```
feature-xxx/
├── presentation/
│   ├── XxxIntent.kt        # 用户意图（sealed interface）
│   ├── XxxState.kt         # 界面状态（data class）
│   ├── XxxEffect.kt        # 副作用（sealed interface）
│   └── XxxViewModel.kt     # ViewModel（处理 Intent，更新 State，发送 Effect）
├── ui/
│   ├── XxxScreen.kt        # 主界面
│   └── components/         # 子组件
└── di/
    └── XxxFeatureModule.kt # Kodein DI 模块
```

**示例：feature-cultivation**
```kotlin
// CultivationIntent.kt
sealed interface CultivationIntent {
    data class StartCultivation(val cultivatorId: String, val duration: Long) : CultivationIntent
    data class AttemptBreakthrough(val cultivatorId: String) : CultivationIntent
    data object StopCultivation : CultivationIntent
}

// CultivationState.kt
data class CultivationState(
    val cultivators: List<CultivatorUiModel> = emptyList(),
    val selectedCultivator: CultivatorUiModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// CultivationEffect.kt
sealed interface CultivationEffect {
    data class BreakthroughSuccess(val cultivatorId: String, val newRealm: String) : CultivationEffect
    data class ShowError(val message: String) : CultivationEffect
}
```

#### Data 模块（数据持久化）

**data-serializer** - 序列化配置
```kotlin
// 定义 JSON 序列化策略
object SerializationConfig {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}
```

**data-datasource** - 数据源实现
```kotlin
// 本地文件数据源
class CultivatorLocalDataSource(
    private val fileSystem: FileSystem,
    private val json: Json
) {
    suspend fun loadAll(): List<CultivatorDto> { ... }
    suspend fun save(disciples: List<CultivatorDto>) { ... }
}
```

**data-repository** - 仓储实现
```kotlin
// 实现领域层定义的接口
class CultivatorRepositoryImpl(
    private val localDataSource: CultivatorLocalDataSource
) : CultivatorRepository {
    override fun getAllCultivators(): Flow<List<Cultivator>> {
        return localDataSource.loadAll()
            .map { dtos -> dtos.map { it.toDomain() } }
    }
}
```

---

## 5. 核心架构设计

### 5.1 领域模型设计

**弟子实体（Disciple）**
```kotlin
@Serializable
data class Disciple(
    val id: DiscipleId,
    val name: String,
    val gender: Gender,
    val age: Int,
    val cultivation: CultivationState,      // 修为状态
    val attributes: DiscipleAttributes,     // 资质属性
    val personality: DisciplePersonality,   // 性格属性
    val role: DiscipleRole,                 // 职务
    val contribution: Int,                  // 贡献点
    val reputation: Int,                    // 声望
    val masterId: DiscipleId?,              // 师父 ID
    val techniques: List<TechniqueProgress>,// 已学功法
    val status: DiscipleStatus,             // 当前状态
    val aiState: AiState,                   // AI 状态
    val inventory: List<ItemStack>,         // 背包物品
) {
    // 派生属性
    val combatPower: Int get() = calculateCombatPower()
    val canBreakthrough: Boolean get() = cultivation.canBreakthrough()
    
    // 行为方法（返回新实例）
    fun cultivate(amount: Int, time: GameTime): Disciple = copy(...)
    fun breakthrough(newRealm: CultivationRealm): Result<Disciple, BreakthroughError> { ... }
}
```

**值对象示例**
```kotlin
@Serializable
data class CultivationState(
    val realm: CultivationRealm,
    val stage: CultivationStage,
    val experience: Long,
    val maxExperience: Long,
    val breakthroughProgress: Float
) {
    fun canBreakthrough(): Boolean = 
        stage == CultivationStage.PEAK && breakthroughProgress >= 1.0f
    
    fun addExperience(amount: Int): CultivationState = copy(...)
    fun advanceRealm(newRealm: CultivationRealm): CultivationState = copy(...)
}
```

### 5.2 FlowMVI 集成方案

**使用范围**: 仅在 Presentation 层使用 FlowMVI，领域层保持纯 Kotlin

**MVI 数据流**:
```
用户操作 → Intent → ViewModel → UseCase → Repository → Flow<State> → UI 更新
                        ↓
                    Effect (导航、Toast 等副作用)
```

**ViewModel 实现模板**:
```kotlin
@OptIn(ExperimentalMviApi::class)
class CultivationViewModel(
    private val cultivateUseCase: CultivateUseCase,
    private val repository: CultivatorRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(CultivationState.initial())
    val state: StateFlow<CultivationState> = _state
    
    private val _effect = MutableStateFlow<CultivationEffect?>(null)
    val effect: Flow<CultivationEffect?> = _effect
    
    fun processIntent(intent: CultivationIntent) {
        when (intent) {
            is CultivationIntent.StartCultivation -> startCultivation(intent.cultivatorId, intent.duration)
            is CultivationIntent.AttemptBreakthrough -> attemptBreakthrough(intent.cultivatorId)
        }
    }
    
    private fun startCultivation(id: String, duration: Long) {
        scope.launch {
            cultivateUseCase(id, duration)
                .catch { e -> sendEffect(CultivationEffect.ShowError(e.message)) }
                .collect { result ->
                    _state.update { it.copy(cultivators = ...) }
                    if (result.breakthroughReady) {
                        sendEffect(CultivationEffect.NavigateToBreakthroughScreen)
                    }
                }
        }
    }
    
    private fun sendEffect(effect: CultivationEffect) {
        scope.launch {
            _effect.value = effect
            _effect.value = null  // 重置，确保下次效果能被观察到
        }
    }
}
```

**Compose UI 消费**:
```kotlin
@Composable
fun CultivationScreen(
    viewModel: CultivationViewModel,
    onNavigateToBreakthrough: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effect.collectAsState(null)
    
    // 处理副作用
    LaunchedEffect(effect) {
        when (val e = effect) {
            is CultivationEffect.NavigateToBreakthroughScreen -> {
                state.selectedCultivator?.let { onNavigateToBreakthrough(it.id) }
            }
            is CultivationEffect.ShowError -> {
                // 显示错误提示
            }
            else -> {}
        }
    }
    
    CultivationScreenContent(state = state, onIntent = viewModel::processIntent)
}
```

### 5.3 Kodein 依赖注入

**DI 模块分层**:
```kotlin
// 1. 领域层 DI（core-domain）
val domainModule = DI.Module("domain-module") {
    bind<CultivateUseCase>() with factory { repo: CultivatorRepository ->
        CultivateUseCase(repo)
    }
}

// 2. 数据层 DI（data-repository）
val repositoryModule = DI.Module("repository-module") {
    bind<CultivatorRepository>() with singleton {
        CultivatorRepositoryImpl(instance())
    }
}

val dataSourceModule = DI.Module("datasource-module") {
    bind<CultivatorLocalDataSource>() with singleton {
        CultivatorLocalDataSource(instance(), instance())
    }
}

// 3. Feature 层 DI（feature-cultivation）
val cultivationFeatureModule = DI.Module("cultivation-feature-module") {
    bind<CultivationViewModel>() with factory {
        CultivationViewModel(instance(), instance())
    }
}

// 4. 根 DI 容器（app-desktop）
fun createAppDI(): DI {
    return DI {
        import(domainModule)
        import(repositoryModule)
        import(dataSourceModule)
        import(cultivationFeatureModule)
        // ... 导入其他模块
    }
}
```

**测试时的依赖替换**:
```kotlin
@Test
fun `test cultivation`() = runTest {
    val testDI = DI {
        bind<CultivatorRepository>() with singleton {
            mockk<CultivatorRepository>().apply {
                coEvery { getCultivator("test-id") } returns flowOf(testDisciple)
            }
        }
        bind<CultivateUseCase>() with factory { CultivateUseCase(instance()) }
    }
    
    val viewModel = CultivationViewModel(
        cultivateUseCase = testDI.direct(),
        repository = testDI.direct()
    )
    
    // 测试逻辑...
}
```

### 5.4 模块间通信机制

**基于 SharedFlow 的事件总线**:
```kotlin
// 领域事件定义（core-domain）
sealed interface DomainEvent {
    data class CultivationCompleted(val cultivatorId: String, val qiGained: Long) : DomainEvent
    data class BuildingUpgraded(val buildingId: String, val newLevel: Int) : DomainEvent
    data class CombatStarted(val combatId: String) : DomainEvent
    data class ResourceChanged(val resourceType: String, val amount: Long) : DomainEvent
}

// 事件总线实现
class EventBus {
    private val _events = MutableSharedFlow<DomainEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<DomainEvent> = _events.asSharedFlow()
    
    suspend fun publish(event: DomainEvent) = _events.emit(event)
}

// 发布事件（UseCase 中）
class CultivateUseCase(
    private val repository: CultivatorRepository,
    private val eventBus: EventBus
) {
    suspend fun invoke(id: String, duration: Long): Flow<CultivationResult> = flow {
        val result = // ... 修炼计算
        eventBus.publish(DomainEvent.CultivationCompleted(id, result.qiGained))
        emit(result)
    }
}

// 订阅事件（ViewModel 中）
class SectViewModel(eventBus: EventBus) {
    init {
        scope.launch {
            eventBus.events
                .filter { it is DomainEvent.ResourceChanged }
                .collect { event ->
                    // 更新宗门资源 UI
                }
        }
    }
}
```

**导航系统**:
```kotlin
// 路由定义（core-navigation）
sealed class Route {
    object Home : Route()
    object Sect : Route()
    object Cultivation : Route()
    data class CultivatorDetail(val cultivatorId: String) : Route()
    data class Breakthrough(val cultivatorId: String) : Route()
}

// 导航器（单例，通过 DI 提供）
class Navigator {
    private val _backStack = MutableStateFlow<List<Route>>(listOf(Route.Home))
    val backStack: StateFlow<List<Route>> = _backStack.asStateFlow()
    
    fun navigate(route: Route) {
        _backStack.value = _backStack.value + route
    }
    
    fun popBackStack() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }
}

// 在 ViewModel 中使用
class CultivationViewModel(
    private val navigator: Navigator
) {
    private fun onBreakthroughReady(cultivatorId: String) {
        navigator.navigate(Route.Breakthrough(cultivatorId))
    }
}
```

---

## 6. AI 系统设计

### 6.1 AI 方案选型

| 方案 | 优点 | 缺点 | 适用场景 | 推荐度 |
|------|------|------|----------|--------|
| **行为树** | 可视化好理解、可复用节点、支持优先级、易调试 | 需要解释执行 | 中等规模 AI，清晰决策逻辑 | ⭐⭐⭐⭐⭐ |
| GOAP | 高度灵活、自动规划 | 实现复杂、运行时计算开销大 | 高度自主决策 | ⭐⭐⭐ |
| 有限状态机 | 简单直观、性能好 | 状态爆炸、难以扩展 | 简单 AI 行为 | ⭐⭐⭐ |
| 实用 AI | 连续决策、自然过渡 | 需要大量调优、决策不透明 | 需要自然行为的 NPC | ⭐⭐⭐⭐ |

**推荐方案**: **行为树 + 实用 AI 评分混合**

### 6.2 行为树实现

**行为树节点定义**:
```kotlin
// 节点基类
abstract class BehaviorNode {
    abstract suspend fun execute(context: AiContext): BehaviorResult
}

// 选择器节点（优先级选择）
class SelectorNode(
    private val children: List<BehaviorNode>,
    private val name: String
) : BehaviorNode() {
    override suspend fun execute(context: AiContext): BehaviorResult {
        for (child in children) {
            val result = child.execute(context)
            if (result != BehaviorResult.FAILURE) {
                return result
            }
        }
        return BehaviorResult.FAILURE
    }
}

// 序列节点（顺序执行）
class SequenceNode(
    private val children: List<BehaviorNode>,
    private val name: String
) : BehaviorNode() {
    override suspend fun execute(context: AiContext): BehaviorResult {
        for (child in children) {
            val result = child.execute(context)
            if (result != BehaviorResult.SUCCESS) {
                return result
            }
        }
        return BehaviorResult.SUCCESS
    }
}

// 条件节点
class ConditionNode(
    private val condition: suspend AiContext.() -> Boolean,
    private val name: String
) : BehaviorNode() {
    override suspend fun execute(context: AiContext): BehaviorResult {
        return if (context.condition()) BehaviorResult.SUCCESS else BehaviorResult.FAILURE
    }
}

// 行动节点
class ActionNode(
    private val action: suspend AiContext.() -> Behavior,
    private val name: String
) : BehaviorNode() {
    override suspend fun execute(context: AiContext): BehaviorResult {
        context.currentBehavior = context.action()
        return BehaviorResult.SUCCESS
    }
}
```

**外门弟子行为树示例**:
```kotlin
val outerDiscipleBehaviorTree = behaviorTree("outer_disciple") {
    selector("root") {
        // 1. 紧急情况：逃跑
        sequence("emergency_flee") {
            condition("is_in_danger") { disciple.status.health < maxHealth * 0.3 }
            action("flee") { FleeBehavior() }
        }
        
        // 2. 疲劳时休息
        sequence("rest_when_tired") {
            condition("is_fatigued") { disciple.status.fatigue > 80 }
            action("rest") { RestBehavior(duration = 4 * 3600_000) }
        }
        
        // 3. 优先修炼
        sequence("cultivate") {
            condition("has_enough_energy") { fatigue < 70 }
            condition("has_spiritual_power") { spiritualPower > 20 }
            action("cultivate") { CultivateBehavior(duration = 2 * 3600_000) }
        }
        
        // 4. 工作获取资源
        sequence("work") {
            condition("needs_contribution") { contribution < 1000 }
            action("work") { WorkBehavior(facilityId = FacilityId(1)) }
        }
        
        // 5. 社交互动
        sequence("socialize") {
            condition("has_free_time") { true }
            action("socialize") { SocialBehavior(targetId = masterId) }
        }
        
        // 6. 默认：休息
        action("default_rest") { RestBehavior(duration = 1 * 3600_000) }
    }
}
```

### 6.3 AI 调度器

```kotlin
class AiScheduler(
    private val decisionEngine: AiDecisionEngine,
    private val worldContext: WorldContext,
    private val config: AiConfig
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val discipleJobs = mutableMapOf<DiscipleId, Job>()
    
    // 注册弟子 AI
    fun register(disciple: Disciple) {
        if (discipleJobs.containsKey(disciple.id)) return
        
        val job = scope.launch {
            runAiLoop(disciple.id)
        }
        discipleJobs[disciple.id] = job
    }
    
    // AI 循环
    private suspend fun runAiLoop(discipleId: DiscipleId) {
        while (isActive) {
            val disciple = worldContext.getDisciple(discipleId) ?: break
            
            // 决策
            val behavior = decisionEngine.decide(disciple, worldContext)
            
            // 执行行为（行为内部会处理挂起）
            val context = AiContext(disciple, worldContext, worldContext.currentTime, null)
            behavior.execute(context)
            
            // 冷却时间（避免每帧都决策）
            delay(config.decisionCooldown)
        }
    }
    
    // 配置
    data class AiConfig(
        val decisionCooldown: Long = 1000,  // 决策冷却时间（毫秒）
        val maxConcurrentDecisions: Int = 50  // 最大并发决策数
    )
}
```

**性能优化策略**:
```kotlin
// 分层更新策略
class AiScheduler {
    private val updateFrequency = mapOf(
        PositionType.SECT_MASTER to 1,      // 每帧更新
        PositionType.ELDER to 2,            // 每 2 帧更新
        PositionType.CLOSE_DISCIPLE to 5,   // 每 5 帧更新
        PositionType.INNER_DISCIPLE to 10,  // 每 10 帧更新
        PositionType.OUTER_DISCIPLE to 20   // 每 20 帧更新
    )
    
    private var frameCount = 0
    
    override fun update(deltaTime: Float) {
        frameCount++
        
        for ((position, frequency) in updateFrequency) {
            if (frameCount % frequency != 0) continue
            
            // 只更新该层级的弟子
            val entities = getDisciplesByPosition(position)
            entities.forEach { runAiLoop(it.id) }
        }
    }
}
```

---

## 7. 数据存储方案

### 7.1 JSON 文件存储

**存储结构**:
```
saves/
├── autosave/
│   ├── world_state.json      # 世界状态
│   ├── sect.json             # 宗门数据
│   ├── disciples.json        # 弟子列表
│   └── resources.json        # 资源库存
└── manual/
    ├── save_001.json
    └── save_002.json
```

**序列化配置**:
```kotlin
object SerializationConfig {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }
    
    // 存档版本管理
    const val CURRENT_VERSION = 1
}
```

**存档数据类**:
```kotlin
@Serializable
data class SaveData(
    val version: Int = 1,
    val timestamp: Long,
    val gameTime: GameTime,
    val sect: SectDto,
    val disciples: List<DiscipleDto>,
    val worldState: WorldStateDto,
    val resources: Map<String, Int>,
    val settings: GameSettings
)

@Serializable
data class DiscipleDto(
    val id: Int,
    val name: String,
    val cultivation: CultivationStateDto,
    val attributes: AttributesDto,
    // ...
)
```

**仓储实现**:
```kotlin
class SaveRepositoryImpl(
    private val fileSystem: FileSystem,
    private val json: Json
) : SaveRepository {
    override suspend fun save(saveData: SaveData): Result<Unit> {
        return try {
            val path = Path("saves/autosave/world_state.json")
            val jsonString = json.encodeToString(SaveData.serializer(), saveData)
            fileSystem.write(path) { writeUtf8(jsonString) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    override suspend fun load(): Result<SaveData> {
        return try {
            val path = Path("saves/autosave/world_state.json")
            val jsonString = fileSystem.read(path) { readUtf8() }
            val saveData = json.decodeFromString(SaveData.serializer(), jsonString)
            Result.Success(saveData)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}
```

### 7.2 异步存档

```kotlin
class SaveService(
    private val saveRepository: SaveRepository,
    private val eventBus: EventBus
) {
    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 自动存档（每 5 分钟）
    fun startAutoSave() {
        saveScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000)  // 5 分钟
                autoSave()
            }
        }
    }
    
    // 手动存档
    suspend fun manualSave(): Result<Unit> {
        val saveData = buildSaveData()
        val result = saveRepository.save(saveData)
        
        result.fold(
            onSuccess = { eventBus.publish(DomainEvent.GameSaved) },
            onFailure = { e -> eventBus.publish(DomainEvent.SaveFailed(e.message)) }
        )
        
        return result
    }
    
    private suspend fun autoSave() {
        val saveData = buildSaveData()
        saveRepository.save(saveData)  // 忽略错误，避免打扰玩家
    }
}
```

---

## 8. 7 周开发计划

### 8.1 总体时间分配

```
┌────────────────────────────────────────────────────────────────┐
│  周次  │  第 1 周  │  第 2 周  │  第 3 周  │  第 4 周  │  第 5 周  │  第 6 周  │  第 7 周  │
├────────────────────────────────────────────────────────────────┤
│  阶段  │  基础   │  核心   │  核心   │  扩展   │  扩展   │  完善   │  发布   │
│        │  架构   │  系统   │  系统   │  功能   │  功能   │  优化   │  测试   │
├────────────────────────────────────────────────────────────────┤
│  重点  │  领域   │  AI+    │  宗门  │  战斗  │  社交  │  UI+   │  修复   │
│        │  模型   │  修炼   │  管理   │  试炼   │  事件   │  性能   │  打包   │
└────────────────────────────────────────────────────────────────┘
```

### 8.2 详细周计划

#### 第 1 周：基础架构搭建
**目标**: 完成模块框架、核心领域模型、DI 配置、序列化

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 项目初始化（Gradle、16 个 module） | P0 | 8h | 所有模块可编译 |
| 配置 libs.versions.toml | P0 | 4h | 依赖版本统一管理 |
| core-domain（领域模型 + 用例） | P0 | 16h | Disciple/Sect 实体完成 |
| data-serializer + data-datasource | P0 | 12h | JSON 序列化/反序列化正常 |
| 集成 Kodein DI | P0 | 8h | 各模块可注入使用 |
| 基础 UI 框架（Compose） | P0 | 8h | 可显示简单界面 |
| **周验收** | | **56h** | 可创建弟子并保存为 JSON |

#### 第 2 周：AI 决策 + 修炼系统
**目标**: 完成行为树框架、AI 调度器、修炼系统、境界突破

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 行为树框架实现 | P0 | 16h | 可定义和执行行为树 |
| AI 调度器实现 | P0 | 12h | 100 弟子可同时运行 AI |
| 修炼系统（境界/突破） | P0 | 16h | 弟子可修炼和突破 |
| 境界配置 DSL | P1 | 8h | 可通过 DSL 定义境界配置 |
| 世界时钟系统 | P0 | 8h | 游戏时间可推进 |
| **周验收** | | **60h** | 弟子可自主修炼并突破境界 |

#### 第 3 周：宗门管理系统
**目标**: 完成资源、设施、弟子管理、宗门主界面

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 资源系统（获取/消耗/存储） | P0 | 12h | 资源流动正常 |
| 设施系统（建设/升级/运行） | P0 | 12h | 设施可正常工作 |
| 弟子管理（招募/培养/晋升） | P0 | 12h | 弟子管理功能完整 |
| 宗门主界面（Compose） | P0 | 12h | 显示宗门状态和资源 |
| 存档/读档系统 | P0 | 8h | 游戏状态可保存和加载 |
| **周验收** | | **56h** | 可完整管理宗门和弟子 |

#### 第 4 周：战斗 + 试炼系统
**目标**: 完成回合制战斗、试炼流程、奖励分配

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 战斗系统基础（回合制） | P0 | 16h | 可进行战斗 |
| 战斗计算（伤害/技能） | P0 | 12h | 伤害计算正确 |
| 试炼系统（小区域试炼） | P0 | 12h | 可参与试炼并获得奖励 |
| 战斗界面 | P0 | 8h | 显示战斗日志和操作 |
| 战斗界面优化 | P1 | 8h | 动画和特效 |
| **周验收** | | **56h** | 弟子可参与战斗和试炼 |

#### 第 5 周：社交 + 事件系统
**目标**: 完成社交关系、随机事件、剧情事件

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 社交系统（师徒/同门） | P0 | 12h | 可建立师徒关系 |
| 关系值计算 | P0 | 8h | 关系变化合理 |
| 随机事件生成 | P0 | 12h | 事件正常触发 |
| 事件处理界面 | P0 | 8h | 玩家可选择处理方式 |
| 剧情事件框架 | P1 | 8h | 关键剧情可触发 |
| **周验收** | | **48h** | 社交和事件系统正常运行 |

#### 第 6 周：UI 完善 + 性能优化
**目标**: 完成所有 UI 界面、性能优化、多平台适配

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 所有功能 UI 完成 | P0 | 24h | 所有功能有对应界面 |
| 主题和样式优化 | P1 | 8h | 界面美观统一 |
| AI 性能优化 | P0 | 12h | 100 弟子稳定 60FPS |
| 序列化性能优化 | P0 | 8h | 存档时间<1 秒 |
| Android 适配 | P0 | 8h | Android 可运行 |
| **周验收** | | **60h** | UI 完整，性能达标 |

#### 第 7 周：测试 + 修复 + 发布
**目标**: 集成测试、Bug 修复、打包发布

| 任务 | 优先级 | 预计工时 | 验收标准 |
|------|--------|----------|----------|
| 集成测试（所有模块联调） | P0 | 16h | 所有功能正常 |
| Bug 修复 | P0 | 16h | 严重 Bug 清零 |
| 性能回归测试 | P0 | 8h | 符合性能指标 |
| 桌面端打包（DMG/MSI） | P0 | 8h | 可安装运行 |
| Android 打包（APK） | P0 | 8h | 可安装运行 |
| 文档完善 | P1 | 8h | README、用户手册完成 |
| **周验收** | | **64h** | 游戏可发布 |

### 8.3 里程碑评审点

| 评审点 | 时间 | 评审内容 | 决策标准 |
|--------|------|----------|----------|
| 需求评审 | 第 1 周末 | 架构设计、模块拆分 | 架构是否合理、模块职责是否清晰 |
| 框架评审 | 第 2 周末 | FlowMVI 集成、AI 原型 | FlowMVI 是否正常工作、AI 是否可决策 |
| 核心功能评审 | 第 4 周末 | 修炼 + 宗门 + 战斗 | 核心玩法循环是否完整 |
| 最终评审 | 第 7 周末 | 完整游戏、测试报告 | 所有 P0 功能完成、性能达标、无严重 Bug |

---

## 9. 风险评估

### 9.1 技术风险

| 风险 | 概率 | 影响 | 应对方案 |
|------|------|------|----------|
| **行为树实现复杂度超预期** | 中 | 高 | 第 1 周快速原型验证，准备简化版 FSM 作为降级方案 |
| **Compose Multiplatform 跨平台问题** | 中 | 中 | 优先保证 JVM 平台，Android 延后，Web 放入 P2 |
| **Kodein DI 跨模块依赖管理复杂** | 低 | 中 | 严格遵循依赖方向，使用版本目录统一管理 |
| **JSON 序列化性能问题（大存档）** | 低 | 低 | 异步序列化、增量保存 |
| **FlowMVI 学习曲线** | 中 | 低 | 第 1 周培训、编写示例代码 |

### 9.2 进度风险

| 风险 | 概率 | 影响 | 应对方案 |
|------|------|------|----------|
| **7 周无法完成全部 P0 功能** | 中 | 高 | 严格按优先级开发，P2 功能准备延后 |
| **模块过多导致集成复杂** | 中 | 中 | 第 4 周开始每周集成测试，提前发现问题 |
| **多平台适配耗时超预期** | 中 | 中 | 优先 JVM，Android 适配简化，Web 延后 |

### 9.3 性能风险

| 风险 | 概率 | 影响 | 应对方案 |
|------|------|------|----------|
| **100 弟子 AI 决策卡顿** | 中 | 高 | 分层更新策略、协程限流、决策缓存 |
| **内存占用超标** | 低 | 中 | 定期 GC、对象池、及时释放无用对象 |
| **UI 渲染卡顿** | 低 | 中 | Compose 性能优化、避免重组、使用 remember |

---

## 10. 附录

### 10.1 术语表

| 术语 | 解释 |
|------|------|
| **MVI** | Model-View-Intent，单向数据流架构模式 |
| **Flow** | Kotlinx Coroutines 的响应式流 |
| **StateFlow** | 热的状态流，保证唯一性和最新值 |
| **SharedFlow** | 热的共享流，支持多订阅者 |
| **Kodein** | Kotlin 依赖注入框架 |
| **Compose Multiplatform** | JetBrains 跨平台 UI 框架 |
| **行为树** | AI 决策算法，树状结构组织行为 |
| **UseCase** | 领域层业务逻辑封装 |
| **Repository** | 数据访问抽象层 |

### 10.2 参考资源

- **FlowMVI**: 
  - 源码仓库：https://github.com/respawn-app/FlowMVI
  - 使用文档：https://opensource.respawn.pro/FlowMVI/
- **Kodein DI**:
  - 源码仓库：https://github.com/kosi-libs/Kodein
  - 使用文档：https://kosi-libs.org/kodein/7.30/index.html
- **Compose Multiplatform**: https://www.jetbrains.com/compose-multiplatform/
- **Kotlinx Coroutines**: https://github.com/Kotlin/kotlinx.coroutines
- **Kotlinx Serialization**: https://github.com/Kotlin/kotlinx.serialization

### 10.3 下一步行动

1. **确认架构方案**: 审核本文档，确认模块拆分和技术选型
2. **技术验证**: 第 1 周完成 FlowMVI + Kodein + Compose 原型
3. **Gradle 配置优化**: 配置 Build Cache 加速编译
4. **CI/CD 规划**: 配置 GitHub Actions 自动构建和测试
5. **详细设计**: 为每个 Feature 模块编写详细设计文档

---

**文档状态**: 初稿待审核  
**审核人**: [待填写]  
**审核日期**: [待填写]  
**修改记录**: 
- v2.0 (2026-02-28): 初始版本，基于需求文档 v1.2
