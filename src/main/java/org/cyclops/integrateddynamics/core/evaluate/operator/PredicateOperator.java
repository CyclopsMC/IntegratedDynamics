package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import net.minecraft.nbt.*;
import net.minecraft.util.JsonUtils;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
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
        public String serialize(PredicateOperator<IValueType<IValue>, IValue> operator) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("valueType", operator.inputType.getUnlocalizedName());
            NBTTagList list = new NBTTagList();
            for (IValue rawValue : operator.rawValues) {
                list.appendTag(new NBTTagString(operator.inputType.serialize(rawValue)));
            }
            tag.setTag("values", list);
            return tag.toString();
        }

        @Override
        public PredicateOperator<IValueType<IValue>, IValue> deserialize(String value) throws EvaluationException {
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
                IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(tag.getString("valueType"));
                NBTTagList list = tag.getTagList("values", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal());
                List<IValue> values = Lists.newArrayList();
                for (NBTBase subTag : list) {
                    values.add(valueType.deserialize(((NBTTagString) subTag).getString()));
                }
                return new PredicateOperator<>(valueType, values);
            } catch (NBTException e) {
                throw new EvaluationException(String.format("Something went wrong while deserializing '%s'.", value));
            }
        }
    }
}
