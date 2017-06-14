package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

/**
 * A base wireless connector part.
 * @author rubensworks
 */
public abstract class PartTypeConnector<P extends PartTypeConnector<P, S>, S extends IPartState<P>> extends PartTypeBase<P, S> {

    public PartTypeConnector(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
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
}
