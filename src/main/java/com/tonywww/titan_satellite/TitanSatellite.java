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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Titan Satellite - 土卫六维度 mod 主类（Forge 1.20.1 骨架）。
 * 统一在构造器里把各 DeferredRegister 注册到 mod 事件总线。
 */
@Mod(TitanSatellite.MODID)
public class TitanSatellite {

    public static final String MODID = "titan_satellite";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TitanSatellite() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        TSFluidTypes.REGISTER.register(modBus);
        TSFluids.REGISTER.register(modBus);
        TSBlocks.REGISTER.register(modBus);
        TSBlockEntities.REGISTER.register(modBus);
        TSItems.REGISTER.register(modBus);
        TSMobEffects.REGISTER.register(modBus);
        TSEntities.REGISTER.register(modBus);
        TSCreativeTabs.REGISTER.register(modBus);

        // 实体属性在 mod 总线事件里注入
        modBus.addListener(TSEntities::onAttributeCreation);

        LOGGER.info("[{}] Titan Satellite dimension mod loaded (Forge 1.20.1 skeleton).", MODID);
    }
}
