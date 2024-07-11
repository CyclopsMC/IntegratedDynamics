package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * A block for the Liquid Chorus fluid.
 * @author rubensworks
 */
public class BlockFluidLiquidChorus extends LiquidBlock {

    public BlockFluidLiquidChorus(Block.Properties builder) {
        super(RegistryEntries.FLUID_LIQUID_CHORUS.get(), builder);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);

        // Simulate chorus-eating
        if (entityIn instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity) entityIn;
            double d0 = entityLiving.getX();
            double d1 = entityLiving.getY();
            double d2 = entityLiving.getZ();

            for (int i = 0; i < 16; ++i) {
                double d3 = entityLiving.getX() + (entityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(entityLiving.getY() + (double) (entityLiving.getRandom().nextInt(16) - 8), 0.0D, worldIn.getMaxBuildHeight() - 1);
                double d5 = entityLiving.getZ() + (entityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;

                if (entityLiving.isPassenger()) {
                    entityLiving.stopRiding();
                }

                if (entityLiving.randomTeleport(d3, d4, d5, true)) {
                    worldIn.playSound(null, d0, d1, d2, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }
}
