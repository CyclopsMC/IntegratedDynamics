package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;

import java.util.Map;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.IPartState} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class DefaultPartState<P extends IPartType> implements IPartState<P>, INBTProvider {

    @Delegate
    private INBTProvider nbtProviderComponent = new NBTProviderComponent(this);
    @NBTPersist
    private int id = -1;
    private final Map<IAspect, IAspectVariable> aspectVariables = Maps.newHashMap();

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

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(IAspectRead<V, T> aspect) {
        return aspectVariables.get(aspect);
    }

    @Override
    public void setVariable(IAspect aspect, IAspectVariable variable) {
        aspectVariables.put(aspect, variable);
    }
}
