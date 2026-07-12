# Forge 1.20.1 自定义维度与 ChunkGenerator 参考：Bumblezone Worldgen 研究笔记

本文基于 TelepathicGrunt/Bumblezone `1.20.x-Arch` 分支的 `common/src/main/java/com/telepathicgrunt/the_bumblezone/worldgen` 与对应 `data/the_bumblezone/worldgen` 数据包整理，目标是给 AI 或移植者提供一份可复用的 1.20.1 Forge 自定义维度、`BiomeSource`、`ChunkGenerator`、噪声与地表规则资料。

> 重要版本提醒：该 Bumblezone 分支带有较新的 1.20.x/Architectury 写法，例如 `MapCodec`、数据组件等。Forge 1.20.1 落地时，部分注册类型可能仍是 `Codec<? extends ...>` 或 `KeyDispatchDataCodec<? extends ...>`。本文会同时说明 Bumblezone 的模式与 1.20.1 Forge 迁移注意点。

## 1. 入口结构总览

Bumblezone 的 worldgen 分为两部分：

1. Java 注册与运行时逻辑：
   - `modinit/BzDimension.java`：注册自定义 `ChunkGenerator`、`BiomeSource`、`DensityFunction` codec。
   - `worldgen/dimension/BzChunkGenerator.java`：继承 `NoiseBasedChunkGenerator`，接管 biome 生成、噪声填充、地表构建、mob 初始生成。
   - `worldgen/dimension/BzBiomeSource.java`：自定义二维 biome 采样器，使用类旧版 Layer 的流程生成蜂巢维度 biome。
   - `worldgen/dimension/layer/*`：自定义 biome 层、缩放层、合并层。
   - `worldgen/dimension/BiomeInfluencedNoiseSampler.java`：根据 biome 类型给地形密度增加高度影响。
   - `worldgen/dimension/NoVerticalBlendBiomeManager.java`：禁止 vanilla biome manager 在 Y 方向混合。
   - `worldgen/surfacerules/PollinatedSurfaceSource.java`：自定义 SurfaceRule 类型，按 3D noise 修改带 `LAYERS` 属性的方块层数。
   - `modinit/BzFeatures.java`、`BzPlacements.java`、`BzStructures.java` 等：注册 feature、placement modifier、structure type、structure placement type、processor、rule test。

2. 数据包资源：
   - `data/the_bumblezone/dimension/the_bumblezone.json`
   - `data/the_bumblezone/dimension_type/the_bumblezone.json`
   - `data/the_bumblezone/worldgen/noise_settings/bz_noise_settings.json`
   - `data/the_bumblezone/worldgen/density_function/*.json`
   - `data/the_bumblezone/worldgen/biome/*.json`
   - `data/the_bumblezone/tags/worldgen/biome/*.json`
   - `data/the_bumblezone/worldgen/configured_feature/*.json`
   - `data/the_bumblezone/worldgen/placed_feature/*.json`
   - `data/the_bumblezone/worldgen/structure/*.json`
   - `data/the_bumblezone/worldgen/structure_set/*.json`

核心设计是：维度 JSON 引用自定义 generator；generator 使用 vanilla `NoiseGeneratorSettings` 和 `NoiseRouter`；噪声设置中的 `final_density` 引用一个自定义 `DensityFunction`，让 biome 类型影响地形高度；`BiomeSource` 自己生成 X/Z 平面上的 biome 分布；features/structures 仍通过数据包挂到 biome。

## 2. 维度注册链路

### 2.1 代码注册

`BzDimension` 注册三个关键类型：

```java
public static final ResourceKey<Level> BZ_WORLD_KEY =
    ResourceKey.create(Registries.DIMENSION, Bumblezone.MOD_DIMENSION_ID);

public static final ResourcefulRegistry<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR =
    ResourcefulRegistries.create(BuiltInRegistries.CHUNK_GENERATOR, Bumblezone.MODID);

public static final ResourcefulRegistry<MapCodec<? extends BiomeSource>> BIOME_SOURCE =
    ResourcefulRegistries.create(BuiltInRegistries.BIOME_SOURCE, Bumblezone.MODID);

public static final ResourcefulRegistry<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS =
    ResourcefulRegistries.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, Bumblezone.MODID);

public static final RegistryEntry<MapCodec<BzChunkGenerator>> BZ_CHUNK_GENERATOR =
    CHUNK_GENERATOR.register("chunk_generator", () -> BzChunkGenerator.CODEC);

public static final RegistryEntry<MapCodec<BzBiomeSource>> BZ_BIOME_SOURCE =
    BIOME_SOURCE.register("biome_source", () -> BzBiomeSource.CODEC);

public static final RegistryEntry<MapCodec<BzChunkGenerator.BiomeNoise>> BZ_BIOME_FUNCTION =
    DENSITY_FUNCTIONS.register("biome_function", BzChunkGenerator.BiomeNoise.CODEC::codec);
```

