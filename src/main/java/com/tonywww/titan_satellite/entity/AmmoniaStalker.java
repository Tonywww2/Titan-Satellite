package com.tonywww.titan_satellite.entity;

import com.tonywww.titan_satellite.block.CryovolcanicGeyserBlock;
import com.tonywww.titan_satellite.registry.TSMobEffects;
import net.minecraft.core.BlockPos;
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

/**
 * 氨泉掠食者（Ammonia-Stalker，敌对，桩）。两栖 + 攻击附毒的完整行为由 M2 / PC-2 填充。
 */
public class AmmoniaStalker extends Monster {

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
            living.addEffect(TSMobEffects.tholinToxin(duration, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0), this);
        }
        return hurt;
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * 借冰火山喷泉弹射（E3 / 设计 §5.1③）：当目标明显在上方且附近有喷泉时，寻路踩上喷泉口，
     * 喷发瞬间被击飞逼近目标。击飞本身由 {@link CryovolcanicGeyserBlock#stepOn} 自动完成，
     * 本目标只负责把掠食者带到喷泉口并停留。
     */
    private static class GeyserLaunchGoal extends Goal {

        private final AmmoniaStalker mob;
        private final double speed;
        private final int searchRadius;
        private BlockPos geyser;
        private int cooldown;

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
            moveToGeyser();
        }

        @Override
        public void stop() {
            this.geyser = null;
            this.cooldown = 100;
        }

        @Override
        public void tick() {
            if (this.geyser == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(this.geyser.getX() + 0.5D, this.geyser.getY() + 1.0D, this.geyser.getZ() + 0.5D);
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
