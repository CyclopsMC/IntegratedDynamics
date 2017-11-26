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
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiMechanicalSqueezer;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockMechanicalSqueezer extends BlockContainerGuiCabled implements IMachine<BlockMechanicalSqueezer, IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> {

    @BlockProperty
    public static final PropertyBool ON = PropertyBool.create("on");

    private static BlockMechanicalSqueezer _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMechanicalSqueezer getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMechanicalSqueezer(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileMechanicalSqueezer.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, EnumFacing side, float motionX, float motionY, float motionZ) {
        return FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, side)
                || super.onBlockActivated(world, blockPos, blockState, player, hand, side, motionX, motionY, motionZ);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerMechanicalSqueezer.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiMechanicalSqueezer.class;
    }

    @Override
    public IRecipeRegistry<BlockMechanicalSqueezer, IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(ISuperRecipeRegistry.class).getRecipeRegistry(this);
    }
}
