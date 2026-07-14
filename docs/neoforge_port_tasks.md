# NeoForge 1.21.1 移植待办 (Titan Satellite)

> 在现有 Stonecutter(flat)+Loom 基础上增加 `1.21.1-neoforge` 节点，与 `1.20.1-forge` 并行。
> 每处平台差异用 Stonecutter `//? if forge { … } //?} else { … //?}` 隔离；共用体不变。
> **纪律：每完成一个包，必须 `:1.20.1-forge:compileJava` 保 Forge 绿，再继续。**
> 详见 [refs/multiloader-build.md](refs/multiloader-build.md)、[refs/loader-platform-api.md](refs/loader-platform-api.md)、[refs/stonecutter.md](refs/stonecutter.md)。

## 阶段 0 · 构建脚手架 ✅ 完成并验证
- [x] settings 加 `version("1.21.1-neoforge","1.21.1")`
- [x] `versions/1.21.1-neoforge/gradle.properties`（mc 1.21.1 / fml **21.1.235** / loom.platform=neoforge）
- [x] build.gradle `processResources` 双 `*.mods.toml` + 按加载器 exclude
- [x] `META-INF/neoforge.mods.toml`（type=required、mixin 声明）
- [x] `:1.20.1-forge:compileJava` BUILD SUCCESSFUL（Forge 不受影响）
- [x] NeoForge 已 provision、stonecutterGenerate 正常、`:1.21.1-neoforge:compileJava` 如期报错=待移植

## 阶段 1 · registry/ + 主类（地基） ✅ 完成（Forge 编译绿）
- [x] 主类 `TitanSatellite`：`@Mod` 总线隔离 + `rl()` / `mcRl()` ResourceLocation 辅助
- [x] 9/9 registry 已移植：TSDimensions/TSCreativeTabs/TSBlockEntities/TSMobEffects/TSEntities/
      TSFluids/TSFluidTypes/TSBlocks/TSItems
- 已确立范式：字段统一 `Supplier<T>`；`DeferredRegister.create(Registries.X, MODID)` 共用；
      仅 `DeferredRegister` import + FLUID_TYPES key + 特定 API 需 `//? if` 隔离
- 已隔离的关键 API：MobEffect 签名、ForgeFlowingFluid↔BaseFlowingFluid、
      ForgeSpawnEggItem↔DeferredSpawnEggItem、LiquidBlock/BucketItem 供应器(.get())、
      FluidType.initializeClient(Forge-only，NeoForge 客户端染色待阶段4)、NeoForgeRegistries.Keys.FLUID_TYPES
- ⚠️ 待 NeoForge 编译验证（需更多包移完才能编译）；neoforge 分支 API 正确性靠知识/参考，待编译器回馈迭代

## 阶段 2 · block/ blockentity/ fluid/ ✅ 完成（Forge 编译绿；NeoForge API 已 javap 核实）
- [x] `block/*`：`TholinCrystalBlock`（`playerWillDestroy` 1.21 返回 `BlockState` + `NeoForge.EVENT_BUS.post().isCanceled()`）、
      `SpecialMethanePumpBlock`（`use`→`useWithoutItem`）；4 个纯 vanilla 块无需改
- [x] `blockentity/SpecialMethanePumpBlockEntity`：Forge 能力(`LazyOptional`+`getCapability`+`invalidateCaps`) →
      NeoForge(`RegisterCapabilitiesEvent.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TYPE, (be,side)->tank)`，主类 addListener 挂接)；
      `saveAdditional`/`load`→`saveAdditional`/`loadAdditional`(带 `HolderLookup.Provider`)；`FluidTank`/`ItemStackHandler` NBT 带 provider；
      `above.getCapability(ForgeCapabilities.ITEM_HANDLER,dir)`→`Capabilities.ItemHandler.BLOCK.getCapability(level,pos,null,above,dir)`；
      `new FluidStack(Fluid,int)` 两版同（免隔离）
