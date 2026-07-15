# 土卫六 (Titan) 维度 - 技术实现方案与设计细化

# Titan Dimension - Technical Design & Refinement

> 配套文档 / Companion to: [titan_design.md](titan_design.md)
> 执行计划 / Execution plan: [总任务表 task-plan.md](task-plan.md) · [平行任务表 parallel-tasks.md](parallel-tasks.md)（本文档为其 Stage 1 设计案）
> 目标平台 / Target: Minecraft **1.20.1 Forge** (Java 17)
> 工程构建 / Build: **Stonecutter (flat) + Architectury Loom**，源码纯 Java，预留多版本扩展位。

本文档在原始创意设计（titan_design.md）之上，补齐三件事：
1. 设计细化：把每个群系/生物/方块/事件落到"可实现的具体参数与状态机"。
2. 技术映射：把玩法映射到 1.20.1 Forge 的注册项、数据驱动 JSON、事件与 Mixin 接入点。
3. 工程结构与里程碑：项目骨架、包结构、构建/运行命令、分阶段落地顺序、风险清单。

---

## 一、总体架构分层 (Architecture Layers)

| 层 | 内容 | 1.20.1 Forge 实现方式 |
|---|---|---|
| 世界生成 (Worldgen) | 维度、群系、地形、结构、矿物/特征 | 数据驱动 JSON (`data/titan_moon/...`)，代码仅注册自定义 Feature/Structure 类型 |
| 内容注册 (Content) | 方块、物品、流体、实体、生物效果、创造模式物品栏 | `DeferredRegister` + `RegistryObject` |
| 大气与氛围 (Atmosphere) | 甲烷雨、浓橙雾、天空/能见度 | 客户端 `DimensionSpecialEffects` + biome 环境参数（极寒/无氧/低重力仅作背景设定，无生存适配层） |
| 事件玩法 (Events) | 甲烷开采塔防、喷泉击飞、晶洞惊扰 | 方块实体 tick + 服务端调度 + 自定义 `ForgeEvent` + Mixin 定制刷怪 |
| 表现层 (Presentation) | 实体渲染、维度天空/雾、粒子、音效 | 客户端事件总线 (`Dist.CLIENT`)：Renderer / DimensionSpecialEffects / Particle |

设计原则：**能数据驱动就不用硬编码**。维度/群系/噪声/放置特征全部走 JSON，Java 只注册"引擎级"的新类型（自定义方块、实体、Feature、事件），从而保持可被数据包与 KubeJS 二次修改。

---

## 二、内容注册清单 (Registry Inventory)

所有注册用 `DeferredRegister`，集中在 `registry/` 包，主类 `TitanMoon` 统一 `register(modBus)`。

### 2.1 方块 (Blocks) -> `ModBlocks`
注册到 `ForgeRegistries.BLOCKS`；每个方块同时在 `ModItems` 注册对应 `BlockItem`。

| 方块 id | 用途 | 骨架状态 |
|---|---|---|
| `tholin_sand` | 托林沙海地表（橙色有机粉末） | 占位已实现（复用原版 sand 贴图） |
| `blackstone_basalt_*` | 甲烷深渊峡谷岩壁 | 计划：复用/变体原版黑石玄武岩 |
| `cryo_ice` / `packed_methane_ice` | 冰火山断崖蓝冰/浮冰变体 | 计划 |
| `cryovolcanic_geyser` | 周期喷发、击飞实体的特殊方块 | 计划（带 BlockEntity 或 randomTick） |
| `methane_pool_core` | 深渊稀有生成，开采事件核心 | 计划（BlockEntity） |
| `special_methane_pump` | 玩家放置的抽取泵，触发塔防 | 计划（BlockEntity + 菜单可选） |
| `tholin_crystal` (发光) | 晶洞高级合成材料，破坏放毒气 | 计划 |
| `weathered_titan_stone` | 荒芜高原/撞击陨坑荒原 表层（风化泰坦石） | M6 新增 |
| `sedimentary_titan_stone` | 液态甲烷深渊 表层（沉积泰坦石） | M6 新增 |
| `branch_crystal` | 撞击陨坑荒原 地表树枝状结晶装饰 | M6 新增 |

### 2.2 物品 (Items) -> `ModItems`
注册到 `ForgeRegistries.ITEMS`。

