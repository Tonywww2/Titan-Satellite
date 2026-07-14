package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.data.loot.TSLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
//? if forge {
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
*///?}

import java.util.concurrent.CompletableFuture;

/**
 * Datagen 总入口（Forge {@link GatherDataEvent}）。
 *
 * <p>运行：{@code ./gradlew :1.20.1-forge:runData}（见 build.gradle.kts 的 "data" 运行配置）。
 * 就地输出到 src/main/resources，生成内容直接替换对应的手写 JSON。
 *
 * <p>范围（用户选 A）：资源类（blockstate/模型/lang/loot/tags/sounds）+ worldgen 的
 * features/biomes/biome_modifier/structure/dimension；<b>保留手写 JSON</b>：密度函数/噪声/noise_settings、
 * planets/*（Ad Astra 自定义）。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanSatellite.MODID)
*///?}
public final class TSDataGenerators {

    private TSDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existing = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // ---- 客户端资源 ----
        generator.addProvider(event.includeClient(), new TSBlockStateProvider(output, existing));
        generator.addProvider(event.includeClient(), new TSItemModelProvider(output, existing));
        generator.addProvider(event.includeClient(), new TSLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new TSLanguageProvider(output, "zh_cn"));
        generator.addProvider(event.includeClient(), new TSSoundDefinitionsProvider(output, existing));

        // ---- 服务端数据 ----
        //? if forge {
        generator.addProvider(event.includeServer(), new TSLootTableProvider(output));
        //?} else {
        /*generator.addProvider(event.includeServer(), new TSLootTableProvider(output, lookup));
        *///?}
        generator.addProvider(event.includeServer(), new TSBlockTagsProvider(output, lookup, existing));
    }
}