Forge 1.20.1 写法通常可以用 `DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID)`、`DeferredRegister.create(Registries.BIOME_SOURCE, MODID)`、`DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID)`。如果目标 mappings 中 registry value 是 `Codec` 而不是 `MapCodec`，把 `RecordCodecBuilder.mapCodec(...)` 改成 `RecordCodecBuilder.create(...)` 并调整 `codec()` 返回类型。

### 2.2 初始化时机

Bumblezone 主类初始化时注册 worldgen 相关 registry：

```java
BzFeatures.FEATURES.init();
BzPredicates.RULE_TEST.init();
BzStructures.STRUCTURES.init();
BzDimension.BIOME_SOURCE.init();
BzDimension.CHUNK_GENERATOR.init();
BzSurfaceRules.SURFACE_RULES.init();
BzDimension.DENSITY_FUNCTIONS.init();
BzPlacements.PLACEMENT_MODIFIER.init();
BzProcessors.STRUCTURE_PROCESSOR.init();
BzStructurePlacementType.STRUCTURE_PLACEMENT_TYPE.init();
```

服务端即将启动时还做两件重要事：

```java
PollinatedSurfaceSource.RandomLayerStateRule.initNoise(serverSeed);
BiomeRegistryHolder.setupBiomeRegistry(event.getServer());
```

`BiomeRegistryHolder` 保存服务器动态 biome registry，供 biome layer 用 int id 与 biome key 相互转换。这个模式能工作，但对 datapack reload、多个动态 registry、跨世界一致性比较敏感。更稳的自定义实现可以尽量在 layer 内保存 `ResourceKey<Biome>` 或 `Holder<Biome>`，少依赖动态 int id。

## 3. 数据包维度文件

### 3.1 `data/<modid>/dimension/<id>.json`

Bumblezone 的维度文件：

```json
{
  "type": "the_bumblezone:the_bumblezone",
  "generator": {
    "type": "the_bumblezone:chunk_generator",
    "settings": "the_bumblezone:bz_noise_settings",
    "biome_source": {
      "type": "the_bumblezone:biome_source",
      "main_biomes": "#the_bumblezone:the_bumblezone",
      "blob_biomes": "#the_bumblezone:blob_biomes",
      "rare_blob_biomes": "#the_bumblezone:rare_blob_biomes"
    }
  }
}
```

要点：

- 顶层 `type` 指向 `dimension_type/<id>.json`。
- `generator.type` 指向注册到 `Registries.CHUNK_GENERATOR` 的 codec id。
- `settings` 指向 `worldgen/noise_settings/<id>.json`。
- `biome_source.type` 指向注册到 `Registries.BIOME_SOURCE` 的 codec id。
- `main_biomes`、`blob_biomes`、`rare_blob_biomes` 使用 biome tag。`Biome.LIST_CODEC` 可以解析 `"#namespace:tag"`。

### 3.2 `dimension_type`

Bumblezone 是一个有天花板的室内/蜂巢维度：

```json
{
  "effects": "the_bumblezone:dimension_special_effects",
  "ultrawarm": false,
  "natural": true,
  "ambient_light": 0.18,
  "has_skylight": false,
  "has_ceiling": true,
  "infiniburn": "#minecraft:infiniburn_overworld",
  "logical_height": 256,
  "height": 256,
  "min_y": 0,
  "has_raids": false,
  "respawn_anchor_works": false,
  "bed_works": true,
  "fixed_time": 6000,
  "piglin_safe": true,
  "coordinate_scale": 4.0,
  "monster_spawn_block_light_limit": 12,
  "monster_spawn_light_level": 11
}
```

常用字段解释：

- `height` 与 `min_y` 必须和 `noise_settings.noise.min_y/height` 对齐。
- `has_ceiling: true` 会影响 mob 初始生成逻辑、地图行为和维度体验。
- `has_skylight: false` 适合洞穴/屋顶维度。
- `coordinate_scale` 控制坐标比例，不直接影响 chunk generator。
- `fixed_time` 固定昼夜，用于没有天空的维度很常见。

Forge 1.20.1 的 `monster_spawn_light_level` 可以是常量或 light level provider，注意与目标版本 JSON schema 匹配。

## 4. `BzBiomeSource`：二维自定义 BiomeSource

### 4.1 Codec 结构

```java
public static final MapCodec<BzBiomeSource> CODEC =
    RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.LONG.fieldOf("seed").orElse(0L).stable().forGetter(s -> s.seed),
        Biome.LIST_CODEC.fieldOf("blob_biomes").orElse(HolderSet.direct()).forGetter(s -> s.blobBiomes),
        Biome.LIST_CODEC.fieldOf("rare_blob_biomes").orElse(HolderSet.direct()).forGetter(s -> s.rareBlobBiomes),
        Biome.LIST_CODEC.fieldOf("main_biomes").orElse(HolderSet.direct()).forGetter(s -> s.mainBiomes)
    ).apply(instance, instance.stable(BzBiomeSource::new)));
```

字段含义：

- `seed`：用于 layer 生成。Bumblezone 的 dimension JSON 没传 seed，所以默认是 `0L`。如果希望 biome 分布随世界种子变化，需要在数据生成或构造时注入世界 seed，或在 generator 中用世界 seed 参与采样。
- `main_biomes`：维度可用主 biome 集合。
- `blob_biomes`：叠加层中的常见 blob biome。
- `rare_blob_biomes`：叠加层中的稀有 blob biome。

