package org.cyclops.integrateddynamics.api.item;

import org.cyclops.integrateddynamics.api.part.aspect.IAspect;

/**
 * Variable facade for variables determined by part aspects.
 * @author rubensworks
 */
public interface IAspectVariableFacade extends IVariableFacade {

    /**
     * @return The part id this aspect is part of.
     */
    public int getPartId();

    /**
     * @return The used active in the selected part.
     */
    public IAspect getAspect();

}
