package com.tonywww.titan_satellite.worldgen.structure;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 土卫六结构块（PF-1）：按 {@link TitanStructure.Variant} 程序化建造托林晶洞 / 先驱前哨站。
 * 所有放置都用 {@link BoundingBox#isInside(net.minecraft.core.Vec3i)} 门控，故跨区块生成时
 * 每个区块只落自己那部分（{@code postProcess} 逐区块调用），整座结构正确拼合。
 */
public class TitanStructurePiece extends StructurePiece {

    private static final ResourceLocation GEODE_LOOT = new ResourceLocation(TitanSatellite.MODID, "chests/tholin_geode");
    private static final ResourceLocation OUTPOST_LOOT = new ResourceLocation(TitanSatellite.MODID, "chests/pioneer_outpost");

    private final TitanStructure.Variant variant;
    private final BlockPos origin;

    public TitanStructurePiece(BlockPos origin, TitanStructure.Variant variant) {
        super(TSStructures.TITAN_PIECE.get(), 0, makeBox(origin, variant));
        this.origin = origin;
        this.variant = variant;
    }

    public TitanStructurePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(TSStructures.TITAN_PIECE.get(), tag);
        this.variant = TitanStructure.Variant.valueOf(tag.getString("Variant"));
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
    }

    private static BoundingBox makeBox(BlockPos o, TitanStructure.Variant v) {
        if (v == TitanStructure.Variant.GEODE) {
            int r = 6;
            return new BoundingBox(o.getX() - r, o.getY() - r, o.getZ() - r, o.getX() + r, o.getY() + r, o.getZ() + r);
        }
        return new BoundingBox(o.getX() - 4, o.getY() - 2, o.getZ() - 4, o.getX() + 4, o.getY() + 5, o.getZ() + 4);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putString("Variant", variant.name());
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        if (variant == TitanStructure.Variant.GEODE) {
            buildGeode(level, box, random);
        } else {
            buildOutpost(level, box, random);
        }
    }

    // ---- 托林晶洞：地下中空晶球 ----
    private void buildGeode(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int r = 5;
        BlockState shell = TSBlocks.CRYO_ICE.get().defaultBlockState();
        BlockState crystal = TSBlocks.THOLIN_CRYSTAL.get().defaultBlockState();
        BlockState hollow = Blocks.CAVE_AIR.defaultBlockState();
        BlockState floor = TSBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > r + 0.5) {
                        continue;
                    }
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (dist > r - 0.7) {
                        put(level, shell, m, box);
                    } else if (dist > r - 1.7) {
                        put(level, crystal, m, box);
                    } else {
                        put(level, hollow, m, box);
                    }
                }
            }
        }

        int floorY = origin.getY() - (r - 2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 6) {
                    put(level, floor, m.set(origin.getX() + dx, floorY, origin.getZ() + dz), box);
                }
            }
        }
        placeChest(level, new BlockPos(origin.getX(), floorY + 1, origin.getZ()), box, random, GEODE_LOOT);
    }

    // ---- 先驱前哨站：地表废弃小屋 ----
    private void buildOutpost(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int half = 3;
        BlockState wall = TSBlocks.TITAN_STONE.get().defaultBlockState();
        BlockState base = TSBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fy = origin.getY();

        // 地板 + 浅地基
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, base, m.set(origin.getX() + dx, fy, origin.getZ() + dz), box);
                put(level, base, m.set(origin.getX() + dx, fy - 1, origin.getZ() + dz), box);
            }
        }
        // 清空内部
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -half + 1; dx <= half - 1; dx++) {
                for (int dz = -half + 1; dz <= half - 1; dz++) {
                    put(level, air, m.set(origin.getX() + dx, fy + wy, origin.getZ() + dz), box);
                }
            }
        }
        // 墙体（南侧留门洞：dz=+half, dx=0, wy 1..2）
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    if (Math.abs(dx) != half && Math.abs(dz) != half) {
                        continue;
                    }
                    if (dz == half && dx == 0 && wy <= 2) {
                        continue;
                    }
                    put(level, wall, m.set(origin.getX() + dx, fy + wy, origin.getZ() + dz), box);
                }
            }
        }
        // 屋顶
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, wall, m.set(origin.getX() + dx, fy + 4, origin.getZ() + dz), box);
            }
        }
        placeChest(level, new BlockPos(origin.getX() - 1, fy + 1, origin.getZ() - 1), box, random, OUTPOST_LOOT);
        spawnProbe(level, new BlockPos(origin.getX() + 1, fy + 1, origin.getZ() + 1), box, random);
    }

    private void put(WorldGenLevel level, BlockState state, BlockPos pos, BoundingBox box) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void placeChest(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random, ResourceLocation loot) {
        if (!box.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(loot, random.nextLong());
        }
    }

    private void spawnProbe(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random) {
        if (!box.isInside(pos)) {
            return;
        }
        Mob probe = TSEntities.CORRUPTED_PROBE.get().create(level.getLevel());
        if (probe != null) {
            probe.setPersistenceRequired();
            probe.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            probe.finalizeSpawn(level, level.getLevel().getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntity(probe);
        }
    }
}
