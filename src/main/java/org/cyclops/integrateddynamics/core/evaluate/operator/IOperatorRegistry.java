package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;

import java.util.Collection;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public interface IOperatorRegistry extends IRegistry, IVariableFacadeHandler<OperatorVariableFacade> {

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
    public Collection<IOperator> getOperators();

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
    public Collection<IOperator> getOperatorsWithOutputType(IValueType valueType);

}
