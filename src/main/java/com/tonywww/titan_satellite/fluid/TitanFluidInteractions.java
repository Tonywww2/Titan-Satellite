package com.tonywww.titan_satellite.fluid;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSFluidTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
//? if forge {
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//?} else {
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.neoforged.neoforge.fluids.FluidType;
*///?}

/**
 * PF-3 流体交互补全 + 自装配。液态甲烷与液态氨接触时互相「速冻」成冰，并给出音效与粒子反馈。
 * 经 {@code @EventBusSubscriber} 自订阅（不改冻结主类）：mod 构造期挂 {@link TitanSounds} 注册表，
 * 通用初始化期登记 {@link FluidInteractionRegistry} 交互。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanSatellite.MODID)
*///?}
public final class TitanFluidInteractions {

    private TitanFluidInteractions() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(TitanFluidInteractions::registerInteractions);
    }

    private static void registerInteractions() {
        // 液态甲烷遇液态氨 → 甲烷侧速冻（源=封装甲烷冰，流动=碎冰）
        addSolidify(TSFluidTypes.LIQUID_METHANE.get(), TSFluidTypes.LIQUID_AMMONIA.get(),
                TSBlocks.PACKED_METHANE_ICE.get().defaultBlockState(),
                TSBlocks.CRUSHED_ICE.get().defaultBlockState());
        // 液态氨遇液态甲烷 → 氨侧速冻（源=低温冰，流动=碎冰）
        addSolidify(TSFluidTypes.LIQUID_AMMONIA.get(), TSFluidTypes.LIQUID_METHANE.get(),
                TSBlocks.CRYO_ICE.get().defaultBlockState(),
                TSBlocks.CRUSHED_ICE.get().defaultBlockState());
    }

    /** 注册「source 流体在 neighbor 流体相邻时速冻成冰」的交互。 */
    private static void addSolidify(FluidType source, FluidType neighbor,
                                    BlockState sourceResult, BlockState flowingResult) {
        FluidInteractionRegistry.HasFluidInteraction predicate =
                (level, currentPos, relativePos, currentState) ->
                        level.getFluidState(relativePos).getFluidType() == neighbor;
        FluidInteractionRegistry.FluidInteraction action =
                (level, currentPos, relativePos, currentState) ->
                        solidify(level, currentPos, currentState, sourceResult, flowingResult);
        FluidInteractionRegistry.addInteraction(source, new InteractionInformation(predicate, action));
    }

    /** 把 source 流体所在方块替换为冰，并播放自定义速冻音效 + 冷凝云粒子。 */
    private static void solidify(Level level, BlockPos pos, FluidState currentState,
                                 BlockState sourceResult, BlockState flowingResult) {
        BlockState result = currentState.isSource() ? sourceResult : flowingResult;
        //? if forge {
        level.setBlockAndUpdate(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, result));
        //?} else {
        /*level.setBlockAndUpdate(pos, EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, result));
        *///?}
        level.playSound(null, pos, TitanSounds.FLUID_SOLIDIFY.get(), SoundSource.BLOCKS, 0.7F, 1.4F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.1D, 0.25D, 0.02D);
        }
    }
}
