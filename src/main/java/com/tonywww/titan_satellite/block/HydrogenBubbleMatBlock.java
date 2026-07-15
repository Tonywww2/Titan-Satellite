package com.tonywww.titan_satellite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
 * 氢泡菌毯（Hydrogen Bubble Mat）：荒原低洼处的化能菌毯，缓释 H₂ 气泡——营养级基座的初级生产者，
 * 甲烷浮游体 / 氢营养蹒兽的食源。
 * <p>随机刻缓释可燃 H₂ 气泡粒子；一旦邻近火源（火焰标签方块或着火实体）即发生<b>轻微轰燃</b>，
 * 并把相邻菌毯延迟点燃形成连锁（复用甲烷冰花范式，但当量更低）。
 */
@SuppressWarnings("deprecation")
public class HydrogenBubbleMatBlock extends Block {

    /** 轻微轰燃当量（明显弱于甲烷冰花的 1.6）。 */
    private static final float DEFLAGRATION_POWER = 1.0F;

    public HydrogenBubbleMatBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 随机刻：邻近火源则轰燃，否则缓释 H₂ 气泡粒子。 */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (hasFireNeighbor(level, pos) || hasBurningEntityNearby(level, pos)) {
            deflagrate(level, pos);
        } else {
            level.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    2, 0.2D, 0.1D, 0.2D, 0.01D);
        }
    }

    /** 相邻方块更新（如放置/点燃火焰）时即时检测。 */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (level instanceof ServerLevel server && hasFireNeighbor(server, pos)) {
            deflagrate(server, pos);
        }
    }

    /** 计划刻：被相邻菌毯引燃时的连锁入口。 */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        deflagrate(level, pos);
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

    private void deflagrate(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(this)) {
            return; // 已被移除/被前一次轰燃波及
        }
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockState(neighborPos).is(this) && !level.getBlockTicks().hasScheduledTick(neighborPos, this)) {
                level.scheduleTick(neighborPos, this, 2 + level.random.nextInt(3));
            }
        }
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                DEFLAGRATION_POWER, Level.ExplosionInteraction.BLOCK);
    }
}
