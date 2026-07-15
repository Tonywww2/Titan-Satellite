package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
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
 * 生物效果注册表。
 */
public final class TMMobEffects {

    private TMMobEffects() {
    }

    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(Registries.MOB_EFFECT, TitanMoon.MODID);

    // 托林毒素 / 异星毒素（凋零式持续扣血；氨泉掠食者攻击 / 织体蛛吐丝 / 晶洞毒气）。
    public static final Supplier<MobEffect> THOLIN_TOXIN = REGISTER.register("tholin_toxin",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xB0822E) {
                //? if forge {
                @Override
                public void applyEffectTick(LivingEntity entity, int amplifier) {
                    float damage = (amplifier + 1) + entity.getHealth() * 0.005F;
                    // 造成伤害前清除目标无敌帧，确保托林毒素每跳都实打（不被 i-frames 减免/挡下）
                    entity.invulnerableTime = 0;
                    entity.hurt(entity.damageSources().sonicBoom(entity), damage);
                }

                @Override
                public boolean isDurationEffectTick(int duration, int amplifier) {
                    int interval = 40 >> amplifier;
                    return interval <= 0 || duration % interval == 0;
                }
                //?} else {
                /*@Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    float damage = (amplifier + 1) + entity.getHealth() * 0.005F;
                    // 造成伤害前清除目标无敌帧，确保托林毒素每跳都实打（不被 i-frames 减免/挡下）
                    entity.invulnerableTime = 0;
                    entity.hurt(entity.damageSources().sonicBoom(entity), damage);
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
