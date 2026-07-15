package com.tonywww.titan_moon.entity;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMEntities;
import net.minecraft.world.entity.Mob;
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
 * 冰硅甲虫的生成放置规则：陆地生成（中立生物，不要求黑暗），仅需下方为可生成方块，
 * 经 mod 总线 {@link SpawnPlacementRegisterEvent} 自订阅注册（不改主类）。
 * 生成注入见 data/titan_moon/forge/biome_modifier/cryo_scavenger_spawn.json。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanMoon.MODID)
*///?}
public final class CryoScavengerSpawn {

    private CryoScavengerSpawn() {
    }

    //? if forge {
    @SubscribeEvent
    public static void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(TMEntities.CRYO_SCAVENGER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
    //?} else {
    /*@SubscribeEvent
    public static void registerSpawnPlacement(RegisterSpawnPlacementsEvent event) {
        event.register(TMEntities.CRYO_SCAVENGER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    *///?}
}
