package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.ResourceLocation;

/**
 * A network element that is identifiable within a certain group.
 * Element id's must be unique within the given group.
 * @author rubensworks
 */
public interface IIdentifiableNetworkElement {

    /**
     * @return The unique id of this element.
     */
    public int getId();

    /**
     * @return A group within which the element is unique.
     */
    public ResourceLocation getGroup();

}
