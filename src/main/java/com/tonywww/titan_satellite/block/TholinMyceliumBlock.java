package com.tonywww.titan_satellite.block;

import java.util.List;

import com.tonywww.titan_satellite.registry.TSItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 托林菌网（Tholin Mycelium）：巢穴 / 大巢的分解者有机壁——生态循环的「分解者」一环，
 * 把附近的生物残渣（本维度生物掉落物）缓慢重整回托林（设计 §2.4 / §3.1 DEC→ENV）。
 * <p>随机刻散发孢子（视觉提示），并消解其上方 1 格内的残渣物品（每刻至多一份，缓慢）。
 * 只作用于本维度生物残渣，不吞噬玩家的工具 / 战利品。
 */
public class TholinMyceliumBlock extends Block {

    public TholinMyceliumBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 孢子（分解者视觉提示）
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                3, 0.3D, 0.1D, 0.3D, 0.0D);
        // 消解上方 1 格内的生物残渣物品（重整回托林）
        AABB box = new AABB(pos.above());
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box,
                e -> e.isAlive() && isResidue(e.getItem()));
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            stack.shrink(1);
            if (stack.isEmpty()) {
                item.discard();
            } else {
                item.setItem(stack);
            }
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    item.getX(), item.getY() + 0.2D, item.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.0D);
            break; // 每刻至多分解一份，缓慢
        }
    }

    /** 是否为本维度生物残渣（可被菌网分解重整回托林）。 */
    private static boolean isResidue(ItemStack stack) {
        return stack.is(TSItems.CRYO_CARAPACE.get())
                || stack.is(TSItems.TOXIC_GLAND.get())
                || stack.is(TSItems.TOUGH_NEURAL_GLAND.get())
                || stack.is(TSItems.THOLIN_SILK_SAC.get())
                || stack.is(TSItems.AERO_MEMBRANE.get())
                || stack.is(TSItems.THOLIN_FIBRE.get());
    }
}
