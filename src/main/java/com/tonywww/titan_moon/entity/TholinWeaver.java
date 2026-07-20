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
import net.minecraft.world.damagesource.DamageSource;
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
 * 托林织体蛛（Tholin-Weaver，敌对）：托林沙海的伏击型中级捕食者。
 * <p>近战附加「缓慢 + 异星毒素」；当有目标且处于中距（3–16 格）可视时，周期性「吐丝」——
 * 在目标处生成一团减速 + 异星毒素的黏网云（复用 {@code AreaEffectCloud} 毒气云范式）。
 * 伏击感由 {@link LeapAtTargetGoal}（扑击）+ 慢速游荡近似。
 * <p>GeckoLib 动画：{@code idle_buried}(半埋伏击) / {@code stalk}(潜行疾走) 由主控制器按移动/目标自动切换；
 * {@code emerge}(破沙而出) / {@code leap_pounce}(扑跃) / {@code attack_melee}(啃咬) / {@code spit_web}(吐丝) /
 * {@code hurt}(受击) / {@code death}(卷腿倒地) 由事件触发。
 */
public class TholinWeaver extends Monster implements GeoEntity {

    private static final RawAnimation IDLE_BURIED = RawAnimation.begin().thenLoop("animation.idle_buried");
    private static final RawAnimation STALK = RawAnimation.begin().thenLoop("animation.stalk");
    private static final RawAnimation EMERGE = RawAnimation.begin().thenPlay("animation.emerge");
    private static final RawAnimation POUNCE = RawAnimation.begin().thenPlay("animation.leap_pounce");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.attack_melee");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("animation.spit_web");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 吐丝冷却计时（tick，不持久化）。 */
    private int webCooldown;

    /** 上一 tick 是否有目标——探测「获得目标」→ 触发 emerge（破沙而出）。 */
    private boolean hadTarget;
    /** 上一 tick 是否着地——探测「离地扑跃」→ 触发 leap_pounce。 */
    private boolean wasOnGround = true;

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
        if (hurt) {
            if (!this.level().isClientSide) {
                this.triggerAnim("action", "attack_melee");
            }
            if (target instanceof LivingEntity living) {
                int duration = switch (this.level().getDifficulty()) {
                    case HARD -> 200;
                    case NORMAL -> 140;
                    default -> 80;
                };
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1), this);
                living.addEffect(TMMobEffects.tholinToxin(duration, 0), this);
            }
        }
        return hurt;
    }

    /** 每 tick 服务端 AI：探测「获得目标」触发 emerge（破沙而出）、「离地扑跃」触发 leap_pounce；
     * 并在有目标且 3–16 格可视时周期性吐丝——在目标处放一团减速 + 异星毒素的黏网云。 */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = this.getTarget();
        // 获得目标（无→有）：破沙而出。
        boolean hasTarget = target != null;
        if (hasTarget && !this.hadTarget) {
            this.triggerAnim("action", "emerge");
        }
        this.hadTarget = hasTarget;
        // 离地扑跃（着地→腾空、有目标且上升）：伏击核心扑跃姿态。
        boolean onGround = this.onGround();
        if (this.wasOnGround && !onGround && hasTarget && this.getDeltaMovement().y > 0.0D) {
            this.triggerAnim("action", "leap_pounce");
        }
        this.wasOnGround = onGround;
        // 吐丝（远程）。
        if (this.webCooldown > 0) {
            this.webCooldown--;
            return;
        }
        if (target != null && this.level() instanceof ServerLevel server) {
            double distSqr = this.distanceToSqr(target);
            if (distSqr >= 9.0D && distSqr <= 256.0D && this.hasLineOfSight(target)) {
                spitWeb(server, target);
                this.webCooldown = 120; // ~6s
            }
        }
    }

    private void spitWeb(ServerLevel level, LivingEntity target) {
        // 吐丝预告 + 喷射姿态（后半身抬起、毒腺前挺蓄力 → 前倾喷出黏液团）。
        this.triggerAnim("action", "spit_web");
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

    /** 受击：触发 hurt 动画（八足一缩、压低身体急退、螯肢外张示威）。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（足向内蜷曲的经典卷腿、缓慢侧翻、毒腺渗液）。 */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
        }
        super.die(source);
    }

    // ------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 移动主控制器：疾走 stalk / 半埋伏击 idle_buried，5 tick 平滑过渡。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：破沙 / 扑跃 / 啃咬 / 吐丝 / 受击 / 死亡由事件触发，覆盖移动动画。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("emerge", EMERGE)
                .triggerableAnim("leap_pounce", POUNCE)
                .triggerableAnim("attack_melee", ATTACK)
                .triggerableAnim("spit_web", SPIT)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<TholinWeaver> state) {
        if (state.isMoving()) {
            return state.setAndContinue(STALK);
        }
        // 有目标时保持潜行戒备姿态；无目标时半埋伏击（idle_buried）。
        return state.setAndContinue(this.getTarget() != null ? STALK : IDLE_BURIED);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
