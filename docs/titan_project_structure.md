# 土卫六 (Titan Moon) 项目结构文档

# Titan Moon - Project Structure Reference

> 目标平台 / Target: Minecraft **1.20.1 Forge 47.4.4** (Java 17)
> 构建 / Build: **Stonecutter (flat) + Architectury Loom**，源码纯 Java
> mod id: `titan_moon`；主包 / group: `com.tonywww.titan_moon`
> 配套文档: [titan_design.md](titan_design.md)（创意）、[titan_technical_design.md](titan_technical_design.md)（技术方案）

> ⚠️ **本文档为早期 M0 骨架快照**：注册表命名已同步为 `TMXxx`，但实体（现 8 种、GeckoLib 渲染）、流体色值、方块清单等已大幅演进，命名以外内容请以源码为准。

本文档精确到每一个类、字段与方法（注册表类采用 `TMXxx` 命名）。
所有类/字段/方法均已通过 `compileJava`；方块/流体/维度已通过 `runServer` 运行时实测。

---

## 一、目录树 (Directory Tree)

```
Titan_Satellite/
├─ settings.gradle.kts              # Stonecutter 插件 + 节点定义
├─ stonecutter.gradle.kts           # 活动节点 + parameters(constants: forge)
├─ build.gradle.kts                 # flat 共享脚本（Loom + 依赖 + 任务）
├─ gradle.properties                # mod.* 元数据 + Gradle 选项
├─ gradlew / gradlew.bat            # Gradle Wrapper 9.6.1
├─ gradle/wrapper/                  # wrapper jar + properties
├─ versions/
│  └─ 1.20.1-forge/gradle.properties  # loom.platform=forge, vers.*
├─ docs/                            # 设计与参考文档
└─ src/main/
   ├─ java/com/tonywww/titan_moon/
   │  ├─ TitanMoon.java        # @Mod 主类
   │  ├─ registry/                  # 全部 DeferredRegister（TMXxx）
   │  │  ├─ TMBlocks.java
   │  │  ├─ TMItems.java
   │  │  ├─ TMFluidTypes.java
   │  │  ├─ TMFluids.java
   │  │  ├─ TMEntities.java
   │  │  ├─ TMCreativeTabs.java
   │  │  └─ TMDimensions.java
   │  ├─ entity/AeroJelly.java      # 实体类
   │  ├─ client/                    # 仅 Dist.CLIENT
   │  │  ├─ AeroJellyRenderer.java
   │  │  └─ TitanClientEvents.java
   │  └─ mixin/package-info.java    # Mixin 占位包
   └─ resources/
      ├─ META-INF/mods.toml
      ├─ pack.mcmeta
      ├─ titan_moon.mixins.json
      ├─ assets/titan_moon/    # blockstates / models / lang
      └─ data/titan_moon/      # dimension_type / dimension / worldgen
```

---

## 二、构建层 (Build Files)

| 文件 | 职责 |
|---|---|
| [settings.gradle.kts](../settings.gradle.kts) | `rootProject.name`；`pluginManagement` 插件仓库；应用 Stonecutter `0.9.6` 与 foojay toolchain 解析；`stonecutter { create(rootProject) { version("1.20.1-forge","1.20.1"); vcsVersion } }`。节点名与逻辑版本分离。 |
| [stonecutter.gradle.kts](../stonecutter.gradle.kts) | 控制器：`stonecutter active "1.20.1-forge"`；`parameters { constants.match(loader,"forge","neoforge") }` 启用 `//? if forge` 版本化注释。 |
| [build.gradle.kts](../build.gradle.kts) | 共享脚本：应用 Loom `1.11-SNAPSHOT`；按 `loom.platform` 选加载器；Java 版本随 mc 版本（17）；`mixin.defaultRefmapName`；仅活动节点生成 IDE run 配置且 `runDir` 指向根 `run/`；依赖 Forge `47.4.4` + 官方 Mojmap；`tasks.configureEach` 给 `createMinecraftArtifacts` 惰性挂 `dependsOn("stonecutterGenerate")`；`processResources` 用 `project.property("mod.*")` 展开 `mods.toml`。 |
| [gradle.properties](../gradle.properties) | `org.gradle.jvmargs`；`mod.id/name/version(0.1.0)/group/authors/description/license`。 |
| [versions/1.20.1-forge/gradle.properties](../versions/1.20.1-forge/gradle.properties) | `vers.mcVersion=1.20.1`、`vers.deps.fml=47.4.4`、`loom.platform=forge`。 |

