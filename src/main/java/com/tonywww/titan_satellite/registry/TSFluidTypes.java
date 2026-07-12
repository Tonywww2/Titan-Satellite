package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * 流体类型（FluidType）注册表：定义液态甲烷、液态氨的物理与客户端渲染属性。
 * 客户端贴图/染色在 {@link #tinted} 里通过 initializeClient 提供（仅客户端加载，
 * 复用原版 water 贴图并按染色区分）。
 */
public final class TSFluidTypes {

    private TSFluidTypes() {
    }

    public static final DeferredRegister<FluidType> REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TitanSatellite.MODID);

    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("block/water_still");
    private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("block/water_flow");

    // 液态甲烷：极寒、密度低于水、琥珀色染色。
    public static final RegistryObject<FluidType> LIQUID_METHANE = REGISTER.register("liquid_methane",
            () -> tinted(0xFFB0822E, FluidType.Properties.create()
                    .density(450).viscosity(1200).temperature(90)
                    .canSwim(true).canDrown(true)));

    // 液态氨：淡蓝色染色。
    public static final RegistryObject<FluidType> LIQUID_AMMONIA = REGISTER.register("liquid_ammonia",
            () -> tinted(0xFF9FC9E8, FluidType.Properties.create()
                    .density(680).viscosity(1100).temperature(240)
                    .canSwim(true).canDrown(true)));

    private static FluidType tinted(int tintARGB, FluidType.Properties properties) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL_TEXTURE;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOWING_TEXTURE;
                    }

                    @Override
                    public int getTintColor() {
                        return tintARGB;
                    }
                });
            }
        };
    }
}
