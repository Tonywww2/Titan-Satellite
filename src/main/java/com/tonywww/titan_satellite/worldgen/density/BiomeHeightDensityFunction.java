package com.tonywww.titan_satellite.worldgen.density;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * 群系影响地形高度的自定义 density function（PA-2 冻结签名，桩实现）。
 * 骨架阶段 {@link #compute} 返回 0；M1（PB-2）填充：按当前 X/Z 所在群系的
 * 期望厚度/高差修正密度，实现峡谷/沙丘/断崖的极端高差。
 *
 * <p>{@code factor} 为群系高度影响强度的数据驱动参数（noise_router/density_function JSON 提供）。
 */
public record BiomeHeightDensityFunction(double factor) implements DensityFunction.SimpleFunction {

    public static final KeyDispatchDataCodec<BiomeHeightDensityFunction> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.DOUBLE.fieldOf("factor")
                            .forGetter(BiomeHeightDensityFunction::factor)
            ).apply(instance, BiomeHeightDensityFunction::new)));

    @Override
    public double compute(FunctionContext context) {
        // 不读群系（SimpleFunction 无 seed/biomeSource）：按 X/Z 多倍频平滑值噪声 (fBm) 产出
        // 连续起伏的正常地形（无台地量化/无锐利断层/无高频走样）；输出乘 factor 作高度修正量。
        double x = context.blockX();
        double z = context.blockZ();

        // 轻度域扭曲：低频低幅，仅让等高线自然弯曲，不产生台地/断层
        double warpX = valueNoise(x * 0.006D, z * 0.006D) * 8.0D;
        double warpZ = valueNoise((x + 4096.0D) * 0.006D, (z - 4096.0D) * 0.006D) * 8.0D;
        double wx = x + warpX;
        double wz = z + warpZ;

        // 多倍频平滑值噪声 (fBm)：连续起伏的正常地形，各频率波长均 >> 4 格噪声单元，无走样
        double relief =
                  valueNoise(wx * 0.0035D, wz * 0.0035D) * 1.00D   // 大尺度起伏（~285 格）
                + valueNoise(wx * 0.0090D, wz * 0.0090D) * 0.50D   // 中尺度丘陵（~110 格）
                + valueNoise(x * 0.0200D, z * 0.0200D) * 0.25D     // 小尺度细节（~50 格）
                + valueNoise(x * 0.0450D, z * 0.0450D) * 0.12D;    // 精细起伏（~22 格）
        relief /= 1.87D;                                          // 归一化到约 [-1,1]

        return Mth.clamp(relief * factor, -Math.abs(factor), Math.abs(factor));
    }

    @Override
    public double minValue() {
        return -Math.abs(factor);
    }

    @Override
    public double maxValue() {
        return Math.abs(factor);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    // 确定性 2D 值噪声：整点哈希 + 平滑插值，输出约 [-1, 1]。
    private static double valueNoise(double x, double z) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        double sx = fx * fx * (3.0D - 2.0D * fx);
        double sz = fz * fz * (3.0D - 2.0D * fz);
        double nx0 = Mth.lerp(sx, hash(x0, z0), hash(x0 + 1, z0));
        double nx1 = Mth.lerp(sx, hash(x0, z0 + 1), hash(x0 + 1, z0 + 1));
        return Mth.lerp(sz, nx0, nx1);
    }

    private static double hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        h = h ^ (h >> 16);
        return (h & 0xFFFF) / 32767.5D - 1.0D;
    }
}
