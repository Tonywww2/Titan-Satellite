package com.tonywww.titan_satellite.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
//? if forge {
import net.minecraftforge.eventbus.api.Event;
//?} else {
/*import net.neoforged.bus.api.Event;
*///?}

/**
 * 甲烷开采塔防波次事件（PA-2 冻结签名）。
 * 泵在每一波开始时 {@code MinecraftForge.EVENT_BUS.post(...)}；整合包/附属可监听以
 * 自定义每波刷怪逻辑与强度。默认波次逻辑与 Mixin 定制刷怪由 M4（PE-2）实现。
 */
public class MethaneExtractionWaveEvent extends Event {

    private final ServerLevel level;
    private final BlockPos pumpPos;
    private final int waveIndex;
    private final int intensity;

    public MethaneExtractionWaveEvent(ServerLevel level, BlockPos pumpPos, int waveIndex, int intensity) {
        this.level = level;
        this.pumpPos = pumpPos;
        this.waveIndex = waveIndex;
        this.intensity = intensity;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public BlockPos getPumpPos() {
        return pumpPos;
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public int getIntensity() {
        return intensity;
    }
}
