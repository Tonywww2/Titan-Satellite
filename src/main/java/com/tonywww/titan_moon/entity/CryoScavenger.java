package com.tonywww.titan_moon.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 冰硅甲虫（Cryo-Scavenger，中立）。平时游荡、不主动攻击玩家；受击后反击并唤醒附近同类，
 * 冰晶护甲对常规物理伤害提供额外减免。掉落冰晶甲壳（见掉落表）。
 */
public class CryoScavenger extends PathfinderMob {

    /** 冰晶护甲：常规物理伤害（穿甲/无敌绕过除外）的乘算系数。 */
    private static final float CARAPACE_DAMAGE_MULTIPLIER = 0.6F;
    /** 冰球冲撞冷却（tick，不持久化）。 */
    private int chargeCooldown;

    public CryoScavenger(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // 中立：仅受击反击，不主动索敌玩家；受击时唤醒附近同类一同反击。
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    /** 冰晶护甲：削减常规物理伤害；穿甲/无敌绕过类伤害（如凋零、虚空、/kill）不减免。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        float finalAmount = amount;
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            finalAmount *= CARAPACE_DAMAGE_MULTIPLIER;
        }
        return super.hurt(source, finalAmount);
    }

    /** 冰球冲撞：有目标且 2–10 格、着地、可视时，周期性朝目标猛冲一次。 */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.onGround() && this.hasLineOfSight(target)) {
            double distSqr = this.distanceToSqr(target);
            if (distSqr > 4.0D && distSqr < 100.0D) {
                Vec3 dir = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.x * 0.9D, 0.32D, dir.z * 0.9D));
                this.hasImpulse = true;
                this.chargeCooldown = 80; // ~4s
                this.playSound(SoundEvents.STONE_HIT, 1.0F, 0.8F);
            }
        }
    }

    /** 冲撞命中附加额外击退（冰球撞飞）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.knockback(0.6D, this.getX() - living.getX(), this.getZ() - living.getZ());
        }
        return hurt;
    }
}
