package com.tonywww.titan_moon.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * 甲烷微浮群（Methane Midge）：极小的漂浮被动群集，营养级最底层、浮游体（Aero-Jelly）的食源。
 * 复用浮游体的无重力飞行悬浮范式，但限制在贴地表的低空 Y 带 [+2, +10] 内成群随风漂移。
 * 渲染用缩小的史莱姆模型占位。
 */
public class MethaneMidge extends PathfinderMob {

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
        this.goalSelector.addGoal(0, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
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
}