---

## 三、Java 源码：逐类逐函数 (Java Sources - Per Class)

### 3.1 `com.tonywww.titan_moon.TitanMoon` — mod 主类

`@Mod(TitanMoon.MODID)` 标注的入口类。

| 成员 | 类型/签名 | 说明 |
|---|---|---|
| `MODID` | `public static final String` = `"titan_moon"` | mod 唯一 id，注册与资源命名空间。 |
| `LOGGER` | `public static final Logger` (`LogUtils.getLogger()`) | SLF4J 日志器。 |
| `TitanMoon()` | 构造器 | 取 `FMLJavaModLoadingContext.get().getModEventBus()`；依次 `register(modBus)`：`TMFluidTypes`→`TMFluids`→`TMBlocks`→`TMItems`→`TMEntities`→`TMCreativeTabs`；`modBus.addListener(TMEntities::onAttributeCreation)`；输出加载日志。 |

### 3.2 `registry` 包

#### `TMBlocks` — 方块注册表
`REGISTER : DeferredRegister<Block>`（`ForgeRegistries.BLOCKS`）。固体方块的 `BlockItem` 在 `TMItems` 注册；流体方块不注册 `BlockItem`。

字段（`RegistryObject<Block>`，除流体为 `RegistryObject<LiquidBlock>`）：

| 常量 | 注册 id | 用途 | 复用贴图 | 关键属性 |
|---|---|---|---|---|
| `TITAN_STONE` | `titan_stone` | 维度基础填充块（default_block） | `block/deepslate` | 强度 1.5/6.0，需正确工具 |
| `TITAN_BASALT` | `titan_basalt` | 甲烷深渊峡谷岩壁 | `block/blackstone` | 强度 1.25/4.2，需正确工具 |
| `THOLIN_SAND` | `tholin_sand` | 托林沙海地表 | `block/sand` | 强度 0.5 |
| `CRUSHED_ICE` | `crushed_ice` | 撞击陨坑荒原 | `block/snow` | 强度 0.5，雪音效 |
| `CRYO_ICE` | `cryo_ice` | 冰火山断崖蓝冰 | `block/blue_ice` | 强度 2.8，玻璃音效 |
| `PACKED_METHANE_ICE` | `packed_methane_ice` | 极地迷宫浮冰 | `block/packed_ice` | 强度 0.5 |
| `CRYOVOLCANIC_GEYSER` | `cryovolcanic_geyser` | 周期喷发击飞（占位） | `block/prismarine` | 发光 6 |
| `METHANE_POOL_CORE` | `methane_pool_core` | 开采塔防触发点（占位） | `block/basalt_top` | 强度 25/1200，发光 3 |
| `SPECIAL_METHANE_PUMP` | `special_methane_pump` | 抽取泵（占位） | `block/iron_block` | 强度 4/6，金属音效 |
| `THOLIN_CRYSTAL` | `tholin_crystal` | 晶洞发光材料（占位） | `block/amethyst_block` | 发光 10 |
| `LIQUID_METHANE_BLOCK` | `liquid_methane` | 液态甲烷流体块（default_fluid） | 流体渲染 | `LiquidBlock(TMFluids.LIQUID_METHANE, …)` |
| `LIQUID_AMMONIA_BLOCK` | `liquid_ammonia` | 液态氨流体块 | 流体渲染 | `LiquidBlock(TMFluids.LIQUID_AMMONIA, …)` |

私有方法：
- `props(MapColor, float destroyTime, float resistance, SoundType)` → `BlockBehaviour.Properties`：统一构造固体方块属性。
- `liquidProps(MapColor)` → `Properties`：流体块属性（`noCollission().strength(100).noLootTable()`）。
- `register(String, Supplier<Block>)` → `RegistryObject<Block>`：注册辅助。

