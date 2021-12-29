package org.cyclops.integrateddynamics.core.part;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.AttachCapabilitiesEventPart;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

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
    private Direction targetSide = null;
    private int id = -1;
    private Map<IAspect, IAspectProperties> aspectProperties = new IdentityHashMap<>();
    private boolean enabled = true;

    private CapabilityDispatcher capabilities = null;
    private IdentityHashMap<Capability<?>, LazyOptional<Object>> volatileCapabilities = new IdentityHashMap<>();

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
        if (this.capabilities != null) {
            tag.put("ForgeCaps", this.capabilities.serializeNBT());
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        this.updateInterval = tag.getInt("updateInterval");
        this.priority = tag.getInt("priority");
        this.channel = tag.getInt("channel");
        if (tag.contains("targetSide", Tag.TAG_INT)) {
            this.targetSide = Direction.values()[tag.getInt("targetSide")];
        }
        this.id = tag.getInt("id");
        this.aspectProperties.clear();
        readAspectProperties("aspectProperties", tag);
        this.enabled = tag.getBoolean("enabled");
        if (this.capabilities != null && tag.contains("ForgeCaps")) {
            this.capabilities.deserializeNBT(tag.getCompound("ForgeCaps"));
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

    public void readAspectProperties(String name, CompoundTag tag) {
        CompoundTag mapTag = tag.getCompound(name);
        ListTag list = mapTag.getList("map", Tag.TAG_COMPOUND);
        if(list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                IAspect key = Aspects.REGISTRY.getAspect(new ResourceLocation(entryTag.getString("key")));
                IAspectProperties value = null;
                if (entryTag.contains("value")) {
                    value = new AspectProperties();
                    value.fromNBT(entryTag.getCompound("value"));
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
    public void setTargetSideOverride(Direction targetSide) {
        this.targetSide = targetSide;
    }

    @Nullable
    @Override
    public Direction getTargetSideOverride() {
        return targetSide;
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

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     */
    public void gatherCapabilities(P partType) {
        AttachCapabilitiesEventPart event = new AttachCapabilitiesEventPart(partType, this);
        MinecraftForge.EVENT_BUS.post(event);
        this.capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities(), event.getListeners()) : null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        LazyOptional<Object> o = volatileCapabilities.get(capability);
        if(o != null && o.isPresent()) {
            return o.cast();
        }
        return capabilities == null ? LazyOptional.empty() : capabilities.getCapability(capability);
    }

    @Override
    public <T> void addVolatileCapability(Capability<T> capability, LazyOptional<T> value) {
        volatileCapabilities.put(capability, (LazyOptional<Object>) value);
    }

    @Override
    public void removeVolatileCapability(Capability<?> capability) {
        volatileCapabilities.remove(capability);
    }

    protected int getDefaultUpdateInterval() {
        return GeneralConfig.defaultPartUpdateFreq;
    }
}