| 物品 id | 来源 | 骨架状态 |
|---|---|---|
| `aero_membrane` | 甲烷浮游体掉落，轻量合成材料 | 占位已实现（复用 phantom_membrane 贴图） |
| `cryo_carapace` | 冰硜甲虫掉落，硬化合成材料 | 计划 |
| `toxic_gland` | 氨泉掠食者掉落，抗性药剂/涂层 | 计划 |
| `depleted_battery` / `precision_components` | 失控探测器掉落，科技线材料 | 计划 |

### 2.3 实体 (Entities) -> `ModEntities`
注册到 `ForgeRegistries.ENTITY_TYPES`；属性经 `EntityAttributeCreationEvent`；生成放置经 `SpawnPlacementRegisterEvent`（或 `FMLCommonSetupEvent` 内 `enqueueWork`）；渲染器经客户端 `EntityRenderersEvent.RegisterRenderers`。

| 实体 id | 类型 | 基类建议 | 关键 AI/机制 | 骨架状态 |
|---|---|---|---|---|
| `aero_jelly` (甲烷浮游体) | 被动 | `Animal`/`FlyingMob` | 空中漂浮、随气流移动、受击缓降 | 占位已实现（复用 Slime 模型渲染成气囊） |
| `cryo_scavenger` (冰硅甲虫) | 中立 | `Monster`/`PathfinderMob` | 受击反击、冰晶护甲减伤 | 计划 |
| `ammonia_stalker` (氨泉掠食者) | 敌对 | `Monster` | 两栖、攻击附带中毒 | 计划 |
| `corrupted_probe` (失控探测器) | 敌对 | `Monster` | 远程激光（类恶魂/守卫者射线） | 计划 |

### 2.4 其他注册项
- 创造模式物品栏 (`CreativeModeTab`) -> `ModCreativeTabs`：注册到 `Registries.CREATIVE_MODE_TAB`，聚合本模组方块与物品。骨架已实现。
- 流体 (`Fluid` + `FluidType`)：液态甲烷、液态氨。骨架暂以占位方块代表，真正流体（含桶、渲染、流动）列为后续里程碑。
- 生物效果 (`MobEffect`)：托林毒素（氨泉掠食者攻击 / 晶洞毒气）。（环境生存相关的缺氧/极寒效果已随环境系统移除。）

---

## 三、世界生成 (Worldgen, 数据驱动)

维度/群系/噪声全部 JSON 化，位于 `src/main/resources/data/titan_moon/`。

### 3.1 维度三件套
- `dimension_type/titan.json`：无天空光的浓雾维度参数（`has_skylight=false` 或低 `ambient_light`、`has_ceiling=false`、`min_y=0`、`height=320`、`logical_height=320`、`coordinate_scale`、`effects` 指向自定义或复用）。
- `dimension/titan.json`：`type` 指向上面的维度类型；`generator` 用 `minecraft:noise` 噪声生成器；`biome_source` 骨架先用 `minecraft:fixed`（固定单群系）跑通，后续换 `minecraft:multi_noise` 承载 5 大群系的 Y 轴分层。
- `worldgen/noise_settings/titan.json`：骨架先复用/引用 `minecraft:overworld` 的 noise settings 快速跑通；后续替换为自定义，追求**强烈破碎感**（悬崖峧壁、断层、陡坡、孤峰）+ 峡谷/沙丘/断崖的极端高差 0-320（可用高频 ridge/erosion 噪声与陡峭的 `final_density` 梯度实现）。

### 3.2 群系 (Biomes) - 六大群系（含新增荒芜高原）
`worldgen/biome/` 下每群系一个 JSON。中文显示名统一加“土卫六·”前缀（仅 lang，id 不变）。表层方块由 `surface_rule` 按 biome 铺设：

