package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 方块状态 + 方块模型 + 方块物品模型 datagen。复刻现有手写 JSON：
 * <ul>
 *   <li>cube_all：blockstate variants ""→block/&lt;name&gt;；模型 parent cube_all + all 贴图；物品 parent=方块模型。
 *       部分方块复用他块贴图（weathered→titan_stone、sedimentary→titan_basalt）。</li>
 *   <li>cross：parent cross + render_type cutout；物品为 generated + layer0=cross 贴图（branch_crystal 复用 warped_roots）。</li>
 *   <li>液体：仅 particle 贴图的无父模型；无 BlockItem。</li>
 * </ul>
 */
public class TSBlockStateProvider extends BlockStateProvider {

    public TSBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TitanSatellite.MODID, exFileHelper);
    }

    /** cube_all 方块名 → 贴图名（均在 titan_satellite:block/ 下）。 */
    private static final Map<String, String> CUBE = new LinkedHashMap<>();
    /** cross 方块名 → 完整贴图 ResourceLocation 串。 */
    private static final Map<String, String> CROSS = new LinkedHashMap<>();
    private static final String[] LIQUIDS = {"liquid_methane", "liquid_ammonia"};

    static {
        CUBE.put("titan_stone", "titan_stone");
        CUBE.put("titan_basalt", "titan_basalt");
        CUBE.put("tholin_sand", "tholin_sand");
        CUBE.put("crushed_ice", "crushed_ice");
        CUBE.put("cryo_ice", "cryo_ice");
        CUBE.put("packed_methane_ice", "packed_methane_ice");
        CUBE.put("cryovolcanic_geyser", "cryovolcanic_geyser");
        CUBE.put("methane_pool_core", "methane_pool_core");
        CUBE.put("special_methane_pump", "special_methane_pump");
        CUBE.put("tholin_crystal", "tholin_crystal");
        CUBE.put("weathered_titan_stone", "titan_stone");       // 复用 titan_stone 贴图
        CUBE.put("sedimentary_titan_stone", "titan_basalt");    // 复用 titan_basalt 贴图
        CUBE.put("abyss_crystal", "abyss_crystal");
        CUBE.put("tholin_tar", "tholin_tar");
        CUBE.put("meteor_fragment", "meteor_fragment");
        CUBE.put("hardened_tholin", "hardened_tholin");
        CUBE.put("ammonia_crystal", "ammonia_crystal");
        CUBE.put("titan_gravel", "titan_gravel");

        CROSS.put("tholin_shrub", "titan_satellite:block/tholin_shrub");
        CROSS.put("methane_ice_bloom", "titan_satellite:block/methane_ice_bloom");
        CROSS.put("frost_bush", "titan_satellite:block/frost_bush");
        CROSS.put("branch_crystal", "minecraft:block/warped_roots"); // 复用原版 warped_roots
    }

    @Override
    protected void registerStatesAndModels() {
        CUBE.forEach((name, texture) -> {
            ModelFile model = models().cubeAll(name, modLoc("block/" + texture));
            Block block = block(name);
            simpleBlock(block, model);
            simpleBlockItem(block, model); // 物品模型 parent = 方块模型
        });

        CROSS.forEach((name, texture) -> {
            ResourceLocation textureRL = new ResourceLocation(texture);
            ModelFile model = models().cross(name, textureRL).renderType("cutout");
            simpleBlock(block(name), model);
            // cross 物品：generated + layer0 = cross 贴图（cross 模型作为手持物品渲染不佳）
            itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", textureRL);
        });

        for (String name : LIQUIDS) {
            // 无父模型，仅 particle 贴图；无 BlockItem
            ModelFile model = models().getBuilder(name).texture("particle", modLoc("block/" + name));
            simpleBlock(block(name), model);
        }
    }

    private static Block block(String name) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TitanSatellite.MODID, name));
    }
}
