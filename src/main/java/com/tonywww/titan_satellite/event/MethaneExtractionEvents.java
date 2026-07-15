package com.tonywww.titan_satellite.event;

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
import org.jetbrains.annotations.Nullable;

/**
 * 甲烷开采塔防「整套流程」的 Forge 事件族，供整合包 / 附属高度自定义。
 *
 * <p>波次开始沿用 <b>冻结签名</b>的 {@link MethaneExtractionWaveEvent}（冻结，仅通知）；
 * 本类补齐生命周期其余节点：启动（可取消）/ 成功 / 失败。全部在服务端由 {@link WaveController}
 * 通过 {@code MinecraftForge.EVENT_BUS.post(...)} 抛出。
 */
public final class MethaneExtractionEvents {

    private MethaneExtractionEvents() {
    }

    /** 所有塔防事件的公共基类：持有泵坐标、核心坐标与所在世界。 */
    public abstract static class PumpEvent extends Event {
        private final ServerLevel level;
        private final BlockPos pumpPos;
        private final BlockPos corePos;

        protected PumpEvent(ServerLevel level, BlockPos pumpPos, BlockPos corePos) {
            this.level = level;
            this.pumpPos = pumpPos;
            this.corePos = corePos;
        }

        public ServerLevel getLevel() {
            return level;
        }

        public BlockPos getPumpPos() {
            return pumpPos;
        }

        public BlockPos getCorePos() {
            return corePos;
        }
    }

    /**
     * 玩家激活泵、开采即将开始时触发。{@link Cancelable}：取消则拒绝启动，
     * 附属可据此加前置条件（如需要钥匙物品、限定维度、限定时间等）。
     */
    //? if forge {
    @Cancelable
    public static class Start extends PumpEvent {
    //?} else {
    /*public static class Start extends PumpEvent implements ICancellableEvent {
    *///?}
        @Nullable
        private final Player activator;

        public Start(ServerLevel level, BlockPos pumpPos, BlockPos corePos, @Nullable Player activator) {
            super(level, pumpPos, corePos);
            this.activator = activator;
        }

        /** 触发本次激活的玩家（命令 / 红石等非玩家来源可能为 {@code null}）。 */
        @Nullable
        public Player getActivator() {
            return activator;
        }
    }

    /**
     * 开采成功（进度拉满、全部波次幸存）时触发。监听者可追加终局奖励 / 推进剧情。不可取消。
     */
    public static class Success extends PumpEvent {
        private final int wavesSurvived;

        public Success(ServerLevel level, BlockPos pumpPos, BlockPos corePos, int wavesSurvived) {
            super(level, pumpPos, corePos);
            this.wavesSurvived = wavesSurvived;
        }

        public int getWavesSurvived() {
            return wavesSurvived;
        }
    }

    /**
     * 开采失败时触发（泵被摧毁 / 被怪物冲垮 / 核心移除 / 主动取消）。不可取消。
     */
    public static class Fail extends PumpEvent {
        /** 失败原因。 */
        public enum Reason {
            /** 泵方块被破坏。 */
            PUMP_DESTROYED,
            /** 泵完整度被逼近的怪物削减至 0。 */
            PUMP_OVERRUN,
            /** 泵下方的甲烷池核心被移除。 */
            CORE_REMOVED,
            /** 被 {@link Start} 监听者或其它逻辑主动取消。 */
            CANCELLED
        }

        private final Reason reason;

        public Fail(ServerLevel level, BlockPos pumpPos, BlockPos corePos, Reason reason) {
            super(level, pumpPos, corePos);
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }
    }
}
