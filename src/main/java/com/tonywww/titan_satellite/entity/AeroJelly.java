package com.tonywww.titan_satellite.entity;

import java.util.EnumSet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * 甲烷浮游体（Aero-Jelly）：漂浮在大气中的被动生物。
 * <p>无重力 + 飞行导航（{@link FlyingMoveControl} / {@link FlyingPathNavigation}）实现平滑悬浮——
 * 天然不受他模组重力属性影响、无抖动；软性限制在地表上方低-中空 Y 带 [+6, +22] 内，避免飞太高/贴地。
 * 渲染复用史莱姆模型（占位）。
 */
public class AeroJelly extends PathfinderMob {

    public AeroJelly(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        // 逃逸捕食者（食物网 §3.1）：躲避托林织体蛛。
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, TholinWeaver.class, 10.0F, 1.0D, 1.4D));
        // 滤食：靠近甲烷微浮群时游向并吸收之（初级消费，设计 §3.2/§3.7）。
        this.goalSelector.addGoal(1, new FilterFeedGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.7D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    /** 软性限高：直接接管竖直速度——低于 [+6] 强制上升、高于 [+20] 下降、带内阻尼动量趋于悬停。 */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.setNoGravity(true);
        double surfaceY = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition()).getY();
        double lo = surfaceY + 6.0D;
        double hi = surfaceY + 20.0D;
        Vec3 m = this.getDeltaMovement();
        double vy;
        if (this.getY() < lo) {
            vy = 0.12D;             // 明确上升，脱离地面
        } else if (this.getY() > hi) {
            vy = -0.1D;             // 明确下降
        } else {
            vy = m.y * 0.5D;        // 带内：阻尼竖直动量，趋于悬停
        }
        this.setDeltaMovement(m.x, vy, m.z);
    }

    /** 浮空生物无摔落伤害。 */
    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    /** 滤食 AI：游向最近的甲烷微浮群并吸收（治疗 + 粒子）。 */
    private static class FilterFeedGoal extends Goal {

        private final AeroJelly jelly;
        private MethaneMidge target;

        FilterFeedGoal(AeroJelly jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.jelly.level().getEntitiesOfClass(MethaneMidge.class,
                    this.jelly.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), Mob::isAlive).stream()
                    .min((a, b) -> Double.compare(this.jelly.distanceToSqr(a), this.jelly.distanceToSqr(b)))
                    .orElse(null);
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }
            this.jelly.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            this.jelly.getNavigation().moveTo(this.target, 1.2D);
            if (this.jelly.distanceToSqr(this.target) < 2.0D) {
                if (this.jelly.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.target.getX(), this.target.getY() + 0.2D, this.target.getZ(),
                            6, 0.2D, 0.2D, 0.2D, 0.0D);
                }
                this.jelly.heal(2.0F);
                this.target.discard();
                this.target = null;
            }
        }
    }
}

