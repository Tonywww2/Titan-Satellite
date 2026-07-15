package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.registry.TMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * 氢营养蹒兽（Hydrotroph Grazer）：荒原上啃食氢泡菌毯的化能食草兽，被动、受惊逃窜，
 * 充实中层营养级（冰硅甲虫/托林织体蛛的潜在猎物）。渲染用原版猪模型占位。
 */
public class HydrotrophGrazer extends PathfinderMob {

    public HydrotrophGrazer(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        // 啼食氢泡菌毯（化能食草）：走向附近菌毯 → 进食（治疗 + 粒子，偶发消耗）。
        this.goalSelector.addGoal(2, new GrazeMatGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    /** 啼食 AI：寻找并走向氢泡菌毯，抵达后周期进食（治疗 + 孢子粒子，30% 消耗一格）。 */
    private static class GrazeMatGoal extends MoveToBlockGoal {

        private final HydrotrophGrazer grazer;
        private int grazeTime;

        GrazeMatGoal(HydrotrophGrazer grazer) {
            super(grazer, 1.0D, 12, 4);
            this.grazer = grazer;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(TMBlocks.HYDROGEN_BUBBLE_MAT.get());
        }

        @Override
        public double acceptedDistance() {
            return 2.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.grazeTime > 0 || super.canContinueToUse();
        }

        @Override
        public void start() {
            this.grazeTime = 0;
            super.start();
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.isReachedTarget()) {
                return;
            }
            this.grazer.getLookControl().setLookAt(
                    this.blockPos.getX() + 0.5D, this.blockPos.getY(), this.blockPos.getZ() + 0.5D);
            if (++this.grazeTime < 40) {
                return;
            }
            this.grazeTime = 0;
            Level level = this.grazer.level();
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.6D, this.blockPos.getZ() + 0.5D,
                        8, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            this.grazer.heal(2.0F);
            if (this.grazer.getRandom().nextFloat() < 0.3F
                    && level.getBlockState(this.blockPos).is(TMBlocks.HYDROGEN_BUBBLE_MAT.get())) {
                level.destroyBlock(this.blockPos, false);
            }
            this.nextStartTick = 200 + this.grazer.getRandom().nextInt(200);
        }
    }
}
