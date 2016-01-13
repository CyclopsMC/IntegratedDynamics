package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

/**
 * Base class for block object operators.
 * @author rubensworks
 */
public class ObjectEntityOperator extends ObjectOperatorBase {

    public ObjectEntityOperator(String name, IFunction function) {
        this(name, name, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectEntityOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectEntityOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 2, function, renderPattern);
    }

    public ObjectEntityOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.OBJECT_ENTITY), ValueTypes.OBJECT_ENTITY, function, renderPattern);
    }

    protected ObjectEntityOperator(String name, IValueType[] inputTypes, IValueType outputType,
                                   IFunction function, IConfigRenderPattern renderPattern) {
        this(name, name, inputTypes, outputType, function, renderPattern);
    }

    protected ObjectEntityOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                   IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedObjectType() {
        return "entity";
    }

    public static ObjectEntityOperator toInt(String name, final IIntegerFunction function) {
        return toInt(name, function, 0);
    }

    public static ObjectEntityOperator toInt(String name, final IIntegerFunction function, final int defaultValue) {
        return new ObjectEntityOperator(name, new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.INTEGER, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a.isPresent() ? function.evaluate(a.get()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static ObjectEntityOperator toBoolean(String name, final IBooleanFunction function) {
        return toBoolean(name, function, false);
    }

    public static ObjectEntityOperator toBoolean(String name, final IBooleanFunction function, final boolean defaultValue) {
        return new ObjectEntityOperator(name, new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.BOOLEAN, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
                return ValueTypeBoolean.ValueBoolean.of(a.isPresent() ? function.evaluate(a.get()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static ObjectEntityOperator toDouble(String name, final IDoubleFunction function) {
        return toDouble(name, function, 0);
    }

    public static ObjectEntityOperator toDouble(String name, final IDoubleFunction function, final int defaultValue) {
        return new ObjectEntityOperator(name, new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.DOUBLE, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
                return ValueTypeDouble.ValueDouble.of(a.isPresent() ? function.evaluate(a.get()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static interface IIntegerFunction {
        public int evaluate(Entity entity) throws EvaluationException;
    }

    public static interface IBooleanFunction {
        public boolean evaluate(Entity entity) throws EvaluationException;
    }

    public static interface IDoubleFunction {
        public double evaluate(Entity entity) throws EvaluationException;
    }

}
