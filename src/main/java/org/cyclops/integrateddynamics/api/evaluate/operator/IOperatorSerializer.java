package org.cyclops.integrateddynamics.api.evaluate.operator;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

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
    public ResourceLocation getUniqueName();

    /**
     * Serialize the given operator.
     *
     * @param valueDeseralizationContext
     * @param operator                   The operator to serialize.
     * @return The serialized operator value.
     */
    public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, O operator);

    /**
     * Deserialize the given operator value.
     *
     * @param valueDeseralizationContext
     * @param value The operator value to deserialize.
     * @return The deserialized operator, null if deserialization failed.
     * @throws EvaluationException If something goes wrong while deserializing
     */
    public O deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws EvaluationException;

}