| biome id | 中文显示名 | 表层方块 | Y 参考 | 专属地物（按群系注入的 Feature） |
|---|---|---|---|---|
| `methane_abyss` | 土卫六·液态甲烷深渊 | `sedimentary_titan_stone`（新） | 0-64 | 碎裂裂隙（fissure，底部甲烷）、甲烷海（methane_mare 大面积下沉） |
| `cratered_wastelands` | 土卫六·撞击陨坑荒原 | `weathered_titan_stone`（新） | 64-110 | 树枝状结晶（branch_crystal）、真陨石坑（带坑缘，底部湖小概率） |
| `tholin_dune_sea` | 土卫六·托林沙海 | `tholin_sand` | 100-170 | 巨型沙脊（megayardang）+ 沙丘起伏 |
| `polar_labyrinth` | 土卫六·极地迷宫冰原 | `packed_methane_ice` | 160-240 | 巨型冰层天坑（ice_sinkhole）、破碎海绵（sponge_cave 地下多孔） |
| `cryovolcanic_cliff` | 土卫六·冰火山断崖 | `cryo_ice` | 220-320 | 冰火山喷泉群（geyser）；**气候嵌套在极地中心** |
| `barren_plateau` | 土卫六·荒芜高原（新） | `weathered_titan_stone`（新） | 180-260 | 台地地形、几乎无空洞、与邻接群系陡峭过渡 |

> **关键（M6 修复）：** 原 `noise_router` 的气候项（temperature/humidity/continents/erosion/depth/ridges）全为常量 0，导致全域气候恒等原点、实际只生成一个群系。M6 给这些项赋气候噪声（参考原版 overworld router 或自定义气候 density function），六群系才能真正按 `multi_noise` 铺开。`cryovolcanic_cliff` 的气候参数与 `polar_labyrinth` 大部分重合、仅一维取极值，使其成为极地气候邻域中心的稀有子区域（概率近似“只在极地中心”）。陡峭过渡靠低 `erosion` 参数 + 增强 `BiomeHeightDensityFunction`（台地项 + 陡边缘）。

### 3.3 结构与特征 (Structures & Features)
- 结构：托林晶洞与地下冰虫巢穴 (`tholin_geode`)、废弃先驱者前哨站 (`pioneer_outpost`，低生成权重)。用 `worldgen/structure` + `structure_set` + 模板池（Jigsaw）或 NBT 模板；代码侧注册自定义 `StructureType`（若逻辑复杂）。
- 特征：`configured_feature` + `placed_feature`；自定义程序化地貌用 `Feature<C>` 类（`ForgeRegistries.FEATURES` 注册）。现有：`methane_lake/giant_crater/megayardang/ice_sinkhole/glowing_crystal_cluster/geyser_patch`；M6 新增 `fissure`（裂隙）、`methane_mare`（甲烷海）、`branch_crystal`（树枝结晶）、`sponge_cave`（破碎海绵），并改进 `giant_crater`（坑缘）、`ice_sinkhole`（增大）。
- **关键（M6 修复）：** biome JSON 的 `features` 留空，特征须由 `forge:add_features` biome_modifier 注入才会自然生成。**当前 `biome_modifier/` 只有生物生成（add_spawns）、缺 add_features → 6 个特征从未自然生成、仅能 `/place`。** M6 为每个群系新建 `*_features.json`（按每群系 tag 或直接 biome 列表限定），让地物只在对应群系出现。

---

## 四、大气与氛围 (Atmosphere & Ambiance)

> 注：原「环境生存系统」（低重力、极寒/缺氧生存、保暖/供氧装备、体温/氧气 Capability、HUD、相关伤害类型）已按设计修正**移除**；土卫六不再有强制生存适配层。极寒/无氧/低重力仅作背景设定与视觉氛围。

### 4.1 甲烷雨 (Liquid Methane Rain)
- 原版天气不易直接替换降水类型；骨架阶段先用 biome 的 `precipitation`/粒子与音效近似，配合客户端 `DimensionSpecialEffects` 的浓橙雾。
- 真正"液态甲烷雨"（可积液/交互）列为后续，可能需 Mixin 到天气/粒子渲染。

### 4.2 客户端维度特效 (Sky/Fog)
- 注册 `DimensionSpecialEffects`（Forge `RegisterDimensionSpecialEffectsEvent`，客户端 Mod 总线）实现浓厚橙黄雾、低能见度、无星空/自定义天体。

---

## 五、事件玩法 (Event-Driven Gameplay)

