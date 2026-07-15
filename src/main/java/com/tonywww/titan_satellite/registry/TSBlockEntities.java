package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.blockentity.HydrogenCollectorBlockEntity;
import com.tonywww.titan_satellite.blockentity.SpecialMethanePumpBlockEntity;
import com.tonywww.titan_satellite.blockentity.TholinComposterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 方块实体注册表。
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

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<HydrogenCollectorBlockEntity>> HYDROGEN_COLLECTOR =
            REGISTER.register("hydrogen_collector", () ->
                    BlockEntityType.Builder.of(HydrogenCollectorBlockEntity::new, TSBlocks.HYDROGEN_COLLECTOR.get())
                            .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TholinComposterBlockEntity>> THOLIN_COMPOSTER =
            REGISTER.register("tholin_composter", () ->
                    BlockEntityType.Builder.of(TholinComposterBlockEntity::new, TSBlocks.THOLIN_COMPOSTER.get())
                            .build(null));
}
