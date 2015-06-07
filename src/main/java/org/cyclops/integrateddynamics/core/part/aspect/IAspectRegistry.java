package org.cyclops.integrateddynamics.core.part.aspect;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.Collection;
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
     * @return The registered element.
     */
    public IAspect register(IPartType partType, IAspect aspect);

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
     * Get all registered aspects.
     * @return The aspects.
     */
    public Set<IAspect> getAspects();

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

    /**
     * Register a model resource location for the given aspect.
     * @param aspect The aspect.
     * @param modelLocation The model resource location.
     */
    @SideOnly(Side.CLIENT)
    public void registerAspectModel(IAspect aspect, ModelResourceLocation modelLocation);

    /**
     * Get the model resource location of the given aspect.
     * @param aspect The aspect.
     * @return The model resource location.
     */
    @SideOnly(Side.CLIENT)
    public  ModelResourceLocation getAspectModel(IAspect aspect);

    /**
     * Get all registered model resource locations for the aspects.
     * @return All model resource locations.
     */
    @SideOnly(Side.CLIENT)
    public Collection<ModelResourceLocation> getAspectModels();

}
