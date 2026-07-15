package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMEntities;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
//? if forge {
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.minecraft.world.entity.SpawnPlacementTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
*///?}

/**
 * 失控探测器的生成放置规则（mod 总线自订阅，不改主类）：地面、标准怪物生成条件。
 * 具体在哪些群系生成由 forge:add_spawns biome_modifier 控制（见
 * data/titan_moon/forge/biome_modifier/corrupted_probe_spawn.json）。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanMoon.MODID)
*///?}
public final class CorruptedProbeSpawn {

    private CorruptedProbeSpawn() {
    }

    //? if forge {
    @SubscribeEvent
    public static void register(SpawnPlacementRegisterEvent event) {
        event.register(TMEntities.CORRUPTED_PROBE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
    //?} else {
    /*@SubscribeEvent
    public static void register(RegisterSpawnPlacementsEvent event) {
        event.register(TMEntities.CORRUPTED_PROBE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
    *///?}
}
