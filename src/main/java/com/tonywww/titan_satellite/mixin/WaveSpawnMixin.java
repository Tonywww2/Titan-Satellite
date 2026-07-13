package com.tonywww.titan_satellite.mixin;

import com.tonywww.titan_satellite.event.WaveController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 塔防波次怪定制 Mixin（PE-2 / T4.2）。
 *
 * <p>注入 vanilla {@link Mob#aiStep()}，对带 {@link WaveController#WAVE_MOB_TAG} 标记的波次怪
 * 做<b>一次性强化</b>：标记为常驻（塔防期间不自然消失）并附加持续加速（「深渊狂暴」，提升进攻强度）。
 * 对应设计 §5.2「结合 Mixin 高度自定义每波怪物的生成逻辑和强度」。
 *
 * <p>选择稳定的 vanilla 注入点（而非 {@code NaturalSpawner}）以降低跨版本脆弱性、并靠标记过滤
 * 只作用于本模组波次怪，见设计 §6 原则 / 风险 R3。附属可继续往同一注入点叠加定制。
 */
@Mixin(Mob.class)
public abstract class WaveSpawnMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void titan$empowerWaveMobs(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        // 服务端、尚未强化、且是波次怪时，做一次性强化。
        if (self.level().isClientSide || self.isPersistenceRequired()) {
            return;
        }
        if (self.getPersistentData().getBoolean(WaveController.WAVE_MOB_TAG)) {
            self.setPersistenceRequired();
            self.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 0, false, false));
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void titan$driveToPump(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        CompoundTag data = self.getPersistentData();
        if (!data.getBoolean(WaveController.WAVE_MOB_TAG) || !data.contains(WaveController.PUMP_POS_TAG)) {
            return;
        }
        // 每 20 tick 强制朝泵寻路一次，增强「攻击泵」的欲望（远离泵时才重寻路，贴近时留给削减完整度）。
        if (self.tickCount % 20 != 0) {
            return;
        }
        BlockPos pump = BlockPos.of(data.getLong(WaveController.PUMP_POS_TAG));
        double dx = pump.getX() + 0.5 - self.getX();
        double dz = pump.getZ() + 0.5 - self.getZ();
        if (dx * dx + dz * dz > 4.0) {
            self.getNavigation().moveTo(pump.getX() + 0.5, pump.getY(), pump.getZ() + 0.5, 1.3D);
        }
    }
}
