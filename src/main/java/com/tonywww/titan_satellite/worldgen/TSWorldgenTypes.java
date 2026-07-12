package com.tonywww.titan_satellite.worldgen;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.worldgen.density.BiomeHeightDensityFunction;
import com.tonywww.titan_satellite.worldgen.feature.FissureFeature;
import com.tonywww.titan_satellite.worldgen.feature.GiantCraterFeature;
import com.tonywww.titan_satellite.worldgen.feature.IceSinkholeFeature;
import com.tonywww.titan_satellite.worldgen.feature.MegayardangFeature;
import com.tonywww.titan_satellite.worldgen.feature.MethaneLakeFeature;
import com.tonywww.titan_satellite.worldgen.feature.MethaneMareFeature;
import com.tonywww.titan_satellite.worldgen.feature.SpongeCaveFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 世界生成自定义类型注册（PA-2 冻结）：自定义 DensityFunction 与 Feature 的 codec/类型。
 * 由 {@link TSSystemsBootstrap} 在 mod 构造阶段自订阅装配到 mod 总线（不改主类）。
 *
 * <p>桩类的实际生成逻辑由 M1（PB-2 density / PB-3 features）填充；此处只冻结注册面。
 */
public final class TSWorldgenTypes {

    private TSWorldgenTypes() {
    }

    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TitanSatellite.MODID);

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, TitanSatellite.MODID);

    // 群系影响地形高度的自定义 density function（PB-2 填充实际算法）。
    public static final RegistryObject<Codec<? extends DensityFunction>> BIOME_HEIGHT =
            DENSITY_FUNCTION_TYPES.register("biome_height", () -> BiomeHeightDensityFunction.CODEC.codec());

    // 程序化地貌特征（PB-3 填充 place 逻辑）。晶簇/喷泉斑等简单特征由 PB-3 走原版特征类型。
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> METHANE_LAKE =
            FEATURES.register("methane_lake", () -> new MethaneLakeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GIANT_CRATER =
            FEATURES.register("giant_crater", () -> new GiantCraterFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MEGAYARDANG =
            FEATURES.register("megayardang", () -> new MegayardangFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ICE_SINKHOLE =
            FEATURES.register("ice_sinkhole", () -> new IceSinkholeFeature(NoneFeatureConfiguration.CODEC));

    // M6/PG-4 群系专属地物：裂隙、甲烷海、破碎海绵。
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FISSURE =
            FEATURES.register("fissure", () -> new FissureFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> METHANE_MARE =
            FEATURES.register("methane_mare", () -> new MethaneMareFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPONGE_CAVE =
            FEATURES.register("sponge_cave", () -> new SpongeCaveFeature(NoneFeatureConfiguration.CODEC));

    /** 由 {@link TSSystemsBootstrap} 在构造阶段调用，把本类的 DeferredRegister 挂到 mod 总线。 */
    public static void register(IEventBus modBus) {
        DENSITY_FUNCTION_TYPES.register(modBus);
        FEATURES.register(modBus);
    }
}
