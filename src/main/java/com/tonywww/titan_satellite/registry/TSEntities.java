package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.AeroJelly;
import com.tonywww.titan_satellite.entity.AmmoniaStalker;
import com.tonywww.titan_satellite.entity.CorruptedProbe;
import com.tonywww.titan_satellite.entity.CryoScavenger;
import com.tonywww.titan_satellite.entity.HydrotrophGrazer;
import com.tonywww.titan_satellite.entity.MethaneMidge;
import com.tonywww.titan_satellite.entity.NativeIceWorm;
import com.tonywww.titan_satellite.entity.TholinWeaver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
//? if forge {
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import java.util.function.Supplier;

/**
 * 实体注册表；属性通过 {@link #onAttributeCreation} 注入（mod 总线）。
 */
public final class TSEntities {

    private TSEntities() {
    }

    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, TitanSatellite.MODID);

    // 甲烷浮游体：漂浮的被动生物（占位，复用史莱姆模型渲染）。
    public static final Supplier<EntityType<AeroJelly>> AERO_JELLY = REGISTER.register("aero_jelly",
            () -> EntityType.Builder.of(AeroJelly::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.2F)
                    .clientTrackingRange(8)
                    .build("aero_jelly"));

    // 冰硅甲虫（中立，桩）。
    public static final Supplier<EntityType<CryoScavenger>> CRYO_SCAVENGER = REGISTER.register("cryo_scavenger",
            () -> EntityType.Builder.of(CryoScavenger::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.6F).clientTrackingRange(10).build("cryo_scavenger"));
    // 氨泉掉食者（敌对，桩）。
    public static final Supplier<EntityType<AmmoniaStalker>> AMMONIA_STALKER = REGISTER.register("ammonia_stalker",
            () -> EntityType.Builder.of(AmmoniaStalker::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.4F).clientTrackingRange(10).build("ammonia_stalker"));
    // 失控探测器（敌对，桩）。
    public static final Supplier<EntityType<CorruptedProbe>> CORRUPTED_PROBE = REGISTER.register("corrupted_probe",
            () -> EntityType.Builder.of(CorruptedProbe::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.8F).clientTrackingRange(12).build("corrupted_probe"));
    // 托林织体蛛（敌对，伏击 + 吐丝）。
    public static final Supplier<EntityType<TholinWeaver>> THOLIN_WEAVER = REGISTER.register("tholin_weaver",
            () -> EntityType.Builder.of(TholinWeaver::new, MobCategory.MONSTER)
                    .sized(1.1F, 0.8F).clientTrackingRange(10).build("tholin_weaver"));
    // 原生冰虫（敌对·精英，巢穴守卫）。
    public static final Supplier<EntityType<NativeIceWorm>> NATIVE_ICE_WORM = REGISTER.register("native_ice_worm",
            () -> EntityType.Builder.of(NativeIceWorm::new, MobCategory.MONSTER)
                    .sized(1.2F, 1.0F).clientTrackingRange(10).build("native_ice_worm"));
    // 甲烷微浮群（被动·飞行群集，浮游体食源）。
    public static final Supplier<EntityType<MethaneMidge>> METHANE_MIDGE = REGISTER.register("methane_midge",
            () -> EntityType.Builder.of(MethaneMidge::new, MobCategory.CREATURE)
                    .sized(0.3F, 0.3F).clientTrackingRange(6).build("methane_midge"));
    // 氢营养蹒兽（被动·食草，充实营养级）。
    public static final Supplier<EntityType<HydrotrophGrazer>> HYDROTROPH_GRAZER = REGISTER.register("hydrotroph_grazer",
            () -> EntityType.Builder.of(HydrotrophGrazer::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.0F).clientTrackingRange(10).build("hydrotroph_grazer"));

    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(AERO_JELLY.get(), AeroJelly.createAttributes().build());
        event.put(CRYO_SCAVENGER.get(), CryoScavenger.createAttributes().build());
        event.put(AMMONIA_STALKER.get(), AmmoniaStalker.createAttributes().build());
        event.put(CORRUPTED_PROBE.get(), CorruptedProbe.createAttributes().build());
        event.put(THOLIN_WEAVER.get(), TholinWeaver.createAttributes().build());
        event.put(NATIVE_ICE_WORM.get(), NativeIceWorm.createAttributes().build());
        event.put(METHANE_MIDGE.get(), MethaneMidge.createAttributes().build());
        event.put(HYDROTROPH_GRAZER.get(), HydrotrophGrazer.createAttributes().build());
    }
}
