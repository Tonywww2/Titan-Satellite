package com.tonywww.titan_satellite.curio;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 「维生套件」饰品：佩戴时每隔 {@code intervalTicks} tick 为佩戴者回复
 * {@code flatHeal + maxHealth * percentHeal} 点生命（仅服务端结算）。
 *
 * <p>{@code curioTick(SlotContext, ItemStack)} 及用到的 {@link LivingEntity} API 在 Curios 5.x / 9.x
 * 两版签名一致（共享）；仅 {@code appendHoverText} 的第二参在 1.20.1（{@code Level}）与 1.21.1
 * （{@code Item.TooltipContext}）不同，用 Stonecutter 隔离。tooltip 文案走 {@code <descId>.desc}
 * 翻译键，动态填入 秒 / 固定量 / 百分比。
 */
public class LifeSupportCurioItem extends Item implements ICurioItem {

    private final int intervalTicks;
    private final float flatHeal;
    private final float percentHeal;

    public LifeSupportCurioItem(Properties properties, int intervalTicks, float flatHeal, float percentHeal) {
        super(properties);
        this.intervalTicks = intervalTicks;
        this.flatHeal = flatHeal;
        this.percentHeal = percentHeal;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (entity.tickCount % this.intervalTicks == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(this.flatHeal + entity.getMaxHealth() * this.percentHeal);
        }
    }

    //? if forge {
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc",
                this.intervalTicks / 20, (int) this.flatHeal, Math.round(this.percentHeal * 100.0F)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc",
                this.intervalTicks / 20, (int) this.flatHeal, Math.round(this.percentHeal * 100.0F)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
    *///?}
}