`collectPossibleBiomes()` 与 `possibleBiomes()` 返回三个 HolderSet 的并集。自定义 `BiomeSource` 必须正确报告可能 biome，否则结构定位、mob spawn、调试和某些缓存会出问题。

### 4.2 只按 X/Z 采样

```java
@Override
public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
    return biomeSampler.sample(x, z);
}

@Override
public Holder<Biome> getNoiseBiome(int x, int y, int z) {
    return biomeSampler.sample(x, z);
}
```

Bumblezone 完全忽略 noise-biome 坐标中的 `y`。这适合屋顶维度：同一 X/Z 柱从底到顶都是同一个 biome，避免垂直 biome 切换导致 surface rule、features、颜色和 mob spawn 变得混乱。

## 5. Biome Layer 管线

`BzBiomeSource.build(...)` 组合了一套类 vanilla 旧版 Layer 系统：

```java
AreaFactory<T> layer = new BzBiomeLayer(seed).run(contextFactory.apply(200L));
layer = new BzBiomePillarLayer().run(contextFactory.apply(1008L), layer);
layer = new BzBiomeScaleLayer(Set.of(HIVE_PILLAR)).run(contextFactory.apply(1055L), layer);
layer = ZoomLayer.FUZZY.run(contextFactory.apply(2003L), layer);
layer = ZoomLayer.FUZZY.run(contextFactory.apply(2523L), layer);
layer = new BzBiomeScaleLayer(Set.of(CRYSTAL_CANYON, SUGAR_WATER_FLOOR)).run(contextFactory.apply(54088L), layer);

AreaFactory<T> layerOverlay = new BzBiomeBlobLayer(blobBiomes, rareBlobBiomes).run(contextFactory.apply(204L));
layerOverlay = ZoomLayer.NORMAL.run(contextFactory.apply(2423L), layerOverlay);
layerOverlay = new BzBiomePollinatedPillarLayer().run(contextFactory.apply(3008L), layerOverlay);
layerOverlay = new BzBiomeScaleLayer(Set.of(POLLINATED_PILLAR)).run(contextFactory.apply(4455L), layerOverlay);
layerOverlay = ZoomLayer.NORMAL.run(contextFactory.apply(2503L), layerOverlay);
layerOverlay = ZoomLayer.NORMAL.run(contextFactory.apply(2603L), layerOverlay);
layerOverlay = new BzBiomePollinatedFieldsLayer().run(contextFactory.apply(3578L), layerOverlay);
layerOverlay = new BzBiomeScaleLayer(Set.of(POLLINATED_FIELDS)).run(contextFactory.apply(4055L), layerOverlay);
layerOverlay = ZoomLayer.FUZZY.run(contextFactory.apply(2853L), layerOverlay);
layerOverlay = ZoomLayer.FUZZY.run(contextFactory.apply(3583L), layerOverlay);
layerOverlay = ZoomLayer.NORMAL.run(contextFactory.apply(4583L), layerOverlay);

layer = new BzBiomeMergeLayer().run(contextFactory.apply(5583L), layerOverlay, layer);
```

### 5.1 主层：`BzBiomeLayer`

主层用 `PerlinSimplexNoise` 决定基础 biome：

- `perlinGen.getValue(x * 0.1, z * 0.0001, false)` 形成长条状 hive wall。
- 当 `abs(perlinNoise) % 0.1 < 0.07` 时生成 `hive_wall`。
- 否则用第二个噪声 `x * 0.02, z * 0.02`：
  - `abs(perlinNoise2) > 0.55` 生成 `crystal_canyon`。
  - 否则生成 `sugar_water_floor`。

这是典型的“主底图”思路：不要直接随机每个点，而是用低频噪声生成大尺度区域。

### 5.2 柱状 biome：`BzBiomePillarLayer`

```java
if (context.nextRandom(12) == 0 && n == c && e == c && s == c && w == c) {
    return HIVE_PILLAR;
}
```

只有当上下左右和中心都是同一个 biome 时才可能插入柱。这样能避免在 biome 边界上产生碎裂柱。

### 5.3 扩张层：`BzBiomeScaleLayer`

如果中心不是目标 biome，但四邻之一是目标 biome，则把中心改成邻居目标 biome。可用于扩张 `hive_pillar`、`crystal_canyon`、`sugar_water_floor`、`pollinated_pillar`、`pollinated_fields`。

### 5.4 Blob 叠加层：`BzBiomeBlobLayer`

叠加层先生成稀疏点：

- `blobBiomes` 非空且 `nextRandom(12) == 0`：随机选择一个常见 blob biome。
- `rareBlobBiomes` 非空且 `nextRandom(48) == 0`：随机选择一个稀有 blob biome。
- 否则返回 `-1`，代表空。

之后叠加层经历多次 zoom、pollinated pillar 插入、pollinated fields 扩张，最后由 `BzBiomeMergeLayer` 合并：

