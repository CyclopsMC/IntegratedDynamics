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
 */
public class BlockFluidLiquidChorus extends FlowingFluidBlock {

    public BlockFluidLiquidChorus(Block.Properties builder) {
        super(() -> RegistryEntries.FLUID_LIQUID_CHORUS, builder);
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);

        // Simulate chorus-eating
        if (entityIn instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity) entityIn;
            double d0 = entityLiving.getX();
            double d1 = entityLiving.getY();
            double d2 = entityLiving.getZ();

            for (int i = 0; i < 16; ++i) {
                double d3 = entityLiving.getX() + (entityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = MathHelper.clamp(entityLiving.getY() + (double) (entityLiving.getRandom().nextInt(16) - 8), 0.0D, worldIn.getMaxBuildHeight() - 1);
                double d5 = entityLiving.getZ() + (entityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;

                if (entityLiving.isPassenger()) {
                    entityLiving.stopRiding();
                }

                if (entityLiving.randomTeleport(d3, d4, d5, true)) {
                    worldIn.playSound(null, d0, d1, d2, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }
}
