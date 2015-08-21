package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.client.gui.GuiPartDisplay;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.DefaultPartStateActiveVariable;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;

/**
 * A part that can display variables..
 * @author rubensworks
 */
public class PartTypeDisplay extends PartTypeBase<PartTypeDisplay, PartTypeDisplay.State> {

    public PartTypeDisplay(String name) {
        super(name);
    }

    @Override
    public Class<? super PartTypeDisplay> getPartTypeClass() {
        return PartTypeDisplay.class;
    }

    @Override
    public float getWidthFactor() {
        return 0.1875F;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    public PartTypeDisplay.State constructDefaultState() {
        return new PartTypeDisplay.State(1);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartDisplay.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiPartDisplay.class;
    }

    public static class State extends DefaultPartStateActiveVariable<PartTypeDisplay> {

        public State(int inventorySize) {
            super(inventorySize);
        }

        @Override
        public Class<? extends IPartState> getPartStateClass() {
            return PartTypeDisplay.State.class;
        }
    }

}
