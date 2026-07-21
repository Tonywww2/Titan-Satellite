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
import net.minecraft.world.damagesource.DamageSource;
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
 * 原生冰虫（Native Ice Worm，敌对·精英）：潜伏于极地迷宫冰原深层洞穴 / 冰虫巢穴的精英守卫兼分解者。
 * <p>高血量 + 高护甲/抗击退（近似「钻地突袭」的耐冲击），近战附加「异星毒素」；伏击感由
 * {@link LeapAtTargetGoal} 扑击近似。破坏巢穴发光晶体可惊醒之
 * （见 {@code TholinCrystalBlock} 惊扰逻辑）。
 */
public class NativeIceWorm extends Monster implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle_coiled");
    private static final RawAnimation SLITHER = RawAnimation.begin().thenLoop("animation.slither");
    private static final RawAnimation AWAKEN = RawAnimation.begin().thenPlay("animation.awaken_erupt");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.attack_melee");
    private static final RawAnimation LEAP = RawAnimation.begin().thenPlay("animation.leap");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("animation.spit_iceshards");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** front-lunge cooldown (ticks, not persisted). */
    private int leapCooldown;

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
            if (!this.level().isClientSide) {
                this.triggerAnim("action", "spit_iceshards");
            }
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
        if (hurt) {
            if (!this.level().isClientSide) {
                this.triggerAnim("action", "attack_melee");
            }
            if (target instanceof LivingEntity living) {
                int duration = switch (this.level().getDifficulty()) {
                    case HARD -> 260;
                    case NORMAL -> 200;
                    default -> 120;
                };
                living.addEffect(TMMobEffects.tholinToxin(duration, 1), this);
            }
        }
        return hurt;
    }

    /** break-crystal awaken / first target lock: burst from ground and roar (awaken_erupt). */
    @Override
    public void setTarget(LivingEntity target) {
        LivingEntity previous = this.getTarget();
        super.setTarget(target);
        if (target != null && previous == null && !this.level().isClientSide) {
            this.triggerAnim("action", "awaken_erupt");
        }
    }

    /** front lunge: periodically leap at a target 4-14 blocks away when grounded and visible. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.leapCooldown > 0) {
            this.leapCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.onGround() && this.hasLineOfSight(target)) {
            double distSqr = this.distanceToSqr(target);
            if (distSqr > 16.0D && distSqr < 196.0D) {
                this.triggerAnim("action", "leap");
                this.leapCooldown = 120;
            }
        }
    }

    /** hurt: armor-shudder guard animation (high armor). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** death: front-to-tail wave collapse (segments go limp, ice armor shatters). */
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
        controllers.add(new AnimationController<>(this, "movement", 10, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("awaken_erupt", AWAKEN)
                .triggerableAnim("attack_melee", ATTACK)
                .triggerableAnim("leap", LEAP)
                .triggerableAnim("spit_iceshards", SPIT)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<NativeIceWorm> state) {
        if (state.isMoving()) {
            return state.setAndContinue(SLITHER);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
