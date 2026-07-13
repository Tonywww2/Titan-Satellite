package com.tonywww.titan_satellite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 冰火山喷泉方块（PE-1 / M4）。
 *
 * <p>周期性喷发：不使用方块实体（见 CR-3，BE 仅甲烷泵），而是用
 * {@code gameTime} 隐式驱动喷发窗口——每 {@link #CYCLE_TICKS} tick 为一个周期，
 * 其中前 {@link #ERUPT_TICKS} tick 处于喷发态。相邻喷泉按坐标计算相位偏移错峰。
 *
 * <ul>
 *   <li>{@link #stepOn}：喷发态时踩上去的实体被向上击飞（配合高差地形垂直探索）。</li>
 *   <li>{@link #animateTick}：客户端喷发喷雾/冰晶粒子 + 低频本地喷涌音。</li>
 * </ul>
 */
public class CryovolcanicGeyserBlock extends Block {

    /** 一个完整喷发周期的长度（tick）。 */
    private static final int CYCLE_TICKS = 140;
    /** 每个周期内处于喷发态的时长（tick）。 */
    private static final int ERUPT_TICKS = 45;
    /** 喷发击飞赋予的向上速度。 */
    private static final double LAUNCH_VELOCITY = 1.15D;

    public CryovolcanicGeyserBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 该方块是否正处于喷发态。基于 {@code gameTime} + 按坐标计算的相位偏移，
     * 使相邻喷泉错峰喷发而非整齐划一。
     */
    public static boolean isErupting(Level level, BlockPos pos) {
        long phase = Math.floorMod((long) pos.getX() * 7L + (long) pos.getZ() * 13L + (long) pos.getY() * 5L, CYCLE_TICKS);
        return Math.floorMod(level.getGameTime() + phase, CYCLE_TICKS) < ERUPT_TICKS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (isErupting(level, pos)) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, LAUNCH_VELOCITY, motion.z);
            entity.hurtMarked = true;      // 触发速度同步包（玩家击飞必需）
            entity.fallDistance = 0.0F;    // 清除已累积坠落伤害
            if (!level.isClientSide && level.getRandom().nextFloat() < 0.25F) {
                level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE,
                        SoundSource.BLOCKS, 0.8F, 1.1F + level.getRandom().nextFloat() * 0.2F);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isErupting(level, pos)) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 1.0D + random.nextDouble() * 0.4D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.18D + random.nextDouble() * 0.22D, 0.0D);
        }
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.3D + random.nextDouble() * 0.4D, pos.getY() + 1.05D,
                    pos.getZ() + 0.3D + random.nextDouble() * 0.4D, 0.0D, 0.25D, 0.0D);
        }
        if (random.nextInt(24) == 0) {
            level.playLocalSound(pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    SoundSource.BLOCKS, 0.4F, 0.7F, false);
        }
    }
}

