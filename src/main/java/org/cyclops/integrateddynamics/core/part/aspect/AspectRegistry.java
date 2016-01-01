package org.cyclops.integrateddynamics.core.part.aspect;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;

import java.util.*;

/**
 * Registry for {@link IAspect}.
 * @author rubensworks
 */
public final class AspectRegistry implements IAspectRegistry {

    private static AspectRegistry INSTANCE = new AspectRegistry();
    private static final IAspectVariableFacade INVALID_FACADE = new AspectVariableFacade(false, -1, null);

    private Map<IPartType, Set<IAspect>> partAspects = Maps.newHashMap();
    private Map<IPartType, Set<IAspectRead>> partReadAspects = Maps.newHashMap();
    private Map<IPartType, Set<IAspectWrite>> partWriteAspects = Maps.newHashMap();
    private Map<IPartType, List<IAspectRead>> partReadAspectsListTransform = Maps.newHashMap();
    private Map<IPartType, List<IAspectWrite>> partWriteAspectsListTransform = Maps.newHashMap();
    private Map<String, IAspect> unlocalizedAspects = Maps.newHashMap();
    private Map<String, IAspectRead> unlocalizedReadAspects = Maps.newHashMap();
    private Map<String, IAspectWrite> unlocalizedWriteAspects = Maps.newHashMap();
    @SideOnly(Side.CLIENT)
    private Map<IAspect, ResourceLocation> aspectModels;

    private AspectRegistry() {
        IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(this);
        if(MinecraftHelpers.isClientSide()) {
            aspectModels = Maps.newHashMap();
        }
    }

    /**
     * @return The unique instance.
     */
    public static AspectRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public IAspect register(IPartType partType, IAspect aspect) {
        registerSubAspectType(partType, aspect, partAspects, unlocalizedAspects);
        if(aspect instanceof IAspectRead) {
            registerSubAspectType(partType, (IAspectRead) aspect, partReadAspects, unlocalizedReadAspects);
            partReadAspectsListTransform.put(partType, Lists.newArrayList(partReadAspects.get(partType)));
        }
        if(aspect instanceof IAspectWrite) {
            registerSubAspectType(partType, (IAspectWrite) aspect, partWriteAspects, unlocalizedWriteAspects);
            partWriteAspectsListTransform.put(partType, Lists.newArrayList(partWriteAspects.get(partType)));
        }
        return aspect;
    }

    protected <T extends IAspect> void registerSubAspectType(IPartType partType, T aspect, Map<IPartType,
                                                             Set<T>> partAspects, Map<String, T> unlocalizedAspects) {
        Set<T> aspects = partAspects.get(partType);
        if(aspects == null) {
            aspects = Sets.newTreeSet(IAspect.AspectComparator.getInstance());
            partAspects.put(partType, aspects);
        }
        aspects.add(aspect);
        unlocalizedAspects.put(aspect.getUnlocalizedName(), aspect);
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
            return Collections.unmodifiableSet(Collections.<IAspect>emptySet());
        }
        return Collections.unmodifiableSet(partAspects.get(partType));
    }

    @Override
    public List<IAspectRead> getReadAspects(IPartType partType) {
        return Collections.unmodifiableList(partReadAspectsListTransform.get(partType));
    }

    @Override
    public List<IAspectWrite> getWriteAspects(IPartType partType) {
        return Collections.unmodifiableList(partWriteAspectsListTransform.get(partType));
    }

    @Override
    public Set<IAspect> getAspects() {
        return ImmutableSet.copyOf(unlocalizedAspects.values());
    }

    @Override
    public Set<IAspectRead> getReadAspects() {
        return ImmutableSet.copyOf(unlocalizedReadAspects.values());
    }

    @Override
    public Set<IAspectWrite> getWriteAspects() {
        return ImmutableSet.copyOf(unlocalizedWriteAspects.values());
    }

    @Override
    public IAspect getAspect(String unlocalizedName) {
        return unlocalizedAspects.get(unlocalizedName);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerAspectModel(IAspect aspect, ResourceLocation modelLocation) {
        aspectModels.put(aspect, modelLocation);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getAspectModel(IAspect aspect) {
        return aspectModels.get(aspect);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Collection<ResourceLocation> getAspectModels() {
        return Collections.unmodifiableCollection(aspectModels.values());
    }

    @Override
    public String getTypeId() {
        return "aspect";
    }

    @Override
    public IAspectVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
        if(!tag.hasKey("partId", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())
                || !tag.hasKey("aspectName", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
            return INVALID_FACADE;
        }
        int partId = tag.getInteger("partId");
        IAspect aspect = getAspect(tag.getString("aspectName"));
        if(aspect == null) {
            return INVALID_FACADE;
        }
        return new AspectVariableFacade(id, partId, aspect);
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, IAspectVariableFacade variableFacade) {
        tag.setInteger("partId", variableFacade.getPartId());
        tag.setString("aspectName", variableFacade.getAspect().getUnlocalizedName());
    }
}
