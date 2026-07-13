package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
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

    // 液态甲烷（CH₄，现实中几乎无色透明/LNG）：极寒、密度低于水；极淡暖黄色 + 半透明液面（贴近无色的液态烃）。
    public static final RegistryObject<FluidType> LIQUID_METHANE = REGISTER.register("liquid_methane",
            () -> tinted(0xE7E3B8, 0x99, FluidType.Properties.create()
                    .density(450).viscosity(1200).temperature(90)
                    .canSwim(true).canDrown(true)));

    // 液态氨（NH₃，现实中几乎无色）：极淡冷青色 + 半透明液面（贴近无色的冷冽液体）。
    public static final RegistryObject<FluidType> LIQUID_AMMONIA = REGISTER.register("liquid_ammonia",
            () -> tinted(0xBAE8E4, 0x99, FluidType.Properties.create()
                    .density(680).viscosity(1100).temperature(240)
                    .canSwim(true).canDrown(true)));

    /**
     * @param rgb        RGB 色（0xRRGGBB，不含 alpha）
     * @param fluidAlpha 世界液面透明度（0x00 全透明 ~ 0xFF 不透明）；桶物品固定不透明以便辨识。
     */
    private static FluidType tinted(int rgb, int fluidAlpha, FluidType.Properties properties) {
        final int worldColor = (fluidAlpha << 24) | (rgb & 0xFFFFFF); // 世界里半透明液面
        final int itemColor = 0xFF000000 | (rgb & 0xFFFFFF);          // 桶物品用不透明色，看得清
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

                    // 桶物品：DynamicFluidContainerModel.Colors 走 getTintColor(FluidStack) → 默认回退到此无参重载
                    @Override
                    public int getTintColor() {
                        return itemColor;
                    }

                    // 世界液面：LiquidBlockRenderer 走此三参重载，返回半透明色
                    @Override
                    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                        return worldColor;
                    }
                });
            }
        };
    }
}
