package org.cyclops.integrateddynamics.part;

import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import java.util.Collections;
import java.util.Set;

/**
 * A basic wireless connector part.
 * @author rubensworks
 */
public class PartTypeConnectorMonoDirectional extends PartTypeConnector<PartTypeConnectorMonoDirectional, PartTypeConnectorMonoDirectional.State> {

    public PartTypeConnectorMonoDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.3125F, 0.5F, 0.5F));
    }

    @Override
    public PartTypeConnectorMonoDirectional.State constructDefaultState() {
        return new PartTypeConnectorMonoDirectional.State();
    }

    @Override
    public Class<? super PartTypeConnectorMonoDirectional> getPartTypeClass() {
        return PartTypeConnectorMonoDirectional.class;
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorMonoDirectional> {

        @Override
        public Set<IPathElement> getReachableElements() {
            return Collections.emptySet(); // TODO
        }
    }

}