#### `TMItems` — 物品注册表
`REGISTER : DeferredRegister<Item>`（`ForgeRegistries.ITEMS`）。

| 常量 | 注册 id | 类型 | 说明 |
|---|---|---|---|
| `AERO_MEMBRANE` | `aero_membrane` | `Item` | 甲烷浮游体掉落材料（复用 `item/phantom_membrane`） |
| `TITAN_STONE` … `THOLIN_CRYSTAL` | 同名 | `BlockItem` | 10 个固体方块对应的方块物品 |
| `LIQUID_METHANE_BUCKET` | `liquid_methane_bucket` | `BucketItem` | `BucketItem(TMFluids.LIQUID_METHANE, …stacksTo(1).craftRemainder(BUCKET))` |
| `LIQUID_AMMONIA_BUCKET` | `liquid_ammonia_bucket` | `BucketItem` | 同上（氨） |

私有方法：
- `register(String, Supplier<Item>)`：注册辅助。
- `blockItem(String, RegistryObject<Block>)`：注册 `BlockItem`（延迟 `block.get()`）。

#### `TMFluidTypes` — 流体类型注册表
`REGISTER : DeferredRegister<FluidType>`（`ForgeRegistries.Keys.FLUID_TYPES`）。

| 成员 | 说明 |
|---|---|
| `STILL_TEXTURE` / `FLOWING_TEXTURE` | 复用 `block/water_still`、`block/water_flow`。 |
| `LIQUID_METHANE : RegistryObject<FluidType>` | 密度 450、粘度 1200、温度 90K；琥珀色染色 `0xFFB0822E`；可游泳/可淹没。 |
| `LIQUID_AMMONIA : RegistryObject<FluidType>` | 密度 680、粘度 1100、温度 240K；淡蓝染色 `0xFF9FC9E8`。 |
| `tinted(int tintARGB, FluidType.Properties)` | 私有工厂：返回匿名 `FluidType`，覆写 `initializeClient(Consumer<IClientFluidTypeExtensions>)` 提供 still/flowing 贴图与 `getTintColor`（仅客户端加载，服务端不触碰客户端类）。 |

#### `TMFluids` — 流体注册表
`REGISTER : DeferredRegister<Fluid>`（`ForgeRegistries.FLUIDS`）。每种液体含 Source + Flowing。

| 常量 | 注册 id | 类型 |
|---|---|---|
| `LIQUID_METHANE` | `liquid_methane` | `ForgeFlowingFluid.Source` |
| `FLOWING_LIQUID_METHANE` | `flowing_liquid_methane` | `ForgeFlowingFluid.Flowing` |
| `LIQUID_AMMONIA` | `liquid_ammonia` | `ForgeFlowingFluid.Source` |
| `FLOWING_LIQUID_AMMONIA` | `flowing_liquid_ammonia` | `ForgeFlowingFluid.Flowing` |

私有方法 `methaneProperties()` / `ammoniaProperties()` → `ForgeFlowingFluid.Properties`：
在注册阶段**延迟构建**（绑定 fluidType/source/flowing/block/bucket + slopeFindDistance/levelDecreasePerBlock），
以此避免与 `TMBlocks`/`TMItems` 的类初始化循环引用。

#### `TMEntities` — 实体注册表
`REGISTER : DeferredRegister<EntityType<?>>`（`ForgeRegistries.ENTITY_TYPES`）。

| 成员 | 说明 |
|---|---|
| `AERO_JELLY : RegistryObject<EntityType<AeroJelly>>` | `EntityType.Builder.of(AeroJelly::new, CREATURE).sized(0.9,1.2).clientTrackingRange(8).build("aero_jelly")`。 |
| `onAttributeCreation(EntityAttributeCreationEvent)` | mod 总线监听：`event.put(AERO_JELLY.get(), AeroJelly.createAttributes().build())`。 |

#### `TMCreativeTabs` — 创造模式物品栏
`REGISTER : DeferredRegister<CreativeModeTab>`（`Registries.CREATIVE_MODE_TAB`）。

| 成员 | 说明 |
|---|---|
| `TITAN : RegistryObject<CreativeModeTab>` | 标题 `itemGroup.titan_moon.titan`；图标 `AERO_MEMBRANE`；`displayItems` 依次加入浮游薄膜 + 10 固体方块 + 2 流体桶。 |

