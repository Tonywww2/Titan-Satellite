package com.tonywww.titan_satellite.curio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if forge {
import java.util.UUID;
//?} else {
/*import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
*///?}
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 「扩展坞」饰品：放入通用 {@code curio} 饰品栏后，为佩戴者授予 {@code amount} 个 {@code targetSlot}
 * 类型的额外饰品栏（例如 ring / belt / back）。取下即收回该栏。
 *
 * <p>实现方式：物品直接 {@code implements ICurioItem}，Curios 会自动为其挂载 ICurio 能力（两版通用，
 * 无需 Forge 的 initCapabilities 或 NeoForge 的能力注册）。授槽通过 {@link SlotAttribute} 修饰符实现——
 * 正的槽位修饰符会让 base 为 0 的目标栏动态出现，无需全局改动其它 mod 的槽位尺寸。
 *
 * <p>两版 Curios API 差异用 Stonecutter 隔离：
 * <ul>
 *   <li>1.20.1(Curios 5.x)：{@code getAttributeModifiers(SlotContext, UUID, ItemStack)} 返回
 *       {@code Multimap<Attribute, AttributeModifier>}；{@code SlotAttribute.getOrCreate} 返回
 *       {@code SlotAttribute}；修饰符构造 {@code (UUID, name, amount, ADDITION)}。</li>
 *   <li>1.21.1(Curios 9.x)：{@code getAttributeModifiers(SlotContext, ResourceLocation, ItemStack)} 返回
 *       {@code Multimap<Holder<Attribute>, AttributeModifier>}；{@code SlotAttribute.getOrCreate} 返回
 *       {@code Holder<Attribute>}；修饰符构造 {@code (ResourceLocation, amount, ADD_VALUE)}。</li>
 * </ul>
 */
public class SlotExpanderCurioItem extends Item implements ICurioItem {

    private final String targetSlot;
    private final int amount;

    public SlotExpanderCurioItem(Properties properties, String targetSlot, int amount) {
        super(properties);
        this.targetSlot = targetSlot;
        this.amount = amount;
    }

    //? if forge {
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(SlotAttribute.getOrCreate(this.targetSlot),
                new AttributeModifier(uuid, "titan_satellite:slot_expander", this.amount, AttributeModifier.Operation.ADDITION));
        return map;
    }
    //?} else {
    /*@Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        map.put(SlotAttribute.getOrCreate(this.targetSlot),
                new AttributeModifier(id, this.amount, AttributeModifier.Operation.ADD_VALUE));
        return map;
    }
    *///?}
}
