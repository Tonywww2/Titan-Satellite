package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 破碎海绵特征（PG-4）：极地迷宫冰原地表下数格用 3D 值噪声挖出密布空洞的多孔冰体（破碎海绵）。
 *
 * <p>仅在噪声高值处挖空（保留原有冰壁），配合边缘衰减使中心多孔、边缘保留壁，形成海绵状孔隙。
 */
public class SpongeCaveFeature extends Feature<NoneFeatureConfiguration> {

    /** 极地迷宫冰原群系键——破碎海绵仅在此群系“内部”生成。 */
    private static final ResourceKey<Biome> POLAR_LABYRINTH =
            ResourceKey.create(Registries.BIOME, new ResourceLocation(TitanSatellite.MODID, "polar_labyrinth"));
    /** 洞体半径之外再向外要求的实心边距（格）：中心与该半径环上须全为极地群系，否则跳过。
     *  取值偏大以抵消群系采样按 4 格量化的误差、并为过渡断崖留足实心冰壁。 */
    private static final int EDGE_BUFFER = 6;
    /** 8 个归一化水平方向（含对角），用于环形采样群系边界。 */
    private static final double[][] DIRS8 = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {0.7071D, 0.7071D}, {0.7071D, -0.7071D}, {-0.7071D, 0.7071D}, {-0.7071D, -0.7071D}
    };

    public SpongeCaveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 5 + random.nextInt(4);            // 半径 5-8
        int height = 8 + random.nextInt(8);            // 高度 8-15
        int topOffset = 2 + random.nextInt(2);         // 地表下 2-3 格起
        double threshold = 0.12D;                      // 孔隙阈值（越低孔越多）

        // 群系边缘剔除：因极端高差，群系过渡处是陡峭断崖，海绵孔洞极易被切穿暴露。
        // 要求洞体中心及其“半径 + 边距”环上一圈全部位于极地迷宫冰原内部，否则放弃本次生成。
        if (!isInsidePolar(level, origin, radius + EDGE_BUFFER)) {
            return false;
        }

        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dy = -topOffset; dy > -topOffset - height; dy--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                    if (horiz > radius) {
                        continue;
                    }
                    int wx = origin.getX() + dx;
                    int wy = origin.getY() + dy;
                    int wz = origin.getZ() + dz;
                    // 3D 噪声 + 边缘衰减：中心多孔、边缘保留冰壁
                    double n = noise3d(wx * 0.18D, wy * 0.18D, wz * 0.18D);
                    double falloff = 1.0D - horiz / radius;
                    if (n * falloff > threshold) {
                        pos.set(wx, wy, wz);
                        if (!level.getBlockState(pos).isAir()) {
                            setBlock(level, pos, air);
                            placed = true;
                        }
                    }
                }
            }
        }
        return placed;
    }

    /** 中心与“半径 r”环上 8 个方向是否全部为极地迷宫冰原（即远离群系过渡断崖）。 */
    private static boolean isInsidePolar(WorldGenLevel level, BlockPos origin, int r) {
        if (!isPolar(level, origin.getX(), origin.getY(), origin.getZ())) {
            return false;
        }
        for (double[] dir : DIRS8) {
            int sx = origin.getX() + (int) Math.round(dir[0] * r);
            int sz = origin.getZ() + (int) Math.round(dir[1] * r);
            if (!isPolar(level, sx, origin.getY(), sz)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPolar(WorldGenLevel level, int x, int y, int z) {
        return level.getBiome(new BlockPos(x, y, z)).is(POLAR_LABYRINTH);
    }

    // 确定性 3D 值噪声：整点哈希 + 三线性平滑插值，输出约 [0, 1]。
    private static double noise3d(double x, double y, double z) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        double fx = smooth(x - x0);
        double fy = smooth(y - y0);
        double fz = smooth(z - z0);
        double c000 = hash(x0, y0, z0);
        double c100 = hash(x0 + 1, y0, z0);
        double c010 = hash(x0, y0 + 1, z0);
        double c110 = hash(x0 + 1, y0 + 1, z0);
        double c001 = hash(x0, y0, z0 + 1);
        double c101 = hash(x0 + 1, y0, z0 + 1);
        double c011 = hash(x0, y0 + 1, z0 + 1);
        double c111 = hash(x0 + 1, y0 + 1, z0 + 1);
        double x00 = Mth.lerp(fx, c000, c100);
        double x10 = Mth.lerp(fx, c010, c110);
        double x01 = Mth.lerp(fx, c001, c101);
        double x11 = Mth.lerp(fx, c011, c111);
        double y0v = Mth.lerp(fy, x00, x10);
        double y1v = Mth.lerp(fy, x01, x11);
        return Mth.lerp(fz, y0v, y1v);
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static double hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >> 13)) * 1274126177;
        h = h ^ (h >> 16);
        return (h & 0xFFFF) / 65535.0D;
    }
}
