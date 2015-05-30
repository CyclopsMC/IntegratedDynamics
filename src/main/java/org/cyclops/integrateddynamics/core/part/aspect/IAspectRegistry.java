package org.cyclops.integrateddynamics.core.part.aspect;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.Set;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.part.aspect.IAspect}.
 * @author rubensworks
 */
public interface IAspectRegistry extends IRegistry {

    /**
     * Register a new aspect for a given part type.
     * @param partType The part type.
     * @param aspect The aspect.
     */
    public void register(IPartType partType, IAspect aspect);

    /**
     * Register a set of aspects for a given part type.
     * @param partType The part type.
     * @param aspects The aspects.
     */
    public void register(IPartType partType, Set<IAspect> aspects);

    /**
     * Get the registered aspects for a given part type.
     * @param partType The part type.
     * @return The aspects.
     */
    public Set<IAspect> getAspects(IPartType partType);

    /**
     * Get an aspect by unlocalized name.
     * @param unlocalizedName The unlocalized name of the aspect.
     * @return The matching aspect.
     */
    public IAspect getAspect(String unlocalizedName);

    /**
     * Write aspect info to an item.
     * @param baseItemStack The item to write to.
     * @param partId The id of the part the given aspect belongs to.
     * @param aspect The aspect in the given part.
     * @return The item with aspect info.
     */
    public ItemStack writeAspect(ItemStack baseItemStack, int partId, IAspect aspect);

    /**
     * Read aspect info from a given item./
     * @param itemStack The item containing aspect information.
     * @return A pair of part id and aspect type. Will be null if the item was invalid.
     */
    public Pair<Integer, IAspect> readAspect(ItemStack itemStack);

}
