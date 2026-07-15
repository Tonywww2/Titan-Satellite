package com.tonywww.titan_moon.curio;

import com.tonywww.titan_moon.config.TMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 「托林毒腺手套」饰品：本身无 per-tick 行为，仅作为 Curios（hands 槽）标记物。
 * 佩戴者造成物理伤害时按概率附加托林毒素的逻辑在 {@link CurioCombatHandler} 的伤害事件里处理。
 *
 * <p>tooltip 展示触发概率（读 {@link TMConfig#TOXIN_GLOVE_CHANCE}）；appendHoverText 第二参两版不同
 * （1.20.1 {@code Level} / 1.21.1 {@code Item.TooltipContext}）→ Stonecutter 隔离。
 */
public class ToxinGloveCurioItem extends Item implements ICurioItem {

    public ToxinGloveCurioItem(Properties properties) {
        super(properties);
    }

    //? if forge {
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        addDesc(tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        addDesc(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }
    *///?}

    private void addDesc(List<Component> tooltip) {
        int chance = (int) Math.round(TMConfig.TOXIN_GLOVE_CHANCE.get() * 100.0);
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc", chance).withStyle(ChatFormatting.GRAY));
    }
}
