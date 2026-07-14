package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 生物效果注册表（桩效果）。具体作用由 M4（毒气）填充监听逻辑。
 */
public final class TSMobEffects {

    private TSMobEffects() {
    }

    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(Registries.MOB_EFFECT, TitanSatellite.MODID);

    // 托林毒素 / 异星毒素（凋零式持续扣血；氨泉掠食者攻击 / 织体蛛吐丝 / 晶洞毒气）。
    public static final Supplier<MobEffect> THOLIN_TOXIN = REGISTER.register("tholin_toxin",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xB0822E) {
                //? if forge {
                @Override
                public void applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().sonicBoom(entity), 1.0F);
                }

                @Override
                public boolean isDurationEffectTick(int duration, int amplifier) {
                    int interval = 40 >> amplifier;
                    return interval <= 0 || duration % interval == 0;
                }
                //?} else {
                /*@Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().sonicBoom(entity), 1.0F);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    int interval = 40 >> amplifier;
                    return interval <= 0 || duration % interval == 0;
                }
                *///?}
            });

    /** 构造托林毒素效果实例（Forge 用 MobEffect / NeoForge 1.21 用 Holder<MobEffect>）。 */
    public static MobEffectInstance tholinToxin(int duration, int amplifier) {
        //? if forge {
        return new MobEffectInstance(THOLIN_TOXIN.get(), duration, amplifier);
        //?} else {
        /*return new MobEffectInstance(
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(THOLIN_TOXIN.get()),
                duration, amplifier);
        *///?}
    }
}
