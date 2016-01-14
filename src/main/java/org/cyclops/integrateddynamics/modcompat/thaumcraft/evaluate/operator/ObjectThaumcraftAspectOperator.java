package org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.operator;

import com.google.common.base.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.operator.ObjectOperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.ThaumcraftModCompat;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import thaumcraft.api.aspects.Aspect;

/**
 * Base class for block object operators.
 * @author rubensworks
 */
public class ObjectThaumcraftAspectOperator extends ObjectOperatorBase {

    public ObjectThaumcraftAspectOperator(String name, IFunction function) {
        this(name, name, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectThaumcraftAspectOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public ObjectThaumcraftAspectOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 2, function, renderPattern);
    }

    public ObjectThaumcraftAspectOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ThaumcraftModCompat.OBJECT_ASPECT), ThaumcraftModCompat.OBJECT_ASPECT, function, renderPattern);
    }

    protected ObjectThaumcraftAspectOperator(String name, IValueType[] inputTypes, IValueType outputType,
                                             IFunction function, IConfigRenderPattern renderPattern) {
        this(name, name, inputTypes, outputType, function, renderPattern);
    }

    public ObjectThaumcraftAspectOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                             IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedObjectType() {
        return "thaumcraft.aspect";
    }

    public static ObjectThaumcraftAspectOperator toInt(String name, final IIntegerFunction function) {
        return toInt(name, function, 0);
    }

    public static ObjectThaumcraftAspectOperator toInt(String name, final IIntegerFunction function, final int defaultValue) {
        return new ObjectThaumcraftAspectOperator(name, new IValueType[]{ThaumcraftModCompat.OBJECT_ASPECT}, ValueTypes.INTEGER, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<Pair<Aspect, Integer>> a = ((ValueObjectTypeAspect.ValueAspect) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a.isPresent() ? function.evaluate(a.get().getKey(), a.get().getValue()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static ObjectThaumcraftAspectOperator toBoolean(String name, final IBooleanFunction function) {
        return toBoolean(name, function, false);
    }

    public static ObjectThaumcraftAspectOperator toBoolean(String name, final IBooleanFunction function, final boolean defaultValue) {
        return new ObjectThaumcraftAspectOperator(name, new IValueType[]{ThaumcraftModCompat.OBJECT_ASPECT}, ValueTypes.BOOLEAN, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                Optional<Pair<Aspect, Integer>> a = ((ValueObjectTypeAspect.ValueAspect) variables[0].getValue()).getRawValue();
                return ValueTypeBoolean.ValueBoolean.of(a.isPresent() ? function.evaluate(a.get().getKey(), a.get().getValue()) : defaultValue);
            }
        }, IConfigRenderPattern.SUFFIX_1_LONG);
    }

    public static interface IIntegerFunction {
        public int evaluate(Aspect aspect, int amount) throws EvaluationException;
    }

    public static interface IBooleanFunction {
        public boolean evaluate(Aspect aspect, int amount) throws EvaluationException;
    }

}
