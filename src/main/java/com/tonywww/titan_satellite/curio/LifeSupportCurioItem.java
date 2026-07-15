package com.tonywww.titan_satellite.curio;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 「维生套件」饰品：佩戴时每隔 {@code intervalTicks} tick 为佩戴者回复
 * {@code flatHeal + maxHealth * percentHeal} 点生命（仅服务端结算）。
 *
 * <p>纯共享代码：{@code curioTick(SlotContext, ItemStack)} 及用到的 {@link LivingEntity} API
 * 在 Curios 5.x / 9.x 两版签名一致，无需 Stonecutter 隔离。
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
}
