package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoader;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.AttachCapabilitiesEventPart;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A default implementation of the {@link IPartState}.
 * @author rubensworks
 */
public abstract class PartStateBase<P extends IPartType> implements IPartState<P>, IDirtyMarkListener {

    private boolean dirty = false;
    private boolean update = false;
    private boolean forceBlockUpdateRender = false;

    private int updateInterval = getDefaultUpdateInterval();
    private int priority = 0;
    private int channel = 0;
    private int maxOffset;
    private Vec3i targetOffset = new Vec3i(0, 0, 0);
    private Direction targetSide = null;
    private int id = -1;
    private Map<IAspect, IAspectProperties> aspectProperties = new IdentityHashMap<>();
    private boolean enabled = true;
    private final Map<String, NonNullList<ItemStack>> inventoriesNamed = Maps.newHashMap();
    private final PartStateOffsetHandler<P> offsetHandler = new PartStateOffsetHandler<>();

    private IdentityHashMap<PartCapability<?>, Optional<Object>> volatileCapabilities = new IdentityHashMap<>();

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("updateInterval", this.updateInterval);
        tag.putInt("priority", this.priority);
        tag.putInt("channel", this.channel);
        if (this.targetSide != null) {
            tag.putInt("targetSide", this.targetSide.ordinal());
        }
        tag.putInt("id", this.id);
        writeAspectProperties("aspectProperties", tag);
        tag.putBoolean("enabled", this.enabled);
        tag.putInt("maxOffset", this.maxOffset);
        tag.putInt("offsetX", this.targetOffset.getX());
        tag.putInt("offsetY", this.targetOffset.getY());
        tag.putInt("offsetZ", this.targetOffset.getZ());

