package com.tonywww.titan_satellite.worldgen.structure;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

/**
 * 结构类型 / 结构块类型注册（PF-1）。用<b>自订阅</b>挂到 mod 总线（{@link FMLConstructModEvent}），
 * 不改主类，与 PA-2 的 {@code TSSystemsBootstrap} 同模式（遵 §2.5）。
 *
 * <p>托林晶洞 / 先驱前哨站共用同一参数化 {@link TitanStructure}（variant 区分），故只需一个
 * {@link StructureType} + 一个 {@link StructurePieceType}；结构本体在 {@code worldgen/structure/*.json}
 * 声明 variant，程序化建造在 {@link TitanStructurePiece}。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanSatellite.MODID)
*///?}
public final class TSStructures {

    private TSStructures() {
    }

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TitanSatellite.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, TitanSatellite.MODID);

    public static final Supplier<StructureType<TitanStructure>> TITAN_STRUCTURE =
            STRUCTURE_TYPES.register("titan_structure", TSStructures::titanStructureType);

    public static final Supplier<StructurePieceType> TITAN_PIECE =
            PIECE_TYPES.register("titan_piece", () -> TitanStructurePiece::new);

    private static StructureType<TitanStructure> titanStructureType() {
        return () -> TitanStructure.CODEC;
    }

    /** 供 NeoForge 主类构造器调用（Forge 走下面 onConstruct 自装配）。 */
    public static void register(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        PIECE_TYPES.register(modBus);
    }

    //? if forge {
    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        register(modBus);
    }
    //?}
}
