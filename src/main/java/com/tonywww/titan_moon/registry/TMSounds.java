package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 音效注册表：土卫六流体相关的自定义音效事件。
 * 由 {@link TitanMoon} 主类在构造阶段装配到 mod 总线；具体音频在 assets/titan_moon/sounds.json 中映射
 * （当前复用原版 ogg 作占位，可播放）。
 */
public final class TMSounds {

    private TMSounds() {
    }

    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, TitanMoon.MODID);

    /** 流体接触速冻成冰时的音效。 */
    public static final Supplier<SoundEvent> FLUID_SOLIDIFY = REGISTER.register("block.titan_fluid.solidify",
            () -> SoundEvent.createVariableRangeEvent(
                    TitanMoon.rl("block.titan_fluid.solidify")));
}
