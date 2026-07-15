package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.blockentity.HydrogenCollectorBlockEntity;
import com.tonywww.titan_moon.blockentity.SpecialMethanePumpBlockEntity;
import com.tonywww.titan_moon.blockentity.TholinComposterBlockEntity;
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
public final class TMBlockEntities {

    private TMBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TitanMoon.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<SpecialMethanePumpBlockEntity>> METHANE_PUMP =
            REGISTER.register("methane_pump", () ->
                    BlockEntityType.Builder.of(SpecialMethanePumpBlockEntity::new, TMBlocks.SPECIAL_METHANE_PUMP.get())
                            .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<HydrogenCollectorBlockEntity>> HYDROGEN_COLLECTOR =
            REGISTER.register("hydrogen_collector", () ->
                    BlockEntityType.Builder.of(HydrogenCollectorBlockEntity::new, TMBlocks.HYDROGEN_COLLECTOR.get())
                            .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TholinComposterBlockEntity>> THOLIN_COMPOSTER =
            REGISTER.register("tholin_composter", () ->
                    BlockEntityType.Builder.of(TholinComposterBlockEntity::new, TMBlocks.THOLIN_COMPOSTER.get())
                            .build(null));
}
