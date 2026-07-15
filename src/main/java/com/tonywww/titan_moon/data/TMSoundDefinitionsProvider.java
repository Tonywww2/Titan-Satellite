package com.tonywww.titan_moon.data;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
//? if forge {
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
//?} else {
/*import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
*///?}

/**
 * sounds.json datagen。复刻现有唯一条目：block.titan_fluid.solidify → minecraft:random/fizz。
 */
public class TMSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public TMSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, TitanMoon.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add("block.titan_fluid.solidify", definition()
                .with(sound(TitanMoon.mcRl("random/fizz"))));
    }
}
