package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.*;
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
    private final PartStateAspectVariablesHandler<P> aspectVariablesHandler = new PartStateAspectVariablesHandler<>();

    private IdentityHashMap<PartCapability<?>, Optional<Object>> volatileCapabilities = new IdentityHashMap<>();

    @Override
    public void serialize(ValueOutput valueOutput) {
        valueOutput.putInt("updateInterval", this.updateInterval);
        valueOutput.putInt("priority", this.priority);
        valueOutput.putInt("channel", this.channel);
        if (this.targetSide != null) {
            valueOutput.putInt("targetSide", this.targetSide.ordinal());
        }
        valueOutput.putInt("id", this.id);
        writeAspectProperties(valueOutput.child("aspectProperties"));
        valueOutput.putBoolean("enabled", this.enabled);
        valueOutput.putInt("maxOffset", this.maxOffset);
        valueOutput.putInt("offsetX", this.targetOffset.getX());
        valueOutput.putInt("offsetY", this.targetOffset.getY());
        valueOutput.putInt("offsetZ", this.targetOffset.getZ());

        // Write inventoriesNamed
        ValueOutput.ValueOutputList namedInventoriesList = valueOutput.childrenList("inventoriesNamed");
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.inventoriesNamed.entrySet()) {
            ValueOutput listEntry = namedInventoriesList.addChild();
            listEntry.putString("tabName", entry.getKey());
            listEntry.putInt("itemCount", entry.getValue().size());
            ContainerHelper.saveAllItems(listEntry, entry.getValue());
        }

        ValueOutput.ValueOutputList errorsTag = valueOutput.childrenList("offsetVariablesSlotMessages");
        for (Int2ObjectMap.Entry<MutableComponent> entry : this.offsetHandler.offsetVariablesSlotMessages.int2ObjectEntrySet()) {
            ValueOutput child = errorsTag.addChild();
            String slot = String.valueOf(entry.getIntKey());
            child.putString("slot", slot);
            NBTClassType.writeNbt(Component.class, slot, entry.getValue(), child);
        }

        // Write the aspect setting variable errors, so that they are also known client-side
        ValueOutput.ValueOutputList aspectErrorsTag = valueOutput.childrenList("aspectVariablesSlotMessages");
        for (Map.Entry<IAspect, Int2ObjectMap<MutableComponent>> entry : this.aspectVariablesHandler.getAspectVariablesSlotMessages().entrySet()) {
            for (Int2ObjectMap.Entry<MutableComponent> slotEntry : entry.getValue().int2ObjectEntrySet()) {
                ValueOutput child = aspectErrorsTag.addChild();
                String slot = String.valueOf(slotEntry.getIntKey());
                child.putString("aspect", entry.getKey().getUniqueName().toString());
                child.putString("slot", slot);
                NBTClassType.writeNbt(Component.class, slot, slotEntry.getValue(), child);
            }
        }
    }

    @Override
    public void deserialize(ValueInput valueInput) {
        this.updateInterval = valueInput.getInt("updateInterval").orElseThrow();
        this.priority = valueInput.getInt("priority").orElseThrow();
        this.channel = valueInput.getInt("channel").orElseThrow();
        this.targetSide = valueInput.getInt("targetSide")
                .map(s -> Direction.values()[s])
                .orElse(null);
        this.id = valueInput.getInt("id").orElseThrow();
        this.aspectProperties.clear();
        readAspectProperties(valueInput.child("aspectProperties").orElseThrow());
        this.enabled = valueInput.getBooleanOr("enabled", false);
        this.maxOffset = valueInput.getInt("maxOffset").orElseThrow();
        this.targetOffset = new Vec3i(
                valueInput.getInt("offsetX").orElseThrow(),
                valueInput.getInt("offsetY").orElseThrow(),
                valueInput.getInt("offsetZ").orElseThrow()
        );

        // Read inventoriesNamed
        for (ValueInput listEntry : valueInput.childrenList("inventoriesNamed").orElseThrow()) {
            NonNullList<ItemStack> list = NonNullList.withSize(listEntry.getInt("itemCount").orElseThrow(), ItemStack.EMPTY);
            String tabName = listEntry.getString("tabName").orElseThrow();
            ContainerHelper.loadAllItems(listEntry, list);
            this.inventoriesNamed.put(tabName, list);
        }

        this.offsetHandler.offsetVariablesSlotMessages.clear();
        ValueInput.ValueInputList errorsTag = valueInput.childrenList("offsetVariablesSlotMessages").orElseThrow();
        for (ValueInput child : errorsTag) {
            String slot = child.getString("slot").orElseThrow();
            MutableComponent unlocalizedString = NBTClassType.readNbt(Component.class, slot, child).copy();
            this.offsetHandler.offsetVariablesSlotMessages.put(Integer.parseInt(slot), unlocalizedString);
        }

        this.aspectVariablesHandler.getAspectVariablesSlotMessages().clear();
        for (ValueInput child : valueInput.childrenListOrEmpty("aspectVariablesSlotMessages")) {
            Identifier aspectId = Identifier.tryParse(child.getString("aspect").orElseThrow());
            IAspect aspect = aspectId == null ? null : Aspects.REGISTRY.getAspect(aspectId);
            if (aspect != null) {
                String slot = child.getString("slot").orElseThrow();
                this.aspectVariablesHandler.getAspectVariablesSlotMessages(aspect)
                        .put(Integer.parseInt(slot), NBTClassType.readNbt(Component.class, slot, child).copy());
            }
        }
    }

    protected void writeAspectProperties(ValueOutput valueOutput) {
        ValueOutput.ValueOutputList list = valueOutput.childrenList("map");
        for(Map.Entry<IAspect, IAspectProperties> entry : aspectProperties.entrySet()) {
            ValueOutput entryTag = list.addChild();
            entryTag.putString("key", entry.getKey().getUniqueName().toString());
            if(entry.getValue() != null) {
                entry.getValue().serialize(entryTag.child("value"));
            }
        }
    }

    public void readAspectProperties(ValueInput valueInput) {
        for (ValueInput entryTag : valueInput.childrenList("map").orElseThrow()) {
            IAspect key = Aspects.REGISTRY.getAspect(Identifier.parse(entryTag.getString("key").orElseThrow()));
            IAspectProperties value = entryTag.child("value")
                    .map(v -> {
                        AspectProperties ap = new AspectProperties();
                        ap.deserialize(v);
                        return ap;
                    })
                    .orElse(null);
            if (key != null && value != null) {
                this.aspectProperties.put(key, value);
            }
        }
    }

    @Override
    public void generateId() {
        this.id = IntegratedDynamics.globalCounters.get().getNext(IPartState.GLOBALCOUNTER_KEY);
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
        markAspectPropertiesChanged(aspect);
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
    public void updateAspectVariables(P partType, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        this.aspectVariablesHandler.updateAspectVariables(partType, this, network, partNetwork, target);
    }

    @Override
    public void markAspectVariablesChanged() {
        this.aspectVariablesHandler.markAspectVariablesChanged();
    }

    @Override
    public void markAspectPropertiesChanged(IAspect aspect) {
        this.aspectVariablesHandler.invalidateDerivedProperties(aspect);
    }

    @Nullable
    @Override
    public MutableComponent getAspectVariableError(IAspect aspect, int slot) {
        return this.aspectVariablesHandler.getAspectVariableError(aspect, slot);
    }

    @Nullable
    @Override
    public IValue getAspectVariableValue(IAspect aspect, int slot) {
        return this.aspectVariablesHandler.getAspectVariableValue(aspect, slot);
    }

    @Nullable
    @Override
    public IAspectProperties getAspectPropertiesVariableDriven(IAspect aspect, IAspectProperties baseProperties) {
        return this.aspectVariablesHandler.getDerivedProperties(aspect, baseProperties);
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
        NeoForge.EVENT_BUS.post(event);
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
        this.aspectVariablesHandler.markAspectVariablesChanged();
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
