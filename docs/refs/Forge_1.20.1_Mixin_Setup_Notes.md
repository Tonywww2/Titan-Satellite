# Forge 1.20.1 Mixin 配置与使用参考

项目已确认允许使用 Mixin（兼容性优先）。本笔记整理 1.20.1 Forge 下的 Mixin 工程配置、`mixins.json` 格式、`@Inject` 与 `@Invoker`/`@Accessor` 写法，作为 The Betweenlands 替代旧 coremod ASM hook 的规范。

研究日期：2026-06-23

主要参考：
- `0999312/umapyoi`（**1.20.1 Forge**，MixinGradle 0.7，Mixin AP 0.8.5）——版本完全对齐，作为工程配置主参考。
- `vectorwing/FarmersDelight`（1.20 分支）——`@Invoker` accessor 实例。
- `TelepathicGrunt/Bumblezone`（本地 1.20.6/NeoForge）——大型 mixin 包组织方式参考（注意是 NeoForge/Architectury，配置不能照抄，仅看结构）。

> 重要：Bumblezone 本地副本是 NeoForge + Architectury（`compatibilityLevel: JAVA_21`、`package: ...mixin.neoforge`），其 build 与 mixins.json **不能直接用于 Forge 1.20.1**。Forge 1.20.1 用 `compatibilityLevel: JAVA_17`，工程配置以 umapyoi 为准。

## 1. build.gradle 配置（umapyoi，Forge 1.20.1）

```gradle
buildscript {
    dependencies {
        classpath 'org.spongepowered:mixingradle:0.7.+'
    }
}
plugins {
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
    id 'org.parchmentmc.librarian.forgegradle' version '1.+'   // 可选：Parchment 映射
    id 'org.spongepowered.mixin' version '0.7.+'
}
apply plugin: 'org.spongepowered.mixin'

minecraft {
    mappings channel: 'parchment', version: '2023.09.03-1.20.1'
    accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')
    runs {
        client {
            // 导出被 mixin 改动的类，便于调试
            property 'mixin.debug.export', 'true'
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
            // server / data 同样加上这三行
        }
    }
}

mixin {
    add sourceSets.main, 'thebetweenlands.mixins.refmap.json'   // refmap 输出名
    config 'thebetweenlands.mixins.json'                         // 你的 mixin 配置
    // 可为不同目标拆多个 config，例如可选 mod 兼容：
    // config 'thebetweenlands.somecompat.mixins.json'
    debug { verbose = true; exportMixinAP = true }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.4.0'
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'   // Mixin 注解处理器
}

jar.finalizedBy('reobfJar')
```

要点：
- `mixingradle 0.7` + plugin `org.spongepowered.mixin 0.7` + `annotationProcessor mixin:0.8.5:processor` 三者配套。
- `mixin { config '...' }` 会把配置写进 jar manifest 的 `MixinConfigs`，Forge 在加载时识别。
- `mixin.env.remapRefMap` / `refMapRemappingFile` 让开发环境正确 remap SRG↔MCP（这是 1.20.1 Forge 特有的坑，缺了开发环境 mixin 找不到目标）。
- TBL 当前 `accesstransformer.cfg` 已存在（空），优先用 AT 解决“只需访问权限”的场景，Mixin 留给“需要插入逻辑”的场景。

## 2. mixins.json 格式（Forge 1.20.1）

`src/main/resources/thebetweenlands.mixins.json`（参考 umapyoi）：

```json
{
  "required": true,
  "package": "thebetweenlands.mixin",
  "compatibilityLevel": "JAVA_17",
  "refmap": "thebetweenlands.mixins.refmap.json",
  "mixins": [
    "RecipeManagerAccessor",
    "SomeCommonMixin"
  ],
  "client": [
    "client.LevelRendererMixin",
    "client.FogRendererMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  },
  "minVersion": "0.8"
}
```

字段：
- `package`：所有 mixin 类的根包（类名在 `mixins`/`client` 里相对此包写）。
- `compatibilityLevel`：1.20.1 用 `JAVA_17`。
- `mixins`：双端都加载的 mixin。
- `client`：仅客户端加载（渲染、雾、天空相关放这里，保证服务端不加载客户端类）。
- `server`：仅服务端（少用）。
- `refmap`：与 build.gradle 的 `add sourceSets.main, '...refmap.json'` 一致。
- `injectors.defaultRequire: 1`：默认每个 inject 至少命中 1 处，命不中即报错（推荐保留，便于早发现失效 mixin）。

