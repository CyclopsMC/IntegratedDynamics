package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static OperatorRegistry INSTANCE = new OperatorRegistry();
    private static final IOperatorVariableFacade INVALID_FACADE = new OperatorVariableFacade(false, null, null);

    private final List<IOperator> operators = Lists.newArrayList();
    private final Map<String, IOperator> namedOperators = Maps.newHashMap();
    private final Map<String, IOperator> globalInteractOperators = Maps.newHashMap();
    private final Map<IValueType<?>, Map<String, IOperator>> scopedInteractOperators = Maps.newHashMap();
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
        namedOperators.put(operator.getUniqueName().toString(), operator);
        inputTypedOperators.put(ImmutableList.copyOf(operator.getInputTypes()), operator);
        outputTypedOperators.put(operator.getOutputType(), operator);
        categoryOperators.put(operator.getUnlocalizedCategoryName(), operator);

        String globalInteractName = operator.getGlobalInteractName();
        if (globalInteractOperators.containsKey(globalInteractName)) {
            throw new IllegalStateException("Detected registration of an operator with non-unique global interact name: " + operator.getUniqueName().toString() + ", " + globalInteractOperators.get(globalInteractName).getUniqueName().toString());
        }
        globalInteractOperators.put(globalInteractName, operator);

        if (operator.getInputTypes().length > 0) {
            Map<String, IOperator> scopedIteracts = scopedInteractOperators.get(operator.getInputTypes()[0]);
            if (scopedIteracts == null) {
                scopedIteracts = Maps.newHashMap();
                scopedInteractOperators.put(operator.getInputTypes()[0], scopedIteracts);
            }
            String scopedInteractName = operator.getScopedInteractName();
            if (scopedIteracts.containsKey(scopedInteractName)) {
                throw new IllegalStateException("Detected registration of an operator with non-unique scoped interact name: " + operator.getUniqueName().toString() + ", " + scopedIteracts.get(scopedInteractName).getUniqueName().toString());
            }
            scopedIteracts.put(scopedInteractName, operator);
        }

        return operator;
    }

    @Override
    public Collection<IOperator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    @Override
    public IOperator getOperator(ResourceLocation uniqueName) {
        return namedOperators.get(uniqueName.toString());
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
        namedSerializers.put(serializer.getUniqueName().toString(), serializer);
    }

    @Override
    public Tag serialize(IOperator value) {
        for (IOperatorSerializer serializer : serializers) {
            if (serializer.canHandle(value)) {
                CompoundTag tag = new CompoundTag();
                tag.putString("serializer", serializer.getUniqueName().toString());
                tag.put("value", serializer.serialize(value));
                return tag;
            }
        }
        return DEFAULT_SERIALIZER.serialize(value);
    }

    @Override
    public IOperator deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws EvaluationException {
        if (value.getId() == Tag.TAG_COMPOUND) {
            CompoundTag tag = (CompoundTag) value;
            String serializerName = tag.getString("serializer");
            IOperatorSerializer serializer = namedSerializers.get(serializerName);
            if (serializer == null) {
                throw new EvaluationException(
                        Component.translatable(L10NValues.OPERATOR_ERROR_NO_DESERIALIZER, value));
            }
            return serializer.deserialize(valueDeseralizationContext, tag.get("value"));
        }
        return DEFAULT_SERIALIZER.deserialize(valueDeseralizationContext, value);
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(Reference.MOD_ID, "operator");
    }

    @Override
    public IOperatorVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("operatorName", Tag.TAG_STRING)
                || !tag.contains("variableIds", Tag.TAG_INT_ARRAY)) {
            return INVALID_FACADE;
        }
        IOperator operator;
        try {
            operator = deserialize(valueDeseralizationContext, tag.get("operatorName"));
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
    public void setVariableFacade(CompoundTag tag, IOperatorVariableFacade variableFacade) {
        tag.put("operatorName", serialize(variableFacade.getOperator()));
        tag.putIntArray("variableIds", variableFacade.getVariableIds());
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof IOperatorVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof IVariable;
    }

    @Override
    public Map<String, IOperator> getGlobalInteractOperators() {
        return globalInteractOperators;
    }

    @Override
    public Map<IValueType<?>, Map<String, IOperator>> getScopedInteractOperators() {
        return scopedInteractOperators;
    }

    public static class OperatorVariablePredicate extends VariablePredicate<LazyExpression> {

        private final Optional<IOperator> operator;
        private final Optional<Int2ObjectMap<VariablePredicate>> inputPredicates;

        public OperatorVariablePredicate(Optional<IValueType> valueType, Optional<ValuePredicate> valuePredicate,
                                         Optional<IOperator> operator, Optional<Map<Integer, VariablePredicate>> inputPredicates) {
            super(LazyExpression.class, valueType, valuePredicate);
            this.operator = operator;
            this.inputPredicates = inputPredicates.map(Int2ObjectOpenHashMap::new);
        }

        public Optional<IOperator> getOperator() {
            return operator;
        }

        @Override
        protected boolean testTyped(LazyExpression variable) {
            if (!super.testTyped(variable)
                    || !(operator.isEmpty() || variable.getOperator() == operator.get())) {
                return false;
            }
            if (inputPredicates.isPresent()) {
                Int2ObjectMap<VariablePredicate> inputPredicatesMap = inputPredicates.get();
                for (int i = 0; i < variable.getInput().length; i++) {
                    IVariable inputVariable = variable.getInput()[i];
                    VariablePredicate variablePredicate = inputPredicatesMap.get(i);
                    if (variablePredicate != null && !variablePredicate.test(inputVariable)) {
                        return false;
                    }
                }
            }
            return true;
        }

        public Optional<Map<Integer, VariablePredicate>> getInputPredicates() {
            return inputPredicates.map(map -> map);
        }
    }
}
