package com.tonywww.titan_satellite;

import com.mojang.logging.LogUtils;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSCreativeTabs;
import com.tonywww.titan_satellite.registry.TSEntities;
import com.tonywww.titan_satellite.registry.TSFluidTypes;
import com.tonywww.titan_satellite.registry.TSFluids;
import com.tonywww.titan_satellite.registry.TSItems;
import com.tonywww.titan_satellite.registry.TSMobEffects;
import com.tonywww.titan_satellite.fluid.TitanSounds;
import net.minecraft.resources.ResourceLocation;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
*///?}
import org.slf4j.Logger;

/**
 * Titan Satellite - 土卫六维度 mod 主类（Forge 1.20.1 骨架）。
 * 统一在构造器里把各 DeferredRegister 注册到 mod 事件总线。
 */
@Mod(TitanSatellite.MODID)
public class TitanSatellite {

    public static final String MODID = "titan_satellite";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 构造本 mod 命名空间下的 ResourceLocation（Forge 1.20.1 构造器 / NeoForge 1.21 工厂）。 */
    public static ResourceLocation rl(String path) {
        //? if forge {
        return new ResourceLocation(MODID, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(MODID, path);
        *///?}
    }

    /** minecraft 命名空间下的 ResourceLocation（Forge 构造器 / NeoForge 工厂）。 */
    public static ResourceLocation mcRl(String path) {
        //? if forge {
        return new ResourceLocation(path);
        //?} else {
        /*return ResourceLocation.withDefaultNamespace(path);
        *///?}
    }

    /** 解析带命名空间的完整串 {@code ns:path}（Forge 构造器 / NeoForge 1.21 parse）。 */
    public static ResourceLocation parse(String str) {
        //? if forge {
        return new ResourceLocation(str);
        //?} else {
        /*return ResourceLocation.parse(str);
        *///?}
    }

    //? if forge {
    public TitanSatellite() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    //?} else {
    /*public TitanSatellite(IEventBus modBus) {
    *///?}

        TSFluidTypes.REGISTER.register(modBus);
        TSFluids.REGISTER.register(modBus);
        TSBlocks.REGISTER.register(modBus);
        TSBlockEntities.REGISTER.register(modBus);
        TSItems.REGISTER.register(modBus);
        TSMobEffects.REGISTER.register(modBus);
        TSEntities.REGISTER.register(modBus);
        TSCreativeTabs.REGISTER.register(modBus);
        TitanSounds.REGISTER.register(modBus);

        // 实体属性在 mod 总线事件里注入
        modBus.addListener(TSEntities::onAttributeCreation);
        //? if neoforge {
        /*modBus.addListener(com.tonywww.titan_satellite.blockentity.SpecialMethanePumpBlockEntity::registerCapabilities);
        modBus.addListener(com.tonywww.titan_satellite.blockentity.HydrogenCollectorBlockEntity::registerCapabilities);
        modBus.addListener(com.tonywww.titan_satellite.blockentity.TholinComposterBlockEntity::registerCapabilities);
        com.tonywww.titan_satellite.worldgen.TSWorldgenTypes.register(modBus);
        com.tonywww.titan_satellite.worldgen.structure.TSStructures.register(modBus);
        *///?}

        LOGGER.info("[{}] Titan Satellite dimension mod loaded.", MODID);
    }
}
