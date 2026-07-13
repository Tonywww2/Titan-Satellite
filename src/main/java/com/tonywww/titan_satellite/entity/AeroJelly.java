package com.tonywww.titan_satellite.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * 甲烷浮游体（Aero-Jelly）：漂浮在大气中的被动生物。
 * <p>无重力 + 飞行导航（{@link FlyingMoveControl} / {@link FlyingPathNavigation}）实现平滑悬浮——
 * 天然不受他模组重力属性影响、无抖动；软性限制在地表上方低-中空 Y 带 [+6, +22] 内，避免飞太高/贴地。
 * 渲染复用史莱姆模型（占位）。
 */
public class AeroJelly extends PathfinderMob {

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
        this.goalSelector.addGoal(0, new WaterAvoidingRandomFlyingGoal(this, 0.7D));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
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
}

