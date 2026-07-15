package com.tonywww.titan_moon.curio;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.config.TMConfig;
import com.tonywww.titan_moon.registry.TMItems;
import com.tonywww.titan_moon.registry.TMMobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
//? if forge {
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
*///?}

/**
 * 毒腺手套的战斗事件处理：佩戴者造成「物理」（不无视护甲）伤害时，按配置概率给目标附加托林毒素。
 * 真实伤害 / sonicBoom 等 {@code BYPASSES_ARMOR} 伤害不触发（托林毒素自身用 sonicBoom 伤害源，故不会自触发死循环）。
 *
 * <p>伤害事件类型两版不同（Forge {@code LivingHurtEvent} / NeoForge {@code LivingIncomingDamageEvent}）→ Stonecutter
 * 隔离；判定逻辑与 Curios 装备查询（{@code CuriosApi.getCuriosInventory().findFirstCurio}）两版一致 → 共享 {@link #handleHit}。
 * 监听游戏（Forge/Game）总线：Forge 显式 {@code bus = FORGE}，NeoForge 沿用 {@code @EventBusSubscriber} 默认。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//?} else {
/*@EventBusSubscriber(modid = TitanMoon.MODID, bus = EventBusSubscriber.Bus.GAME)
*///?}
public final class CurioCombatHandler {

    private CurioCombatHandler() {
    }

    //? if forge {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        handleHit(event.getEntity(), event.getSource());
    }
    //?} else {
    /*@SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        handleHit(event.getEntity(), event.getSource());
    }
    *///?}

    private static void handleHit(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide() || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        Item glove = TMItems.THOLIN_VENOM_GLOVE.get();
        boolean equipped = CuriosApi.getCuriosInventory(attacker)
                .map(handler -> handler.findFirstCurio(glove).isPresent())
                .orElse(false);
        if (!equipped) {
            return;
        }
        if (attacker.getRandom().nextDouble() >= TMConfig.TOXIN_GLOVE_CHANCE.get()) {
            return;
        }
        victim.addEffect(TMMobEffects.tholinToxin(TMConfig.TOXIN_GLOVE_DURATION.get(), TMConfig.TOXIN_GLOVE_AMPLIFIER.get()));
    }
}