- [x] `fluid/TitanSounds`（registry 范式）、`fluid/TitanFluidInteractions`（NeoForge `@EventBusSubscriber`(bus 参数已废弃/忽略)、
      `EventHooks.fireFluidPlaceBlockEvent`、`FluidState.getFluidType()` 两版同免隔离；`TitanSounds` 注册移到主类构造器）
- javap 核实要点：`@EventBusSubscriber` 的 bus 由事件类型自动路由(IModBusEvent→mod 总线)；能力键 `Capabilities.{Fluid,Item}Handler.BLOCK`

## 阶段 3 · entity/（15 + 7 spawn）✅ 完成（Forge 编译绿；NeoForge API 已 javap 核实）
- [x] 7 个 `*Spawn`：`SpawnPlacements`→`SpawnPlacementTypes`、`SpawnPlacementRegisterEvent`→`RegisterSpawnPlacementsEvent`、
      `@Mod.EventBusSubscriber`→`@EventBusSubscriber`；`SpawnPlacementTypes.ON_GROUND`；
      CorruptedProbe 用方法名 `register`+`Operation.OR`，其余 `registerSpawnPlacement`+`Operation.REPLACE`
- [x] `AmmoniaStalker`：`BlockPathTypes`→`PathType`（import + `setPathfindingMalus` 用法均隔离）
- [x] `TSMobEffects.tholinToxin(dur,amp)` 助手：**1.21 `MobEffectInstance` 收 `Holder<MobEffect>` 不是 `MobEffect`**→
      neoforge 用 `BuiltInRegistries.MOB_EFFECT.wrapAsHolder(...)`（javap 核实 `Registry.wrapAsHolder`）；
      THOLIN_TOXIN 4 处调用点(AmmoniaStalker/NativeIceWorm/TholinWeaver/TholinCrystalBlock)全改助手
- 无实体 override `finalizeSpawn` / 用 `MobType`（grep 确认，省事）；目标 1.21.1→Properties 不需 setId

## 阶段 4 · client/（11）✅ 完成（Forge 编译绿；NeoForge 客户端 API 已 javap 核实）
- [x] 8 渲染器 `TEXTURE`+`FogHandler` METHANE_ABYSS+`TitanDimensionEffects` KEY → `TitanSatellite.rl(path)`（纯 vanilla 模型，无其他改动）
- [x] `TitanDimensionEffects`/`FogHandler`/`TitanClientEvents`：`RegisterDimensionSpecialEffectsEvent`/`ViewportEvent`/
      `EntityRenderersEvent`/`RegisterColorHandlersEvent` 事件 API 两版签名相同→仅 import + `@EventBusSubscriber` 注解隔离
- [x] 甲烷/氨流体客户端染色：Forge `DynamicFluidContainerModel`(Forge-only) ↔ NeoForge
      `RegisterClientExtensionsEvent.registerFluidType(IClientFluidTypeExtensions, ...)`（补阶段1 `initializeClient` 缺口，
      复刻甲烷/氨 tint + 水贴图）
- javap 核实：`DimensionSpecialEffects`/`ViewportEvent`/事件 API 两版一致；`RegisterClientExtensionsEvent` 在
      `net.neoforged.neoforge.client.extensions.common`（非 `.event`）；NeoForge `@EventBusSubscriber` 省 bus 参数

## 阶段 5 · event/ config/ mixin/ ✅ 完成（Forge 编译绿；NeoForge API 已 javap 核实）
- [x] `config/TSConfig`：`ForgeConfigSpec`↔`ModConfigSpec`（字段类型简名隔离）；`ModLoadingContext.get().getActiveContainer()
      .registerConfig(...)`（registerConfig 在 ModContainer）；`MinecraftForge`↔`NeoForge` 事件总线；
      **1.21 `AttributeModifier(ResourceLocation, double, Operation)`**（Record，替 UUID 构造）+ `ADD_VALUE`/`ADD_MULTIPLIED_TOTAL`
- [x] `event/*`：自定义事件 `net.minecraftforge.eventbus.api.Event`↔`net.neoforged.bus.api.Event`；
      **`@Cancelable`(Forge) ↔ `implements ICancellableEvent`(NeoForge)**；`EVENT_BUS.post` 取消判断 `.isCanceled()`；
      `WaveController` 1.21 `finalizeSpawn` 4 参（去尾 CompoundTag）
