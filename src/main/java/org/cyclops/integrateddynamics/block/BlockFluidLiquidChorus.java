package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockFluidClassic;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.fluid.FluidLiquidChorus;

/**
 * A blockState for the {@link BlockFluidLiquidChorus} fluid.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorus extends ConfigurableBlockFluidClassic {

    private static BlockFluidLiquidChorus _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockFluidLiquidChorus getInstance() {
        return _instance;
    }

    public BlockFluidLiquidChorus(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, FluidLiquidChorus.getInstance(), Material.WATER);
        
        if (MinecraftHelpers.isClientSide())
            this.setParticleColor(0.694117647F, 0.505882353F, 0.694117647F);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);

        // Simulate chorus-eating
        if (entityIn instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            double d0 = entityLiving.posX;
            double d1 = entityLiving.posY;
            double d2 = entityLiving.posZ;

            for (int i = 0; i < 16; ++i) {
                double d3 = entityLiving.posX + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                double d4 = MathHelper.clamp(entityLiving.posY + (double) (entityLiving.getRNG().nextInt(16) - 8), 0.0D, (double) (worldIn.getActualHeight() - 1));
                double d5 = entityLiving.posZ + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;

                if (entityLiving.isRiding()) {
                    entityLiving.dismountRidingEntity();
                }

                if (entityLiving.attemptTeleport(d3, d4, d5)) {
                    worldIn.playSound((EntityPlayer) null, d0, d1, d2, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }
}