```java
if (overlayBiomeId == -1) return baseBiomeId;
else return overlayBiomeId;
```

这个设计适合“主地形 + 小区域特色生态”的维度。

## 6. `BzChunkGenerator`：继承 `NoiseBasedChunkGenerator`

Bumblezone 没有从零实现 `ChunkGenerator`，而是继承 `NoiseBasedChunkGenerator`。这是 1.20.1 自定义维度最稳的路线：保留 Mojang 的 `NoiseRouter`、`NoiseChunk`、`SurfaceSystem`、heightmap 等内部机制，只覆盖必要行为。

### 6.1 Codec

```java
public static final MapCodec<BzChunkGenerator> CODEC =
    RecordCodecBuilder.mapCodec(instance -> instance.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
        NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(g -> g.settings)
    ).apply(instance, instance.stable(BzChunkGenerator::new)));
```

维度 JSON 中的 `generator` 字段正是按这个 codec 解析：

```json
{
  "type": "the_bumblezone:chunk_generator",
  "settings": "the_bumblezone:bz_noise_settings",
  "biome_source": { "type": "the_bumblezone:biome_source", ... }
}
```

### 6.2 构造器初始化

构造器从 `NoiseGeneratorSettings` 中拿到：

- `defaultBlock`
- `defaultFluid`
- `NoiseRouter`
- `seaLevel`
- `spawnTarget`

并建立全局 fluid picker：

```java
Aquifer.FluidStatus sea = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());
this.globalFluidPicker = (x, y, z) -> sea;
```

因为 Bumblezone 的 `aquifers_enabled` 是 `false`，fluid picker 更多是满足 `NoiseChunk` API 和默认 fluid 行为。

### 6.3 自定义 DensityFunction：`BiomeNoise`

`BzChunkGenerator.BiomeNoise` 是一个 `DensityFunction.SimpleFunction`：

```java
public record BiomeNoise() implements DensityFunction.SimpleFunction {
    public static Climate.Sampler sampler;
    public static BiomeSource biomeSource;

    @Override
    public double compute(FunctionContext ctx) {
        return BiomeInfluencedNoiseSampler.calculateBaseNoise(
            ctx.blockX(), ctx.blockZ(), sampler, biomeSource, BiomeRegistryHolder.BIOME_REGISTRY);
    }

    @Override public double minValue() { return -10; }
    @Override public double maxValue() { return 10; }
}
```

它被注册为 `the_bumblezone:biome_function`，然后在 `final_density.json` 中引用：

```json
{
  "type": "the_bumblezone:biome_function"
}
```

注意：这里用 static 字段把 `BiomeSource` 和 `Climate.Sampler` 传给 density function。原因是 datapack JSON 创建出来的 density function 本身只收到坐标上下文，不天然知道当前 generator 的 biome source。这个写法简单，但多维度、多服务器或热重载场景要小心。更健壮的方案是避免 density function 依赖全局状态，或者确保只有一个相关 generator 实例并在服务端生命周期中正确刷新。

### 6.4 Biome 创建：强制 Y=0

```java
private BiomeResolver getBiomeResolver(BiomeResolver noiseBiome) {
    return (x, y, z, sampler) -> noiseBiome.getNoiseBiome(x, 0, z, sampler);
}
```

`createBiomes(...)` 创建 `NoiseChunk` 和 climate sampler 后调用：

```java
chunkAccess.fillBiomesFromNoise(biomeResolver, sampler);
```

这会把 chunk 内所有 biome container 位置都按同一个 X/Z biome 取样，消除垂直 biome 过渡。

### 6.5 地表构建：`NoVerticalBlendBiomeManager`

`buildSurface(...)` 先构造或取得 `BiomeManager`，再包一层：

```java
biomeManager = new NoVerticalBlendBiomeManager(biomeManager);
surfaceSystem.buildSurface(..., biomeManager, ..., noiseSettings.surfaceRule());
```

`NoVerticalBlendBiomeManager.getBiome(BlockPos)` 复制 vanilla zoom/fiddled-distance 逻辑，但把 Y 坐标固定为 `0`。这样 SurfaceRule 中的 `minecraft:biome` 条件不会因为 y 方向采样抖动而错选 biome。

### 6.6 噪声填充：`fillFromNoise` 与 `doFill`

Bumblezone 重写了 `fillFromNoise(...)`，但整体仍沿用 `NoiseChunk` 插值流程：

1. 从 `NoiseGeneratorSettings.noiseSettings()` 得到 `minY`、`height`、cell width、cell height。
2. 获取 chunk section 范围并 `acquire()`，异步填充完成后 `release()`。
3. `NoiseChunk.forChunk(...)` 创建噪声计算对象。
4. 三重循环遍历 noise cell、cell 内 y、cell 内 x/z。
5. 通过 mixin accessor 调用 `NoiseChunk#getInterpolatedState()`。
6. 如果状态非空气，则写入 section，并更新 `OCEAN_FLOOR_WG` 与 `WORLD_SURFACE_WG` heightmap。
7. 如果 aquifer 需要流体更新且方块有 fluid state，则标记 postprocessing。

