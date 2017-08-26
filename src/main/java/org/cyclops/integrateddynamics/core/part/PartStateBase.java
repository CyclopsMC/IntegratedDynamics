package org.cyclops.integrateddynamics.core.part;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.AttachCapabilitiesEventPart;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A default implementation of the {@link IPartState}.
 * @author rubensworks
 */
public abstract class PartStateBase<P extends IPartType> implements IPartState<P>, IDirtyMarkListener {

    private boolean dirty = false;
    private boolean update = false;

    private int updateInterval = getDefaultUpdateInterval();
    private int priority = 0;
    private int id = -1;
    private Map<IAspect, IAspectProperties> aspectProperties = new IdentityHashMap<>();
    private boolean enabled = true;

    private CapabilityDispatcher capabilities = null;
    private IdentityHashMap<Capability<?>, Object> volatileCapabilities = new IdentityHashMap<>();

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("updateInterval", this.updateInterval);
        tag.setInteger("priority", this.priority);
        tag.setInteger("id", this.id);
        writeAspectProperties("aspectProperties", tag);
        tag.setBoolean("enabled", this.enabled);
        if (this.capabilities != null) {
            tag.setTag("ForgeCaps", this.capabilities.serializeNBT());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.updateInterval = tag.getInteger("updateInterval");
        this.priority = tag.getInteger("priority");
        this.id = tag.getInteger("id");
        this.aspectProperties.clear();
        readAspectProperties("aspectProperties", tag);
        this.enabled = tag.getBoolean("enabled");
        if (this.capabilities != null && tag.hasKey("ForgeCaps")) {
            this.capabilities.deserializeNBT(tag.getCompoundTag("ForgeCaps"));
        }
    }

    protected void writeAspectProperties(String name, NBTTagCompound tag) {
        NBTTagCompound mapTag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for(Map.Entry<IAspect, IAspectProperties> entry : aspectProperties.entrySet()) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setString("key", entry.getKey().getUnlocalizedName());
            if(entry.getValue() != null) {
                entryTag.setTag("value", entry.getValue().toNBT());
            }
            list.appendTag(entryTag);
        }
        mapTag.setTag("map", list);
        tag.setTag(name, mapTag);
    }

    public void readAspectProperties(String name, NBTTagCompound tag) {
        NBTTagCompound mapTag = tag.getCompoundTag(name);
        NBTTagList list = mapTag.getTagList("map", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        if(list.tagCount() > 0) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entryTag = list.getCompoundTagAt(i);
                IAspect key = Aspects.REGISTRY.getAspect(entryTag.getString("key"));
                IAspectProperties value = null;
                if (entryTag.hasKey("value")) {
                    value = new AspectProperties();
                    value.fromNBT(entryTag.getCompoundTag("value"));
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
    public void onDirty() {
        this.dirty = true;
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

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     */
    public void gatherCapabilities(P partType) {
        AttachCapabilitiesEventPart event = new AttachCapabilitiesEventPart(partType, this);
        MinecraftForge.EVENT_BUS.post(event);
        this.capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities()) : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability) {
        return volatileCapabilities.containsKey(capability)
                || (capabilities != null && capabilities.hasCapability(capability, null));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        Object o = volatileCapabilities.get(capability);
        if(o != null) {
            return (T) o;
        }
        return capabilities == null ? null : capabilities.getCapability(capability, null);
    }

    @Override
    public <T> void addVolatileCapability(Capability<T> capability, T value) {
        volatileCapabilities.put(capability, value);
    }

    @Override
    public void removeVolatileCapability(Capability<?> capability) {
        volatileCapabilities.remove(capability);
    }

    protected int getDefaultUpdateInterval() {
        return GeneralConfig.defaultPartUpdateFreq;
    }
}
