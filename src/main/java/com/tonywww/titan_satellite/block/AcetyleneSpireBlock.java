package com.tonywww.titan_satellite.block;

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
 * 乙炔冰笋（Acetylene Ice Spire）：液态甲烷深渊「乙炔大晶洞」的高能乙炔富集晶柱（初级生产者 / 燃料源）。
 * <p>富集的乙炔极不稳定：一旦邻近火源（火焰标签方块或着火实体）即发生<b>剧烈连锁爆炸</b>——
 * 当量高于甲烷冰花，并把相邻乙炔冰笋延迟引爆，形成沿晶脉传播的链爆。
 */
public class AcetyleneSpireBlock extends Block {

    /** 单柱爆炸当量（高能乙炔，明显强于甲烷冰花的 1.6）。 */
    private static final float EXPLOSION_POWER = 2.4F;

    public AcetyleneSpireBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 随机刻：检测邻近火源（火焰方块或着火实体）。 */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (hasFireNeighbor(level, pos) || hasBurningEntityNearby(level, pos)) {
            detonate(level, pos);
        }
    }

    /** 相邻方块更新（如放置/点燃火焰）时即时检测。 */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (level instanceof ServerLevel server && hasFireNeighbor(server, pos)) {
            detonate(server, pos);
        }
    }

    /** 计划刻：被相邻冰笋引爆时的连锁入口。 */
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
            return; // 已被移除/被前一次爆炸波及
        }
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
