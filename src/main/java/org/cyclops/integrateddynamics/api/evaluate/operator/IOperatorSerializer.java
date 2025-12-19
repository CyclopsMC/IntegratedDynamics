package org.cyclops.integrateddynamics.api.evaluate.operator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public Identifier getUniqueName();

    /**
     * Serialize the given operator.
     *
     * @param valueOutput
     * @param operator    The operator to serialize.
     */
    public void serialize(ValueOutput valueOutput, O operator);

    /**
     * Deserialize the given operator value.
     *
     * @param valueInput
     * @return The deserialized operator, null if deserialization failed.
     * @throws EvaluationException If something goes wrong while deserializing
     */
    public O deserialize(ValueInput valueInput) throws EvaluationException;

}
