package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
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
public final class TSCreativeTabs {

    private TSCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TitanSatellite.MODID);

    public static final Supplier<CreativeModeTab> TITAN = REGISTER.register("titan", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TitanSatellite.MODID + ".titan"))
                    .icon(() -> new ItemStack(TSItems.AERO_MEMBRANE.get()))
                    .displayItems((params, output) -> {
                        output.accept(TSItems.AERO_MEMBRANE.get());
                        // 基础地形方块
                        output.accept(TSBlocks.TITAN_STONE.get());
                        output.accept(TSBlocks.TITAN_BASALT.get());
                        output.accept(TSBlocks.THOLIN_SAND.get());
                        output.accept(TSBlocks.CRUSHED_ICE.get());
                        output.accept(TSBlocks.CRYO_ICE.get());
                        output.accept(TSBlocks.PACKED_METHANE_ICE.get());
                        // 特殊方块
                        output.accept(TSBlocks.CRYOVOLCANIC_GEYSER.get());
                        output.accept(TSBlocks.METHANE_POOL_CORE.get());
                        output.accept(TSBlocks.SPECIAL_METHANE_PUMP.get());
                        output.accept(TSBlocks.THOLIN_CRYSTAL.get());
                        // M6 新增表层/装饰块
                        output.accept(TSBlocks.WEATHERED_TITAN_STONE.get());
                        output.accept(TSBlocks.SEDIMENTARY_TITAN_STONE.get());
                        output.accept(TSBlocks.BRANCH_CRYSTAL.get());
                        // 装饰地物新增方块（CR-15）
                        output.accept(TSBlocks.ABYSS_CRYSTAL.get());
                        output.accept(TSBlocks.THOLIN_TAR.get());
                        output.accept(TSBlocks.METEOR_FRAGMENT.get());
                        output.accept(TSBlocks.HARDENED_THOLIN.get());
                        output.accept(TSBlocks.THOLIN_SHRUB.get());
                        output.accept(TSBlocks.METHANE_ICE_BLOOM.get());
                        output.accept(TSBlocks.AMMONIA_CRYSTAL.get());
                        output.accept(TSBlocks.TITAN_GRAVEL.get());
                        output.accept(TSBlocks.FROST_BUSH.get());
                        // M7 宏伟地物构成方块
                        output.accept(TSBlocks.ACETYLENE_SPIRE.get());
                        output.accept(TSBlocks.THOLIN_MYCELIUM.get());
                        // 流体桶
                        output.accept(TSItems.LIQUID_METHANE_BUCKET.get());
                        output.accept(TSItems.LIQUID_AMMONIA_BUCKET.get());
                        // 生物掉落材料
                        output.accept(TSItems.CRYO_CARAPACE.get());
                        output.accept(TSItems.TOXIC_GLAND.get());
                        output.accept(TSItems.DEPLETED_BATTERY.get());
                        output.accept(TSItems.PRECISION_COMPONENTS.get());
                        // 生态深化掉落材料（ECO）
                        output.accept(TSItems.CRYSTALLINE_TWIG.get());
                        output.accept(TSItems.THOLIN_FIBRE.get());
                        output.accept(TSItems.TOUGH_NEURAL_GLAND.get());
                        output.accept(TSItems.THOLIN_SILK_SAC.get());
                        // 刷怪蛋
                        output.accept(TSItems.AERO_JELLY_SPAWN_EGG.get());
                        output.accept(TSItems.CRYO_SCAVENGER_SPAWN_EGG.get());
                        output.accept(TSItems.AMMONIA_STALKER_SPAWN_EGG.get());
                        output.accept(TSItems.CORRUPTED_PROBE_SPAWN_EGG.get());
                        output.accept(TSItems.THOLIN_WEAVER_SPAWN_EGG.get());
                        output.accept(TSItems.NATIVE_ICE_WORM_SPAWN_EGG.get());
                        output.accept(TSItems.METHANE_MIDGE_SPAWN_EGG.get());
                        output.accept(TSItems.HYDROTROPH_GRAZER_SPAWN_EGG.get());
                    })
                    .build());
}