对自定义 generator 来说，这部分是最容易写错的地方。除非必须改变填充策略，否则 1.20.1 Forge 更建议：

- 继承 `NoiseBasedChunkGenerator`。
- 尽量让 `NoiseGeneratorSettings` 和 `NoiseRouter` 数据驱动。
- 只在需要禁用 carver、控制 biome Y、修改 surface biome manager 时覆盖方法。

### 6.7 禁用 carver

```java
@Override
public void applyCarvers(..., GenerationStep.Carving carving) {}
```

Bumblezone 不用 vanilla carver，而是通过自己的 density function、features 或 structure 来塑形。

### 6.8 高度查询

`getBaseHeight(...)` 与 `getBaseColumn(...)` 都走 `iterateNoiseColumn(...)`。该方法单独构造一个宽度为 1 的 `NoiseChunk`，沿 Y 从上往下找第一个满足 heightmap predicate 的方块。自定义 generator 必须保证高度查询与 `fillFromNoise` 的实际方块生成一致，否则结构定位、spawn、feature placement 会偏。

### 6.9 初始生物生成

`spawnOriginalMobs(...)`：

- 检查 `disableMobGeneration`。
- 用中心 chunk 顶部 biome 的 mob settings。
- 调用自定义 `spawnNonBeeMobsForChunkGeneration(...)`。

`spawnNonBeeMobsForChunkGeneration` 会过滤 `EntityType.BEE`，只生成非蜜蜂的 creature 类实体。原因是 Bumblezone 对蜜蜂有额外生态/行为控制，不希望 vanilla chunk generation 原始生成逻辑直接刷蜜蜂。

对自己的维度：

- 如果不需要 chunk 初始动物生成，可以保持默认或禁用。
- 如果有 ceiling 维度，生成位置常要从高处向下找空气/地面。
- 特殊实体最好过滤或单独在 feature/structure 中生成。

## 7. 噪声设置与 DensityFunction 组合

### 7.1 `noise_settings`

Bumblezone 的 `bz_noise_settings.json` 关键字段：

```json
{
  "ore_veins_enabled": false,
  "legacy_random_source": true,
  "disable_mob_generation": false,
  "aquifers_enabled": false,
  "default_block": { "Name": "minecraft:honeycomb_block" },
  "default_fluid": {
    "Name": "the_bumblezone:sugar_water_block",
    "Properties": { "level": "0" }
  },
  "sea_level": 40,
  "noise_router": {
    "final_density": "the_bumblezone:final_density",
    "temperature": 0,
    "vegetation": 0,
    "continents": 0,
    "erosion": 0,
    "depth": 0,
    "ridges": 0,
    "barrier": 0,
    "fluid_level_floodedness": 0,
    "fluid_level_spread": 0,
    "lava": 0
  },
  "noise": {
    "min_y": 0,
    "height": 256,
    "size_horizontal": 1,
    "size_vertical": 1
  },
  "surface_rule": { "...": "..." }
}
```

要点：

- `default_block` 是密度为正时默认填充的方块。
- `default_fluid` 是 sea level/aquifer 相关默认流体。
- `final_density` 是最终决定方块/空气的 density function。
- `temperature` 等 climate 参数都设为 `0`，因为 biome 分布完全由自定义 `BiomeSource` 决定，不依赖 vanilla climate parameter list。
- `size_horizontal`、`size_vertical` 越小，cell 越密，地形越细，但生成开销更高。

### 7.2 `final_density.json`

Bumblezone 最终密度大致是：

```json
{
  "type": "min",
  "argument1": 0.25,
  "argument2": {
    "type": "max",
    "argument1": -0.1,
    "argument2": {
      "type": "interpolated",
      "argument": {
        "type": "max",
        "argument1": -1.5,
        "argument2": {
          "type": "minecraft:add",
          "argument1": "the_bumblezone:floor_final",
          "argument2": {
            "type": "minecraft:add",
            "argument1": "the_bumblezone:ceiling_final",
            "argument2": {
              "type": "minecraft:add",
              "argument1": "the_bumblezone:coast",
              "argument2": {
                "type": "minecraft:add",
                "argument1": { "type": "the_bumblezone:biome_function" },
                "argument2": {
                  "type": "max",
                  "argument1": "the_bumblezone:ceiling",
                  "argument2": "the_bumblezone:floor"
                }
              }
            }
          }
        }
      }
    }
  }
}
```

含义：

- `floor`：底部实体地形趋势。
- `ceiling`：顶部实体地形趋势。
- `coast`：海平面附近或糖水岸线修饰。
- `biome_function`：根据 biome 抬高/压低地形。
- `max(ceiling, floor)`：同时保留天花板和地板结构，适合 roofed dimension。
- 外层 `max(-0.1, ...)` 与 `min(0.25, ...)` 把密度夹在范围内，避免过强极值。
- `interpolated` 让 density 在 cell 间平滑。

### 7.3 地板与天花板

`floor.json`：

