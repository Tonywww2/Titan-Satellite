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
 * 冰硅甲虫（Cryo-Scavenger，中立）。平时游荡、不主动攻击玩家；受击后反击并唤醒附近同类，
 * 冰晶护甲对常规物理伤害提供额外减免。掉落冰晶甲壳（见掉落表）。
 * <p>GeckoLib 动画：{@code idle}(静止) / {@code walk}(多足波浪爬行) 由移动主控制器按速度切换；
 * {@code attack_melee}(啃咬) / {@code roll_*}(蜷球冲撞三段) / {@code hurt}(受击) / {@code death}(翻倒碎壳)
 * 由事件触发（动作控制器覆盖移动动画）。
 */
public class CryoScavenger extends PathfinderMob implements GeoEntity {

    /** 冰晶护甲：常规物理伤害（穿甲/无敌绕过除外）的乘算系数。 */
    private static final float CARAPACE_DAMAGE_MULTIPLIER = 0.6F;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.attack_melee");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");
    /** 冰球冲撞：蜷球(roll_curl) → 高速滚撞(roll_charge ×3) → 弹开落地(roll_uncurl) 一次性序列。 */
    private static final RawAnimation ROLL = RawAnimation.begin()
            .thenPlay("animation.roll_curl")
            .thenPlay("animation.roll_charge")
            .thenPlay("animation.roll_charge")
            .thenPlay("animation.roll_charge")
            .thenPlay("animation.roll_uncurl");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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

    /** 冰晶护甲：削减常规物理伤害；穿甲/无敌绕过类伤害（如凋零、虚空、/kill）不减免；受击触发 hurt 动画。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        float finalAmount = amount;
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            finalAmount *= CARAPACE_DAMAGE_MULTIPLIER;
        }
        boolean result = super.hurt(source, finalAmount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（足软翻倒、冰壳板块碎裂剥落露出深色躯体）。 */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
        }
        super.die(source);
    }

    /** 冰球冲撞：有目标且 2–10 格、着地、可视时，周期性朝目标猛冲一次（触发 roll 三段动画）。 */
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
                this.triggerAnim("action", "roll");
            }
        }
    }

    /** 冲撞命中：啃咬动画 + 附加额外击退（冰球撞飞）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            if (!this.level().isClientSide) {
                this.triggerAnim("action", "attack_melee");
            }
            if (target instanceof LivingEntity living) {
                living.knockback(0.6D, this.getX() - living.getX(), this.getZ() - living.getZ());
            }
        }
        return hurt;
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 移动主控制器：静止 idle / 爬行 walk（多足波浪爬行），5 tick 平滑过渡。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：啃咬 / 冰球冲撞 / 受击 / 死亡由事件触发（覆盖移动动画）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack_melee", ATTACK)
                .triggerableAnim("roll", ROLL)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<CryoScavenger> state) {
        if (state.isMoving()) {
            return state.setAndContinue(WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
