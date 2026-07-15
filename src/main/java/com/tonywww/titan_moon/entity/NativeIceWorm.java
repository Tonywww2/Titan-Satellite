package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.registry.TMMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 原生冰虫（Native Ice Worm，敌对·精英）：潜伏于极地迷宫冰原深层洞穴 / 冰虫巢穴的精英守卫兼分解者。
 * <p>高血量 + 高护甲/抗击退（近似「钻地突袭」的耐冲击），近战附加「异星毒素」；伏击感由
 * {@link LeapAtTargetGoal} 扑击近似。渲染复用放大的蠹虫模型（占位）。破坏巢穴发光晶体可惊醒之
 * （见 {@code TholinCrystalBlock} 惊扰逻辑）。
 */
public class NativeIceWorm extends Monster {

    public NativeIceWorm(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.3F));
        // 远程：喷射冰晶碎屑（伤害随攻击力，并附异星毒素 + 短暂缓慢）；5~16 格时启用，贴近改用近战。
        this.goalSelector.addGoal(2, new RangedHitscan.AttackGoal(this, 5.0D, 16.0D, 30, 80, target -> {
            RangedHitscan.beam(this, target,
                    (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75D),
                    ParticleTypes.SNOWFLAKE, SoundEvents.SNOW_GOLEM_SHOOT, 0.8F);
            target.addEffect(TMMobEffects.tholinToxin(160, 0), this);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0), this);
        }));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 近战命中：附加「异星毒素」（精英，等级 I、时长偏长）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int duration = switch (this.level().getDifficulty()) {
                case HARD -> 260;
                case NORMAL -> 200;
                default -> 120;
            };
            living.addEffect(TMMobEffects.tholinToxin(duration, 1), this);
        }
        return hurt;
    }
}
