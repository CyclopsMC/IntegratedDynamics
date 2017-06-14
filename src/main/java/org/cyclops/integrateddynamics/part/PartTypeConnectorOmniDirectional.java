package org.cyclops.integrateddynamics.part;

import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;

/**
 * An advanced wireless connector part.
 * @author rubensworks
 */
public class PartTypeConnectorOmniDirectional extends PartTypeConnector<PartTypeConnectorOmniDirectional, PartStateEmpty<PartTypeConnectorOmniDirectional>> {

    public PartTypeConnectorOmniDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.25F, 0.625F, 0.625F));
    }

    @Override
    public PartStateEmpty<PartTypeConnectorOmniDirectional> constructDefaultState() {
        return new PartStateEmpty<PartTypeConnectorOmniDirectional>();
    }

    @Override
    public Class<? super PartTypeConnectorOmniDirectional> getPartTypeClass() {
        return PartTypeConnectorOmniDirectional.class;
    }
}
