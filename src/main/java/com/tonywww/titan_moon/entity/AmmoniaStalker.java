package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.block.CryovolcanicGeyserBlock;
import com.tonywww.titan_moon.registry.TMMobEffects;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
//? if forge {
import net.minecraft.world.level.pathfinder.BlockPathTypes;
//?} else {
/*import net.minecraft.world.level.pathfinder.PathType;
*///?}

import java.util.EnumSet;

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
 * 氨泉掠食者（Ammonia-Stalker，敌对）。两栖掠食者：攻击附异星毒素 + 挖掘疲劳。
 * <p>GeckoLib 动画：movement 控制器按状态切换 {@code idle}/{@code stalk_walk}/{@code swim}；
 * action 控制器事件触发 {@code attack_melee}/{@code spit_ammonia}/{@code geyser}(蓄力→弹射→扑击)/{@code hurt}/{@code death}。
 */
public class AmmoniaStalker extends Monster implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.stalk_walk");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.swim");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.attack_melee");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("animation.spit_ammonia");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.death");
    /** 借喷泉弹射：蓄力瞄准 → 弹射伸展 → 空中扑击落地，一次性序列。 */
    private static final RawAnimation GEYSER = RawAnimation.begin()
            .thenPlay("animation.geyser_mount")
            .thenPlay("animation.geyser_launch")
            .thenPlay("animation.air_pounce");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AmmoniaStalker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        //? if forge {
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        //?} else {
        /*this.setPathfindingMalus(PathType.WATER, 0.0F);
        *///?}
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    //? if forge {
    @SuppressWarnings("deprecation")
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    //?} else {
    /*@Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }
    *///?}

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int duration = switch (this.level().getDifficulty()) {
                case HARD -> 200;
                case NORMAL -> 140;
                default -> 80;
            };
            living.addEffect(TMMobEffects.tholinToxin(duration, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0), this);
        }
        if (hurt && !this.level().isClientSide) {
            this.triggerAnim("action", "attack_melee");
        }
        return hurt;
    }

    /** 受击：触发 hurt 动画（侧闪护爪、氨泵闪烁）。 */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()) {
            this.triggerAnim("action", "hurt");
        }
        return result;
    }

    /** 死亡：触发 death 动画（前扑瘫倒，氨泵辉光消退）。 */
    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide) {
            this.triggerAnim("action", "death");
        }
        super.die(source);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GeyserLaunchGoal(this, 1.15D, 8));
        // 远程：喷吐氨液（伤害随攻击力，并附异星毒素 + 挖掘疲劳）；5~14 格时启用，贴近改用近战。
        this.goalSelector.addGoal(2, new RangedHitscan.AttackGoal(this, 5.0D, 14.0D, 25, 70, target -> {
            this.triggerAnim("action", "spit_ammonia");
            RangedHitscan.beam(this, target,
                    (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75D),
                    ParticleTypes.SPIT, SoundEvents.LLAMA_SPIT, 1.0F);
            target.addEffect(TMMobEffects.tholinToxin(100, 0), this);
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0), this);
        }));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // 顶级掠食（食物网）：无玩家时猎食下位物种——托林织体蛛、冰硅甲虫。
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TholinWeaver.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, CryoScavenger.class, true));
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 移动主控制器：水中 swim / 陆地移动 stalk_walk / 静止 idle，5 tick 平滑过渡。
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        // 动作控制器：近战啃咬 / 喷吐氨液 / 喷泉弹射序列 / 受击 / 死亡由事件触发（覆盖移动动画）。
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack_melee", ATTACK)
                .triggerableAnim("spit_ammonia", SPIT)
                .triggerableAnim("geyser", GEYSER)
                .triggerableAnim("hurt", HURT)
                .triggerableAnim("death", DEATH));
    }

    private PlayState movementPredicate(AnimationState<AmmoniaStalker> state) {
        if (this.isInWater()) {
            return state.setAndContinue(SWIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 借冰火山喷泉弹射：当目标明显在上方且附近有喷泉时，寻路踩上喷泉口，
     * 喷发瞬间被击飞逼近目标。击飞本身由 {@link CryovolcanicGeyserBlock#stepOn} 自动完成，
     * 本目标只负责把掠食者带到喷泉口并停留。
     */
    private static class GeyserLaunchGoal extends Goal {

        private final AmmoniaStalker mob;
        private final double speed;
        private final int searchRadius;
        private BlockPos geyser;
        private int cooldown;
        /** 本次借喷泉是否已触发弹射动画序列（防重复）。 */
        private boolean triggered;

        GeyserLaunchGoal(AmmoniaStalker mob, double speed, int searchRadius) {
            this.mob = mob;
            this.speed = speed;
            this.searchRadius = searchRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            LivingEntity target = this.mob.getTarget();
            if (target == null || !this.mob.onGround()) {
                return false;
            }
            // 仅当目标明显在上方（喷泉弹射能拉近垂直高差）才借喷泉
            if (target.getY() - this.mob.getY() < 3.0D) {
                return false;
            }
            this.geyser = findNearestGeyser();
            return this.geyser != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return this.geyser != null && target != null && this.mob.onGround()
                    && this.mob.level().getBlockState(this.geyser).getBlock() instanceof CryovolcanicGeyserBlock;
        }

        @Override
        public void start() {
            this.triggered = false;
            moveToGeyser();
        }

        @Override
        public void stop() {
            this.geyser = null;
            this.triggered = false;
            this.cooldown = 100;
        }

        @Override
        public void tick() {
            if (this.geyser == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(this.geyser.getX() + 0.5D, this.geyser.getY() + 1.0D, this.geyser.getZ() + 0.5D);
            // 踩上喷泉口（水平贴近、着地）时触发一次弹射动画序列：蓄力→弹射→空中扑击。
            if (!this.triggered && this.mob.onGround()) {
                double dx = this.geyser.getX() + 0.5D - this.mob.getX();
                double dz = this.geyser.getZ() + 0.5D - this.mob.getZ();
                if (dx * dx + dz * dz < 1.6D) {
                    this.mob.triggerAnim("action", "geyser");
                    this.triggered = true;
                }
            }
            if (this.mob.getNavigation().isDone()) {
                moveToGeyser();
            }
        }

        private void moveToGeyser() {
            this.mob.getNavigation().moveTo(this.geyser.getX() + 0.5D, this.geyser.getY() + 1.0D, this.geyser.getZ() + 0.5D, this.speed);
        }

        private BlockPos findNearestGeyser() {
            BlockPos origin = this.mob.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -this.searchRadius; dx <= this.searchRadius; dx++) {
                for (int dz = -this.searchRadius; dz <= this.searchRadius; dz++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (this.mob.level().getBlockState(cursor).getBlock() instanceof CryovolcanicGeyserBlock) {
                            double d = cursor.distSqr(origin);
                            if (d < bestDist) {
                                bestDist = d;
                                best = cursor.immutable();
                            }
                        }
                    }
                }
            }
            return best;
        }
    }
}
