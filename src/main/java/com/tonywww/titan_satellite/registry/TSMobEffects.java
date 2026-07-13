package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
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

    // 托林毒素 / 异星毒素（凋零式持续扣血；氨泉掠食者攻击 / 织体蛛吐丝 / 晶洞毒气）。
    public static final RegistryObject<MobEffect> THOLIN_TOXIN = REGISTER.register("tholin_toxin",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xB0822E) {
                @Override
                public void applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().magic(), 1.0F);
                }

                @Override
                public boolean isDurationEffectTick(int duration, int amplifier) {
                    int interval = 40 >> amplifier;
                    return interval <= 0 || duration % interval == 0;
                }
            });
}
