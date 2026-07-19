package com.tonywww.titan_moon.entity;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
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
 * 甲烷微浮群（Methane Midge）：极小的漂浮被动群集，营养级最底层、浮游体（Aero-Jelly）的食源。
 * 复用浮游体的无重力飞行悬浮范式，限制在贴地表的低空 Y 带 [+2, +10] 内成群随风漂移。
 * <p>GeckoLib 动画：{@code idle_float}(静止循环) / {@code drift}(移动循环) 由主控制器按移动状态自动切换；
 * {@code hurt}(受击) / {@code death}(死亡) / {@code disturbed_scatter}(受惊散逃) 由事件触发。
 */
public class MethaneMidge extends PathfinderMob implements GeoEntity {

    private static final RawAnimation IDLE_FLOAT = RawAnimation.begin().thenLoop("animation.idle_float");
    private static final RawAnimation DRIFT = RawAnimation.begin().thenLoop("animation.drift");
    private static final RawAnimation SCATTER = RawAnimation.begin().thenPlay("animation.disturbed_scatter");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MethaneMidge(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 8.0D);
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
        // 受惊散逃：附近出现玩家或滤食者（浮游体）时，触发 disturbed_scatter 动画并短暂远离。
        this.goalSelector.addGoal(0, new DisturbedScatterGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /** 软性限低空：低于地表+2 强制上升、高于地表+10 下降、带内阻尼动量，贴地表成群漂移。 */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.setNoGravity(true);
        double surfaceY = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition()).getY();
        double lo = surfaceY + 2.0D;
        double hi = surfaceY + 10.0D;
        Vec3 m = this.getDeltaMovement();
        double vy;
        if (this.getY() < lo) {
            vy = 0.08D;
        } else if (this.getY() > hi) {
            vy = -0.08D;
        } else {
            vy = m.y * 0.5D;
        }
        this.setDeltaMovement(m.x, vy, m.z);
    }

    /** 浮空生物无摔落伤害。 */
    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    /** 受击：触发 hurt 动画（气泡一收一颤、内光闪红由渲染层表现）。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（气泡膨大→破裂），并散出一小团甲烷白雾粒子。 */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 0.15D, this.getZ(),
                        12, 0.12D, 0.12D, 0.12D, 0.02D);
            }
        }
        super.die(source);
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主控制器：按移动状态在 idle_float / drift 之间平滑切换（5 tick 过渡）。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：受击 / 死亡 / 受惊散逃 由事件触发（即时，无过渡）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH)
                .triggerableAnim("scatter", SCATTER));
    }

    private PlayState movementPredicate(AnimationState<MethaneMidge> state) {
        if (state.isMoving()) {
            return state.setAndContinue(DRIFT);
        }
        return state.setAndContinue(IDLE_FLOAT);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 受惊散逃：附近出现玩家或浮游体（滤食者）时，触发 {@code disturbed_scatter} 动画并朝远离威胁的方向冲一小段。
     * 带冷却，避免持续威胁下反复触发。
     */
    private static class DisturbedScatterGoal extends Goal {

        private final MethaneMidge midge;
        private int cooldown;
        private Vec3 fleeTarget;

        DisturbedScatterGoal(MethaneMidge midge) {
            this.midge = midge;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            return !this.nearbyThreats().isEmpty();
        }

        private List<? extends net.minecraft.world.entity.Entity> nearbyThreats() {
            return this.midge.level().getEntities(this.midge,
                    this.midge.getBoundingBox().inflate(4.0D),
                    e -> e instanceof Player || e instanceof AeroJelly);
        }

        @Override
        public void start() {
            if (!this.midge.level().isClientSide) {
                this.midge.triggerAnim("action", "scatter");
            }
            List<? extends net.minecraft.world.entity.Entity> threats = this.nearbyThreats();
            Vec3 away;
            if (!threats.isEmpty()) {
                away = this.midge.position().subtract(threats.get(0).position());
                if (away.lengthSqr() < 1.0E-4D) {
                    away = new Vec3(1.0D, 0.0D, 0.0D);
                }
                away = away.normalize().scale(4.0D);
            } else {
                double angle = this.midge.getRandom().nextDouble() * Math.PI * 2.0D;
                away = new Vec3(Math.cos(angle) * 4.0D, 0.0D, Math.sin(angle) * 4.0D);
            }
            this.fleeTarget = this.midge.position().add(away.x, 0.5D, away.z);
            this.midge.getNavigation().moveTo(this.fleeTarget.x, this.fleeTarget.y, this.fleeTarget.z, 1.6D);
            this.cooldown = 60;
        }

        @Override
        public boolean canContinueToUse() {
            return this.fleeTarget != null && !this.midge.getNavigation().isDone();
        }

        @Override
        public void stop() {
            this.fleeTarget = null;
        }
    }
}
