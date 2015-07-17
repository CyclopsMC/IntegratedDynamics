package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static OperatorRegistry INSTANCE = new OperatorRegistry();
    private static final OperatorVariableFacade INVALID_FACADE = new OperatorVariableFacade(false, null, null);

    private final List<IOperator> operators = Lists.newLinkedList();
    private final Map<String, IOperator> namedOperators = Maps.newHashMap();
    private final Map<IValueType, List<IOperator>> outputTypedOperators = Maps.newHashMap();

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
        namedOperators.put(operator.getUnlocalizedName(), operator);
        CollectionHelpers.addToMapList(outputTypedOperators, operator.getOutputType(), operator);
        return operator;
    }

    @Override
    public List<IOperator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    @Override
    public IOperator getOperator(String unlocalizedName) {
        return namedOperators.get(unlocalizedName);
    }

    @Override
    public List<IOperator> getOperatorsWithOutputType(IValueType valueType) {
        return Collections.unmodifiableList(outputTypedOperators.containsKey(valueType)
                ? outputTypedOperators.get(valueType)
                : Collections.<IOperator>emptyList());
    }

    @Override
    public String getTypeId() {
        return "operator";
    }

    @Override
    public OperatorVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
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
    public void setVariableFacade(NBTTagCompound tag, OperatorVariableFacade variableFacade) {
        tag.setString("operatorName", variableFacade.getOperator().getUnlocalizedName());
        tag.setIntArray("variableIds", variableFacade.getVariableIds());
    }
}
