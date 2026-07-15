package com.tonywww.titan_satellite.worldgen;

//? if forge {
import com.mojang.serialization.Codec;
//?} else {
/*import com.mojang.serialization.MapCodec;
*///?}
import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.worldgen.density.BiomeHeightDensityFunction;
import com.tonywww.titan_satellite.worldgen.feature.CryovolcanoFeature;
import com.tonywww.titan_satellite.worldgen.feature.FissureFeature;
import com.tonywww.titan_satellite.worldgen.feature.FrozenAmmoniaFallsFeature;
import com.tonywww.titan_satellite.worldgen.feature.GiantCraterFeature;
import com.tonywww.titan_satellite.worldgen.feature.HoodooLabyrinthFeature;
import com.tonywww.titan_satellite.worldgen.feature.IceCathedralFeature;
import com.tonywww.titan_satellite.worldgen.feature.IceSinkholeFeature;
import com.tonywww.titan_satellite.worldgen.feature.ImpactBasinFeature;
import com.tonywww.titan_satellite.worldgen.feature.MegayardangFeature;
import com.tonywww.titan_satellite.worldgen.feature.MethaneCascadeFeature;
import com.tonywww.titan_satellite.worldgen.feature.MethaneLakeFeature;
import com.tonywww.titan_satellite.worldgen.feature.MethaneMareFeature;
import com.tonywww.titan_satellite.worldgen.feature.SilkShroudFeature;
import com.tonywww.titan_satellite.worldgen.feature.SpireFeature;
import com.tonywww.titan_satellite.worldgen.feature.SpongeCaveFeature;
import com.tonywww.titan_satellite.worldgen.feature.TholinArchFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

/**
 * 世界生成自定义类型注册：自定义 DensityFunction 与 Feature 的 codec/类型。
 * 由 {@link TSSystemsBootstrap} 在 mod 构造阶段自订阅装配到 mod 总线（不改主类）。
 *
 * <p>此处只登记 codec/类型，实际生成逻辑在各 Feature / DensityFunction 类中。
 */
public final class TSWorldgenTypes {

    private TSWorldgenTypes() {
    }

    //? if forge {
    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TitanSatellite.MODID);
    //?} else {
    /*public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TitanSatellite.MODID);
    *///?}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TitanSatellite.MODID);

    // 群系影响地形高度的自定义 density function。
    //? if forge {
    public static final Supplier<Codec<? extends DensityFunction>> BIOME_HEIGHT =
            DENSITY_FUNCTION_TYPES.register("biome_height", () -> BiomeHeightDensityFunction.CODEC.codec());
    //?} else {
    /*public static final Supplier<MapCodec<? extends DensityFunction>> BIOME_HEIGHT =
            DENSITY_FUNCTION_TYPES.register("biome_height", () -> BiomeHeightDensityFunction.CODEC.codec());
    *///?}

    // 程序化地貌特征。晶簇/喷泉斑等简单特征走原版特征类型。
    public static final Supplier<Feature<NoneFeatureConfiguration>> METHANE_LAKE =
            FEATURES.register("methane_lake", () -> new MethaneLakeFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> GIANT_CRATER =
            FEATURES.register("giant_crater", () -> new GiantCraterFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> MEGAYARDANG =
            FEATURES.register("megayardang", () -> new MegayardangFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> ICE_SINKHOLE =
            FEATURES.register("ice_sinkhole", () -> new IceSinkholeFeature(NoneFeatureConfiguration.CODEC));

    // 群系专属地物：裂隙、甲烷海、破碎海绵。
    public static final Supplier<Feature<NoneFeatureConfiguration>> FISSURE =
            FEATURES.register("fissure", () -> new FissureFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> METHANE_MARE =
            FEATURES.register("methane_mare", () -> new MethaneMareFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> SPONGE_CAVE =
            FEATURES.register("sponge_cave", () -> new SpongeCaveFeature(NoneFeatureConfiguration.CODEC));

    // 装饰地物：通用尖塔/石林，BlockStateConfiguration 指定方块，多群系复用。
    public static final Supplier<Feature<BlockStateConfiguration>> SPIRE =
            FEATURES.register("spire", () -> new SpireFeature(BlockStateConfiguration.CODEC));

    // ---- 宏伟地物 Feature（乙炔大晶洞/陨坑镜湖群走数据版 geode/lake，无 Java）----
    public static final Supplier<Feature<NoneFeatureConfiguration>> IMPACT_BASIN =
            FEATURES.register("impact_basin", () -> new ImpactBasinFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> METHANE_CASCADE =
            FEATURES.register("methane_cascade", () -> new MethaneCascadeFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> THOLIN_ARCH =
            FEATURES.register("tholin_arch", () -> new TholinArchFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> SILK_SHROUD =
            FEATURES.register("silk_shroud", () -> new SilkShroudFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> ICE_CATHEDRAL =
            FEATURES.register("ice_cathedral", () -> new IceCathedralFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> CRYOVOLCANO =
            FEATURES.register("cryovolcano", () -> new CryovolcanoFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> FROZEN_AMMONIA_FALLS =
            FEATURES.register("frozen_ammonia_falls", () -> new FrozenAmmoniaFallsFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<Feature<NoneFeatureConfiguration>> HOODOO_LABYRINTH =
            FEATURES.register("hoodoo_labyrinth", () -> new HoodooLabyrinthFeature(NoneFeatureConfiguration.CODEC));

    /** 由 {@link TSSystemsBootstrap} 在构造阶段调用，把本类的 DeferredRegister 挂到 mod 总线。 */
    public static void register(IEventBus modBus) {
        DENSITY_FUNCTION_TYPES.register(modBus);
        FEATURES.register(modBus);
    }
}
