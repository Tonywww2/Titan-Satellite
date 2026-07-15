package com.tonywww.titan_moon.worldgen;

import com.tonywww.titan_moon.TitanMoon;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?}

/**
 * 系统自装配：在 mod 构造阶段（{@link FMLConstructModEvent}，早于 RegisterEvent）
 * 把 worldgen 自定义类型的 DeferredRegister 挂到 mod 总线。
 *
 * <p>@EventBusSubscriber 由 Forge 自动发现并加载，故本装配【不需要主类改动】——
 * Capability 的注册/附加各自用 @EventBusSubscriber，同理不改主类。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?}
public final class TMSystemsBootstrap {

    private TMSystemsBootstrap() {
    }

    //? if forge {
    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TMWorldgenTypes.register(modBus);
    }
    //?}
}
