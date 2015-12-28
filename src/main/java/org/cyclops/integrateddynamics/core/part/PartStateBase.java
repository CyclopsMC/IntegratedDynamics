package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

import java.util.Map;

/**
 * A default implementation of the {@link IPartState} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public abstract class PartStateBase<P extends IPartType> implements IPartState<P>, INBTProvider, IDirtyMarkListener {

    private boolean dirty = false;
    private boolean update = false;
    @Delegate
    private INBTProvider nbtProviderComponent = new NBTProviderComponent(this);
    @NBTPersist
    private int updateInterval = GeneralConfig.defaultPartUpdateFreq;
    @NBTPersist
    private int id = -1;
    @NBTPersist
    private Map<String, IAspectProperties> aspectProperties = Maps.newHashMap();
    @NBTPersist
    private boolean enabled;

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        writeGeneratedFieldsToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        readGeneratedFieldsFromNBT(tag);
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
        return aspectProperties.get(aspect.getUnlocalizedName());
    }

    @Override
    public void setAspectProperties(IAspect aspect, IAspectProperties properties) {
        aspectProperties.put(aspect.getUnlocalizedName(), properties);
        sendUpdate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
