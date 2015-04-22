package org.cyclops.integrateddynamics.core.part;

import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.IPartState} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class DefaultPartState<P extends IPartType> implements IPartState<P>, INBTProvider {

    @Delegate
    private INBTProvider nbtProviderComponent = new NBTProviderComponent(this);

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        writeGeneratedFieldsToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        readGeneratedFieldsFromNBT(tag);
    }
}
