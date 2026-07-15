package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.registry.TMMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 托林织体蛛（Tholin-Weaver，敌对）：托林沙海的伏击型中级捕食者。
 * <p>近战附加「缓慢 + 异星毒素」；当有目标且处于中距（3–16 格）可视时，周期性「吐丝」——
 * 在目标处生成一团减速 + 异星毒素的黏网云（复用 {@code AreaEffectCloud} 毒气云范式）。
 * 伏击感由 {@link LeapAtTargetGoal}（扑击）+ 慢速游荡近似。渲染复用蜘蛛模型（占位）。
 */
public class TholinWeaver extends Monster {

    /** 吐丝冷却计时（tick，不持久化）。 */
    private int webCooldown;

    public TholinWeaver(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // 捕食（食物网）：无玩家时猎食下位物种——甲烷浮游体、冰硅甲虫。
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AeroJelly.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, CryoScavenger.class, true));
    }

    /** 近战命中：附加「缓慢 II」+「异星毒素」。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int duration = switch (this.level().getDifficulty()) {
                case HARD -> 200;
                case NORMAL -> 140;
                default -> 80;
            };
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1), this);
            living.addEffect(TMMobEffects.tholinToxin(duration, 0), this);
        }
        return hurt;
    }

    /** 吐丝：有目标且 3–16 格可视时，周期性在目标处放一团减速 + 异星毒素的黏网云。 */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.webCooldown > 0) {
            this.webCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.level() instanceof ServerLevel server) {
            double distSqr = this.distanceToSqr(target);
            if (distSqr >= 9.0D && distSqr <= 256.0D && this.hasLineOfSight(target)) {
                spitWeb(server, target);
                this.webCooldown = 120; // ~6s
            }
        }
    }

    private void spitWeb(ServerLevel level, LivingEntity target) {
        // 远程毒液命中：伤害随攻击力属性缩放，并沿视线绘制黏液粒子束 + 音效。
        RangedHitscan.beam(this, target,
                (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75D),
                ParticleTypes.ITEM_SLIME, SoundEvents.SPIDER_HURT, 0.7F);
        // 命中处生成减速 + 异星毒素的黏网云。
        AreaEffectCloud web = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        web.setOwner(this);
        web.setRadius(2.0F);
        web.setDuration(120);
        web.setWaitTime(0);
        web.setRadiusOnUse(-0.2F);
        web.setRadiusPerTick(-2.0F / 120.0F);
        web.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        web.addEffect(TMMobEffects.tholinToxin(100, 0));
        level.addFreshEntity(web);
    }
}