#### `TMDimensions` — 维度 ResourceKey 常量
无 DeferredRegister，仅存 key 供运行时判定/传送。

| 成员 | 说明 |
|---|---|
| `TITAN_LEVEL : ResourceKey<Level>` | `titan_moon:titan`（维度实例 key）。 |
| `TITAN_DIM_TYPE : ResourceKey<DimensionType>` | `titan_moon:titan`（维度类型 key）。 |

### 3.3 `entity` 包

#### `AeroJelly extends PathfinderMob` — 甲烷浮游体
| 成员 | 说明 |
|---|---|
| `AeroJelly(EntityType<? extends PathfinderMob>, Level)` | 构造器。 |
| `createAttributes()` `static → AttributeSupplier.Builder` | MAX_HEALTH 8.0、MOVEMENT_SPEED 0.15、FOLLOW_RANGE 12.0。 |
| `registerGoals()` `@Override` | 0 `FloatGoal`；1 `WaterAvoidingRandomStrollGoal(0.8)`；2 `LookAtPlayerGoal(Player,8)`；3 `RandomLookAroundGoal`。 |

### 3.4 `client` 包（仅 `Dist.CLIENT`）

#### `AeroJellyRenderer extends MobRenderer<AeroJelly, SlimeModel<AeroJelly>>`
| 成员 | 说明 |
|---|---|
| `TEXTURE` | `textures/entity/slime/slime.png`（复用史莱姆贴图）。 |
| `AeroJellyRenderer(EntityRendererProvider.Context)` | `new SlimeModel<>(ctx.bakeLayer(ModelLayers.SLIME))`，阴影 0.5。 |
| `getTextureLocation(AeroJelly)` `@Override` | 返回 `TEXTURE`。 |

#### `TitanClientEvents`
`@Mod.EventBusSubscriber(modid=MODID, bus=MOD, value=Dist.CLIENT)`。
| 成员 | 说明 |
|---|---|
| `onRegisterRenderers(EntityRenderersEvent.RegisterRenderers)` `@SubscribeEvent` | 为 `TMEntities.AERO_JELLY` 注册 `AeroJellyRenderer::new`。 |

### 3.5 `mixin` 包
- `package-info.java`：Mixin 占位包文档。当前 `titan_moon.mixins.json` 的 `mixins` 为空，按技术方案第六章后续加入。

---

## 四、资源文件 (Resources)

### 4.1 元数据
| 文件 | 说明 |
|---|---|
| `META-INF/mods.toml` | `modLoader=javafml`；`${...}` 由 `processResources` 展开；声明 `[[mixins]] config=titan_moon.mixins.json`；依赖 forge `[47,)` 与 minecraft `[1.20.1,1.21)`。 |
| `pack.mcmeta` | `pack_format=15`（1.20.1）。 |
| `titan_moon.mixins.json` | `package=com.tonywww.titan_moon.mixin`；`compatibilityLevel=JAVA_17`；`refmap=titan_moon.refmap.json`；`mixins=[]`、`client=[]`（占位）。 |

### 4.2 assets（`assets/titan_moon/`）
- `blockstates/`：11 个（10 固体方块 + 2 流体块，`liquid_ammonia`/`liquid_methane`；`tholin_sand` 等），单变体指向对应方块模型。
- `models/block/`：11 个。固体为 `cube_all` + 复用原版贴图；流体为仅含 `particle` 贴图的占位模型。
- `models/item/`：13 个。方块物品继承对应方块模型；`aero_membrane` 与两个桶继承 `item/generated`（复用 `phantom_membrane`/`water_bucket`）。
- `lang/en_us.json`、`lang/zh_cn.json`：物品栏名 + 12 方块名 + 3 物品名（薄膜/两桶）+ 实体名。

