package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.blockentity.SpecialMethanePumpBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 方块实体注册表。当前仅甲烷泵（开采塔防状态机由 M4 / PE-2 填充）。
 */
public final class TSBlockEntities {

    private TSBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TitanSatellite.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<SpecialMethanePumpBlockEntity>> METHANE_PUMP =
            REGISTER.register("methane_pump", () ->
                    BlockEntityType.Builder.of(SpecialMethanePumpBlockEntity::new, TSBlocks.SPECIAL_METHANE_PUMP.get())
                            .build(null));
}
