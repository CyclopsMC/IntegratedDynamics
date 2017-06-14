package org.cyclops.integrateddynamics.part;

import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;

/**
 * A basic wireless connector part.
 * @author rubensworks
 */
public class PartTypeConnectorMonoDirectional extends PartTypeConnector<PartTypeConnectorMonoDirectional, PartStateEmpty<PartTypeConnectorMonoDirectional>> {

    public PartTypeConnectorMonoDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.25F, 0.5F, 0.5F));
    }

    @Override
    public PartStateEmpty<PartTypeConnectorMonoDirectional> constructDefaultState() {
        return new PartStateEmpty<PartTypeConnectorMonoDirectional>();
    }

    @Override
    public Class<? super PartTypeConnectorMonoDirectional> getPartTypeClass() {
        return PartTypeConnectorMonoDirectional.class;
    }
}
