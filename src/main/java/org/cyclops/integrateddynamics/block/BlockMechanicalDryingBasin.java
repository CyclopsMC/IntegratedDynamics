package org.cyclops.integrateddynamics.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.api.ISuperRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiMechanicalDryingBasin;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasin extends BlockContainerGuiCabled implements IMachine<BlockMechanicalDryingBasin, IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> {

    @BlockProperty
    public static final PropertyBool ON = PropertyBool.create("on");

    private static BlockMechanicalDryingBasin _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMechanicalDryingBasin getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMechanicalDryingBasin(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileMechanicalDryingBasin.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, EnumFacing side, float motionX, float motionY, float motionZ) {
        return FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, EnumFacing.UP)
                || FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, EnumFacing.DOWN)
                || super.onBlockActivated(world, blockPos, blockState, player, hand, side, motionX, motionY, motionZ);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerMechanicalDryingBasin.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiMechanicalDryingBasin.class;
    }

    @Override
    public IRecipeRegistry<BlockMechanicalDryingBasin, IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(ISuperRecipeRegistry.class).getRecipeRegistry(this);
    }
}
