package org.cyclops.integrateddynamics.core.part;

import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.IPartState} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public abstract class DefaultPartState<P extends IPartType> implements IPartState<P>, INBTProvider, IDirtyMarkListener {

    private boolean dirty = false;
    @Delegate
    private INBTProvider nbtProviderComponent = new NBTProviderComponent(this);
    @NBTPersist
    private int id = -1;

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
    public boolean isDirtyAndReset() {
        boolean wasDirty = this.dirty;
        this.dirty = false;
        return wasDirty;
    }

    @Override
    public void onDirty() {
        this.dirty = true;
    }

}
