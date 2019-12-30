package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * A block for the Liquid Chorus fluid.
 * @author rubensworks
 * TODO: doesn't seem to be rendering properly due to Forge not fully handling Fluid rendering yet.
 */
public class BlockFluidLiquidChorus extends FlowingFluidBlock {

    public BlockFluidLiquidChorus(Block.Properties builder) {
        super(() -> RegistryEntries.FLUID_LIQUID_CHORUS, builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);

        // Simulate chorus-eating
        if (entityIn instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity) entityIn;
            double d0 = entityLiving.posX;
            double d1 = entityLiving.posY;
            double d2 = entityLiving.posZ;

            for (int i = 0; i < 16; ++i) {
                double d3 = entityLiving.posX + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                double d4 = MathHelper.clamp(entityLiving.posY + (double) (entityLiving.getRNG().nextInt(16) - 8), 0.0D, (double) (worldIn.getActualHeight() - 1));
                double d5 = entityLiving.posZ + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;

                if (entityLiving.isPassenger()) {
                    entityLiving.stopRiding();
                }

                if (entityLiving.attemptTeleport(d3, d4, d5, true)) {
                    worldIn.playSound(null, d0, d1, d2, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }
}
