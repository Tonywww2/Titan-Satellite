package com.tonywww.titan_moon.entity;

import java.util.EnumSet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
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
 * 甲烷浮游体（Aero-Jelly）：漂浮在大气中的被动生物。
 * <p>无重力 + 飞行导航（{@link FlyingMoveControl} / {@link FlyingPathNavigation}）实现平滑悬浮——
 * 天然不受他模组重力属性影响、无抖动；软性限制在地表上方低-中空 Y 带 [+6, +22] 内，避免飞太高/贴地。
 * <p>GeckoLib 动画：{@code idle_hover}(静止悬停) / {@code swim_drift}(移动喷推) / {@code flee}(遇织体蛛急逃)
 * 由主控制器按状态自动切换；{@code filter_feed}(滤食) / {@code hurt}(受击) / {@code death}(死亡) 由事件触发；
 * {@code look_at_player}(伞顶朝玩家) 为叠加层持续播放。
 */
public class AeroJelly extends PathfinderMob implements GeoEntity {

    private static final RawAnimation IDLE_HOVER = RawAnimation.begin().thenLoop("animation.idle_hover");
    private static final RawAnimation SWIM_DRIFT = RawAnimation.begin().thenLoop("animation.swim_drift");
    private static final RawAnimation FLEE = RawAnimation.begin().thenLoop("animation.flee");
    private static final RawAnimation FILTER_FEED = RawAnimation.begin().thenPlay("animation.filter_feed");
    private static final RawAnimation LOOK_AT_PLAYER = RawAnimation.begin().thenLoop("animation.look_at_player");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AeroJelly(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        // 逃逸捕食者（食物网）：躲避托林织体蛛。
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, TholinWeaver.class, 10.0F, 1.0D, 1.4D));
        // 滤食：靠近甲烷微浮群时游向并吸收之（初级消费）。
        this.goalSelector.addGoal(1, new FilterFeedGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.7D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    /** 软性限高：直接接管竖直速度——低于 [+6] 强制上升、高于 [+20] 下降、带内阻尼动量趋于悬停。 */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.setNoGravity(true);
        double surfaceY = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition()).getY();
        double lo = surfaceY + 6.0D;
        double hi = surfaceY + 20.0D;
        Vec3 m = this.getDeltaMovement();
        double vy;
        if (this.getY() < lo) {
            vy = 0.12D;             // 明确上升，脱离地面
        } else if (this.getY() > hi) {
            vy = -0.1D;             // 明确下降
        } else {
            vy = m.y * 0.5D;        // 带内：阻尼竖直动量，趋于悬停
        }
        this.setDeltaMovement(m.x, vy, m.z);
    }

    /** 浮空生物无摔落伤害。 */
    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    /** 受击：触发 hurt 动画（伞体骤缩凹陷、内光闪红由渲染层表现）。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（膜体泄气瘪缩、螺旋下沉），并冒一小团泄气白雾。 */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 0.4D, this.getZ(),
                        16, 0.25D, 0.25D, 0.25D, 0.02D);
            }
        }
        super.die(source);
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主控制器：按状态在 flee / swim_drift / idle_hover 之间平滑切换（5 tick 过渡）。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：滤食 / 受击 / 死亡 由事件触发（即时，无过渡）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("filter_feed", FILTER_FEED)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
        // 叠加层：伞顶朝玩家缓转，持续播放、不打断搏动（look_at_player 仅动 bell 旋转，与搏动的 bell 缩放不冲突）。
        controllers.add(new AnimationController<>(this, "look", 0, state -> state.setAndContinue(LOOK_AT_PLAYER)));
    }

    private PlayState movementPredicate(AnimationState<AeroJelly> state) {
        if (this.isFleeing()) {
            return state.setAndContinue(FLEE);
        }
        if (state.isMoving()) {
            return state.setAndContinue(SWIM_DRIFT);
        }
        return state.setAndContinue(IDLE_HOVER);
    }

    /** 附近（10 格内）出现活着的托林织体蛛时判定为逃逸状态，驱动 flee 循环动画。 */
    private boolean isFleeing() {
        return !this.level().getEntitiesOfClass(TholinWeaver.class,
                this.getBoundingBox().inflate(10.0D), Mob::isAlive).isEmpty();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 滤食 AI：游向最近的甲烷微浮群并吸收（治疗 + 粒子）。 */
    private static class FilterFeedGoal extends Goal {

        private final AeroJelly jelly;
        private MethaneMidge target;

        FilterFeedGoal(AeroJelly jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.jelly.level().getEntitiesOfClass(MethaneMidge.class,
                    this.jelly.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), Mob::isAlive).stream()
                    .min((a, b) -> Double.compare(this.jelly.distanceToSqr(a), this.jelly.distanceToSqr(b)))
                    .orElse(null);
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive();
        }

        /** 锁定猎物开始滤食：触发 filter_feed 动画（触须前伸张网）。 */
        @Override
        public void start() {
            if (!this.jelly.level().isClientSide) {
                this.jelly.triggerAnim("action", "filter_feed");
            }
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }
            this.jelly.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            this.jelly.getNavigation().moveTo(this.target, 1.2D);
            if (this.jelly.distanceToSqr(this.target) < 2.0D) {
                if (this.jelly.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.target.getX(), this.target.getY() + 0.2D, this.target.getZ(),
                            6, 0.2D, 0.2D, 0.2D, 0.0D);
                }
                this.jelly.heal(2.0F);
                this.target.discard();
                this.target = null;
            }
        }
    }
}

