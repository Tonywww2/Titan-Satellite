package com.tonywww.titan_moon.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
 * 失控探测器（Corrupted-Probe，敌对）。远程发射【即时激光光束】（hitscan，无弹射物实体）：
 * 对 4~16 格内且视线通畅的目标蓄力后射线判定，命中造成伤害（<b>伤害随攻击力属性缩放</b>）并沿光束生成
 * 电火花粒子 + 音效；目标贴近（4 格内）则改用近战放电。掉落废弃高能电池 / 精密电子元件（见掉落表）。
 */
public class CorruptedProbe extends Monster {

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
        // 远程：即时激光（伤害 = 攻击力属性）；仅当目标 4~16 格时启用，贴近则让位近战。
        this.goalSelector.addGoal(1, new RangedHitscan.AttackGoal(this, 4.0D, 16.0D, 30, 60,
                target -> RangedHitscan.beam(this, target,
                        (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE),
                        ParticleTypes.ELECTRIC_SPARK, SoundEvents.BLAZE_SHOOT, 1.2F)));
        // 近战：贴身火花放电。
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
