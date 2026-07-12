package com.tonywww.titan_satellite.block;

import java.util.List;

import com.tonywww.titan_satellite.event.TholinCrystalDisturbedEvent;
import com.tonywww.titan_satellite.registry.TSMobEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;

/**
 * 托林晶体方块。玩家破坏时有概率「惊扰晶洞」：释放毒气云（托林毒素 + 中毒）并惊醒附近潜伏的敌对生物。
 * 惊扰前会 post 可取消 / 可自定义的 {@link TholinCrystalDisturbedEvent}，供整合包 / 附属扩展。
 */
public class TholinCrystalBlock extends Block {

    /** 破坏时触发惊扰的概率。 */
    private static final float DISTURB_CHANCE = 0.5F;
    /** 毒气云默认半径（方块）。 */
    private static final float GAS_RADIUS = 3.0F;
    /** 毒气云默认存在时长（tick，约 10s）。 */
    private static final int GAS_DURATION_TICKS = 200;
    /** 惊醒潜伏敌对的默认半径（方块）。 */
    private static final double WAKE_RADIUS = 12.0D;

    public TholinCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel
                && !player.getAbilities().instabuild
                && serverLevel.random.nextFloat() < DISTURB_CHANCE) {
            disturb(serverLevel, pos, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /** 组织一次晶洞惊扰：先 post 可取消 / 可自定义事件，未被取消则按（可能被调整的）参数放毒气 + 惊怪。 */
    private void disturb(ServerLevel level, BlockPos pos, Player breaker) {
        TholinCrystalDisturbedEvent event = new TholinCrystalDisturbedEvent(
                level, pos, breaker, GAS_RADIUS, GAS_DURATION_TICKS, WAKE_RADIUS);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return; // 监听者取消了本次惊扰
        }
        if (event.isReleaseGas()) {
            releaseGas(level, pos, event.getGasRadius(), event.getGasDurationTicks());
        }
        if (event.isWakeMobs()) {
            wakeNearbyMobs(level, pos, event.getWakeRadius(), breaker);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    /** 在晶体处生成一团逐渐消散的毒气云，对其中生物施加托林毒素与中毒。 */
    private void releaseGas(ServerLevel level, BlockPos pos, float radius, int durationTicks) {
        if (radius <= 0.0F || durationTicks <= 0) {
            return;
        }
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        cloud.setRadius(radius);
        cloud.setDuration(durationTicks);
        cloud.setWaitTime(0);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setRadiusPerTick(-radius / (float) durationTicks);
        cloud.addEffect(new MobEffectInstance(TSMobEffects.THOLIN_TOXIN.get(), durationTicks, 0));
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 0));
        level.addFreshEntity(cloud);
    }

    /** 惊醒半径内所有尚无目标的潜伏敌对生物，令其锁定破坏者。 */
    private void wakeNearbyMobs(ServerLevel level, BlockPos pos, double radius, Player breaker) {
        if (breaker == null) {
            return;
        }
        AABB area = new AABB(pos).inflate(radius);
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area, mob -> mob instanceof Enemy && mob.isAlive());
        for (Mob mob : mobs) {
            if (mob.getTarget() == null) {
                mob.setTarget(breaker);
            }
        }
    }
}
