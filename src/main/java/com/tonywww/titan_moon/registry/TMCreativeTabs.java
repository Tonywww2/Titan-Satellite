package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 创造模式物品栏：聚合本 mod 的方块与物品。
 */
public final class TMCreativeTabs {

    private TMCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TitanMoon.MODID);

    public static final Supplier<CreativeModeTab> TITAN = REGISTER.register("titan", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TitanMoon.MODID + ".titan"))
                    .icon(() -> new ItemStack(TMItems.AERO_MEMBRANE.get()))
                    .displayItems((params, output) -> {
                        output.accept(TMItems.AERO_MEMBRANE.get());
                        // 基础地形方块
                        output.accept(TMBlocks.TITAN_STONE.get());
                        output.accept(TMBlocks.TITAN_BASALT.get());
                        output.accept(TMBlocks.THOLIN_SAND.get());
                        output.accept(TMBlocks.CRUSHED_ICE.get());
                        output.accept(TMBlocks.CRYO_ICE.get());
                        output.accept(TMBlocks.PACKED_METHANE_ICE.get());
                        // 特殊方块
                        output.accept(TMBlocks.CRYOVOLCANIC_GEYSER.get());
                        output.accept(TMBlocks.METHANE_POOL_CORE.get());
                        output.accept(TMBlocks.SPECIAL_METHANE_PUMP.get());
                        output.accept(TMBlocks.THOLIN_CRYSTAL.get());
                        // 新增表层/装饰块
                        output.accept(TMBlocks.WEATHERED_TITAN_STONE.get());
                        output.accept(TMBlocks.SEDIMENTARY_TITAN_STONE.get());
                        output.accept(TMBlocks.BRANCH_CRYSTAL.get());
                        // 装饰地物新增方块
                        output.accept(TMBlocks.ABYSS_CRYSTAL.get());
                        output.accept(TMBlocks.THOLIN_TAR.get());
                        output.accept(TMBlocks.METEOR_FRAGMENT.get());
                        output.accept(TMBlocks.HARDENED_THOLIN.get());
                        output.accept(TMBlocks.THOLIN_SHRUB.get());
                        output.accept(TMBlocks.METHANE_ICE_BLOOM.get());
                        output.accept(TMBlocks.AMMONIA_CRYSTAL.get());
                        output.accept(TMBlocks.TITAN_GRAVEL.get());
                        output.accept(TMBlocks.FROST_BUSH.get());
                        // 宏伟地物构成方块
                        output.accept(TMBlocks.ACETYLENE_SPIRE.get());
                        output.accept(TMBlocks.THOLIN_MYCELIUM.get());
                        output.accept(TMBlocks.HYDROGEN_BUBBLE_MAT.get());
                        // 材料加工链被动生产方块
                        output.accept(TMBlocks.HYDROGEN_COLLECTOR.get());
                        output.accept(TMBlocks.THOLIN_COMPOSTER.get());
                        // 流体桶
                        output.accept(TMItems.LIQUID_METHANE_BUCKET.get());
                        output.accept(TMItems.LIQUID_AMMONIA_BUCKET.get());
                        output.accept(TMItems.LIQUID_HYDROGEN_BUCKET.get());
                        // 生物掉落材料
                        output.accept(TMItems.CRYO_CARAPACE.get());
                        output.accept(TMItems.TOXIC_GLAND.get());
                        output.accept(TMItems.DEPLETED_BATTERY.get());
                        output.accept(TMItems.PRECISION_COMPONENTS.get());
                        // 生态深化掉落材料
                        output.accept(TMItems.CRYSTALLINE_TWIG.get());
                        output.accept(TMItems.THOLIN_FIBRE.get());
                        output.accept(TMItems.TOUGH_NEURAL_GLAND.get());
                        output.accept(TMItems.THOLIN_SILK_SAC.get());
                        // 材料加工链
                        output.accept(TMItems.THOLIN_DUST.get());
                        output.accept(TMItems.CONDENSED_ACETYLENE.get());
                        output.accept(TMItems.HYDROGEN_CAPSULE.get());
                        output.accept(TMItems.METEORIC_IRON_INGOT.get());
                        output.accept(TMItems.SILICON_DUST.get());
                        output.accept(TMItems.METHANE_ICE_SHARD.get());
                        output.accept(TMItems.ABYSS_CRYSTAL_DUST.get());
                        output.accept(TMItems.AMMONIA_SALT.get());
                        output.accept(TMItems.THOLIN_CRYSTAL_DUST.get());
                        output.accept(TMItems.POLYPHOSPHAZENE_COENZYME.get());
                        output.accept(TMItems.AZOTOSOME_SHEET.get());
                        output.accept(TMItems.CRYO_ALLOY_INGOT.get());
                        output.accept(TMItems.BIO_BATTERY.get());
                        output.accept(TMItems.TITAN_ANTIDOTE.get());
                        // 饰品（Curios 扩展坞系列）
                        output.accept(TMItems.CRYO_ALLOY_RING_MOUNT.get());
                        output.accept(TMItems.THOLIN_FIBRE_BELT_RIG.get());
                        output.accept(TMItems.METEORIC_IRON_BACK_FRAME.get());
                        output.accept(TMItems.THOLIN_LIFE_SUPPORT_KIT.get());
                        output.accept(TMItems.THOLIN_VENOM_GLOVE.get());
                        // 刷怪蛋
                        output.accept(TMItems.AERO_JELLY_SPAWN_EGG.get());
                        output.accept(TMItems.CRYO_SCAVENGER_SPAWN_EGG.get());
                        output.accept(TMItems.AMMONIA_STALKER_SPAWN_EGG.get());
                        output.accept(TMItems.CORRUPTED_PROBE_SPAWN_EGG.get());
                        output.accept(TMItems.THOLIN_WEAVER_SPAWN_EGG.get());
                        output.accept(TMItems.NATIVE_ICE_WORM_SPAWN_EGG.get());
                        output.accept(TMItems.METHANE_MIDGE_SPAWN_EGG.get());
                        output.accept(TMItems.HYDROTROPH_GRAZER_SPAWN_EGG.get());
                    })
                    .build());
}
