package org.cyclops.integrateddynamics.api.evaluate.operator;

import net.minecraft.nbt.INBT;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;

/**
 * A serialization action for operators.
 * @param <O> The operator type
 * @author rubensworks
 */
public interface IOperatorSerializer<O extends IOperator> {

    /**
     * @param operator The operator to test.
     * @return If this can serialize the given operator.
     */
    public boolean canHandle(IOperator operator);

    /**
     * @return The unique name of this serializer.
     */
    public String getUniqueName();

    /**
     * Serialize the given operator.
     * @param operator The operator to serialize.
     * @return The serialized operator value.
     */
    public INBT serialize(O operator);

    /**
     * Deserialize the given operator value.
     * @param value The operator value to deserialize.
     * @return The deserialized operator, null if deserialization failed.
     * @throws EvaluationException If something goes wrong while deserializing
     */
    public O deserialize(INBT value) throws EvaluationException;

}
