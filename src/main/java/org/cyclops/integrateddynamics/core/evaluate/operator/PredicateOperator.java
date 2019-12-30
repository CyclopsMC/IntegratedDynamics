package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
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
        super("pred", "pred", new IValueType[]{inputType},
                ValueTypes.BOOLEAN, variables -> ValueTypeBoolean.ValueBoolean.of(
                        predicate.test(variables.getValue(0))), IConfigRenderPattern.PREFIX_1);
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
        public String getUniqueName() {
            return "predicate";
        }

        @Override
        public INBT serialize(PredicateOperator<IValueType<IValue>, IValue> operator) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("valueType", operator.inputType.getTranslationKey());
            ListNBT list = new ListNBT();
            for (IValue rawValue : operator.rawValues) {
                list.add(operator.inputType.serialize(rawValue));
            }
            tag.put("values", list);
            return tag;
        }

        @Override
        public PredicateOperator<IValueType<IValue>, IValue> deserialize(INBT value) throws EvaluationException {
            try {
                CompoundNBT tag = (CompoundNBT) value;
                IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(tag.getString("valueType"));
                ListNBT list = (ListNBT) tag.get("values");
                List<IValue> values = Lists.newArrayList();
                for (INBT subTag : list) {
                    values.add(ValueHelpers.deserializeRaw(valueType, subTag));
                }
                return new PredicateOperator<>(valueType, values);
            } catch (ClassCastException e) {
                throw new EvaluationException(String.format("Something went wrong while deserializing '%s'.", value));
            }
        }
    }
}
