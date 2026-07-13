package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

/**
 * sounds.json datagen。复刻现有唯一条目：block.titan_fluid.solidify → minecraft:random/fizz。
 */
public class TSSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public TSSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, TitanSatellite.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add("block.titan_fluid.solidify", definition()
                .with(sound(new ResourceLocation("minecraft", "random/fizz"))));
    }
}
