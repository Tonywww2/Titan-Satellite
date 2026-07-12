package com.tonywww.titan_satellite.worldgen;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * PA-2 系统自装配：在 mod 构造阶段（{@link FMLConstructModEvent}，早于 RegisterEvent）
 * 把 worldgen 自定义类型的 DeferredRegister 挂到 mod 总线。
 *
 * <p>@EventBusSubscriber 由 Forge 自动发现并加载，故本装配【不需要主类改动】——
 * 这正是 PA-2 与 PA-1 文件不相交的关键。Capability 的注册/附加各自用 @EventBusSubscriber，
 * 同理不改主类。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TSSystemsBootstrap {

    private TSSystemsBootstrap() {
    }

    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TSWorldgenTypes.register(modBus);
    }
}
