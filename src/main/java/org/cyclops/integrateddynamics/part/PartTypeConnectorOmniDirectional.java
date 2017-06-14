package org.cyclops.integrateddynamics.part;

import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import java.util.Collections;
import java.util.Set;

/**
 * An advanced wireless connector part.
 * @author rubensworks
 */
public class PartTypeConnectorOmniDirectional extends PartTypeConnector<PartTypeConnectorOmniDirectional, PartTypeConnectorOmniDirectional.State> {

    public PartTypeConnectorOmniDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.3125F, 0.625F, 0.625F));
    }

    @Override
    public PartTypeConnectorOmniDirectional.State constructDefaultState() {
        return new PartTypeConnectorOmniDirectional.State();
    }

    @Override
    public Class<? super PartTypeConnectorOmniDirectional> getPartTypeClass() {
        return PartTypeConnectorOmniDirectional.class;
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorOmniDirectional> {

        @Override
        public Set<IPathElement> getReachableElements() {
            return Collections.emptySet(); // TODO
        }
    }
}
