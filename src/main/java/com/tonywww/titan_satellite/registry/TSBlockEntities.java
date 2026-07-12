package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.blockentity.SpecialMethanePumpBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块实体注册表。当前仅甲烷泵（开采塔防状态机由 M4 / PE-2 填充）。
 */
public final class TSBlockEntities {

    private TSBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TitanSatellite.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<SpecialMethanePumpBlockEntity>> METHANE_PUMP =
            REGISTER.register("methane_pump", () ->
                    BlockEntityType.Builder.of(SpecialMethanePumpBlockEntity::new, TSBlocks.SPECIAL_METHANE_PUMP.get())
                            .build(null));
}
