package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.block.BlockInvisibleLightConfig;
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

    public static final int LIGHT_LEVEL = 15;

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
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    // The update methods are only required in the case of BlockInvisibleLight

    @Override
    public void update(Network network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.update(network, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    @Override
    public boolean isUpdate(PartStateEmpty<PartTypePanelLightStatic> state) {
        return ConfigHandler.isEnabled(BlockInvisibleLightConfig.class);
    }

    @Override
    public void onNetworkRemoval(Network network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkRemoval(network, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }
}
