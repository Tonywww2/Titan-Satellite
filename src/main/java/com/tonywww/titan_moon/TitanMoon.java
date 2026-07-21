package com.tonywww.titan_moon;

import com.mojang.logging.LogUtils;
import com.tonywww.titan_moon.registry.TMBlockEntities;
import com.tonywww.titan_moon.registry.TMBlocks;
import com.tonywww.titan_moon.registry.TMCreativeTabs;
import com.tonywww.titan_moon.registry.TMEntities;
import com.tonywww.titan_moon.registry.TMFluidTypes;
import com.tonywww.titan_moon.registry.TMFluids;
import com.tonywww.titan_moon.registry.TMItems;
import com.tonywww.titan_moon.registry.TMMobEffects;
import com.tonywww.titan_moon.registry.TMSounds;
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
 * Titan Moon - 土卫六维度 mod 主类（Forge 1.20.1）。
 * 统一在构造器里把各 DeferredRegister 注册到 mod 事件总线。
 */
@Mod(TitanMoon.MODID)
public class TitanMoon {

    public static final String MODID = "titan_moon";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 构造本 mod 命名空间下的 ResourceLocation（Forge 1.20.1 构造器 / NeoForge 1.21 工厂）。 */
    @SuppressWarnings("removal")
    public static ResourceLocation rl(String path) {
        //? if forge {
        return new ResourceLocation(MODID, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(MODID, path);
        *///?}
    }

    /** 解析带命名空间的完整串 {@code ns:path}（Forge 构造器 / NeoForge 1.21 parse）。 */
    @SuppressWarnings("removal")
    public static ResourceLocation parse(String str) {
        //? if forge {
        return new ResourceLocation(str);
        //?} else {
        /*return ResourceLocation.parse(str);
        *///?}
    }

    @SuppressWarnings("removal")
    //? if forge {
    public TitanMoon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    //?} else {
    /*public TitanMoon(IEventBus modBus) {
    *///?}

        TMFluidTypes.REGISTER.register(modBus);
        TMFluids.REGISTER.register(modBus);
        TMBlocks.REGISTER.register(modBus);
        TMBlockEntities.REGISTER.register(modBus);
        TMItems.REGISTER.register(modBus);
        TMMobEffects.REGISTER.register(modBus);
        TMEntities.REGISTER.register(modBus);
        TMCreativeTabs.REGISTER.register(modBus);
        TMSounds.REGISTER.register(modBus);

        // 实体属性在 mod 总线事件里注入
        modBus.addListener(TMEntities::onAttributeCreation);
        //? if neoforge {
        /*modBus.addListener(com.tonywww.titan_moon.blockentity.SpecialMethanePumpBlockEntity::registerCapabilities);
        modBus.addListener(com.tonywww.titan_moon.blockentity.HydrogenCollectorBlockEntity::registerCapabilities);
        modBus.addListener(com.tonywww.titan_moon.blockentity.TholinComposterBlockEntity::registerCapabilities);
        com.tonywww.titan_moon.worldgen.TMWorldgenTypes.register(modBus);
        com.tonywww.titan_moon.worldgen.structure.TMStructures.register(modBus);
        *///?}

        LOGGER.info("[{}] Titan Moon dimension mod loaded.", MODID);
    }
}
