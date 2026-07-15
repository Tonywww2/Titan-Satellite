package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.block.AcetyleneSpireBlock;
import com.tonywww.titan_satellite.block.CryovolcanicGeyserBlock;
import com.tonywww.titan_satellite.block.HydrogenBubbleMatBlock;
import com.tonywww.titan_satellite.block.HydrogenCollectorBlock;
import com.tonywww.titan_satellite.block.MethaneIceBloomBlock;
import com.tonywww.titan_satellite.block.MethanePoolCoreBlock;
import com.tonywww.titan_satellite.block.SpecialMethanePumpBlock;
import com.tonywww.titan_satellite.block.SlowingBushBlock;
import com.tonywww.titan_satellite.block.TholinComposterBlock;
import com.tonywww.titan_satellite.block.TholinCrystalBlock;
import com.tonywww.titan_satellite.block.TholinMyceliumBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

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
            DeferredRegister.create(Registries.BLOCK, TitanSatellite.MODID);

    // ---- 基础地形方块（可作维度 default_block / surface_rule）----
    // 泰坦基岩：维度基础填充方块（占位，复用深板岩贴图）。
    public static final Supplier<Block> TITAN_STONE = register("titan_stone", () ->
            new Block(props(MapColor.DEEPSLATE, 1.5F, 6.0F, SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    // 泰坦玄武岩：甲烷深渊峡谷岩壁（占位，复用黑石贴图）。
    public static final Supplier<Block> TITAN_BASALT = register("titan_basalt", () ->
            new Block(props(MapColor.COLOR_BLACK, 1.25F, 4.2F, SoundType.BASALT).requiresCorrectToolForDrops()));
    // 托林沙：托林沙海群系地表（占位，复用沙子贴图）。
    public static final Supplier<Block> THOLIN_SAND = register("tholin_sand", () ->
            new Block(props(MapColor.COLOR_ORANGE, 0.5F, 0.5F, SoundType.SAND)));
    // 碎冰：撞击陨坑荒原地表（占位）。
    public static final Supplier<Block> CRUSHED_ICE = register("crushed_ice", () ->
            new Block(props(MapColor.SNOW, 0.5F, 0.5F, SoundType.SNOW)));
    // 寒冰：冰火山断崖蓝冰变体（占位）。
    public static final Supplier<Block> CRYO_ICE = register("cryo_ice", () ->
            new Block(props(MapColor.ICE, 2.8F, 2.8F, SoundType.GLASS)));
    // 甲烷浮冰：极地迷宫冰原浮冰变体（占位）。
    public static final Supplier<Block> PACKED_METHANE_ICE = register("packed_methane_ice", () ->
            new Block(props(MapColor.COLOR_LIGHT_BLUE, 0.5F, 0.5F, SoundType.GLASS)));

    // ---- 特殊/功能方块 ----
    // 冰火山喷泉：周期喷发击飞实体。
    public static final Supplier<Block> CRYOVOLCANIC_GEYSER = register("cryovolcanic_geyser", () ->
            new CryovolcanicGeyserBlock(props(MapColor.ICE, 1.5F, 1.5F, SoundType.GLASS).lightLevel(s -> 6)));
    // 甲烷池核心：开采塔防事件触发点。
    public static final Supplier<Block> METHANE_POOL_CORE = register("methane_pool_core", () ->
            new MethanePoolCoreBlock(props(MapColor.COLOR_BLACK, 25.0F, 1200.0F, SoundType.NETHER_ORE)
                    .requiresCorrectToolForDrops().lightLevel(s -> 3)));
    // 特制甲烷泵：玩家放置以开采（带方块实体）。
    public static final Supplier<Block> SPECIAL_METHANE_PUMP = register("special_methane_pump", () ->
            new SpecialMethanePumpBlock(props(MapColor.METAL, 4.0F, 6.0F, SoundType.METAL).requiresCorrectToolForDrops()));
    // 托林晶体：晶洞发光合成材料（破坏放毒气）。
    public static final Supplier<Block> THOLIN_CRYSTAL = register("tholin_crystal", () ->
            new TholinCrystalBlock(props(MapColor.COLOR_ORANGE, 1.5F, 1.5F, SoundType.AMETHYST)
                    .requiresCorrectToolForDrops().lightLevel(s -> 10)));

    // ---- 群系特色化新增表层/装饰方块 ----
    // 风化泰坦石：荒芜高原 / 撞击陨坑荒原 表层（占位复用 titan_stone 贴图）。
    public static final Supplier<Block> WEATHERED_TITAN_STONE = register("weathered_titan_stone", () ->
            new Block(props(MapColor.TERRACOTTA_GRAY, 1.4F, 5.5F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 沉积泰坦石：液态甲烷深渊 表层（占位复用 titan_basalt 贴图）。
    public static final Supplier<Block> SEDIMENTARY_TITAN_STONE = register("sedimentary_titan_stone", () ->
            new Block(props(MapColor.TERRACOTTA_BROWN, 1.3F, 4.5F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 树枝状结晶：撞击陨坑荒原 地表装饰（十字无碰撞，占位复用原版 warped_roots 形态）。
    public static final Supplier<Block> BRANCH_CRYSTAL = register("branch_crystal", () ->
            new Block(props(MapColor.COLOR_LIGHT_BLUE, 0.0F, 0.0F, SoundType.AMETHYST_CLUSTER)
                    .noCollission().noOcclusion().lightLevel(s -> 4)));

    // ---- 装饰地物新增方块（纯色占位贴图；附加不改现有）----
    // 深渊晶体：液态甲烷深渊 暗紫发光矿物装饰。
    public static final Supplier<Block> ABYSS_CRYSTAL = register("abyss_crystal", () ->
            new Block(props(MapColor.COLOR_PURPLE, 1.5F, 1.5F, SoundType.AMETHYST).lightLevel(s -> 6)));
    // 托林焦油：液态甲烷深渊 黑色焦油洼装饰。
    public static final Supplier<Block> THOLIN_TAR = register("tholin_tar", () ->
            new Block(props(MapColor.COLOR_BLACK, 0.5F, 0.5F, SoundType.HONEY_BLOCK)));
    // 陨铁碎块：撞击陨坑荒原 金属陨石残片矿物。
    public static final Supplier<Block> METEOR_FRAGMENT = register("meteor_fragment", () ->
            new Block(props(MapColor.COLOR_GRAY, 3.0F, 6.0F, SoundType.METAL).requiresCorrectToolForDrops()));
    // 硬化托林：托林沙海 结壳/风柱材料。
    public static final Supplier<Block> HARDENED_THOLIN = register("hardened_tholin", () ->
            new Block(props(MapColor.COLOR_ORANGE, 1.2F, 1.2F, SoundType.STONE).requiresCorrectToolForDrops()));
    // 托林灌木：托林沙海 十字有机装饰（无碰撞，穿行减速）。
    public static final Supplier<Block> THOLIN_SHRUB = register("tholin_shrub", () ->
            new SlowingBushBlock(props(MapColor.COLOR_ORANGE, 0.0F, 0.0F, SoundType.GRASS).noCollission().noOcclusion()));
    // 甲烷冰花：极地迷宫冰原 十字发光冰晶装饰（无碰撞）；随机刻检测火源→连锁爆炸（见 MethaneIceBloomBlock）。
    public static final Supplier<Block> METHANE_ICE_BLOOM = register("methane_ice_bloom", () ->
            new MethaneIceBloomBlock(props(MapColor.COLOR_LIGHT_BLUE, 0.0F, 0.0F, SoundType.AMETHYST_CLUSTER)
                    .noCollission().noOcclusion().lightLevel(s -> 5).randomTicks()));
    // 氨晶体：冰火山断崖 青色发光晶体矿物。
    public static final Supplier<Block> AMMONIA_CRYSTAL = register("ammonia_crystal", () ->
            new Block(props(MapColor.COLOR_CYAN, 1.5F, 1.5F, SoundType.AMETHYST).lightLevel(s -> 7)));
    // 泰坦砾石：荒芜高原 砾石场装饰（静态，不下落）。
    public static final Supplier<Block> TITAN_GRAVEL = register("titan_gravel", () ->
            new Block(props(MapColor.STONE, 0.6F, 0.6F, SoundType.GRAVEL)));
    // 霜枯灌木：陨坑/荒原/冰火山 十字枯枝装饰（无碰撞，穿行减速）。
    public static final Supplier<Block> FROST_BUSH = register("frost_bush", () ->
            new SlowingBushBlock(props(MapColor.COLOR_LIGHT_GRAY, 0.0F, 0.0F, SoundType.GRASS).noCollission().noOcclusion()));

    // ---- 宏伟地物构成方块 ----
    // 乙炔冰笋：液态甲烷深渊「乙炔大晶洞」的高能乙炔晶柱（发光；近火连锁爆炸，见 AcetyleneSpireBlock）。
    public static final Supplier<Block> ACETYLENE_SPIRE = register("acetylene_spire", () ->
            new AcetyleneSpireBlock(props(MapColor.SAND, 1.0F, 1.0F, SoundType.AMETHYST).lightLevel(s -> 8).randomTicks()));
    // 托林菌网：巢穴/大巢的分解者有机壁（分解生物残渣重整回托林，见 TholinMyceliumBlock）。
    public static final Supplier<Block> THOLIN_MYCELIUM = register("tholin_mycelium", () ->
            new TholinMyceliumBlock(props(MapColor.COLOR_BROWN, 0.6F, 0.6F, SoundType.MOSS).randomTicks()));
    // 氢泡菌毯：荒原低洼化能菌毯（缓释 H₂ 气泡，浮游体/蹒兽食源；近火轻微轰燃，见 HydrogenBubbleMatBlock）。
    public static final Supplier<Block> HYDROGEN_BUBBLE_MAT = register("hydrogen_bubble_mat", () ->
            new HydrogenBubbleMatBlock(props(MapColor.COLOR_GREEN, 0.4F, 0.4F, SoundType.MOSS_CARPET).randomTicks()));

    // ---- 材料加工链被动生产方块（带方块实体）----
    // 集氢罩：架在氢泡菌毯上被动产氢（见 HydrogenCollectorBlock）。不要求正确工具，确保掉落。
    public static final Supplier<Block> HYDROGEN_COLLECTOR = register("hydrogen_collector", () ->
            new HydrogenCollectorBlock(props(MapColor.METAL, 2.0F, 3.0F, SoundType.METAL)));
    // 托林堆肥槽：架在托林菌网上，吞生物残渣产托林粉末（见 TholinComposterBlock）。
    public static final Supplier<Block> THOLIN_COMPOSTER = register("tholin_composter", () ->
            new TholinComposterBlock(props(MapColor.COLOR_BROWN, 1.2F, 2.0F, SoundType.WOOD)));

    // ---- 流体方块（无 BlockItem，用桶交互；可作维度 default_fluid）----
    //? if forge {
    public static final Supplier<LiquidBlock> LIQUID_METHANE_BLOCK = REGISTER.register("liquid_methane", () ->
            new LiquidBlock(TSFluids.LIQUID_METHANE, liquidProps(MapColor.COLOR_BROWN)));
    public static final Supplier<LiquidBlock> LIQUID_AMMONIA_BLOCK = REGISTER.register("liquid_ammonia", () ->
            new LiquidBlock(TSFluids.LIQUID_AMMONIA, liquidProps(MapColor.COLOR_LIGHT_BLUE)));
    public static final Supplier<LiquidBlock> LIQUID_HYDROGEN_BLOCK = REGISTER.register("liquid_hydrogen", () ->
            new LiquidBlock(TSFluids.LIQUID_HYDROGEN, liquidProps(MapColor.COLOR_LIGHT_BLUE)));
    //?} else {
    /*public static final Supplier<LiquidBlock> LIQUID_METHANE_BLOCK = REGISTER.register("liquid_methane", () ->
            new LiquidBlock(TSFluids.LIQUID_METHANE.get(), liquidProps(MapColor.COLOR_BROWN)));
    public static final Supplier<LiquidBlock> LIQUID_AMMONIA_BLOCK = REGISTER.register("liquid_ammonia", () ->
            new LiquidBlock(TSFluids.LIQUID_AMMONIA.get(), liquidProps(MapColor.COLOR_LIGHT_BLUE)));
    public static final Supplier<LiquidBlock> LIQUID_HYDROGEN_BLOCK = REGISTER.register("liquid_hydrogen", () ->
            new LiquidBlock(TSFluids.LIQUID_HYDROGEN.get(), liquidProps(MapColor.COLOR_LIGHT_BLUE)));
    *///?}

    private static BlockBehaviour.Properties props(MapColor color, float destroyTime, float resistance, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(destroyTime, resistance).sound(sound);
    }

    private static BlockBehaviour.Properties liquidProps(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().strength(100.0F).noLootTable();
    }

    private static Supplier<Block> register(String name, Supplier<Block> supplier) {
        return REGISTER.register(name, supplier);
    }
}