### 4.3 data（`data/titan_moon/`）— 数据驱动 worldgen
| 文件 | 说明 |
|---|---|
| `dimension_type/titan.json` | `min_y=0, height=320, logical_height=320`；`has_skylight=true`；`ambient_light=0.1`；`effects=minecraft:overworld`；`monster_spawn_light_level` 为 `uniform{value:{0..7}}`（注意 IntProvider 需嵌套 `value`）。 |
| `dimension/titan.json` | `type=titan_moon:titan`；`generator=minecraft:noise`，`settings=titan_moon:titan`，`biome_source=minecraft:fixed → titan_moon:tholin_dune_sea`。 |
| `worldgen/noise_settings/titan.json` | 见下方 worldgen 链路。 |
| `worldgen/biome/tholin_dune_sea.json` | `has_precipitation=false`，`temperature=-0.7`；橙色系 `effects`；`spawners/spawn_costs/carvers={}`，`features=[]`。 |

---

## 五、Worldgen 链路与地形分层 (Worldgen Chain)

数据链：`dimension → generator(noise) → noise_settings → surface_rule/biome`。

`noise_settings/titan.json` 关键参数（**已实测**）：
- `sea_level = 72`
- `default_block = titan_moon:titan_stone`（**维度基础方块**）
- `default_fluid = titan_moon:liquid_methane`（level 0，**维度基础液体**）
- `noise = { min_y:0, height:320, size_horizontal:1, size_vertical:2 }`
- `noise_router.final_density = y_clamped_gradient(from_y 40→+1, to_y 88→-1)` → 密度零点 y≈64
- `surface_rule = condition(stone_depth: floor, offset 0, add_surface_depth) → block tholin_sand`（深度受限，仅表层）

生成结果（同一 X/Z 柱，实测 `runServer` 探测确认）：

| Y 区间 | 方块 |
|---|---|
| 0 – ~62 | `titan_stone`（default_block） |
| ~63（表层） | `tholin_sand`（surface_rule） |
| 64 – 71 | `liquid_methane`（default_fluid，海平面 72 以下的甲烷海） |
| 72 – 320 | 空气 |

> **替换机制**：更换维度基础方块/液体只需改 `noise_settings.json` 的 `default_block` / `default_fluid`（指向任意已注册方块/流体块）与 `surface_rule`；`sea_level` 控制液面高度。改动需重启（`/reload` 不刷新 worldgen），且需在全新区块观察。

---

## 六、注册项汇总 (Registry Inventory)

| 注册表 | 类 | 数量 | 内容 |
|---|---|---|---|
| `BLOCKS` | `TMBlocks` | 12 | 6 基础地形 + 4 特殊 + 2 流体块 |
| `ITEMS` | `TMItems` | 13 | 1 材料 + 10 方块物品 + 2 桶 |
| `FLUID_TYPES` | `TMFluidTypes` | 2 | 甲烷、氨 |
| `FLUIDS` | `TMFluids` | 4 | 甲烷/氨 各 Source+Flowing |
| `ENTITY_TYPES` | `TMEntities` | 1 | `aero_jelly` |
| `CREATIVE_MODE_TAB` | `TMCreativeTabs` | 1 | `titan` |
| 维度 key | `TMDimensions` | 2 | Level + DimensionType key |

---

## 七、验证状态 (Verification Status)

- 编译：`.\gradlew.bat :1.20.1-forge:compileJava` → BUILD SUCCESSFUL（仅少量 `[removal]` 弃用告警）。
- 完整构建：`.\gradlew.bat :1.20.1-forge:build` → 产出 `versions/1.20.1-forge/build/libs/titan_moon-forge-0.1.0+1.20.1.jar`。
- 运行时：`.\gradlew.bat :1.20.1-forge:runServer` → `Done`，mod 加载、流体注册无初始化崩溃；
  `titan_moon:titan` 维度可进入、地形分层（titan_stone / tholin_sand / liquid_methane）实测命中。
- **待验证**（需客户端）：`runClient` 目视维度天空/雾、方块贴图、`aero_jelly` 渲染、流体染色。

---

## 八、后续里程碑映射 (Milestones)
详见 [titan_technical_design.md](titan_technical_design.md) 第八章。当前处于 **M0 骨架**（本文档所述内容），
后续 M1 世界生成（五群系 multi_noise + 自定义噪声/特征）、M2 生物、M3 环境系统、M4 事件玩法、M5 结构与打磨。
