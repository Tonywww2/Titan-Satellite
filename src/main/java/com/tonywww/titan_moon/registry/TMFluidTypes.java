package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.resources.ResourceLocation;
//? if forge {
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Consumer;
//?} else {
/*import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
*///?}
import java.util.function.Supplier;

/**
 * 流体类型（FluidType）注册表：定义液态甲烷、液态氨的物理与客户端渲染属性。
 * 客户端贴图/染色在 {@link #tinted} 里通过 initializeClient 提供（仅客户端加载，
 * 复用原版 water 贴图并按染色区分）。
 */
public final class TMFluidTypes {

    private TMFluidTypes() {
    }

    //? if forge {
    public static final DeferredRegister<FluidType> REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TitanMoon.MODID);
    //?} else {
    /*public static final DeferredRegister<FluidType> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TitanMoon.MODID);
    *///?}

    /** 三种流体的染色基色（0xRRGGBB，不含 alpha）；Forge 经 {@link #tinted} 使用，
     *  NeoForge 经 {@code TitanClientEvents.fluidExt} 复用同一组值，避免两处平台分支色值漂移。 */
    public static final int COLOR_METHANE = 0xE7E3B8;
    public static final int COLOR_AMMONIA = 0xBAE8E4;
    public static final int COLOR_HYDROGEN = 0xCFE8F0;
    /** 世界液面透明度（0x00 全透明 ~ 0xFF 不透明）；桶物品固定不透明以便辨识。 */
    public static final int WORLD_ALPHA = 0x99;

    /** 流体贴图：复用原版 water 静止/流动贴图（再按 {@link #tinted} 染色）。 */
    public static final ResourceLocation STILL_TEXTURE = TitanMoon.parse("block/water_still");
    public static final ResourceLocation FLOWING_TEXTURE = TitanMoon.parse("block/water_flow");

    // 液态甲烷（CH₄，现实中几乎无色透明/LNG）：极寒、密度低于水；极淡暖黄色 + 半透明液面（贴近无色的液态烃）。
    public static final Supplier<FluidType> LIQUID_METHANE = REGISTER.register("liquid_methane",
            () -> tinted(COLOR_METHANE, WORLD_ALPHA, FluidType.Properties.create()
                    .density(450).viscosity(1200).temperature(90)
                    .canSwim(true).canDrown(true)));

    // 液态氨（NH₃，现实中几乎无色）：极淡冷青色 + 半透明液面（贴近无色的冷冽液体）。
    public static final Supplier<FluidType> LIQUID_AMMONIA = REGISTER.register("liquid_ammonia",
            () -> tinted(COLOR_AMMONIA, WORLD_ALPHA, FluidType.Properties.create()
                    .density(680).viscosity(1100).temperature(240)
                    .canSwim(true).canDrown(true)));

    // 液氢（H₂，深冷液体）：极淡冰蓝、密度极低、极寒；集氢罩被动产出，经 forge:hydrogen/c:hydrogen tag 接入 Mek 转化炉。
    public static final Supplier<FluidType> LIQUID_HYDROGEN = REGISTER.register("liquid_hydrogen",
            () -> tinted(COLOR_HYDROGEN, WORLD_ALPHA, FluidType.Properties.create()
                    .density(70).viscosity(200).temperature(20)
                    .canSwim(false).canDrown(false)));

    /**
     * @param rgb        RGB 色（0xRRGGBB，不含 alpha）
     * @param fluidAlpha 世界液面透明度（0x00 全透明 ~ 0xFF 不透明）；桶物品固定不透明以便辨识。
     */
    private static FluidType tinted(int rgb, int fluidAlpha, FluidType.Properties properties) {
        final int worldColor = (fluidAlpha << 24) | (rgb & 0xFFFFFF); // 世界里半透明液面
        final int itemColor = 0xFF000000 | (rgb & 0xFFFFFF);          // 桶物品用不透明色，看得清
        //? if forge {
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
        //?} else {
        /*// NeoForge：FluidType 无 initializeClient；客户端染色由 RegisterClientExtensionsEvent 注册（阶段4）。
        return new FluidType(properties);
        *///?}
    }
}
