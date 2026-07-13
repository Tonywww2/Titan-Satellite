package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.AeroJelly;
import com.tonywww.titan_satellite.entity.AmmoniaStalker;
import com.tonywww.titan_satellite.entity.CorruptedProbe;
import com.tonywww.titan_satellite.entity.CryoScavenger;
import com.tonywww.titan_satellite.entity.TholinWeaver;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体注册表；属性通过 {@link #onAttributeCreation} 注入（mod 总线）。
 */
public final class TSEntities {

    private TSEntities() {
    }

    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TitanSatellite.MODID);

    // 甲烷浮游体：漂浮的被动生物（占位，复用史莱姆模型渲染）。
    public static final RegistryObject<EntityType<AeroJelly>> AERO_JELLY = REGISTER.register("aero_jelly",
            () -> EntityType.Builder.of(AeroJelly::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.2F)
                    .clientTrackingRange(8)
                    .build("aero_jelly"));

    // 冰硅甲虫（中立，桩）。
    public static final RegistryObject<EntityType<CryoScavenger>> CRYO_SCAVENGER = REGISTER.register("cryo_scavenger",
            () -> EntityType.Builder.of(CryoScavenger::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.6F).clientTrackingRange(10).build("cryo_scavenger"));
    // 氨泉掉食者（敌对，桩）。
    public static final RegistryObject<EntityType<AmmoniaStalker>> AMMONIA_STALKER = REGISTER.register("ammonia_stalker",
            () -> EntityType.Builder.of(AmmoniaStalker::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.4F).clientTrackingRange(10).build("ammonia_stalker"));
    // 失控探测器（敌对，桩）。
    public static final RegistryObject<EntityType<CorruptedProbe>> CORRUPTED_PROBE = REGISTER.register("corrupted_probe",
            () -> EntityType.Builder.of(CorruptedProbe::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.8F).clientTrackingRange(12).build("corrupted_probe"));
    // 托林织体蛛（敌对，伏击 + 吐丝）。
    public static final RegistryObject<EntityType<TholinWeaver>> THOLIN_WEAVER = REGISTER.register("tholin_weaver",
            () -> EntityType.Builder.of(TholinWeaver::new, MobCategory.MONSTER)
                    .sized(1.1F, 0.8F).clientTrackingRange(10).build("tholin_weaver"));

    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(AERO_JELLY.get(), AeroJelly.createAttributes().build());
        event.put(CRYO_SCAVENGER.get(), CryoScavenger.createAttributes().build());
        event.put(AMMONIA_STALKER.get(), AmmoniaStalker.createAttributes().build());
        event.put(CORRUPTED_PROBE.get(), CorruptedProbe.createAttributes().build());
        event.put(THOLIN_WEAVER.get(), TholinWeaver.createAttributes().build());
    }
}