- [x] `mixin/`：**无需改动**——`WaveSpawnMixin`/`TitanMixinPlugin` 纯 SpongePowered+vanilla API（`aiStep`/`MobEffects`/
      `MobEffectInstance` 全 co-vary），两版通用；refmap 由各节点自处理
- javap 核实：`MobSpawnType.EVENT` 1.21.1 仍在；`ModConfigSpec implements IConfigSpec`；`NeoForge.EVENT_BUS`；
      `AttributeModifier` 是 Record（`ResourceLocation id()`）

## 阶段 6 · data/（datagen）+ worldgen/ ✅ 完成（两端 :compileJava 均 BUILD SUCCESSFUL）
- [x] datagen：NeoForge 保留了 Forge 风格模型生成器（`BlockStateProvider`/`ItemModelProvider`/`LanguageProvider`/
      `SoundDefinitionsProvider`/`BlockTagsProvider`/`DynamicFluidContainerModelBuilder` 同名，只换包 `net.neoforged.neoforge.*`）；
      `GatherDataEvent`/`ExistingFileHelper` 换包；`new ResourceLocation` → `TitanSatellite.rl/mcRl/parse`
- [x] **1.21 loot datagen 大改**：`LootTableSubProvider.generate(BiConsumer<ResourceKey<LootTable>,…>)`；
      `SubProviderEntry(Function<HolderLookup.Provider,…>,…)`（子 provider 构造器收 provider）；
      `LootingEnchantFunction`→`EnchantedCountIncreaseFunction.lootingMultiplier(provider,…)`；
      `LootTableProvider` 4 参构造（带 lookup future）；`BlockLootSubProvider` 3 参构造
- [x] worldgen/：`TSWorldgenTypes`/`TSStructures`/`TSSystemsBootstrap`（DeferredRegister/IEventBus 隔离，`Registries.FEATURE`，
      `RegistryObject`→`Supplier`，onConstruct 自装配 Forge-only + NeoForge 主类构造器 `register(modBus)`）；
      ~17 feature/density 文件纯 vanilla 免改
- [x] **javac 兜底捕获的 6 处 1.21 vanilla 变更**：`BaseEntityBlock.codec()`、`canBreatheUnderwater` final→`canDrownInFluidType`、
      Codec→MapCodec（density/structure）、`setLootTable(ResourceKey<LootTable>)`
- [x] 构建坑：`mods.toml` 的 `[[dependencies.${id}]]` 表键 → 硬编码 `titan_satellite`（Loom TOML 解析）

## 阶段 7 · 运行期与验证 ✅ 编译/打包完成
- [x] **两端 `:compileJava` 均 BUILD SUCCESSFUL**（Forge 1.20.1 + NeoForge 1.21.1）
- [x] `pack.mcmeta` pack_format 版本化：`vers.packFormat`（15 ↔ 34）经 processResources `expand` 注入
- [x] **两端 `:build` 均 BUILD SUCCESSFUL**，产出 mod jar：
      `titan_satellite-forge-0.1.0+1.20.1.jar`（344 KB）+ `titan_satellite-neoforge-0.1.0+1.21.1.jar`（342 KB）
- [x] 元数据分离正确：NeoForge 包内仅 `neoforge.mods.toml`（`mods.toml` 已 exclude）
- [ ] （可选后续）`runClient`/`runData`/`runServer` 运行期冒烟；worldgen JSON 1.21 格式校验；
      NeoForge `AmmoniaStalker` 水下呼吸如需与 Forge 完全一致可加实体类型 tag `minecraft:can_breathe_under_water`

> 排障：Gradle 9.6 会吞掉 javac 诊断（compileJava FAILED 但控制台无错误行）。对策：`-I printcp.init.gradle.kts`
> dump 编译类路径，再对 `versions/1.21.1-neoforge/build/generated/stonecutter/main/java` 直接跑 javac（见 build/compile_nf.ps1）。
> Loom 配置报错会被 daemon 缓存，改 toml/构建脚本后需 `gradlew --stop`。
