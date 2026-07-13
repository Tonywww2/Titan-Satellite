package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.block.CryovolcanicGeyserBlock;
import com.tonywww.titan_satellite.block.MethanePoolCoreBlock;
import com.tonywww.titan_satellite.block.SpecialMethanePumpBlock;
import com.tonywww.titan_satellite.block.TholinCrystalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * 方块注册表。固体方块对应的 BlockItem 在 {@link TSItems} 注册；
 * 流体方块（LiquidBlock）不注册 BlockItem，改用桶交互。
 *
 * <p>基础地形方块可用于维度 worldgen 的 default_block / surface_rule，
 * 流体方块可用于 default_fluid —— 见 data/titan_satellite/worldgen/noise_settings/titan.json。
 */
public final class TSBlocks {

    private TSBlocks() {
    }

    public static final DeferredRegister<Block> REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TitanSatellite.MODID);

    // ---- 基础地形方块（可作维度 default_block / surface_rule）----
    // 泰坦基岩：维度基础填充方块（占位，复用深板岩贴图）。
    public static final RegistryObject<Block> TITAN_STONE = register("titan_stone", () ->
            new Block(props(MapColor.DEEPSLATE, 1.5F, 6.0F, SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    // 泰坦玄武岩：甲烷深渊峡谷岩壁（占位，复用黑石贴图）。
    public static final RegistryObject<Block> TITAN_BASALT = register("titan_basalt", () ->
            new Block(props(MapColor.COLOR_BLACK, 1.25F, 4.2F, SoundType.BASALT).requiresCorrectToolForDrops()));
    // 托林沙：托林沙海群系地表（占位，复用沙子贴图）。
    public static final RegistryObject<Block> THOLIN_SAND = register("tholin_sand", () ->
            new Block(props(MapColor.COLOR_ORANGE, 0.5F, 0.5F, SoundType.SAND)));
    // 碎冰：撞击陨坑荒原地表（占位）。
    public static final RegistryObject<Block> CRUSHED_ICE = register("crushed_ice", () ->
            new Block(props(MapColor.SNOW, 0.5F, 0.5F, SoundType.SNOW)));
    // 寒冰：冰火山断崖蓝冰变体（占位）。
    public static final RegistryObject<Block> CRYO_ICE = register("cryo_ice", () ->
            new Block(props(MapColor.ICE, 2.8F, 2.8F, SoundType.GLASS)));
    // 甲烷浮冰：极地迷宫冰原浮冰变体（占位）。
    public static final RegistryObject<Block> PACKED_METHANE_ICE = register("packed_methane_ice", () ->
            new Block(props(MapColor.COLOR_LIGHT_BLUE, 0.5F, 0.5F, SoundType.GLASS)));

    // ---- 特殊/功能方块（占位，行为逻辑后续里程碑实现）----
    // 冰火山喷泉：周期喷发击飞实体（行为由 M4/PE-1 填充）。
    public static final RegistryObject<Block> CRYOVOLCANIC_GEYSER = register("cryovolcanic_geyser", () ->
            new CryovolcanicGeyserBlock(props(MapColor.ICE, 1.5F, 1.5F, SoundType.GLASS).lightLevel(s -> 6)));
    // 甲烷池核心：开采塔防事件触发点（行为由 M4/PE-2 填充）。
    public static final RegistryObject<Block> METHANE_POOL_CORE = register("methane_pool_core", () ->
            new MethanePoolCoreBlock(props(MapColor.COLOR_BLACK, 25.0F, 1200.0F, SoundType.NETHER_ORE)
                    .requiresCorrectToolForDrops().lightLevel(s -> 3)));
    // 特制甲烷泵：玩家放置以开采（带方块实体，行为由 M4/PE-2 填充）。
    public static final RegistryObject<Block> SPECIAL_METHANE_PUMP = register("special_methane_pump", () ->
            new SpecialMethanePumpBlock(props(MapColor.METAL, 4.0F, 6.0F, SoundType.METAL).requiresCorrectToolForDrops()));
    // 托林晶体：晶洞发光合成材料（破坏放毒气，行为由 M4/PE-3 填充）。
    public static final RegistryObject<Block> THOLIN_CRYSTAL = register("tholin_crystal", () ->
            new TholinCrystalBlock(props(MapColor.COLOR_ORANGE, 1.5F, 1.5F, SoundType.AMETHYST)
                    .requiresCorrectToolForDrops().lightLevel(s -> 10)));

    // ---- M6 群系特色化新增表层/装饰方块（PG-1）----
    // 风化泰坦石：荒芜高原 / 撞击陨坑荒原 表层（占位复用 titan_stone 贴图）。
    public static final RegistryObject<Block> WEATHERED_TITAN_STONE = register("weathered_titan_stone", () ->
            new Block(props(MapColor.TERRACOTTA_GRAY, 1.4F, 5.5F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 沉积泰坦石：液态甲烷深渊 表层（占位复用 titan_basalt 贴图）。
    public static final RegistryObject<Block> SEDIMENTARY_TITAN_STONE = register("sedimentary_titan_stone", () ->
            new Block(props(MapColor.TERRACOTTA_BROWN, 1.3F, 4.5F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 树枝状结晶：撞击陨坑荒原 地表装饰（十字无碰撞，占位复用原版 warped_roots 形态）。
    public static final RegistryObject<Block> BRANCH_CRYSTAL = register("branch_crystal", () ->
            new Block(props(MapColor.COLOR_LIGHT_BLUE, 0.0F, 0.0F, SoundType.AMETHYST_CLUSTER)
                    .noCollission().noOcclusion().lightLevel(s -> 4)));

    // ---- 装饰地物新增方块（CR-15，纯色占位贴图；附加不改现有）----
    // 深渊晶体：液态甲烷深渊 暗紫发光矿物装饰。
    public static final RegistryObject<Block> ABYSS_CRYSTAL = register("abyss_crystal", () ->
            new Block(props(MapColor.COLOR_PURPLE, 1.5F, 1.5F, SoundType.AMETHYST).lightLevel(s -> 6)));
    // 托林焦油：液态甲烷深渊 黑色焦油洼装饰。
    public static final RegistryObject<Block> THOLIN_TAR = register("tholin_tar", () ->
            new Block(props(MapColor.COLOR_BLACK, 0.5F, 0.5F, SoundType.HONEY_BLOCK)));
    // 陨铁碎块：撞击陨坑荒原 金属陨石残片矿物。
    public static final RegistryObject<Block> METEOR_FRAGMENT = register("meteor_fragment", () ->
            new Block(props(MapColor.COLOR_GRAY, 3.0F, 6.0F, SoundType.METAL).requiresCorrectToolForDrops()));
    // 硬化托林：托林沙海 结壳/风柱材料。
    public static final RegistryObject<Block> HARDENED_THOLIN = register("hardened_tholin", () ->
            new Block(props(MapColor.COLOR_ORANGE, 1.2F, 1.2F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 托林灌木：托林沙海 十字有机装饰（无碰撞）。
    public static final RegistryObject<Block> THOLIN_SHRUB = register("tholin_shrub", () ->
            new Block(props(MapColor.COLOR_ORANGE, 0.0F, 0.0F, SoundType.GRASS).noCollission().noOcclusion()));
    // 甲烷冰花：极地迷宫冰原 十字发光冰晶装饰（无碰撞）。
    public static final RegistryObject<Block> METHANE_ICE_BLOOM = register("methane_ice_bloom", () ->
            new Block(props(MapColor.COLOR_LIGHT_BLUE, 0.0F, 0.0F, SoundType.AMETHYST_CLUSTER)
                    .noCollission().noOcclusion().lightLevel(s -> 5)));
    // 氨晶体：冰火山断崖 青色发光晶体矿物。
    public static final RegistryObject<Block> AMMONIA_CRYSTAL = register("ammonia_crystal", () ->
            new Block(props(MapColor.COLOR_CYAN, 1.5F, 1.5F, SoundType.AMETHYST).lightLevel(s -> 7)));
    // 泰坦砾石：荒芜高原 砾石场装饰（静态，不下落）。
    public static final RegistryObject<Block> TITAN_GRAVEL = register("titan_gravel", () ->
            new Block(props(MapColor.STONE, 0.6F, 0.6F, SoundType.GRAVEL)));
    // 霜枯灌木：陨坑/荒原/冰火山 十字枯枝装饰（无碰撞）。
    public static final RegistryObject<Block> FROST_BUSH = register("frost_bush", () ->
            new Block(props(MapColor.COLOR_LIGHT_GRAY, 0.0F, 0.0F, SoundType.GRASS).noCollission().noOcclusion()));

    // ---- 流体方块（无 BlockItem，用桶交互；可作维度 default_fluid）----
    public static final RegistryObject<LiquidBlock> LIQUID_METHANE_BLOCK = REGISTER.register("liquid_methane", () ->
            new LiquidBlock(TSFluids.LIQUID_METHANE, liquidProps(MapColor.COLOR_BROWN)));
    public static final RegistryObject<LiquidBlock> LIQUID_AMMONIA_BLOCK = REGISTER.register("liquid_ammonia", () ->
            new LiquidBlock(TSFluids.LIQUID_AMMONIA, liquidProps(MapColor.COLOR_LIGHT_BLUE)));

    private static BlockBehaviour.Properties props(MapColor color, float destroyTime, float resistance, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(destroyTime, resistance).sound(sound);
    }

    private static BlockBehaviour.Properties liquidProps(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().strength(100.0F).noLootTable();
    }

    private static RegistryObject<Block> register(String name, Supplier<Block> supplier) {
        return REGISTER.register(name, supplier);
    }
}
