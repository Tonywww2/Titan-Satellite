package com.tonywww.titan_moon.event;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
//? if forge {
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
//?} else {
/*import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
*///?}

/**
 * 托林晶体「晶洞惊扰」事件（可取消）。{@link com.tonywww.titan_moon.block.TholinCrystalBlock}
 * 在玩家破坏晶体且触发概率命中后、释放毒气 / 惊醒潜伏敌对之前，post 到
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}。
 *
 * <p>整合包 / 附属可监听以提供自定义：
 * <ul>
 *   <li>取消事件（{@code setCanceled(true)}）——完全阻止本次惊扰；</li>
 *   <li>{@link #setReleaseGas(boolean)} / {@link #setWakeMobs(boolean)} 分别开关毒气与惊怪；</li>
 *   <li>{@link #setGasRadius(float)} / {@link #setGasDurationTicks(int)} 调整毒气云范围与时长；</li>
 *   <li>{@link #setWakeRadius(double)} 调整惊醒潜伏敌对的半径。</li>
 * </ul>
 */
//? if forge {
@Cancelable
public class TholinCrystalDisturbedEvent extends Event {
//?} else {
/*public class TholinCrystalDisturbedEvent extends Event implements ICancellableEvent {
*///?}

    private final ServerLevel level;
    private final BlockPos pos;
    @Nullable
    private final Player breaker;

    private boolean releaseGas = true;
    private boolean wakeMobs = true;
    private float gasRadius;
    private int gasDurationTicks;
    private double wakeRadius;

    public TholinCrystalDisturbedEvent(ServerLevel level, BlockPos pos, @Nullable Player breaker,
                                       float gasRadius, int gasDurationTicks, double wakeRadius) {
        this.level = level;
        this.pos = pos;
        this.breaker = breaker;
        this.gasRadius = gasRadius;
        this.gasDurationTicks = gasDurationTicks;
        this.wakeRadius = wakeRadius;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    /** 破坏晶体的玩家；非玩家来源（如未来的爆炸触发）可能为 {@code null}。 */
    @Nullable
    public Player getBreaker() {
        return breaker;
    }

    public boolean isReleaseGas() {
        return releaseGas;
    }

    public void setReleaseGas(boolean releaseGas) {
        this.releaseGas = releaseGas;
    }

    public boolean isWakeMobs() {
        return wakeMobs;
    }

    public void setWakeMobs(boolean wakeMobs) {
        this.wakeMobs = wakeMobs;
    }

    public float getGasRadius() {
        return gasRadius;
    }

    public void setGasRadius(float gasRadius) {
        this.gasRadius = gasRadius;
    }

    public int getGasDurationTicks() {
        return gasDurationTicks;
    }

    public void setGasDurationTicks(int gasDurationTicks) {
        this.gasDurationTicks = gasDurationTicks;
    }

    public double getWakeRadius() {
        return wakeRadius;
    }

    public void setWakeRadius(double wakeRadius) {
        this.wakeRadius = wakeRadius;
    }
}
