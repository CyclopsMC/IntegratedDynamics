package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static OperatorRegistry INSTANCE = new OperatorRegistry();
    private static final IOperatorVariableFacade INVALID_FACADE = new OperatorVariableFacade(false, null, null);

    private final List<IOperator> operators = Lists.newLinkedList();
    private final Map<String, IOperator> namedOperators = Maps.newHashMap();
    private final Multimap<List<IValueType>, IOperator> inputTypedOperators = HashMultimap.create();
    private final Multimap<IValueType, IOperator> outputTypedOperators = HashMultimap.create();

    private OperatorRegistry() {
        if(MinecraftHelpers.isModdedEnvironment()) {
            IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(this);
        }
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
        namedOperators.put(operator.getUniqueName(), operator);
        inputTypedOperators.put(Lists.newArrayList(operator.getInputTypes()), operator);
        outputTypedOperators.put(operator.getOutputType(), operator);
        return operator;
    }

    @Override
    public Collection<IOperator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    @Override
    public IOperator getOperator(String uniqueName) {
        return namedOperators.get(uniqueName);
    }

    @Override
    public Collection<IOperator> getOperatorsWithInputTypes(IValueType... valueTypes) {
        return inputTypedOperators.get(Lists.newArrayList(valueTypes));
    }

    @Override
    public Collection<IOperator> getOperatorsWithOutputType(IValueType valueType) {
        return outputTypedOperators.get(valueType);
    }

    @Override
    public String getTypeId() {
        return "operator";
    }

    @Override
    public IOperatorVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
        if(!tag.hasKey("operatorName", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                || !tag.hasKey("variableIds", MinecraftHelpers.NBTTag_Types.NBTTagIntArray.ordinal())) {
            return INVALID_FACADE;
        }
        IOperator operator = getOperator(tag.getString("operatorName"));
        if(operator == null) {
            return INVALID_FACADE;
        }
        int[] variableIds = tag.getIntArray("variableIds");
        return new OperatorVariableFacade(id, operator, variableIds);
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, IOperatorVariableFacade variableFacade) {
        tag.setString("operatorName", variableFacade.getOperator().getUniqueName());
        tag.setIntArray("variableIds", variableFacade.getVariableIds());
    }
}
