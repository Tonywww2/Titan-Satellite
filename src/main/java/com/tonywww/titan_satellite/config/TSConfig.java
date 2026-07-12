package com.tonywww.titan_satellite.config;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.event.WaveController;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.UUID;

/**
 * Titan Satellite 平衡配置（PF-1..3 之后的收尾平衡层，PF-4）。
 *
 * <p>生成全局配置文件 {@code config/titan_satellite-common.toml}，当前暴露<b>甲烷开采塔防
 * 波次怪难度</b>的可调项——服务端在波次怪加入世界时（{@link EntityJoinLevelEvent}）读取本配置，
 * 对带 {@link WaveController#WAVE_MOB_TAG} 标记的怪一次性施加最大生命乘子 + 攻击力加成。
 *
 * <p>只读 PE-2 暴露的公有标记常量，不改任何已冻结/他人 Owns 文件；经 {@link FMLConstructModEvent}
 * 自订阅注册配置并挂 Forge 总线监听（不改主类）。其余系统的平衡数值见 {@code docs/test-matrix.md}
 * 的「平衡参考」一节（当前硬编码在各自 Owns 文件内）。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TSConfig {

    private TSConfig() {
    }

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WAVE_MOB_SCALING_ENABLED;
    public static final ForgeConfigSpec.DoubleValue WAVE_MOB_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue WAVE_MOB_DAMAGE_BONUS;

    private static final UUID HEALTH_MOD_UUID = UUID.fromString("6f2a1c9e-2d1b-4a7c-9d3e-8b1a2c3d4e5f");
    private static final UUID DAMAGE_MOD_UUID = UUID.fromString("7a3b2d0f-3e2c-4b8d-8e4f-9c2b3d4e5f60");
    /** 持久标记，避免存档重载后二次施加属性修饰。 */
    private static final String APPLIED_TAG = "TitanWaveScaled";

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Titan Satellite — 甲烷开采塔防（Methane Extraction Defense）平衡").push("methane_extraction_defense");
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
        SPEC = builder.build();
    }

    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
        // EntityJoinLevelEvent 在 Forge 总线；构造阶段手动挂监听（本类注解绑 MOD 总线）。
        MinecraftForge.EVENT_BUS.addListener(TSConfig::onEntityJoinLevel);
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
            maxHealth.addPermanentModifier(new AttributeModifier(
                    HEALTH_MOD_UUID, "titan_wave_health", healthMult - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
        double damageBonus = WAVE_MOB_DAMAGE_BONUS.get();
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && damageBonus > 0.0D) {
            attack.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MOD_UUID, "titan_wave_damage", damageBonus, AttributeModifier.Operation.ADDITION));
        }
        mob.getPersistentData().putBoolean(APPLIED_TAG, true);
    }
}
