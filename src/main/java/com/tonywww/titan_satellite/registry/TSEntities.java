package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.AeroJelly;
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

    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(AERO_JELLY.get(), AeroJelly.createAttributes().build());
    }
}
