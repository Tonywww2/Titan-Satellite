package com.tonywww.titan_moon.config;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.event.WaveController;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
//? if forge {
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
//?} else {
/*import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
*///?}

import java.util.UUID;

/**
 * Titan Moon 平衡配置。
 *
 * <p>生成全局配置文件 {@code config/titan_moon-common.toml}，暴露两类可调项：
 * ①<b>甲烷开采塔防波次怪难度</b>——服务端在波次怪加入世界时（{@link EntityJoinLevelEvent}）读取本配置，
 * 对带 {@link WaveController#WAVE_MOB_TAG} 标记的怪一次性施加最大生命乘子 + 攻击力加成；
 * ②<b>方块实体调优</b>——集氢罩 / 甲烷泵 / 托林堆肥槽的产出间隔、产量、槽容量、波数、完整度等平衡数值，
 * 各方块实体在构造 / tick 时读取（详见各自 {@code blockentity} 实现）。
 *
 * <p>经 {@link FMLConstructModEvent} 自订阅注册配置并挂 Forge 总线监听（不改主类）。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TitanMoon.MODID)
*///?}
public final class TMConfig {

    private TMConfig() {
    }

    //? if forge {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WAVE_MOB_SCALING_ENABLED;
    public static final ForgeConfigSpec.DoubleValue WAVE_MOB_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue WAVE_MOB_DAMAGE_BONUS;
    // 集氢罩 / 甲烷泵 / 托林堆肥槽调优
    public static final ForgeConfigSpec.IntValue HYDROGEN_PRODUCE_INTERVAL;
    public static final ForgeConfigSpec.IntValue HYDROGEN_FLUID_PER_INTERVAL;
    public static final ForgeConfigSpec.IntValue HYDROGEN_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue PUMP_MAX_PROGRESS;
    public static final ForgeConfigSpec.IntValue PUMP_WAVE_COUNT;
    public static final ForgeConfigSpec.IntValue PUMP_MAX_INTEGRITY;
    public static final ForgeConfigSpec.DoubleValue PUMP_ATTACK_RADIUS;
    public static final ForgeConfigSpec.IntValue PUMP_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue PUMP_FLUID_PER_TICK;
    public static final ForgeConfigSpec.IntValue PUMP_ITEM_INTERVAL;
    public static final ForgeConfigSpec.IntValue PUMP_EXTRACTION_PER_INTENSITY;
    public static final ForgeConfigSpec.IntValue COMPOSTER_PROCESS_INTERVAL;
    // 饰品（Curios）
    public static final ForgeConfigSpec.DoubleValue LIFE_SUPPORT_FLAT_HEAL;
    public static final ForgeConfigSpec.DoubleValue LIFE_SUPPORT_PERCENT_HEAL;
    public static final ForgeConfigSpec.DoubleValue TOXIN_GLOVE_CHANCE;
    public static final ForgeConfigSpec.IntValue TOXIN_GLOVE_DURATION;
    public static final ForgeConfigSpec.IntValue TOXIN_GLOVE_AMPLIFIER;
    //?} else {
    /*public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue WAVE_MOB_SCALING_ENABLED;
    public static final ModConfigSpec.DoubleValue WAVE_MOB_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue WAVE_MOB_DAMAGE_BONUS;
    // 集氢罩 / 甲烷泵 / 托林堆肥槽调优
    public static final ModConfigSpec.IntValue HYDROGEN_PRODUCE_INTERVAL;
    public static final ModConfigSpec.IntValue HYDROGEN_FLUID_PER_INTERVAL;
    public static final ModConfigSpec.IntValue HYDROGEN_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue PUMP_MAX_PROGRESS;
    public static final ModConfigSpec.IntValue PUMP_WAVE_COUNT;
    public static final ModConfigSpec.IntValue PUMP_MAX_INTEGRITY;
    public static final ModConfigSpec.DoubleValue PUMP_ATTACK_RADIUS;
    public static final ModConfigSpec.IntValue PUMP_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue PUMP_FLUID_PER_TICK;
    public static final ModConfigSpec.IntValue PUMP_ITEM_INTERVAL;
    public static final ModConfigSpec.IntValue PUMP_EXTRACTION_PER_INTENSITY;
    public static final ModConfigSpec.IntValue COMPOSTER_PROCESS_INTERVAL;
    // 饰品（Curios）
    public static final ModConfigSpec.DoubleValue LIFE_SUPPORT_FLAT_HEAL;
    public static final ModConfigSpec.DoubleValue LIFE_SUPPORT_PERCENT_HEAL;
    public static final ModConfigSpec.DoubleValue TOXIN_GLOVE_CHANCE;
    public static final ModConfigSpec.IntValue TOXIN_GLOVE_DURATION;
    public static final ModConfigSpec.IntValue TOXIN_GLOVE_AMPLIFIER;
    *///?}

