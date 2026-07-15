package com.tonywww.titan_moon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/**
 * 甲烷冰花（Methane Ice Bloom）：高危易燃的碳基花簇。
 * 通过随机刻检测周围是否存在<b>着火的实体</b>或<b>火焰标签方块</b>；命中则瞬间引爆，
 * 并把相邻的甲烷冰花延迟点燃，形成连锁化学爆炸。
 */
@SuppressWarnings("deprecation")
public class MethaneIceBloomBlock extends Block {

    /** 单株爆炸当量（小于 TNT 的 4.0）。 */
    private static final float EXPLOSION_POWER = 1.6F;

    public MethaneIceBloomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 随机刻：检测邻近火源（火焰方块或着火实体）。 */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (hasFireNeighbor(level, pos) || hasBurningEntityNearby(level, pos)) {
            detonate(level, pos);
        }
    }

    /** 相邻方块更新（如放置/点燃火焰）时即时检测，反应更灵敏。 */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (level instanceof ServerLevel server && hasFireNeighbor(server, pos)) {
            detonate(server, pos);
        }
    }

    /** 计划刻：被相邻冰花引燃时的连锁引爆入口。 */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        detonate(level, pos);
    }

    private static boolean hasFireNeighbor(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.is(BlockTags.FIRE) || neighbor.getBlock() instanceof BaseFireBlock) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBurningEntityNearby(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(1.5D);
        return !level.getEntitiesOfClass(Entity.class, box, Entity::isOnFire).isEmpty();
    }

    private void detonate(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(this)) {
            return; // 已被移除/被前一次爆炸波及，避免重复引爆
        }
        // 连锁：相邻甲烷冰花延迟引爆（错峰，避免同刻递归）。
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockState(neighborPos).is(this) && !level.getBlockTicks().hasScheduledTick(neighborPos, this)) {
                level.scheduleTick(neighborPos, this, 2 + level.random.nextInt(3));
            }
        }
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
    }
}
