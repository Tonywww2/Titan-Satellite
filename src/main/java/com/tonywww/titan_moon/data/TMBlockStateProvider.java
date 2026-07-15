package com.tonywww.titan_moon.data;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
//? if forge {
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
*///?}

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 方块状态 + 方块模型 + 方块物品模型 datagen。复刻现有手写 JSON：
 * <ul>
 *   <li>cube_all：blockstate variants ""→block/&lt;name&gt;；模型 parent cube_all + all 贴图；物品 parent=方块模型。
 *       部分方块复用他块贴图（weathered→titan_stone、sedimentary→titan_basalt）。</li>
 *   <li>cross：parent cross + render_type cutout；物品为 generated + layer0=cross 贴图（branch_crystal 用纯色占位贴图）。</li>
 *   <li>液体：仅 particle 贴图的无父模型；无 BlockItem。</li>
 * </ul>
 */
public class TMBlockStateProvider extends BlockStateProvider {

    public TMBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TitanMoon.MODID, exFileHelper);
    }

    /** cube_all 方块名 → 贴图名（均在 titan_moon:block/ 下）。 */
    private static final Map<String, String> CUBE = new LinkedHashMap<>();
    /** cross 方块名 → 完整贴图 ResourceLocation 串。 */
    private static final Map<String, String> CROSS = new LinkedHashMap<>();
    private static final String[] LIQUIDS = {"liquid_methane", "liquid_ammonia", "liquid_hydrogen"};
    /**
     * 需四向（Y 0/90/180/270）随机旋转模型、以打破大面积平铺重复的自然地形 / 矿物 cube_all 方块。
     * 随机 Y 旋转主要打散俯视时的顶/底面重复纹理（仿原版沙砾/石头做法）。
     * 功能机械方块（{@code cryovolcanic_geyser} / {@code methane_pool_core} /
     * {@code special_methane_pump}）保持固定朝向，<b>不</b>在此列。
     */
    private static final Set<String> RANDOM_Y_ROTATE = Set.of(
            // 大面积地形：石 / 沙 / 冰 / 砾石
            "titan_stone", "titan_basalt", "weathered_titan_stone", "sedimentary_titan_stone",
            "tholin_sand", "crushed_ice", "cryo_ice", "packed_methane_ice", "titan_gravel",
            // 矿物 / 装饰实心方块
            "abyss_crystal", "ammonia_crystal", "tholin_crystal", "meteor_fragment",
            "hardened_tholin", "tholin_tar");

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
        CUBE.put("acetylene_spire", "acetylene_spire");
        CUBE.put("tholin_mycelium", "tholin_mycelium");
        CUBE.put("hydrogen_bubble_mat", "hydrogen_bubble_mat");
        CUBE.put("hydrogen_collector", "hydrogen_collector");
        CUBE.put("tholin_composter", "tholin_composter");

        CROSS.put("tholin_shrub", "titan_moon:block/tholin_shrub");
        CROSS.put("methane_ice_bloom", "titan_moon:block/methane_ice_bloom");
        CROSS.put("frost_bush", "titan_moon:block/frost_bush");
        CROSS.put("branch_crystal", "titan_moon:block/branch_crystal"); // 纯色占位贴图
    }

    @Override
    protected void registerStatesAndModels() {
        CUBE.forEach((name, texture) -> {
            ModelFile model = models().cubeAll(name, modLoc("block/" + texture));
            Block block = block(name);
            if (RANDOM_Y_ROTATE.contains(name)) {
                // 四向随机 Y 旋转（uvlock=false 才会真正旋转贴图，打破大面积平铺重复）
                simpleBlock(block, ConfiguredModel.allYRotations(model, 0, false));
            } else {
                simpleBlock(block, model);
            }
            simpleBlockItem(block, model); // 物品模型 parent = 方块模型（不旋转）
        });

        CROSS.forEach((name, texture) -> {
            ResourceLocation textureRL = TitanMoon.parse(texture);
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
        //? if forge {
        return ForgeRegistries.BLOCKS.getValue(TitanMoon.rl(name));
        //?} else {
        /*return BuiltInRegistries.BLOCK.get(TitanMoon.rl(name));
        *///?}
    }
}