```json
{
  "type": "minecraft:add",
  "argument1": {
    "type": "minecraft:y_clamped_gradient",
    "from_y": 15,
    "to_y": 135,
    "from_value": 1,
    "to_value": -2
  },
  "argument2": {
    "type": "minecraft:noise",
    "noise": "the_bumblezone:floor",
    "xz_scale": 0.66,
    "y_scale": 0.2
  }
}
```

低处密度偏正，高处密度偏负，再加噪声形成起伏地板。

`ceiling.json`：

```json
{
  "type": "minecraft:add",
  "argument1": {
    "type": "minecraft:y_clamped_gradient",
    "from_y": 95,
    "to_y": 240,
    "from_value": -2,
    "to_value": 1
  },
  "argument2": {
    "type": "minecraft:noise",
    "noise": "the_bumblezone:ceiling",
    "xz_scale": 0.66,
    "y_scale": 0.2
  }
}
```

高处密度偏正，低处密度偏负，形成天花板。

`floor_final` 和 `ceiling_final` 是额外 y 梯度，用于加固底部和顶部边界：

- `floor_final`: y 8 到 38 从 `0.6` 过渡到 `-0.5`。
- `ceiling_final`: y 230 到 248 从 `-0.35` 过渡到 `1`。

`coast` 在 y 39 到 50 从 `0.65` 过渡到 `0.0`，用于海平面/糖水区域附近的地形处理。

### 7.4 Biome 影响高度

`BiomeInfluencedNoiseSampler`：

- 采样中心 biome 和半径 2 内的 5x5 邻近 biome。
- 每个邻近点按距离给权重：`10 / sqrt(x*x + z*z + 0.2) / 22`。
- 通过 `BzBiomeHeightRegistry` 查询 biome 的 `depth` 和 `weightModifier`。
- 如果邻居 biome depth 与中心不同，则 `lerp(center.weightModifier, neighborDepth, centerDepth)`，让中心 biome 控制边界混合强度。
- 总和除以 60 后作为 density 加成。
- 使用 `ConcurrentHashMap<Long, Float>` 缓存 X/Z 结果，超过 2000 条就清空。

Bumblezone 的 biome terrain 参数：

| Biome | depth | weightModifier |
| --- | ---: | ---: |
| `hive_pillar` | 22.0 | 0.35 |
| `hive_wall` | 19.0 | 0.25 |
| `pollinated_fields` | 5.4 | 0.9 |
| `pollinated_pillar` | 22.5 | 0.05 |
| `sugar_water_floor` | -3.7 | 0.75 |
| `crystal_canyon` | 0.0 | 0.75 |
| `floral_meadow` | 0.2 | 0.75 |
| `howling_constructs` | 0.18 | 0.78 |

设计模式：把 biome 不仅作为装饰/颜色/mob 容器，也作为 terrain profile 输入。这样同一套 density function 可以按 biome 产生不同高度。

## 8. Surface Rules

Bumblezone 的地表规则主要写在 `bz_noise_settings.json` 中，使用 vanilla surface rule：

- 顶部/底部边界用 `y_above`、`vertical_gradient` 放置 `beehive_beeswax` 或 `honeycomb_block`。
- 用 `minecraft:biome` 按 biome 切换地表材质。
- 用 `minecraft:stone_depth` 区分 floor/ceiling 表层深度。
- 用 `minecraft:noise_threshold` 生成雕刻蜡、泥土、糖石等花纹。

示例模式：

```json
{
  "type": "minecraft:condition",
  "if_true": {
    "type": "minecraft:biome",
    "biome_is": ["the_bumblezone:crystal_canyon"]
  },
  "then_run": {
    "type": "minecraft:sequence",
    "sequence": [
      {
        "type": "minecraft:condition",
        "if_true": {
          "type": "minecraft:stone_depth",
          "offset": 0,
          "add_surface_depth": true,
          "add_surface_secondary_depth": true,
          "secondary_depth_range": 4,
          "surface_type": "floor"
        },
        "then_run": {
          "type": "minecraft:block",
          "result_state": { "Name": "the_bumblezone:carvable_wax" }
        }
      }
    ]
  }
}
```

`PollinatedSurfaceSource` 是注册了但当前主 `bz_noise_settings.json` 未直接引用的扩展点。它展示了自定义 `SurfaceRules.RuleSource` 的写法：

- 注册到 `BuiltInRegistries.MATERIAL_RULE`。
- codec 读取一个 `result_state`。
- `tryApply(x, y, z)` 中按 OpenSimplex 3D noise 修改 `LAYERS` 属性。
- 服务端启动时用世界种子初始化 noise。

在 Forge 1.20.1 中，自定义 surface rule 需要确认 registry 类型和 codec 返回类型与 mappings 匹配。

## 9. Biome JSON 与 feature 阶段

Bumblezone biome JSON 的 `features` 是一个 10 个阶段的二维数组，对应 `GenerationStep.Decoration` 的顺序。以 `hive_wall` 为例：

