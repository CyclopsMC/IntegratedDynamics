package org.cyclops.integrateddynamics.part;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;

/**
 * A panel part that simply emits light.
 * @author rubensworks
 */
public class PartTypePanelLightStatic extends PartTypePanel<PartTypePanelLightStatic, PartStateEmpty<PartTypePanelLightStatic>> {

    public static final int LIGHT_LEVEL = 15;

    public PartTypePanelLightStatic(String name) {
        super(name);
    }

    @Override
    public boolean supportsOffsets() {
        return false;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock();
    }

    @Override
    public PartStateEmpty<PartTypePanelLightStatic> constructDefaultState() {
        return new PartStateEmpty<PartTypePanelLightStatic>();
    }

    @Override
    public int getConsumptionRate(PartStateEmpty<PartTypePanelLightStatic> state) {
        return GeneralConfig.panelLightStaticBaseConsumption;
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkAddition(network, partNetwork, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target,
                                      PartStateEmpty<PartTypePanelLightStatic> state, BlockGetter world,
                                      Block neighbourBlock, BlockPos neighbourPos) {
        super.onBlockNeighborChange(network, partNetwork, target, state, world, neighbourBlock, neighbourPos);
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkRemoval(network, partNetwork, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onPostRemoved(network, partNetwork, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }

    @Override
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state, boolean updated) {
        boolean wasEnabled = isEnabled(state);
        super.postUpdate(network, partNetwork, target, state, updated);
        boolean isEnabled = isEnabled(state);
        if(wasEnabled != isEnabled) {
            PartTypePanelLightDynamic.setLightLevel(target, isEnabled ? LIGHT_LEVEL : 0);
        }
    }
}
