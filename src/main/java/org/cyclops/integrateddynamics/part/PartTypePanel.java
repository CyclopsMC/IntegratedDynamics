package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

/**
 * A base part that is flat and can be used to render things on.
 * @author rubensworks
 */
public abstract class PartTypePanel<P extends PartTypePanel<P, S>, S extends IPartState<P>> extends PartTypeBase<P, S> {

    public PartTypePanel(String name) {
        super(name, new RenderPosition(0.1875F, 0.625F, 0.625F));
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

}
