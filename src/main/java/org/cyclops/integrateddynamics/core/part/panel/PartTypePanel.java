package org.cyclops.integrateddynamics.core.part.panel;

import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

/**
 * A base part that is flat and can be used to render things on.
 * @author rubensworks
 */
public abstract class PartTypePanel<P extends PartTypePanel<P, S>, S extends IPartState<P>> extends PartTypeBase<P, S> {

    public PartTypePanel(String name) {
        super(name, new PartRenderPosition(0.125F, 0.1875F, 0.625F, 0.625F));
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus();
    }

}
