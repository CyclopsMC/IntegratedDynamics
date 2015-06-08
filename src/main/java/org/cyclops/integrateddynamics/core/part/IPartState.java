package org.cyclops.integrateddynamics.core.part;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;

/**
 * A value holder for an {@link IPartType}.
 * This is what will be serialized from and to NBT.
 * This object is mutable and should not be recreated.
 * @author rubensworks
 */
public interface IPartState<P extends IPartType> {

    public static final String GLOBALCOUNTER_KEY = "part";

    /**
     * Write a state to NBT.
     * @param tag The tag to write to.
     */
    public void writeToNBT(NBTTagCompound tag);

    /**
     * Read a state from NBT.
     * @param tag The tag to read from.
     */
    public void readFromNBT(NBTTagCompound tag);

    /**
     * Generate a server-wide unique ID for this part state.
     */
    public void generateId();

    /**
     * A server-wide unique ID for this part that is persisted when the part is broken and moved.
     * @return The unique ID
     */
    public int getId();

    /**
     * Get the singleton variable for an aspect.
     * This only retrieves the previously stored state.
     * Better to call {@link org.cyclops.integrateddynamics.core.part.IPartType#getVariable(PartTarget, IPartState, org.cyclops.integrateddynamics.core.part.aspect.IAspectRead)}.
     * @param aspect The aspect from the part of this state.
     * @return The variable that exists only once for an aspect in this part state.
     */
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(IAspectRead<V, T> aspect);

    /**
     * Get the singleton variable for an aspect.
     * @param aspect The aspect from the part of this state.
     * @param variable The variable that exists only once for an aspect in this part state.
     */
    public void setVariable(IAspect aspect, IAspectVariable variable);

}