        // Write inventoriesNamed
        ListTag namedInventoriesList = new ListTag();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.inventoriesNamed.entrySet()) {
            CompoundTag listEntry = new CompoundTag();
            listEntry.putString("tabName", entry.getKey());
            listEntry.putInt("itemCount", entry.getValue().size());
            ContainerHelper.saveAllItems(listEntry, entry.getValue());
            namedInventoriesList.add(listEntry);
        }
        tag.put("inventoriesNamed", namedInventoriesList);

        CompoundTag errorsTag = new CompoundTag();
        for (Int2ObjectMap.Entry<MutableComponent> entry : this.offsetHandler.offsetVariablesSlotMessages.int2ObjectEntrySet()) {
            NBTClassType.writeNbt(MutableComponent.class, String.valueOf(entry.getIntKey()), entry.getValue(), errorsTag);
        }
        tag.put("offsetVariablesSlotMessages", errorsTag);
    }

    @Override
    public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
        this.updateInterval = tag.getInt("updateInterval");
        this.priority = tag.getInt("priority");
        this.channel = tag.getInt("channel");
        if (tag.contains("targetSide", Tag.TAG_INT)) {
            this.targetSide = Direction.values()[tag.getInt("targetSide")];
        }
        this.id = tag.getInt("id");
        this.aspectProperties.clear();
        readAspectProperties(valueDeseralizationContext, "aspectProperties", tag);
        this.enabled = tag.getBoolean("enabled");
        this.maxOffset = tag.getInt("maxOffset");
        this.targetOffset = new Vec3i(tag.getInt("offsetX"), tag.getInt("offsetY"), tag.getInt("offsetZ"));

        // Read inventoriesNamed
        for (Tag listEntry : tag.getList("inventoriesNamed", Tag.TAG_COMPOUND)) {
            NonNullList<ItemStack> list = NonNullList.withSize(((CompoundTag) listEntry).getInt("itemCount"), ItemStack.EMPTY);
            String tabName = ((CompoundTag) listEntry).getString("tabName");
            ContainerHelper.loadAllItems((CompoundTag) listEntry, list);
            this.inventoriesNamed.put(tabName, list);
        }

        this.offsetHandler.offsetVariablesSlotMessages.clear();
        CompoundTag errorsTag = tag.getCompound("offsetVariablesSlotMessages");
        for (String slot : errorsTag.getAllKeys()) {
            MutableComponent unlocalizedString = NBTClassType.readNbt(MutableComponent.class, slot, errorsTag);
            this.offsetHandler.offsetVariablesSlotMessages.put(Integer.parseInt(slot), unlocalizedString);
        }
    }

    protected void writeAspectProperties(String name, CompoundTag tag) {
        CompoundTag mapTag = new CompoundTag();
        ListTag list = new ListTag();
        for(Map.Entry<IAspect, IAspectProperties> entry : aspectProperties.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey().getUniqueName().toString());
            if(entry.getValue() != null) {
                entryTag.put("value", entry.getValue().toNBT());
            }
            list.add(entryTag);
        }
        mapTag.put("map", list);
        tag.put(name, mapTag);
    }

    public void readAspectProperties(ValueDeseralizationContext valueDeseralizationContext, String name, CompoundTag tag) {
        CompoundTag mapTag = tag.getCompound(name);
        ListTag list = mapTag.getList("map", Tag.TAG_COMPOUND);
        if(list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                IAspect key = Aspects.REGISTRY.getAspect(new ResourceLocation(entryTag.getString("key")));
                IAspectProperties value = null;
                if (entryTag.contains("value")) {
                    value = new AspectProperties();
                    value.fromNBT(valueDeseralizationContext, entryTag.getCompound("value"));
                }
                if (key != null && value != null) {
                    this.aspectProperties.put(key, value);
                }
            }
        }
    }

    @Override
    public void generateId() {
        this.id = IntegratedDynamics.globalCounters.getNext(IPartState.GLOBALCOUNTER_KEY);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public Vec3i getTargetOffset() {
        return targetOffset;
    }

    @Override
    public void setTargetOffset(Vec3i targetOffset) {
        this.targetOffset = targetOffset;
        this.markDirty();
    }

    @Override
    public void setTargetSideOverride(Direction targetSide) {
        this.targetSide = targetSide;
    }

    @Nullable
    @Override
    public Direction getTargetSideOverride() {
        return targetSide;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirtyAndReset() {
        boolean wasDirty = this.dirty;
        this.dirty = false;
        return wasDirty;
    }

    @Override
    public boolean isUpdateAndReset() {
        boolean wasUpdate = this.update;
        this.update = false;
        return wasUpdate;
    }

    @Override
    public void forceBlockRenderUpdate() {
        this.forceBlockUpdateRender = true;
    }

    @Override
    public boolean isForceBlockRenderUpdateAndReset() {
        boolean wasForceBlockUpdateRender = this.forceBlockUpdateRender;
        this.forceBlockUpdateRender = false;
        return wasForceBlockUpdateRender;
    }

    @Override
    public void onDirty() {
        this.dirty = true;
        this.forceBlockRenderUpdate();
    }

    /**
     * Enables a flag that tells the part container to send an NBT update to the client(s).
     */
    public void sendUpdate() {
        this.update = true;
    }

    @Override
    public IAspectProperties getAspectProperties(IAspect aspect) {
        return aspectProperties.get(aspect);
    }

    @Override
    public void setAspectProperties(IAspect aspect, IAspectProperties properties) {
        aspectProperties.put(aspect, properties);
        sendUpdate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean wasEnabled = this.enabled;
        this.enabled = enabled;
        if (this.enabled != wasEnabled) {
            sendUpdate();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public NonNullList<ItemStack> getInventoryNamed(String name) {
        return this.inventoriesNamed.get(name);
    }

    public void setInventoryNamed(String name, NonNullList<ItemStack> inventory) {
        this.inventoriesNamed.put(name, inventory);
        onDirty();
    }

    @Override
    public Map<String, NonNullList<ItemStack>> getInventoriesNamed() {
        return this.inventoriesNamed;
    }

    @Override
    public void clearInventoriesNamed() {
        this.inventoriesNamed.clear();
    }

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     */
    public void gatherCapabilities(P partType) {
        AttachCapabilitiesEventPart event = new AttachCapabilitiesEventPart(partType, this);
        ModLoader.get().postEventWrapContainerInModOrder(event);
    }

    @Override
    public <T> Optional<T> getCapability(P partType, PartCapability<T> capability, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        Optional<Object> o = volatileCapabilities.get(capability);
        if(o != null && o.isPresent()) {
            return (Optional<T>) o;
        }
        return Optional.ofNullable(capability.getCapability(partType, target));
    }

    @Override
    public <T> void addVolatileCapability(PartCapability<T> capability, Optional<T> value) {
        volatileCapabilities.put(capability, (Optional<Object>) value);
    }

    @Override
    public void removeVolatileCapability(PartCapability<?> capability) {
        volatileCapabilities.remove(capability);
    }

    protected int getDefaultUpdateInterval() {
        return GeneralConfig.defaultPartUpdateFreq;
    }

    @Override
    public void initializeOffsets(PartTarget target) {
        this.offsetHandler.initializeVariableEvaluators(this.offsetHandler.getOffsetVariablesInventory(this), target);
    }

    @Override
    public void updateOffsetVariables(P partType, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        this.offsetHandler.updateOffsetVariables(partType, this, network, partNetwork, target);
    }

    @Nullable
    @Override
    public MutableComponent getOffsetVariableError(int slot) {
        return this.offsetHandler.getOffsetVariableError(slot);
    }

    @Override
    public boolean requiresOffsetUpdates() {
        return this.offsetHandler.offsetVariableEvaluators.stream().anyMatch(InventoryVariableEvaluator::hasVariable);
    }

    @Override
    public void markOffsetVariablesChanged() {
        this.offsetHandler.markOffsetVariablesChanged();
    }

    @Override
    public int getMaxOffset() {
        return maxOffset;
    }

    @Override
    public void setMaxOffset(int maxOffset) {
        this.maxOffset = maxOffset;
        markDirty();
    }
}
