package org.cyclops.integrateddynamics.core.part.aspect;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.part.aspect.IAspect}.
 * @author rubensworks
 */
public final class AspectRegistry implements IAspectRegistry {

    private static AspectRegistry INSTANCE = new AspectRegistry();

    private Map<IPartType, Set<IAspect>> partAspects = Maps.newHashMap();
    private Map<String, IAspect> unlocalizedAspects = Maps.newHashMap();

    private AspectRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static AspectRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public IAspect register(IPartType partType, IAspect aspect) {
        Set<IAspect> aspects = partAspects.get(partType);
        if(aspects == null) {
            aspects = Sets.newHashSet();
            partAspects.put(partType, aspects);
        }
        aspects.add(aspect);
        unlocalizedAspects.put(aspect.getUnlocalizedName(), aspect);
        return aspect;
    }

    @Override
    public void register(IPartType partType, Set<IAspect> aspects) {
        for(IAspect aspect : aspects) {
            register(partType, aspect);
        }
    }

    @Override
    public Set<IAspect> getAspects(IPartType partType) {
        if(!partAspects.containsKey(partType)) {
            return Collections.emptySet();
        }
        return partAspects.get(partType);
    }

    @Override
    public IAspect getAspect(String unlocalizedName) {
        return unlocalizedAspects.get(unlocalizedName);
    }

    @Override
    public ItemStack writeAspect(ItemStack baseItemStack, int partId, IAspect aspect) {
        ItemStack itemStack = baseItemStack.copy();
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        tag.setInteger("partId", partId);
        tag.setString("aspectName", aspect.getUnlocalizedName());
        return itemStack;
    }

    @Override
    public Pair<Integer, IAspect> readAspect(ItemStack itemStack) {
        if(!itemStack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        if(!tag.hasKey("partId", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())
                || !tag.hasKey("aspectName", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
            return null;
        }
        int pairId = tag.getInteger("partId");
        IAspect aspect = getAspect(tag.getString("aspectName"));
        if(aspect == null) {
            return null;
        }
        return Pair.of(pairId, aspect);
    }

}