### 5.1 冰火山喷泉击飞 (Cryovolcanic Geyser)
- 方块 `cryovolcanic_geyser`（**PE-1 已实现**）：不用 BlockEntity（CR-3）也不依赖 `randomTicks`（PA-1 注册未开启），而是用 `gameTime` + 按坐标相位隐式驱动喷发周期（CYCLE 140 / ERUPT 45 tick，相邻喷泉错峰）；`stepOn` 在喷发态给站立实体施加向上 `setDeltaMovement`（+`hurtMarked` 同步、清坠落伤害）击飞，用于垂直跨越 Y 轴高差；`animateTick` 客户端喷雾/冰晶粒子 + 原版喷涌音效。
- 表现：喷发时生成冰晶/氨雾粒子 + 音效。

### 5.2 甲烷开采塔防事件 (Methane Extraction Defense) - 终局核心
状态机（BlockEntity `special_methane_pump` 驱动，服务端）：

```
IDLE  -- 玩家在 methane_pool_core 上方放置并激活泵 --> RUNNING
RUNNING:
  - 每 tick 推进开采进度 progress (0..MAX)
  - 到达每一波阈值 -> 触发一波刷怪 (wave++)
  - 泵被摧毁 / 玩家取消 --> FAILED
  - progress 达成 --> SUCCESS (产出终局燃料/奖励)
```

- **自定义 Forge 事件**：定义 `MethaneExtractionWaveEvent extends net.minecraftforge.eventbus.api.Event`（含 `pumpPos`、`waveIndex`、`intensity` 等字段），泵在每波开始时 `MinecraftForge.EVENT_BUS.post(event)`。整合包/附属可监听以自定义每波逻辑。
- **波次刷怪**：默认实现根据 `waveIndex` 计算数量与强度，在泵周围环形选点生成深渊怪；生成逻辑可被 **Mixin** 注入定制（对应设计中"结合 Mixins 高度自定义每波怪物的生成逻辑和强度"）。
- **胜负与产出**：成功后核心方块转为可持续供能状态或产出终局燃料物品；失败则事件重置、怪物退散。

### 5.3 晶洞惊扰 (Geode Disturbance)
- 破坏 `tholin_crystal` 有概率：释放毒气（区域 `MobEffect` 云或自定义粒子 + 中毒）并唤醒巢穴中潜伏的敌对生物（触发附近生成/激活）。

---

## 六、Mixin 接入点 (Mixin Targets)

Loom 内建 Mixin 支持；配置 `titan_moon.mixins.json`（骨架先留空 `mixins: []`，随功能逐步加入）。预期注入点：

| 目的 | 目标类/方法 | 注入方式 |
|---|---|---|
| 塔防波次刷怪定制 | 自定义刷怪逻辑或 `NaturalSpawner` 相关 | `@Inject` / `@Redirect` |
| 甲烷雨/天气替换 | 天气或降水渲染相关 | `@Inject` (客户端) |

原则：优先用 Forge 官方事件与属性；仅在无事件可用时才上 Mixin，降低跨版本维护成本。

---

## 七、工程结构 (Project Structure)

Stonecutter flat + Architectury Loom，当前单节点 `1.20.1-forge`（Java 17）。

```
Titan_Satellite/
  settings.gradle.kts            # rootProject.name, 插件仓库, Stonecutter create
  stonecutter.gradle.kts         # active 节点 + parameters(constants: forge)
  build.gradle.kts               # flat 单脚本(按 loom.platform 切加载器)
  gradle.properties              # mod.id/name/version/group + jvmargs
  versions/
    1.20.1-forge/gradle.properties   # loom.platform=forge, vers.mcVersion, vers.deps.fml
  src/main/
    java/com/tonywww/titan_moon/
      TitanMoon.java        # @Mod 主类, MODID, DeferredRegister 汇总注册
      registry/                  # ModBlocks/ModItems/ModEntities/ModCreativeTabs/ModDimensions
      entity/                    # AeroJelly 等实体类
      client/                    # 客户端渲染注册(仅 Dist.CLIENT)
      block/ item/ worldgen/ event/ compat/  # 逐步填充
    resources/
      META-INF/mods.toml
      pack.mcmeta
      titan_moon.mixins.json
      assets/titan_moon/... # lang, blockstates, models
      data/titan_moon/...   # dimension_type, dimension, worldgen/biome
  gradlew / gradlew.bat / gradle/wrapper/  # Gradle Wrapper
```

