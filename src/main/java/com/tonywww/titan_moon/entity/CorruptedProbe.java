package com.tonywww.titan_moon.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import software.bernie.geckolib.animatable.GeoEntity;
//? if forge {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
*///?}
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 失控探测器（Corrupted-Probe，敌对）。远程发射【即时激光光束】（hitscan，无弹射物实体）：
 * 对 4~16 格内且视线通畅的目标蓄力后射线判定，命中造成伤害（<b>伤害随攻击力属性缩放</b>）并沿光束生成
 * 电火花粒子 + 音效；目标贴近（4 格内）则改用近战放电。掉落废弃高能电池 / 精密电子元件（见掉落表）。
 * <p>GeckoLib 动画：movement 控制器 {@code idle_hover}(不稳定悬浮)/{@code drift_patrol}(巡弋+镜头扫描)；
 * action 控制器事件触发 {@code laser}(蓄力预告→骤射后坐→冷却散热，蓄力起始整体触发一次)/{@code attack_melee}/{@code hurt}/{@code death}。
 */
public class CorruptedProbe extends Monster implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle_hover");
    private static final RawAnimation DRIFT = RawAnimation.begin().thenLoop("animation.drift_patrol");
    /** 即时激光三段：蓄力预告 → 骤亮射束(后坐) → 冷却散热；蓄力起始时整体触发一次。 */
    private static final RawAnimation LASER = RawAnimation.begin()
            .thenPlay("animation.charge")
            .thenPlay("animation.fire")
            .thenPlay("animation.cooldown");
    private static final RawAnimation MELEE = RawAnimation.begin().thenPlay("animation.attack_melee");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
        // 蓄力起始触发 laser 动画序列（1.5s 蓄力预告 → 骤射后坐 → 3s 冷却），给玩家躲避窗口。
        this.goalSelector.addGoal(1, new RangedHitscan.AttackGoal(this, 4.0D, 16.0D, 30, 60,
                target -> RangedHitscan.beam(this, target,
                        (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE),
                        ParticleTypes.ELECTRIC_SPARK, SoundEvents.BLAZE_SHOOT, 1.2F),
                () -> {
                    if (!this.level().isClientSide) {
                        this.triggerAnim("action", "laser");
                    }
                }));
        // 近战：贴身火花放电。
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 近战命中：触发贴身放电前冲动画。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && !this.level().isClientSide) {
            this.triggerAnim("action", "attack_melee");
        }
        return hurt;
    }

    /** 受击：机体一震、镜头骤闪爆火花。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：镜头急闪熄灭、失控旋转坠落报废。 */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
        }
        super.die(source);
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 移动主控制器：巡弋 drift_patrol / 静止 idle_hover，5 tick 平滑过渡。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：激光三段序列 / 近战放电 / 受击 / 死亡由事件触发（覆盖移动动画）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("laser", LASER)
                .triggerableAnim("attack_melee", MELEE)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<CorruptedProbe> state) {
        if (state.isMoving()) {
            return state.setAndContinue(DRIFT);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
