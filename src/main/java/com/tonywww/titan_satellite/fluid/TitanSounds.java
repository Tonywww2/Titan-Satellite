package com.tonywww.titan_satellite.fluid;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * PF-3 音效注册表：土卫六流体相关的自定义音效事件。
 * 经 {@link TitanFluidInteractions} 在 mod 构造阶段自订阅装配到 mod 总线（不改冻结主类）。
 * 具体音频在 assets/titan_satellite/sounds.json 中映射（当前复用原版 ogg 作占位，实测可播放）。
 */
public final class TitanSounds {

    private TitanSounds() {
    }

    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TitanSatellite.MODID);

    /** 流体接触速冻成冰时的音效。 */
    public static final RegistryObject<SoundEvent> FLUID_SOLIDIFY = REGISTER.register("block.titan_fluid.solidify",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(TitanSatellite.MODID, "block.titan_fluid.solidify")));
}