关键构建约定（来自 refs，已并入脚本）：
- 节点名与逻辑版本分离：`version("1.20.1-forge", "1.20.1")`。
- `createMinecraftArtifacts` 必须 `dependsOn("stonecutterGenerate")`。
- `loom.platform=forge` 写在节点 `gradle.properties`。
- Java 工具链 17；本机无 JDK 17，故 settings 加 `foojay-resolver-convention` 自动下载。

构建/运行命令：
```
./gradlew.bat :1.20.1-forge:build
./gradlew.bat :1.20.1-forge:runClient --console=plain
```

---

## 八、落地里程碑 (Milestones)

- M0 工程骨架（本次）：Stonecutter/Loom 构建通、主类加载、示例方块/物品/实体注册、维度可进入的最小 JSON。
- M1 世界生成：五大群系 multi_noise 分层 + 自定义 noise_settings + 关键特征（甲烷湖/陨石坑/沙脊/天坑）。
- M2 生物：四种生物完整 AI、属性、渲染、生成规则与掉落表。
- ~~M3 环境系统~~（**已取消**：低重力/缺氧/极寒生存、保暖供氧装备、Capability、HUD、相关伤害均按设计修正移除）。
- M4 事件玩法：喷泉击飞、甲烷开采塔防（含自定义事件 + Mixin 刷怪）、晶洞惊扰。
- M5 结构与打磨：晶洞/前哨站结构、维度天空雾特效、流体（甲烷/氨）、音效与本地化、平衡。
- M6 群系特色化：修复气候 router（六群系真正分布）+ 3 新表层块（风化/沉积泰坦石、树枝结晶）+ 新群系荒芜高原 + 群系专属地形/地物（裂隙/甲烷海/真陨石坑/树枝结晶/破碎海绵/嵌套冰火山）+ 特征注入（add_features）。

---

## 九、构建验证与待验证 (Build Verification / To Verify)

### 已验证（本机实测，2026-07，联网下载依赖后）
- **工具链组合可用**：Gradle `9.6.1`（wrapper）+ Stonecutter `0.9.6` + Architectury Loom `1.11.458` + Forge `1.20.1-47.4.4` 相互兼容；Gradle 以本机 jdk-21 运行（编译工具链 Java 17 由 foojay 解析）。
- **配置成功**：`gradlew :1.20.1-forge:build --dry-run` → BUILD SUCCESSFUL，任务图正常。
- **Java 编译通过**：`gradlew :1.20.1-forge:compileJava` → BUILD SUCCESSFUL（仅 4 条 `[removal]` 弃用告警，见下）。
- **完整构建产出 jar**：`gradlew :1.20.1-forge:build` → 产出 `versions/1.20.1-forge/build/libs/titan_moon-forge-0.1.0+1.20.1.jar`；`META-INF/mods.toml` 的 `${...}` 模板已正确展开；维度/群系/噪声/lang/模型等资源均已打包进 jar。

### 构建脚本落地时修正的两处（Kotlin DSL 陷阱）
1. `createMinecraftArtifacts` 在配置期尚不存在（Loom 1.11 惰性注册该任务）→ 由 `named(...)` 改为
   `tasks.configureEach { if (name == "createMinecraftArtifacts") dependsOn("stonecutterGenerate") }`。
2. `processResources` 块内读 `gradle.properties` 必须用 `project.property("mod.id")`；裸 `property()` 会命中 `Task.property` 而报 "unknown property"。

### 已知告警（1.20.1 上无害）
- `new ResourceLocation(...)` 与 `FMLJavaModLoadingContext.get()` 在 Forge 47.4.4 被标记 `forRemoval`；1.20.1 下替代工厂方法未必存在，保持现状，待引入更高版本节点时用 Stonecutter `//? if` 版本化处理。

### 仍待验证（需启动游戏）
1. **进游戏实测维度**：占位 worldgen JSON 已通过打包与静态校验，但尚未启动客户端确认维度可进入、地形正常。`gradlew :1.20.1-forge:runClient` 后新建世界，`/execute in titan_moon:titan run tp @s 0 100 0` 核对地表 `tholin_sand`、Y 范围与群系橙色调。
2. **实体渲染**：`/summon titan_moon:aero_jelly` 确认复用史莱姆模型正常显示。
