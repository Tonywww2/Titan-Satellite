package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 生物效果注册表（桩效果）。具体作用由 M4（毒气）填充监听逻辑。
 */
public final class TSMobEffects {

    private TSMobEffects() {
    }

    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TitanSatellite.MODID);

    // 托林毒素（氨泉掉食者攻击 / 晶洞毒气）。
    public static final RegistryObject<MobEffect> THOLIN_TOXIN = REGISTER.register("tholin_toxin",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xB0822E) {
            });
}
