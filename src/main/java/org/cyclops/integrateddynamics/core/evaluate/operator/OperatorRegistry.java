package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static OperatorRegistry INSTANCE = new OperatorRegistry();

    private final List<IOperator> operators = Lists.newLinkedList();
    private final Map<String, IOperator> namedOperators = Maps.newHashMap();
    private final Map<IValueType, List<IOperator>> outputTypedOperators = Maps.newHashMap();

    private OperatorRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static OperatorRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <O extends IOperator> O register(O operator) {
        operators.add(operator);
        namedOperators.put(operator.getOperatorName(), operator);
        CollectionHelpers.addToMapList(outputTypedOperators, operator.getOutputType(), operator);
        return operator;
    }

    @Override
    public List<IOperator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    @Override
    public IOperator getOperator(String operatorName) {
        return namedOperators.get(operatorName);
    }

    @Override
    public List<IOperator> getOperatorsWithOutputType(IValueType valueType) {
        return Collections.unmodifiableList(outputTypedOperators.containsKey(valueType)
                ? outputTypedOperators.get(valueType)
                : Collections.<IOperator>emptyList());
    }
}