```json
"features": [
  ["the_bumblezone:honeycomb_caves", "the_bumblezone:honeycomb_holes"],
  [],
  [],
  ["the_bumblezone:bee_dungeon", "the_bumblezone:spider_infested_bee_dungeon", "the_bumblezone:tree_dungeon"],
  [],
  ["the_bumblezone:web_wall"],
  ["the_bumblezone:giant_honey_crystals_cave", "the_bumblezone:honey_crystals_common", "the_bumblezone:sticky_honey_residue_feature"],
  [],
  ["the_bumblezone:cave_sugar_waterfall", "the_bumblezone:sugar_waterfall_high", "the_bumblezone:sugar_waterfall_low"],
  []
]
```

常见顺序可按 vanilla enum 理解：

1. `RAW_GENERATION`
2. `LAKES`
3. `LOCAL_MODIFICATIONS`
4. `UNDERGROUND_STRUCTURES`
5. `SURFACE_STRUCTURES`
6. `STRONGHOLDS`
7. `UNDERGROUND_ORES`
8. `UNDERGROUND_DECORATION`
9. `FLUID_SPRINGS`
10. `VEGETAL_DECORATION`

实际 mappings/版本中 enum 名称可能略有差异，但 JSON 数组位置必须与目标版本一致。

## 10. Feature、Placement、Structure 注册模式

### 10.1 Feature

`BzFeatures` 注册自定义 feature：

```java
public static final RegistryEntry<Feature<NbtFeatureConfig>> HONEYCOMB_HOLE =
    FEATURES.register("honeycomb_holes", () -> new HoneycombHole(NbtFeatureConfig.CODEC));

public static final RegistryEntry<Feature<NoneFeatureConfiguration>> HONEYCOMB_CAVES =
    FEATURES.register("honeycomb_caves", () -> new HoneycombCaves(NoneFeatureConfiguration.CODEC));
```

数据流：

1. Java 注册 `Feature<CONFIG>` 到 `Registries.FEATURE`。
2. `worldgen/configured_feature/<id>.json` 用 `"type": "<modid>:<feature_id>"` 和 config。
3. `worldgen/placed_feature/<id>.json` 引用 configured feature 并添加 placement modifiers。
4. biome JSON 的 `features` 数组引用 placed feature。

### 10.2 PlacementModifier

`BzPlacements` 注册了适合屋顶维度的 placement modifier：

- `honeycomb_hole_placer`
- `random_3d_underground_chunk_placement`
- `random_3d_cluster_placement`
- `condition_based_placement`
- `roofed_dimension_surface_placement`
- `roofed_dimension_ceiling_placement`
- `fixed_offset`
- `structure_disallow_by_tag`

自定义 placement modifier 需要：

```java
public class MyPlacement extends PlacementModifier {
    public static final MapCodec<MyPlacement> CODEC = ...;

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource random, BlockPos pos) {
        ...
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacements.MY_PLACEMENT.get();
    }
}
```

1.20.1 中 `PlacementModifierType` 通常返回 `Codec`/`MapCodec` 的 supplier，按 mappings 调整。

### 10.3 Structure

`BzStructures` 注册：

- `pollinated_stream`
- `honey_cave_room`
- `generic_optimized_structure`

`BzStructurePlacementType` 注册：

- `advanced_random_spread`

数据流：

1. Java 注册 `StructureType<T>`。
2. `worldgen/structure/<id>.json` 声明结构配置、biomes tag、step、spawn_overrides 等。
3. `worldgen/structure_set/<id>.json` 声明结构集合和 placement。
4. biome tag `tags/worldgen/biome/has_structure/<id>.json` 控制结构允许在哪些 biome 出现。

## 11. 迁移到 Forge 1.20.1 的推荐最小模板

### 11.1 注册类

```java
public final class ModWorldgen {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
        DeferredRegister.create(Registries.BIOME_SOURCE, MODID);

    public static final DeferredRegister<KeyDispatchDataCodec<? extends DensityFunction>> DENSITY_FUNCTIONS =
        DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID);

    public static final RegistryObject<Codec<MyChunkGenerator>> MY_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("chunk_generator", () -> MyChunkGenerator.CODEC);

    public static final RegistryObject<Codec<MyBiomeSource>> MY_BIOME_SOURCE =
        BIOME_SOURCES.register("biome_source", () -> MyBiomeSource.CODEC);
}
```

如果你的 1.20.1 mappings 使用 `MapCodec`，按 Bumblezone 模式改成 `MapCodec<? extends ...>`。以本地 mappings 为准。

### 11.2 自定义 BiomeSource

最小要求：

- 提供 `CODEC`。
- 覆盖 `codec()`。
- 覆盖 `collectPossibleBiomes()`。
- 覆盖 `getNoiseBiome(...)`。
- 如果实现 `BiomeManager.NoiseBiomeSource`，还要提供无 sampler 版本。

### 11.3 自定义 ChunkGenerator

推荐继承 `NoiseBasedChunkGenerator`，最小覆盖：

- `codec()`
- 必要时覆盖 `createBiomes()`，控制 biome resolver。
- 必要时覆盖 `buildSurface()`，使用自定义 `BiomeManager`。
- 必要时覆盖 `applyCarvers()` 禁用或替换 carver。
- 如果改写填充逻辑，必须同步维护 heightmap、section acquire/release、fluid postprocessing。

