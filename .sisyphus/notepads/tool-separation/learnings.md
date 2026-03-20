# Tool Separation - Learnings

## Project Context
- Kotlin Multiplatform project with Gradle
- Current detekt custom rules in `client/src/commonMain/kotlin/com/sect/game/detekt/`
- META-INF service file at `client/src/jvmMain/resources/META-INF/services/`
- detekt.yml at `client/detekt.yml`

## Key Files to Migrate
1. `NoChineseInTestMethodName.kt` - Rule implementation
2. `NoChineseInTestMethodNameRuleSetProvider.kt` - RuleSetProvider
3. `detekt.yml` - Configuration
4. META-INF service file

## Target Structure
```
tools/
├── detekt-rules/
│   ├── build.gradle.kts
│   ├── detekt.yml
│   └── src/main/
│       ├── kotlin/com/sect/game/tools/detekt/
│       └── resources/META-INF/services/
├── kover-config/
│   └── build.gradle.kts
└── dokka-config/
    └── build.gradle.kts
```

## Build Configuration Notes
- detekt-rules: JVM-only, needs detekt-api dependency
- Must configure META-INF/services file generation in JAR
- client uses `detekt { dependencies { detekt(project(":tools:detekt-rules")) } }` to reference rules

## Task 2 Findings (detekt-rules build setup)
- `kotlin("jvm")` plugin required to use `kotlin { jvmToolchain(11) }` block
- Without kotlin("jvm"), get "Unresolved reference: jvmToolchain"
- sourceSets.main.resources.srcDirs("src/main/resources") enables META-INF/services inclusion
- Pre-existing dokka-config build error blocks full project configuration

## Task 3 Findings (Rule File Migration)
- Successfully migrated NoChineseInTestMethodName.kt and NoChineseInTestMethodNameRuleSetProvider.kt
- Package updated from `com.sect.game.detekt` to `com.sect.game.tools.detekt`
- detekt API imports remain unchanged (io.gitlab.arturbosch.detekt.api.*)
- RuleSetProvider correctly references NoChineseInTestMethodName with new package
- Build verification blocked by missing Java 11 toolchain on this machine (only Java 17 available)

## Task 4 Findings (META-INF Service File)
- Created service file at `tools/detekt-rules/src/main/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider`
- Content: `com.sect.game.tools.detekt.NoChineseInTestMethodNameRuleSetProvider`
- Reference format from client module: single line with fully qualified class name
- Directory created using `mkdir -p` to ensure parent directories exist

## Task 6 Findings (Old Detekt Files Cleanup)
- Deleted `client/src/commonMain/kotlin/com/sect/game/detekt/` (2 .kt files)
- Deleted `client/src/jvmMain/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider`
- Deleted `client/detekt.yml`
- Removed empty `META-INF` directory tree after service file deletion
- Git status confirms all 3 items marked as deleted
