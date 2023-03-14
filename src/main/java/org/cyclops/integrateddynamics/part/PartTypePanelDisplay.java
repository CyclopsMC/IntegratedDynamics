package org.cyclops.integrateddynamics.part;

import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * A part that can display variables.
 * @author rubensworks
 */
public class PartTypePanelDisplay extends PartTypePanelVariableDriven<PartTypePanelDisplay, PartTypePanelDisplay.State> {

    public PartTypePanelDisplay(String name) {
        super(name);
    }

    @Override
    public boolean supportsOffsets() {
        return false;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus();
    }

    @Override
    public PartTypePanelDisplay.State constructDefaultState() {
        return new PartTypePanelDisplay.State();
    }

    @Override
    public int getConsumptionRate(State state) {
        return state.hasVariable() ? GeneralConfig.panelDisplayBaseConsumptionEnabled : GeneralConfig.panelDisplayBaseConsumptionDisabled;
    }

    @Override
    public boolean forceLightTransparency(State state) {
        return true;
    }

    public static class State extends PartTypePanelVariableDriven.State<PartTypePanelDisplay, PartTypePanelDisplay.State> {

    }

}