## 3. 注入式 Mixin：@Inject（umapyoi 实例，Forge 1.20.1）

```java
@Mixin(value = Player.class, priority = 10)
public class PlayerFoodExhaustionMixin {
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void foodExhaustion(float exhaustion, CallbackInfo ci) {
        Player player = (Player)(Object) this;     // 拿到被注入实例
        ItemStack soul = UmapyoiAPI.getUmaSoul(player);
        if (!soul.isEmpty()) {
            if (!player.level().isClientSide()) {
                player.getFoodData().addExhaustion(exhaustion * customMultiplier);
            }
            ci.cancel();                            // 取消原方法
        }
    }
}
```

要点：
- `(Target)(Object) this` 取得目标实例。
- `@At("HEAD")` / `@At("RETURN")` / `@At("INVOKE", target="...")` 等定位注入点。
- 想取消/改返回值用 `cancellable = true` + `ci.cancel()`（void）或 `CallbackInfoReturnable<T>` + `cir.setReturnValue(...)`。
- `priority` 控制多 mixin 命中同一点时的顺序。

## 4. 访问式 Mixin：@Invoker / @Accessor（FarmersDelight 实例）

当只需要调用私有方法或读写私有字段（而非插入逻辑）时，用 accessor 接口型 mixin。FD 的 `RecipeManagerAccessor`：

```java
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Invoker("byType")
    <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipeMap(RecipeType<T> type);
}
```

用法：`((RecipeManagerAccessor) level.getRecipeManager()).getRecipeMap(type)`。

字段访问类似：

```java
@Mixin(SomeClass.class)
public interface SomeAccessor {
    @Accessor("fieldName") SomeType getField();
    @Accessor("fieldName") void setField(SomeType v);
}
```

> 经验法则：只读字段 → 优先用 access transformer（`accesstransformer.cfg`）放开 `public`，比 mixin 更轻。只有 vanilla 私有**方法**或需要在已有逻辑中**插入**代码时才上 mixin。

## 5. mixin 包结构建议（参考 Bumblezone 组织，按 Forge 调整）

```text
thebetweenlands/mixin/
  RecipeManagerAccessor.java           # 双端 accessor
  world/ ...                           # 世界生成相关
  entities/ ...                        # 实体相关
  client/                              # 仅客户端（写进 mixins.json 的 "client" 段）
    LevelRendererMixin.java
    FogRendererMixin.java
```

把客户端 mixin 单独放 `client/` 子包，并只列在 mixins.json 的 `client` 段，避免服务端加载。

## 6. 对 The Betweenlands 的迁移建议

原 coremod（`thebetweenlands.core`）中：
- `PreRenderShadersHookTransformer`（向渲染管线注入 `onPreRenderShaders`）——这是必须用 Mixin（或 Forge `RenderLevelStageEvent`）替代的典型。先评估能否用 Forge render stage event；不行再用客户端 `LevelRendererMixin @Inject`。详见任务清单阶段 16.4 / 21。
- `TheBetweenlandsPreconditions` 等启动校验——直接删除，1.20.1 不需要 coremod 自检。
- 其余 module 逐个判断：能用 Forge event → event；只需访问权限 → AT；必须插入逻辑 → Mixin。

迁移顺序建议：先把 build.gradle + 空 `thebetweenlands.mixins.json` 配好并编译通过（确认 mixin 工具链 OK），再逐个添加 accessor/inject。每加一个都跑 `runClient`/`runServer` 验证命中与双端安全。

## 7. 参考路径

- umapyoi（工程配置 + @Inject）：`build.gradle`、`src/main/resources/umapyoi.mixins.json`、`src/main/java/net/tracen/umapyoi/mixin/PlayerFoodExhaustionMixin.java`
- FarmersDelight（@Invoker accessor）：`src/main/java/vectorwing/farmersdelight/common/mixin/accessor/RecipeManagerAccessor.java`
- Bumblezone（包结构，NeoForge，仅参考组织）：`common/.../mixin/*`、`*.mixins.json`
