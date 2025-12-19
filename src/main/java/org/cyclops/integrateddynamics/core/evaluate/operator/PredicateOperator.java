package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;
import java.util.function.Predicate;

/**
 * An operator that wraps around a predicate.
 * @author rubensworks
 */
public class PredicateOperator<T extends IValueType<V>, V extends IValue> extends OperatorBase {

    private final String unlocalizedType;
    private final T inputType;
    private final List<V> rawValues;

    public PredicateOperator(T inputType, List<V> rawValues) {
        this(rawValues::contains, inputType, rawValues);
    }

    public PredicateOperator(Predicate<V> predicate, T inputType, List<V> rawValues) {
        super("pred", "pred", "pred", null, false,
                new IValueType[]{inputType}, ValueTypes.BOOLEAN, variables -> ValueTypeBoolean.ValueBoolean.of(
                        predicate.test(variables.getValue(0, inputType))), IConfigRenderPattern.PREFIX_1);
        this.inputType = inputType;
        this.rawValues = rawValues;
        this.unlocalizedType = "predicate";
    }

    @Override
    protected String getUnlocalizedType() {
        return unlocalizedType;
    }

    @Override
    public IOperator materialize() {
        return this;
    }

    public static class Serializer implements IOperatorSerializer<PredicateOperator<IValueType<IValue>, IValue>> {
        @Override
        public boolean canHandle(IOperator operator) {
            return operator instanceof PredicateOperator;
        }

        @Override
        public Identifier getUniqueName() {
            return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "predicate");
        }

        @Override
        public void serialize(ValueOutput valueOutput, PredicateOperator<IValueType<IValue>, IValue> operator) {
            valueOutput.putString("valueType", operator.inputType.getTranslationKey());
            ValueOutput.ValueOutputList list = valueOutput.childrenList("values");
            for (IValue rawValue : operator.rawValues) {
                operator.inputType.serialize(list.addChild(), rawValue);
            }
        }

        @Override
        public PredicateOperator<IValueType<IValue>, IValue> deserialize(ValueInput valueInput) throws EvaluationException {
            IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(Identifier.parse(valueInput.getString("valueType").orElseThrow()));
            ValueInput.ValueInputList list = valueInput.childrenList("values").orElseThrow();
            List<IValue> values = Lists.newArrayList();
            for (ValueInput subTag : list) {
                values.add(ValueHelpers.deserializeRaw(subTag, valueType));
            }
            return new PredicateOperator<>(valueType, values);
        }
    }
}