### 11.4 数据包清单

至少需要：

```text
data/<modid>/dimension/<dimension_id>.json
data/<modid>/dimension_type/<dimension_type_id>.json
data/<modid>/worldgen/noise_settings/<settings_id>.json
data/<modid>/worldgen/density_function/<final_density>.json
data/<modid>/worldgen/biome/<biome_id>.json
data/<modid>/tags/worldgen/biome/<tag>.json
```

如果有 feature/structure：

```text
data/<modid>/worldgen/configured_feature/<id>.json
data/<modid>/worldgen/placed_feature/<id>.json
data/<modid>/worldgen/structure/<id>.json
data/<modid>/worldgen/structure_set/<id>.json
data/<modid>/tags/worldgen/biome/has_structure/<id>.json
```

## 12. 常见坑

1. Codec 类型与版本不匹配  
   1.20.1、1.20.4、1.20.5+ 的 `Codec`/`MapCodec` 返回类型差异很容易导致注册失败。先看目标 mappings 的 `ChunkGenerator#codec()`、`BiomeSource#codec()`、`DensityFunction#codec()` 签名。

2. `dimension_type.height/min_y` 与 `noise_settings.noise.height/min_y` 不一致  
   会导致高度查询、section index、生成范围异常。

3. `possibleBiomes()` 不完整  
   结构定位、mob spawn、biome 过滤可能失效。

4. 垂直 biome 混合未处理  
   屋顶维度如果忽略 Y 采样，`createBiomes` 和 `buildSurface` 都要一致处理，否则 biome container 与 surface rule 看到的 biome 不一致。

5. 自定义 density function 使用静态状态  
   Bumblezone 的 `BiomeNoise` 用 static `sampler/biomeSource`，简单但要注意多世界和 reload。自己的实现最好评估是否能数据驱动或在服务端生命周期重置。

6. 重写 `fillFromNoise` 风险高  
   漏掉 heightmap 更新、section release、fluid postprocessing 都可能造成世界生成 bug。能用 vanilla `NoiseBasedChunkGenerator` 就不要手写整套填充。

7. Biome layer 用动态 registry int id  
   int id 来自当前 registry，可能随 datapack 变动。Bumblezone 在 server start 缓存 registry 后转换。更稳方案是 layer 内保持 key/holder 或建立自己的稳定枚举，再最后映射到 biome holder。

8. Feature 阶段数组位置错误  
   biome JSON 的 `features` 外层数组顺序必须与目标版本 `GenerationStep.Decoration` 对齐。

9. SurfaceRule 中 `minecraft:biome` 与实际 biome manager 不一致  
   如果 `BiomeSource` 忽略 Y，但 `SurfaceSystem` 使用 vanilla biome manager 的三维混合，地表材质会在垂直方向异常切换。

10. 世界种子没有进入 BiomeSource  
    Bumblezone 的 `seed` 字段默认 `0`，dimension JSON 未提供时 biome 分布固定。若你想随世界 seed 变化，需要显式设计 seed 来源。

## 13. Bumblezone 模式可复用结论

- 自定义维度不必完全重写世界生成；继承 `NoiseBasedChunkGenerator`，把复杂度放进 `NoiseGeneratorSettings`、density functions、surface rules 和 biome source，维护成本更低。
- 对屋顶/洞穴维度，固定 Y 方向 biome 采样非常重要。Bumblezone 同时处理了 `createBiomes` 和 `buildSurface`。
- `BiomeSource` 可以完全摆脱 vanilla climate parameter list，使用自己的 layer/noise 算法，但仍需正确返回 `possibleBiomes`。
- 把 biome 参数接入 density function 是一种强大的地形控制方式：不同 biome 不只是换装饰，而是改变地形高度、柱、墙、峡谷、地板水域。
- 数据包侧的 `dimension -> generator -> noise_settings -> density_function/surface_rule -> biome features` 是完整链路。排查问题时按这条链逐层验证。

## 14. 参考路径

- GitHub 源目录：`https://github.com/TelepathicGrunt/Bumblezone/tree/1.20.x-Arch/common/src/main/java/com/telepathicgrunt/the_bumblezone/worldgen`
- 关键 Java：
  - `modinit/BzDimension.java`
  - `worldgen/dimension/BzChunkGenerator.java`
  - `worldgen/dimension/BzBiomeSource.java`
  - `worldgen/dimension/BiomeInfluencedNoiseSampler.java`
  - `worldgen/dimension/NoVerticalBlendBiomeManager.java`
  - `worldgen/dimension/layer/*.java`
  - `worldgen/surfacerules/PollinatedSurfaceSource.java`
- 关键数据：
  - `data/the_bumblezone/dimension/the_bumblezone.json`
  - `data/the_bumblezone/dimension_type/the_bumblezone.json`
  - `data/the_bumblezone/worldgen/noise_settings/bz_noise_settings.json`
  - `data/the_bumblezone/worldgen/density_function/*.json`
  - `data/the_bumblezone/worldgen/biome/*.json`
  - `data/the_bumblezone/tags/worldgen/biome/*.json`
