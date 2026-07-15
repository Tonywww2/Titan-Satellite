package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
//? if forge {
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 流体（Fluid）注册表：每种液体有 Source + Flowing 两个 Fluid。
 * Properties 在方法里延迟构建（在注册阶段调用），避免与 {@link TSBlocks}/{@link TSItems}
 * 产生类初始化期的循环引用。
 */
public final class TSFluids {

    private TSFluids() {
    }

    public static final DeferredRegister<Fluid> REGISTER =
            DeferredRegister.create(Registries.FLUID, TitanSatellite.MODID);

    //? if forge {
    public static final Supplier<FlowingFluid> LIQUID_METHANE =
            REGISTER.register("liquid_methane", () -> new ForgeFlowingFluid.Source(methaneProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_METHANE =
            REGISTER.register("flowing_liquid_methane", () -> new ForgeFlowingFluid.Flowing(methaneProperties()));

    public static final Supplier<FlowingFluid> LIQUID_AMMONIA =
            REGISTER.register("liquid_ammonia", () -> new ForgeFlowingFluid.Source(ammoniaProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_AMMONIA =
            REGISTER.register("flowing_liquid_ammonia", () -> new ForgeFlowingFluid.Flowing(ammoniaProperties()));

    public static final Supplier<FlowingFluid> LIQUID_HYDROGEN =
            REGISTER.register("liquid_hydrogen", () -> new ForgeFlowingFluid.Source(hydrogenProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_HYDROGEN =
            REGISTER.register("flowing_liquid_hydrogen", () -> new ForgeFlowingFluid.Flowing(hydrogenProperties()));

    private static ForgeFlowingFluid.Properties methaneProperties() {
        return new ForgeFlowingFluid.Properties(TSFluidTypes.LIQUID_METHANE, LIQUID_METHANE, FLOWING_LIQUID_METHANE)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .block(TSBlocks.LIQUID_METHANE_BLOCK)
                .bucket(TSItems.LIQUID_METHANE_BUCKET);
    }

    private static ForgeFlowingFluid.Properties ammoniaProperties() {
        return new ForgeFlowingFluid.Properties(TSFluidTypes.LIQUID_AMMONIA, LIQUID_AMMONIA, FLOWING_LIQUID_AMMONIA)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(TSBlocks.LIQUID_AMMONIA_BLOCK)
                .bucket(TSItems.LIQUID_AMMONIA_BUCKET);
    }

    private static ForgeFlowingFluid.Properties hydrogenProperties() {
        return new ForgeFlowingFluid.Properties(TSFluidTypes.LIQUID_HYDROGEN, LIQUID_HYDROGEN, FLOWING_LIQUID_HYDROGEN)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(TSBlocks.LIQUID_HYDROGEN_BLOCK)
                .bucket(TSItems.LIQUID_HYDROGEN_BUCKET);
    }
    //?} else {
    /*public static final Supplier<FlowingFluid> LIQUID_METHANE =
            REGISTER.register("liquid_methane", () -> new BaseFlowingFluid.Source(methaneProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_METHANE =
            REGISTER.register("flowing_liquid_methane", () -> new BaseFlowingFluid.Flowing(methaneProperties()));

    public static final Supplier<FlowingFluid> LIQUID_AMMONIA =
            REGISTER.register("liquid_ammonia", () -> new BaseFlowingFluid.Source(ammoniaProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_AMMONIA =
            REGISTER.register("flowing_liquid_ammonia", () -> new BaseFlowingFluid.Flowing(ammoniaProperties()));

    public static final Supplier<FlowingFluid> LIQUID_HYDROGEN =
            REGISTER.register("liquid_hydrogen", () -> new BaseFlowingFluid.Source(hydrogenProperties()));
    public static final Supplier<FlowingFluid> FLOWING_LIQUID_HYDROGEN =
            REGISTER.register("flowing_liquid_hydrogen", () -> new BaseFlowingFluid.Flowing(hydrogenProperties()));

    private static BaseFlowingFluid.Properties methaneProperties() {
        return new BaseFlowingFluid.Properties(TSFluidTypes.LIQUID_METHANE, LIQUID_METHANE, FLOWING_LIQUID_METHANE)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .block(TSBlocks.LIQUID_METHANE_BLOCK)
                .bucket(TSItems.LIQUID_METHANE_BUCKET);
    }

    private static BaseFlowingFluid.Properties ammoniaProperties() {
        return new BaseFlowingFluid.Properties(TSFluidTypes.LIQUID_AMMONIA, LIQUID_AMMONIA, FLOWING_LIQUID_AMMONIA)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(TSBlocks.LIQUID_AMMONIA_BLOCK)
                .bucket(TSItems.LIQUID_AMMONIA_BUCKET);
    }

    private static BaseFlowingFluid.Properties hydrogenProperties() {
        return new BaseFlowingFluid.Properties(TSFluidTypes.LIQUID_HYDROGEN, LIQUID_HYDROGEN, FLOWING_LIQUID_HYDROGEN)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(TSBlocks.LIQUID_HYDROGEN_BLOCK)
                .bucket(TSItems.LIQUID_HYDROGEN_BUCKET);
    }
    *///?}
}
