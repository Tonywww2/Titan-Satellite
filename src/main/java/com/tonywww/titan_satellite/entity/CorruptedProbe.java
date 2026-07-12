package com.tonywww.titan_satellite.entity;

import java.util.EnumSet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 失控探测器（Corrupted-Probe，敌对）。发射【即时激光光束】（hitscan，无弹射物实体，见 §7 CR-1）：
 * 对射程内且视线通畅的目标蓄力后射线判定，命中造成伤害并沿光束生成电火花粒子 + 音效。
 * 掉落废弃高能电池 / 精密电子元件（见掉落表）。
 */
public class CorruptedProbe extends Monster {

    private static final double LASER_RANGE = 16.0D;
    private static final int CHARGE_TICKS = 30;
    private static final int COOLDOWN_TICKS = 60;
    private static final float LASER_DAMAGE = 6.0F;

    public CorruptedProbe(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LaserAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 即时激光命中：造成伤害 + 沿光束电火花粒子 + 音效。仅服务端执行。 */
    private void fireLaser(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        target.hurt(this.damageSources().mobAttack(this), LASER_DAMAGE);
        Vec3 start = this.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length > 1.0E-4D) {
            Vec3 step = delta.normalize().scale(0.5D);
            Vec3 point = start;
            for (double d = 0.0D; d < length; d += 0.5D) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                point = point.add(step);
            }
        }
        server.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 6, 0.2D, 0.2D, 0.2D, 0.01D);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.2F);
    }

    /** 蓄力-发射即时激光的 AI 目标（占用 MOVE+LOOK，发射时停下瞄准）。 */
    private static class LaserAttackGoal extends Goal {

        private final CorruptedProbe probe;
        private int chargeTime;
        private int cooldown;

        LaserAttackGoal(CorruptedProbe probe) {
            this.probe = probe;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private boolean targetInRange() {
            LivingEntity target = probe.getTarget();
            return target != null && target.isAlive()
                    && probe.distanceToSqr(target) <= LASER_RANGE * LASER_RANGE
                    && probe.hasLineOfSight(target);
        }

        @Override
        public boolean canUse() {
            return targetInRange();
        }

        @Override
        public boolean canContinueToUse() {
            return targetInRange() || chargeTime > 0;
        }

        @Override
        public void start() {
            chargeTime = 0;
        }

        @Override
        public void stop() {
            chargeTime = 0;
            cooldown = COOLDOWN_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = probe.getTarget();
            if (target == null) {
                return;
            }
            probe.getLookControl().setLookAt(target, 30.0F, 30.0F);
            probe.getNavigation().stop();
            if (cooldown > 0) {
                cooldown--;
            }
            if (targetInRange()) {
                chargeTime++;
                if (chargeTime >= CHARGE_TICKS && cooldown <= 0) {
                    probe.fireLaser(target);
                    chargeTime = 0;
                    cooldown = COOLDOWN_TICKS;
                }
            } else {
                chargeTime = Math.max(0, chargeTime - 2);
            }
        }
    }
}
