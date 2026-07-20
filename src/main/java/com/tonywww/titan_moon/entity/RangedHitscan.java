package com.tonywww.titan_moon.entity;

import java.util.EnumSet;
import java.util.function.Consumer;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * 敌对生物远程攻击的通用支持：
 * <ul>
 *   <li>{@link #beam} —— 即时命中（hitscan，无弹射物实体）：对目标造成伤害、
 *       沿视线绘制粒子束、播放音效。<b>伤害由调用方按 {@code Attributes.ATTACK_DAMAGE} 传入</b>，
 *       从而远程伤害随生物攻击力属性缩放。</li>
 *   <li>{@link AttackGoal} —— 可复用的「蓄力—发射—冷却」远程 AI：目标位于 [minRange, maxRange]
 *       且视线通畅时停下蓄力，就绪后触发一次 {@code fire}，随后冷却期让出控制权，使近战 / 接近目标
 *       得以运行，形成「远程消耗 + 拉近后近战」的混合战斗。</li>
 * </ul>
 */
public final class RangedHitscan {

    private RangedHitscan() {
    }

    /**
     * 即时命中远程攻击：对 {@code target} 造成 {@code damage}（调用方按攻击力属性算好），
     * 沿 shooter→target 视线绘制 {@code particle} 粒子束并在命中处爆开，播放 {@code sound}。仅服务端生效。
     */
    public static void beam(Mob shooter, LivingEntity target, float damage,
                            ParticleOptions particle, SoundEvent sound, float pitch) {
        if (!(shooter.level() instanceof ServerLevel server)) {
            return;
        }
        target.hurt(shooter.damageSources().mobAttack(shooter), damage);
        Vec3 start = shooter.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length > 1.0E-4D) {
            Vec3 step = delta.normalize().scale(0.5D);
            Vec3 point = start;
            for (double d = 0.0D; d < length; d += 0.5D) {
                server.sendParticles(particle, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                point = point.add(step);
            }
        }
        server.sendParticles(particle, end.x, end.y, end.z, 6, 0.2D, 0.2D, 0.2D, 0.01D);
        shooter.playSound(sound, 1.0F, pitch);
    }

    /**
     * 通用「蓄力—发射—冷却」远程 AI 目标；具体命中效果由构造时传入的 {@code fire} 决定
     * （通常内部调用 {@link #beam} 并附加减益）。占用 MOVE + LOOK：蓄力时停下瞄准，发射后进入冷却
     * 并让出控制权，冷却期由近战 / 接近目标接管。
     *
     * @param minRange 最小生效距离；小于此距离改由近战处理（0 表示贴身也可远程）
     * @param maxRange 最大生效距离
     */
    public static class AttackGoal extends Goal {

        private final Mob mob;
        private final double minRangeSqr;
        private final double maxRangeSqr;
        private final int chargeTicks;
        private final int cooldownTicks;
        private final Consumer<LivingEntity> fire;
        private final Runnable onChargeStart;
        private int charge;
        private int cooldown;
        private boolean fired;

        public AttackGoal(Mob mob, double minRange, double maxRange, int chargeTicks, int cooldownTicks,
                          Consumer<LivingEntity> fire) {
            this(mob, minRange, maxRange, chargeTicks, cooldownTicks, fire, null);
        }

        /**
         * @param onChargeStart 蓄力刚开始（首个蓄力 tick）时回调一次，可用于触发「蓄力预告」动画；{@code null} 表示不回调。
         */
        public AttackGoal(Mob mob, double minRange, double maxRange, int chargeTicks, int cooldownTicks,
                          Consumer<LivingEntity> fire, Runnable onChargeStart) {
            this.mob = mob;
            this.minRangeSqr = minRange * minRange;
            this.maxRangeSqr = maxRange * maxRange;
            this.chargeTicks = chargeTicks;
            this.cooldownTicks = cooldownTicks;
            this.fire = fire;
            this.onChargeStart = onChargeStart;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private boolean targetInBand() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double distSqr = this.mob.distanceToSqr(target);
            return distSqr >= this.minRangeSqr && distSqr <= this.maxRangeSqr && this.mob.hasLineOfSight(target);
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            return targetInBand();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.fired) {
                return false;
            }
            return this.charge > 0 || targetInBand();
        }

        @Override
        public void start() {
            this.charge = 0;
            this.fired = false;
        }

        @Override
        public void stop() {
            this.charge = 0;
            this.fired = false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.mob.getNavigation().stop();
            if (targetInBand()) {
                this.charge++;
                if (this.charge == 1 && this.onChargeStart != null) {
                    this.onChargeStart.run();
                }
                if (this.charge >= this.chargeTicks) {
                    this.fire.accept(target);
                    this.charge = 0;
                    this.fired = true;
                    this.cooldown = this.cooldownTicks;
                }
            } else {
                this.charge = Math.max(0, this.charge - 2);
            }
        }
    }
}
