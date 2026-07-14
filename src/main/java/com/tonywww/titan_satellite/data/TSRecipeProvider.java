package com.tonywww.titan_satellite.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 材料加工链配方 datagen（全 datagen、两版都生效）。
 *
 * <p>自定义 {@link DataProvider} 手搓 raw JSON —— 不引用 Create/Mekanism/FD 的类（它们是
 * {@code modCompileOnly}，dev 运行期缺类），故 mod 变体配方以 JSON 直写并用 {@code mod_loaded}
 * 条件门控（缺该 mod 则跳过，不报错）。
 *
 * <p>版本差异集中在 {@link #ing}/{@link #craftResult}/{@link #cookResult} 与条件常量里：
 * 1.20.1 用 {@code recipes/} 目录、ingredient 对象 {@code {"item":id}}、result {@code {"item":..}}、
 * cooking result 为字符串、条件 key {@code "conditions"} + {@code forge:*}；
 * 1.21.1 用 {@code recipe/} 目录、ingredient 字符串、result {@code {"id":..}}、cooking result 为对象、
 * 条件 key {@code "neoforge:conditions"} + {@code neoforge:*}。
 *
 * <p>互斥分档（注意点 0）：熔制类只写一份 {@code smelting}/{@code blasting}（常开，Create 鼓风机 /
 * Mek 熔炉自动派生）；非熔制 base 用 {@code not(create)+not(mek)} 门控，Create/Mek 变体各自
 * {@code mod_loaded} —— 见 MC6/MC7/MC8。
 */
public class TSRecipeProvider implements DataProvider {

    private final PackOutput.PathProvider forgePath;  // recipes/
    private final PackOutput.PathProvider neoPath;    // recipe/
    private final Map<String, JsonObject> recipes = new LinkedHashMap<>();
    /** 当前发射目标格式：false=Forge(1.20.1)，true=NeoForge(1.21.1)。 */
    private boolean neo;

    public TSRecipeProvider(PackOutput output) {
        this.forgePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.neoPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        emitAll(cache, futures, false, forgePath);
        emitAll(cache, futures, true, neoPath);
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void emitAll(CachedOutput cache, List<CompletableFuture<?>> futures, boolean neoFmt, PackOutput.PathProvider path) {
        this.neo = neoFmt;
        recipes.clear();
        buildRecipes();
        recipes.forEach((name, json) ->
                futures.add(DataProvider.saveStable(cache, json, path.json(TitanSatellite.rl(name)))));
    }

    @Override
    public String getName() {
        return "Titan Satellite Recipes";
    }

    // ============================================================
    // 配方定义
    // ============================================================
    private void buildRecipes() {
        // ---- 熔制类（常开、无条件；Create 鼓风机 / Mek 熔炉自动派生，注意点 0）----
        cooking("minecraft:smelting", "condensed_acetylene_from_smelting", ts("hardened_tholin"), ts("condensed_acetylene"), 0.7F, 200);
        cooking("minecraft:smelting", "condensed_acetylene_from_tholin_tar", ts("tholin_tar"), ts("condensed_acetylene"), 0.5F, 200);
        cooking("minecraft:smelting", "meteoric_iron_ingot_from_smelting", ts("meteor_fragment"), ts("meteoric_iron_ingot"), 0.7F, 200);
        cooking("minecraft:blasting", "meteoric_iron_ingot_from_blasting", ts("meteor_fragment"), ts("meteoric_iron_ingot"), 0.7F, 100);

        // ---- 常开合成（无机器等价，不门控）----
        // 氢气瓶手采兜底（被动主路是集氢罩）
        shapeless("hydrogen_capsule_manual", ts("hydrogen_capsule"), 1, null, ts("hydrogen_bubble_mat"));
        // 甲烷冰晶熔入桶 → 液态甲烷（和平可再生甲烷）
        shapeless("liquid_methane_bucket_from_shards", ts("liquid_methane_bucket"), 1, null,
                ts("methane_ice_shard"), ts("methane_ice_shard"), ts("methane_ice_shard"), ts("methane_ice_shard"), "minecraft:bucket");
        // 托林晶粉 → 荧石（可再生发光；试剂③用途）
        shapeless("glowstone_from_tholin_crystal_dust", "minecraft:glowstone", 1, null,
                ts("tholin_crystal_dust"), ts("tholin_crystal_dust"), ts("tholin_crystal_dust"), ts("tholin_crystal_dust"));
        // 生物电池装配（无机器等价，常开）
        shaped("bio_battery", ts("bio_battery"), 1, null, new String[]{"SPS", "CBC", "SAS"},
                "S", ts("silicon_dust"), "P", ts("precision_components"), "C", ts("polyphosphazene_coenzyme"),
                "B", ts("depleted_battery"), "A", ts("azotosome_sheet"));

        // ---- 互斥 base：not(create)+not(mek)（粉碎/化学；有机器时禁用）----
        shapeless("tholin_dust", ts("tholin_dust"), 4, notCreateMek(), ts("hardened_tholin"));
        shapeless("condensed_acetylene_from_spire", ts("condensed_acetylene"), 2, notCreateMek(), ts("acetylene_spire"));
        shapeless("silicon_dust", ts("silicon_dust"), 1, notCreateMek(), ts("crystalline_twig"));
        shapeless("abyss_crystal_dust", ts("abyss_crystal_dust"), 2, notCreateMek(), ts("abyss_crystal"));
        shapeless("ammonia_salt", ts("ammonia_salt"), 2, notCreateMek(), ts("ammonia_crystal"));
        shapeless("tholin_crystal_dust", ts("tholin_crystal_dust"), 2, notCreateMek(), ts("tholin_crystal"));
        shapeless("polyphosphazene_coenzyme", ts("polyphosphazene_coenzyme"), 1, notCreateMek(),
                ts("condensed_acetylene"), ts("condensed_acetylene"), ts("hydrogen_capsule"));
        // 高产辅酶（消耗稀有深渊晶粉；试剂①用途）
        shapeless("polyphosphazene_coenzyme_abyss", ts("polyphosphazene_coenzyme"), 2, notCreateMek(),
                ts("abyss_crystal_dust"), ts("condensed_acetylene"), ts("condensed_acetylene"), ts("hydrogen_capsule"));
        shapeless("azotosome_sheet", ts("azotosome_sheet"), 1, notCreateMek(),
                ts("aero_membrane"), ts("tholin_dust"), ts("tholin_dust"));
        shapeless("cryo_alloy_ingot", ts("cryo_alloy_ingot"), 1, notCreateMek(),
                ts("cryo_carapace"), ts("cryo_carapace"), ts("meteoric_iron_ingot"));

        // ---- 互斥 base：not(farmersdelight)（有 FD 时走炖锅）----
        shapeless("titan_antidote", ts("titan_antidote"), 2, notFD(),
                ts("tough_neural_gland"), ts("toxic_gland"), ts("ammonia_salt"));

        buildModRecipes();
    }

    /**
     * mod 变体配方（各自 {@code mod_loaded} 门控；dev 无该 mod 会被跳过，故 schema 无法在 dev 验证——
     * 按各 mod 文档格式手搓）。熔制类不写（Create 鼓风机 / Mek 熔炉自动派生，注意点 0）。
     */
    private void buildModRecipes() {
        // ---- Create（mod_loaded:create）----
        createCrushing("tholin_dust_create", ts("hardened_tholin"), ts("tholin_dust"), 4, 200);
        createCrushing("condensed_acetylene_spire_create", ts("acetylene_spire"), ts("condensed_acetylene"), 2, 250);
        createCrushing("silicon_dust_create", ts("crystalline_twig"), ts("silicon_dust"), 1, 150);
        createCrushing("abyss_crystal_dust_create", ts("abyss_crystal"), ts("abyss_crystal_dust"), 2, 200);
        createCrushing("ammonia_salt_create", ts("ammonia_crystal"), ts("ammonia_salt"), 2, 200);
        createCrushing("tholin_crystal_dust_create", ts("tholin_crystal"), ts("tholin_crystal_dust"), 2, 200);
        createMixing("polyphosphazene_coenzyme_create", ts("polyphosphazene_coenzyme"), 1, true,
                ts("condensed_acetylene"), ts("condensed_acetylene"), ts("hydrogen_capsule"));
        createMixing("cryo_alloy_ingot_create", ts("cryo_alloy_ingot"), 1, true,
                ts("cryo_carapace"), ts("cryo_carapace"), ts("meteoric_iron_ingot"));
        createCompacting("azotosome_sheet_create", ts("azotosome_sheet"), 1,
                ts("aero_membrane"), ts("tholin_dust"), ts("tholin_dust"));

        // ---- Mekanism（mod_loaded:mekanism）----
        mekSingle("mekanism:enriching", "tholin_dust_mek", ts("hardened_tholin"), 1, ts("tholin_dust"), 4);
        mekSingle("mekanism:crushing", "condensed_acetylene_spire_mek", ts("acetylene_spire"), 1, ts("condensed_acetylene"), 2);
        mekSingle("mekanism:enriching", "silicon_dust_mek", ts("crystalline_twig"), 1, ts("silicon_dust"), 1);
        mekSingle("mekanism:crushing", "abyss_crystal_dust_mek", ts("abyss_crystal"), 1, ts("abyss_crystal_dust"), 2);
        mekSingle("mekanism:crushing", "ammonia_salt_mek", ts("ammonia_crystal"), 1, ts("ammonia_salt"), 2);
        mekSingle("mekanism:crushing", "tholin_crystal_dust_mek", ts("tholin_crystal"), 1, ts("tholin_crystal_dust"), 2);
        mekCombining("azotosome_sheet_mek", ts("aero_membrane"), 1, ts("tholin_dust"), 2, ts("azotosome_sheet"), 1);
        mekCombining("cryo_alloy_ingot_mek", ts("cryo_carapace"), 2, ts("meteoric_iron_ingot"), 1, ts("cryo_alloy_ingot"), 1);
        // 组合机：凝乙炷 + 氢气瓶（物品）→ 辅酶（用物品氢而非气体，避开 chemical_input 的版本差异）
        mekCombining("polyphosphazene_coenzyme_mek", ts("condensed_acetylene"), 2, ts("hydrogen_capsule"), 1,
                ts("polyphosphazene_coenzyme"), 1);

        // ---- Farmer's Delight（mod_loaded:farmersdelight）----
        fdCooking("titan_antidote_fd", ts("titan_antidote"), 2, 200, 0.5F,
                ts("tough_neural_gland"), ts("toxic_gland"), ts("ammonia_salt"));
    }

    // ============================================================
    // mod 变体配方 JSON 构建器（不引用 mod 类，纯 JSON）
    // ============================================================
    private void createCrushing(String name, String inputId, String resultId, int count, int time) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "create:crushing");
        JsonArray ings = new JsonArray();
        ings.add(ing(inputId));
        r.add("ingredients", ings);
        r.add("results", modResults(resultId, count));
        r.addProperty("processingTime", time);
        addConds(r, modLoadedArr("create"));
        put(name, r);
    }

    private void createMixing(String name, String resultId, int count, boolean heated, String... ins) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "create:mixing");
        JsonArray ings = new JsonArray();
        for (String i : ins) {
            ings.add(ing(i));
        }
        r.add("ingredients", ings);
        r.add("results", modResults(resultId, count));
        if (heated) {
            r.addProperty("heatRequirement", "heated");
        }
        addConds(r, modLoadedArr("create"));
        put(name, r);
    }

    private void createCompacting(String name, String resultId, int count, String... ins) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "create:compacting");
        JsonArray ings = new JsonArray();
        for (String i : ins) {
            ings.add(ing(i));
        }
        r.add("ingredients", ings);
        r.add("results", modResults(resultId, count));
        addConds(r, modLoadedArr("create"));
        put(name, r);
    }

    private void mekSingle(String type, String name, String inId, int inAmt, String outId, int outCount) {
        JsonObject r = new JsonObject();
        r.addProperty("type", type);
        r.add("input", mekItemInput(inId, inAmt));
        r.add("output", craftResult(outId, outCount));
        addConds(r, modLoadedArr("mekanism"));
        put(name, r);
    }

    private void mekCombining(String name, String mainId, int mainAmt, String extraId, int extraAmt, String outId, int outCount) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "mekanism:combining");
        r.add(neo ? "main_input" : "mainInput", mekItemInput(mainId, mainAmt));
        r.add(neo ? "extra_input" : "extraInput", mekItemInput(extraId, extraAmt));
        r.add("output", craftResult(outId, outCount));
        addConds(r, modLoadedArr("mekanism"));
        put(name, r);
    }

    private void fdCooking(String name, String resultId, int count, int time, float xp, String... ins) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "farmersdelight:cooking");
        JsonArray ings = new JsonArray();
        for (String i : ins) {
            ings.add(ing(i));
        }
        r.add("ingredients", ings);
        r.add("result", craftResult(resultId, count));
        r.addProperty("experience", xp);
        r.addProperty("cookingtime", time);
        addConds(r, modLoadedArr("farmersdelight"));
        put(name, r);
    }

    private JsonArray modResults(String id, int count) {
        JsonArray a = new JsonArray();
        a.add(craftResult(id, count));
        return a;
    }

    /** Mek 物品输入：1.20.1 {"ingredient":{"item":id},"amount":N?}；1.21.1 {"item":id,"count":N}。 */
    private JsonObject mekItemInput(String id, int amount) {
        JsonObject o = new JsonObject();
        if (neo) {
            o.addProperty("item", id);
            o.addProperty("count", amount);
        } else {
            o.add("ingredient", ing(id));
            if (amount != 1) {
                o.addProperty("amount", amount);
            }
        }
        return o;
    }

    // ============================================================
    // 原版配方 JSON 构建器
    // ============================================================
    private void shapeless(String name, String resultId, int count, JsonObject[] conds, String... ingredients) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ings = new JsonArray();
        for (String id : ingredients) {
            ings.add(ing(id));
        }
        r.add("ingredients", ings);
        r.add("result", craftResult(resultId, count));
        if (conds != null) {
            addConds(r, conds);
        }
        put(name, r);
    }

    private void shaped(String name, String resultId, int count, JsonObject[] conds, String[] pattern, String... keyPairs) {
        JsonObject r = new JsonObject();
        r.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pat = new JsonArray();
        for (String row : pattern) {
            pat.add(row);
        }
        r.add("pattern", pat);
        JsonObject key = new JsonObject();
        for (int i = 0; i < keyPairs.length; i += 2) {
            key.add(keyPairs[i], ing(keyPairs[i + 1]));
        }
        r.add("key", key);
        r.add("result", craftResult(resultId, count));
        if (conds != null) {
            addConds(r, conds);
        }
        put(name, r);
    }

    private void cooking(String type, String name, String ingredientId, String resultId, float xp, int time) {
        JsonObject r = new JsonObject();
        r.addProperty("type", type);
        r.add("ingredient", ing(ingredientId));
        r.add("result", cookResult(resultId));
        r.addProperty("experience", xp);
        r.addProperty("cookingtime", time);
        put(name, r);
    }

    // ============================================================
    // 版本差异帮助方法
    // ============================================================
    /** ingredient：1.20.1 与 1.21.1 单物品 ingredient 同为对象 {"item":id}（1.21.1 的 Ingredient codec
     * 是 either(array,object)，不接受裸字符串）。 */
    private JsonElement ing(String id) {
        JsonObject o = new JsonObject();
        o.addProperty("item", id);
        return o;
    }

    /** crafting/mod result：1.20.1 {"item":id,"count":n}；1.21.1 {"id":id,"count":n}。 */
    private JsonObject craftResult(String id, int count) {
        JsonObject o = new JsonObject();
        if (neo) {
            o.addProperty("id", id);
        } else {
            o.addProperty("item", id);
        }
        o.addProperty("count", count);
        return o;
    }

    /** cooking result：1.20.1 字符串 "id"；1.21.1 对象 {"id":id,"count":1}。 */
    private JsonElement cookResult(String id) {
        if (neo) {
            JsonObject o = new JsonObject();
            o.addProperty("id", id);
            o.addProperty("count", 1);
            return o;
        }
        return new JsonPrimitive(id);
    }

    // ============================================================
    // 条件帮助方法（按 this.neo 切换 key/前缀）
    // ============================================================
    private String condKey() {
        return neo ? "neoforge:conditions" : "conditions";
    }

    private String condPrefix() {
        return neo ? "neoforge" : "forge";
    }

    private JsonObject modLoaded(String modid) {
        JsonObject o = new JsonObject();
        o.addProperty("type", condPrefix() + ":mod_loaded");
        o.addProperty("modid", modid);
        return o;
    }

    private JsonObject notCond(JsonObject inner) {
        JsonObject o = new JsonObject();
        o.addProperty("type", condPrefix() + ":not");
        o.add("value", inner);
        return o;
    }

    private void addConds(JsonObject recipe, JsonObject[] conds) {
        JsonArray a = new JsonArray();
        for (JsonObject c : conds) {
            a.add(c);
        }
        recipe.add(condKey(), a);
    }

    private JsonObject[] notCreateMek() {
        return new JsonObject[]{notCond(modLoaded("create")), notCond(modLoaded("mekanism"))};
    }

    private JsonObject[] notFD() {
        return new JsonObject[]{notCond(modLoaded("farmersdelight"))};
    }

    private JsonObject[] modLoadedArr(String modid) {
        return new JsonObject[]{modLoaded(modid)};
    }

    private static String ts(String name) {
        return TitanSatellite.MODID + ":" + name;
    }

    private void put(String name, JsonObject json) {
        recipes.put(name, json);
    }
}
