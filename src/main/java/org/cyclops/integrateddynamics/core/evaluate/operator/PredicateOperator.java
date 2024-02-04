package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import org.cyclops.integrateddynamics.core.helper.L10NValues;

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
        public ResourceLocation getUniqueName() {
            return new ResourceLocation(Reference.MOD_ID, "predicate");
        }

        @Override
        public Tag serialize(PredicateOperator<IValueType<IValue>, IValue> operator) {
            CompoundTag tag = new CompoundTag();
            tag.putString("valueType", operator.inputType.getTranslationKey());
            ListTag list = new ListTag();
            for (IValue rawValue : operator.rawValues) {
                list.add(operator.inputType.serialize(rawValue));
            }
            tag.put("values", list);
            return tag;
        }

        @Override
        public PredicateOperator<IValueType<IValue>, IValue> deserialize(Tag value) throws EvaluationException {
            try {
                CompoundTag tag = (CompoundTag) value;
                IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(tag.getString("valueType")));
                ListTag list = (ListTag) tag.get("values");
                List<IValue> values = Lists.newArrayList();
                for (Tag subTag : list) {
                    values.add(ValueHelpers.deserializeRaw(valueType, subTag));
                }
                return new PredicateOperator<>(valueType, values);
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new EvaluationException(Component.translatable(L10NValues.VALUETYPE_ERROR_DESERIALIZE,
                        value, e.getMessage()));
            }
        }
    }
}