    //? if forge {
    private static final UUID HEALTH_MOD_UUID = UUID.fromString("6f2a1c9e-2d1b-4a7c-9d3e-8b1a2c3d4e5f");
    private static final UUID DAMAGE_MOD_UUID = UUID.fromString("7a3b2d0f-3e2c-4b8d-8e4f-9c2b3d4e5f60");
    //?} else {
    /*private static final net.minecraft.resources.ResourceLocation HEALTH_MOD_RL = TitanMoon.rl("wave_health");
    private static final net.minecraft.resources.ResourceLocation DAMAGE_MOD_RL = TitanMoon.rl("wave_damage");
    *///?}
    /** 持久标记，避免存档重载后二次施加属性修饰。 */
    private static final String APPLIED_TAG = "TitanWaveScaled";

    static {
        //? if forge {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        //?} else {
        /*ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        *///?}
        builder.comment("Titan Moon — 甲烷开采塔防（Methane Extraction Defense）平衡").push("methane_extraction_defense");
        WAVE_MOB_SCALING_ENABLED = builder
                .comment("是否启用对塔防波次怪的难度缩放（下面两项）。")
                .define("waveMobScalingEnabled", true);
        WAVE_MOB_HEALTH_MULTIPLIER = builder
                .comment("塔防波次怪的最大生命乘子（1.0 = 原版属性）。")
                .defineInRange("waveMobHealthMultiplier", 1.0D, 0.25D, 8.0D);
        WAVE_MOB_DAMAGE_BONUS = builder
                .comment("塔防波次怪的额外攻击力（点，叠加在基础攻击之上）。")
                .defineInRange("waveMobDamageBonus", 0.0D, 0.0D, 20.0D);
        builder.pop();

        builder.comment("集氢罩（Hydrogen Collector）——被动产液氢。").push("hydrogen_collector");
        HYDROGEN_PRODUCE_INTERVAL = builder
                .comment("每次产出液氢的间隔 tick。")
                .defineInRange("produceInterval", 20, 1, 1200);
        HYDROGEN_FLUID_PER_INTERVAL = builder
                .comment("每次产出的液氢量（mB）。")
                .defineInRange("fluidPerInterval", 100, 1, 100000);
        HYDROGEN_TANK_CAPACITY = builder
                .comment("集氢罩内部流体槽容量（mB）。")
                .defineInRange("tankCapacity", 8000, 1000, 100000000);
        builder.pop();

        builder.comment("甲烷泵（Special Methane Pump）——开采塔防状态机。").push("methane_pump");
        PUMP_MAX_PROGRESS = builder
                .comment("完成一次开采所需 tick（满进度）。")
                .defineInRange("maxProgress", 2400, 200, 720000);
        PUMP_WAVE_COUNT = builder
                .comment("一次开采的总波数。")
                .defineInRange("waveCount", 5, 1, 50);
        PUMP_MAX_INTEGRITY = builder
                .comment("泵完整度上限。")
                .defineInRange("maxIntegrity", 100, 10, 100000);
        PUMP_ATTACK_RADIUS = builder
                .comment("怪物「攻击」泵的判定半径（格）。")
                .defineInRange("attackRadius", 2.5D, 1.0D, 16.0D);
        PUMP_TANK_CAPACITY = builder
                .comment("甲烷泵内部流体槽容量（mB）。")
                .defineInRange("tankCapacity", 16000, 1000, 100000000);
        PUMP_FLUID_PER_TICK = builder
                .comment("运行期每 tick 产出的液态甲烷（mB）。")
                .defineInRange("fluidPerTick", 8, 1, 100000);
        PUMP_ITEM_INTERVAL = builder
                .comment("运行期每隔多少 tick 产出一次材料。")
                .defineInRange("itemInterval", 240, 20, 72000);
        PUMP_EXTRACTION_PER_INTENSITY = builder
                .comment("每累计抽取多少 mB 提升 1 点生态压力（波次强度）。")
                .defineInRange("extractionPerIntensity", 4000, 100, 100000000);
        builder.pop();

        builder.comment("托林堆肥槽（Tholin Composter）——被动分解产托林粉末。").push("tholin_composter");
        COMPOSTER_PROCESS_INTERVAL = builder
                .comment("每转化 1 份生物残渣→托林粉末所需 tick。")
                .defineInRange("processInterval", 160, 1, 72000);
        builder.pop();

        builder.comment("饰品（Curios accessories）——维生套件回复量、毒腺手套触发。").push("curios");
        LIFE_SUPPORT_FLAT_HEAL = builder
                .comment("托林维生套件每次回复的固定生命值。")
                .defineInRange("lifeSupportFlatHeal", 1.0D, 0.0D, 1024.0D);
        LIFE_SUPPORT_PERCENT_HEAL = builder
                .comment("托林维生套件每次回复的最大生命值比例（0.01 = 1%）。")
                .defineInRange("lifeSupportPercentHeal", 0.01D, 0.0D, 1.0D);
        TOXIN_GLOVE_CHANCE = builder
                .comment("托林毒腺手套造成物理伤害时附加托林毒素的概率（0.25 = 25%）。")
                .defineInRange("toxinGloveChance", 0.25D, 0.0D, 1.0D);
        TOXIN_GLOVE_DURATION = builder
                .comment("托林毒腺手套附加的托林毒素持续 tick（20 tick = 1 秒）。")
                .defineInRange("toxinGloveDurationTicks", 100, 1, 72000);
        TOXIN_GLOVE_AMPLIFIER = builder
                .comment("托林毒腺手套附加的托林毒素等级（0 = I 级）。")
                .defineInRange("toxinGloveAmplifier", 0, 0, 4);
        builder.pop();

        SPEC = builder.build();
    }

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        //? if forge {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
        // EntityJoinLevelEvent 在 Forge 总线；构造阶段手动挂监听（本类注解绑 MOD 总线）。
        MinecraftForge.EVENT_BUS.addListener(TMConfig::onEntityJoinLevel);
        //?} else {
        /*ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, SPEC);
        NeoForge.EVENT_BUS.addListener(TMConfig::onEntityJoinLevel);
        *///?}
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !WAVE_MOB_SCALING_ENABLED.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (!mob.getPersistentData().getBoolean(WaveController.WAVE_MOB_TAG)
                || mob.getPersistentData().getBoolean(APPLIED_TAG)) {
            return;
        }
        double healthMult = WAVE_MOB_HEALTH_MULTIPLIER.get();
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && healthMult != 1.0D) {
            //? if forge {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    HEALTH_MOD_UUID, "titan_wave_health", healthMult - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            //?} else {
            /*maxHealth.addPermanentModifier(new AttributeModifier(
                    HEALTH_MOD_RL, healthMult - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            *///?}
            mob.setHealth(mob.getMaxHealth());
        }
        double damageBonus = WAVE_MOB_DAMAGE_BONUS.get();
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && damageBonus > 0.0D) {
            //? if forge {
            attack.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MOD_UUID, "titan_wave_damage", damageBonus, AttributeModifier.Operation.ADDITION));
            //?} else {
            /*attack.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MOD_RL, damageBonus, AttributeModifier.Operation.ADD_VALUE));
            *///?}
        }
        mob.getPersistentData().putBoolean(APPLIED_TAG, true);
    }
}
