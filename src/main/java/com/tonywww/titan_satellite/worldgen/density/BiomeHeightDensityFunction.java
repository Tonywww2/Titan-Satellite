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
        // 不读群系（SimpleFunction 无 seed/biomeSource）：按 X/Z 多尺度噪声 + 台地量化 +
        // 陡边过渡 + 高频破碎，产出台地/陡崖/断层/孤峰的破碎地貌；输出乘 factor 作高度修正量。
        double x = context.blockX();
        double z = context.blockZ();

        // 域扭曲：低+中频噪声偏移采样坐标，打破规整的同心台地/等高线轮廓，使台地与断层扭曲不规则
        double warpX = valueNoise(x * 0.013D, z * 0.013D) * 16.0D + valueNoise(x * 0.047D, z * 0.047D) * 6.0D;
        double warpZ = valueNoise((x + 4096.0D) * 0.013D, (z - 4096.0D) * 0.013D) * 16.0D
                + valueNoise((x - 4096.0D) * 0.047D, (z + 4096.0D) * 0.047D) * 6.0D;
        double wx = x + warpX;
        double wz = z + warpZ;

        double base = valueNoise(wx * 0.0030D, wz * 0.0030D);      // 大尺度：台地/盆地大区（已扭曲）
        double mid = valueNoise(wx * 0.0110D, wz * 0.0110D);       // 中尺度：丘陵/沙脊（已扭曲）
        double detail = valueNoise(x * 0.0330D, z * 0.0330D);      // 细节起伏
        double fracture = valueNoise(x * 0.0700D, z * 0.0700D);    // 中高频：少量断层/破碎（适度保留）

        // 台地化弱化 + 更缓的边缘，避免 4x4 格锄齿；台阶输入叠加中频抖动
        double terraced = terrace(base + mid * 0.12D, 4.0D, 0.35D);
        double plateau = Mth.lerp(0.28D, base, terraced);

        // 以平滑的 base/mid/detail 为主，仅保留少量 fracture 作粗糙感（移除更高频 weather，消除亚格锄齿）
        double relief =
                  plateau * 0.48D                                 // 平缓台地大区（已扭曲）
                + mid * 0.28D                                     // 中尺度丘陵/沙脊
                + detail * 0.20D                                  // 细节起伏
                + fracture * 0.04D;                               // 少量破碎（适度保留）

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

    // 台地量化：把 v∈[-1,1] 量化为 levels 级平台，edge∈[0,1] 控制平台间过渡陡度（越大越像陡崖）。
    private static double terrace(double v, double levels, double edge) {
        double scaled = (v * 0.5D + 0.5D) * levels;
        double idx = Math.floor(scaled);
        double frac = scaled - idx;
        double lo = 0.5D - (1.0D - edge) * 0.5D;
        double hi = 0.5D + (1.0D - edge) * 0.5D;
        double sharp;
        if (frac <= lo) {
            sharp = 0.0D;
        } else if (frac >= hi) {
            sharp = 1.0D;
        } else {
            double t = (frac - lo) / (hi - lo);
            sharp = t * t * (3.0D - 2.0D * t);
        }
        return ((idx + sharp) / levels) * 2.0D - 1.0D;
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
