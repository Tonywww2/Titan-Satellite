package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.registry.TMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
 * 氢营养蹒兽（Hydrotroph Grazer）：荒原上啃食氢泡菌毯的化能食草兽，被动、受惊逃窜，
 * 充实中层营养级（冰硅甲虫/托林织体蛛的潜在猎物）。
 * <p>GeckoLib 动画：{@code idle}(静止循环) / {@code walk}(蹒跚行走) / {@code panic}(受惊疾奔) 由主控制器
 * 按移动速度自动切换；{@code graze}(低头进食) / {@code hurt}(受击) / {@code death}(瘫倒) 由事件触发。
 */
public class HydrotrophGrazer extends PathfinderMob implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    private static final RawAnimation PANIC = RawAnimation.begin().thenLoop("animation.panic");
    private static final RawAnimation GRAZE = RawAnimation.begin().thenPlay("animation.graze");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HydrotrophGrazer(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        // 啼食氢泡菌毯（化能食草）：走向附近菌毯 → 进食（治疗 + 粒子，偶发消耗）。
        this.goalSelector.addGoal(2, new GrazeMatGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    /** 受击：触发 hurt 动画（浑身一缩、后退半步、低头由动画表现）。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（前肢先软、身体侧倾缓缓瘫倒）。 */
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
        // 移动主控制器：静止 idle / 行走 walk / 高速 panic（按水平速度阈值区分），5 tick 平滑过渡。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：进食 / 受击 / 死亡由事件触发（即时，无过渡）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("graze", GRAZE)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<HydrotrophGrazer> state) {
        if (state.isMoving()) {
            // 受惊疾奔（PanicGoal 速 1.4）水平速度显著高于游荡（0.8×0.22）→ 用速度平方阈值区分 walk / panic。
            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.02D) {
                return state.setAndContinue(PANIC);
            }
            return state.setAndContinue(WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 啼食 AI：寻找并走向氢泡菌毯，抵达后周期进食（治疗 + 孢子粒子，30% 消耗一格）。 */
    private static class GrazeMatGoal extends MoveToBlockGoal {

        private final HydrotrophGrazer grazer;
        private int grazeTime;

        GrazeMatGoal(HydrotrophGrazer grazer) {
            super(grazer, 1.0D, 12, 4);
            this.grazer = grazer;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(TMBlocks.HYDROGEN_BUBBLE_MAT.get());
        }

        @Override
        public double acceptedDistance() {
            return 2.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.grazeTime > 0 || super.canContinueToUse();
        }

        @Override
        public void start() {
            this.grazeTime = 0;
            super.start();
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.isReachedTarget()) {
                return;
            }
            this.grazer.getLookControl().setLookAt(
                    this.blockPos.getX() + 0.5D, this.blockPos.getY(), this.blockPos.getZ() + 0.5D);
            // 抵达菌毯、每个进食周期开始时触发 graze 动画（低头贴地、下颌刮擦式咀嚼）。
            if (this.grazeTime == 0 && !this.grazer.level().isClientSide) {
                this.grazer.triggerAnim("action", "graze");
            }
            if (++this.grazeTime < 40) {
                return;
            }
            this.grazeTime = 0;
            Level level = this.grazer.level();
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.6D, this.blockPos.getZ() + 0.5D,
                        8, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            this.grazer.heal(2.0F);
            if (this.grazer.getRandom().nextFloat() < 0.3F
                    && level.getBlockState(this.blockPos).is(TMBlocks.HYDROGEN_BUBBLE_MAT.get())) {
                level.destroyBlock(this.blockPos, false);
            }
            this.nextStartTick = 200 + this.grazer.getRandom().nextInt(200);
        }
    }
}
