package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * 维度相关的 ResourceKey 常量。数据驱动的维度定义见
 * {@code data/titan_satellite/dimension/titan.json}；这里的 key 供后续
 * 判定"是否在土卫六维度"与传送逻辑使用。
 */
public final class TSDimensions {

    private TSDimensions() {
    }

    public static final ResourceKey<Level> TITAN_LEVEL =
            ResourceKey.create(Registries.DIMENSION, TitanSatellite.rl("titan"));

    public static final ResourceKey<DimensionType> TITAN_DIM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, TitanSatellite.rl("titan"));
}
