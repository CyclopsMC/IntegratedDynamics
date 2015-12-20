package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for block object operators.
 * @author rubensworks
 */
public class ObjectItemOperator extends ObjectOperatorBase {

    public ObjectItemOperator(String name, IFunction function) {
        this(name, name, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectItemOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectItemOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 2, function, renderPattern);
    }

    public ObjectItemOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.OBJECT_ITEMSTACK), ValueTypes.OBJECT_ITEMSTACK, function, renderPattern);
    }

    protected ObjectItemOperator(String name, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern) {
        this(name, name, inputTypes, outputType, function, renderPattern);
    }

    protected ObjectItemOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedObjectType() {
        return "item";
    }

    public static ObjectItemOperator toInt(String name, final IIntegerFunction function) {
        return toInt(name, function, 0);
    }

    public static ObjectItemOperator toInt(String name, final IIntegerFunction function, final int defaultValue) {
        return new ObjectItemOperator(name, new IValueType[]{ValueTypes.OBJECT_ITEM}, ValueTypes.INTEGER, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a.isPresent() ? function.evaluate(a.get()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static ObjectItemOperator toBoolean(String name, final IBooleanFunction function) {
        return toBoolean(name, function, false);
    }

    public static ObjectItemOperator toBoolean(String name, final IBooleanFunction function, final boolean defaultValue) {
        return new ObjectItemOperator(name, new IValueType[]{ValueTypes.OBJECT_ITEM}, ValueTypes.BOOLEAN, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
                return ValueTypeBoolean.ValueBoolean.of(a.isPresent() ? function.evaluate(a.get()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static interface IIntegerFunction {
        public int evaluate(ItemStack itemStack) throws EvaluationException;
    }

    public static interface IBooleanFunction {
        public boolean evaluate(ItemStack itemStack) throws EvaluationException;
    }

}
