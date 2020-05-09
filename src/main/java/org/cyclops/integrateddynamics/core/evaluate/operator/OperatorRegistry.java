package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static OperatorRegistry INSTANCE = new OperatorRegistry();
    private static final IOperatorVariableFacade INVALID_FACADE = new OperatorVariableFacade(false, null, null);

    private final List<IOperator> operators = Lists.newArrayList();
    private final Map<String, IOperator> namedOperators = Maps.newHashMap();
    private final Multimap<List<IValueType>, IOperator> inputTypedOperators = HashMultimap.create();
    private final Multimap<IValueType, IOperator> outputTypedOperators = HashMultimap.create();
    private final Multimap<String, IOperator> categoryOperators = HashMultimap.create();
    private final List<IOperatorSerializer> serializers = Lists.newArrayList();
    private final Map<String, IOperatorSerializer> namedSerializers = Maps.newHashMap();
    private final IOperatorSerializer DEFAULT_SERIALIZER = new OperatorSerializerDefault();

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
        inputTypedOperators.put(ImmutableList.copyOf(operator.getInputTypes()), operator);
        outputTypedOperators.put(operator.getOutputType(), operator);
        categoryOperators.put(operator.getUnlocalizedCategoryName(), operator);
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
        return inputTypedOperators.get(ImmutableList.copyOf(valueTypes));
    }

    @Override
    public Collection<IOperator> getOperatorsWithOutputType(IValueType valueType) {
        return outputTypedOperators.get(valueType);
    }

    @Override
    public Collection<IOperator> getOperatorsInCategory(String categoryName) {
        return categoryOperators.get(categoryName);
    }

    @Override
    public void registerSerializer(IOperatorSerializer serializer) {
        serializers.add(serializer);
        namedSerializers.put(serializer.getUniqueName(), serializer);
    }

    @Override
    public String serialize(IOperator value) {
        for (IOperatorSerializer serializer : serializers) {
            if (serializer.canHandle(value)) {
                return serializer.getUniqueName() + ":" + serializer.serialize(value);
            }
        }
        return DEFAULT_SERIALIZER.serialize(value);
    }

    @Override
    public IOperator deserialize(String value) throws EvaluationException {
        String[] split = value.split(":");
        if (split.length > 1) {
            String serializerName = split[0];
            String subValue = StringUtils.join(ArrayUtils.subarray(split, 1, split.length), ":");
            IOperatorSerializer serializer = namedSerializers.get(serializerName);
            if (serializer == null) {
                throw new EvaluationException(String.format("No serializer was found to deserialize the operator value '%s'", value));
            }
            return serializer.deserialize(subValue);
        }
        return DEFAULT_SERIALIZER.deserialize(value);
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
        IOperator operator;
        try {
            operator = deserialize(tag.getString("operatorName"));
        } catch (EvaluationException e) {
            return INVALID_FACADE;
        }
        if(operator == null) {
            return INVALID_FACADE;
        }
        int[] variableIds = tag.getIntArray("variableIds");
        return new OperatorVariableFacade(id, operator, variableIds);
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, IOperatorVariableFacade variableFacade) {
        tag.setString("operatorName", serialize(variableFacade.getOperator()));
        tag.setIntArray("variableIds", variableFacade.getVariableIds());
    }

    @Override
    public VariablePredicate deserializeVariablePredicate(JsonObject element, @Nullable IValueType valueType, ValuePredicate valuePredicate) {
        JsonElement operatorElement = element.get("operator");
        IOperator operator = null;
        if (operatorElement != null && !operatorElement.isJsonNull()) {
            operator = Operators.REGISTRY.getOperator(JsonUtils.getString(element, "operator"));
            if (operator == null) {
                throw new JsonSyntaxException("Unknown operator type '" + JsonUtils.getString(element, "operator")
                        + "', valid types are: " + Operators.REGISTRY.getOperators().stream()
                        .map(IOperator::getUniqueName).collect(Collectors.toList()));
            }
        }
        JsonElement inputElement = element.get("input");
        Int2ObjectMap<VariablePredicate> inputPredicates = new Int2ObjectOpenHashMap<>();
        if (inputElement != null && !inputElement.isJsonNull()) {
            for (Map.Entry<String, JsonElement> inputEntry : inputElement.getAsJsonObject().entrySet()) {
                try {
                    int slot = Integer.parseInt(inputEntry.getKey());
                    inputPredicates.put(slot, VariablePredicate.deserialize(inputEntry.getValue()));
                } catch (NumberFormatException e) {
                    throw new JsonSyntaxException("All inputs must refer to an input id as key, but got '" + inputEntry.getKey() + '"');
                }
            }
        }
        return new OperatorVariablePredicate(valueType, valuePredicate, operator, inputPredicates);
    }

    public static class OperatorVariablePredicate extends VariablePredicate<LazyExpression> {

        private final IOperator operator;
        private final Int2ObjectMap<VariablePredicate> inputPredicates;

        public OperatorVariablePredicate(@Nullable IValueType valueType, ValuePredicate valuePredicate,
                                         @Nullable IOperator operator, Int2ObjectMap<VariablePredicate> inputPredicates) {
            super(LazyExpression.class, valueType, valuePredicate);
            this.operator = operator;
            this.inputPredicates = inputPredicates;
        }

        @Override
        protected boolean testTyped(LazyExpression variable) {
            if (!super.testTyped(variable)
                    || !(operator == null || variable.getOperator() == operator)) {
                return false;
            }
            for (int i = 0; i < variable.getInput().length; i++) {
                IVariable inputVariable = variable.getInput()[i];
                VariablePredicate variablePredicate = inputPredicates.get(i);
                if (variablePredicate != null && !variablePredicate.test(inputVariable)) {
                    return false;
                }
            }
            return true;
        }
    }
}
