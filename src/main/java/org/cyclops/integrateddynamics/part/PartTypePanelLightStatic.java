package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.core.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;

/**
 * A panel part that simply emits light.
 * @author rubensworks
 */
public class PartTypePanelLightStatic extends PartTypePanel<PartTypePanelLightStatic, PartStateEmpty<PartTypePanelLightStatic>> {

    public PartTypePanelLightStatic(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock(blockConfig);
    }

    @Override
    public Class<? super PartTypePanelLightStatic> getPartTypeClass() {
        return PartTypePanelLightStatic.class;
    }

    @Override
    public PartStateEmpty<PartTypePanelLightStatic> constructDefaultState() {
        return new PartStateEmpty<PartTypePanelLightStatic>();
    }

    @Override
    protected boolean hasGui() {
        return false;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return null;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return null;
    }

    @Override
    public void onNetworkAddition(Network network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkAddition(network, target, state);
        IBlockAccess world = target.getCenter().getPos().getWorld();
        BlockPos pos = target.getCenter().getPos().getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof IDynamicLightBlock) {
            ((IDynamicLightBlock) block).setLightLevel(world, pos, target.getCenter().getSide(), 15);
        }
    }

    @Override
    public void onNetworkRemoval(Network network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkRemoval(network, target, state);
        IBlockAccess world = target.getCenter().getPos().getWorld();
        BlockPos pos = target.getCenter().getPos().getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof IDynamicLightBlock) {
            ((IDynamicLightBlock) block).setLightLevel(world, pos, target.getCenter().getSide(), 0);
        }
    }
}
