package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

import java.util.List;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public interface IOperatorRegistry extends IRegistry {

    /**
     * Register a new operator.
     * @param operator The operator.
     * @param <O> The operator type.
     * @return The registered operator.
     */
    public <O extends IOperator> O register(O operator);

    /**
     * @return All registered operators.
     */
    public List<IOperator> getOperators();

    /**
     * Get the operator with the given name.
     * @param operatorName The unique operator name.
     * @return The corresponding operator or null.
     */
    public IOperator getOperator(String operatorName);

    /**
     * Get the operators with the given output value type.
     * @param valueType The output value type.
     * @return The corresponding operators.
     */
    public List<IOperator> getOperatorsWithOutputType(IValueType valueType);

}
