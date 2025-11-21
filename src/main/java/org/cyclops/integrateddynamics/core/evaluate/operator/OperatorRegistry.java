package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.slf4j.Logger;

import java.util.*;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class OperatorRegistry implements IOperatorRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
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
        if(IModHelpers.get().getMinecraftHelpers().isModdedEnvironment()) {
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
    public void serialize(ValueOutput valueOutput, IOperator value) {
        for (IOperatorSerializer serializer : serializers) {
            if (serializer.canHandle(value)) {
                valueOutput.putString("serializer", serializer.getUniqueName().toString());
                serializer.serialize(valueOutput.child("value"), value);
                return;
            }
        }
        DEFAULT_SERIALIZER.serialize(valueOutput, value);
    }

    @Override
    public IOperator deserialize(ValueInput valueInput) throws EvaluationException {
        IOperatorSerializer serializer = valueInput.getString("serializer")
                .map(namedSerializers::get)
                .orElse(DEFAULT_SERIALIZER);
        if (serializer == null) { // Can be null if namedSerializers does not contain the serializer
            throw new EvaluationException(
                    Component.translatable(L10NValues.OPERATOR_ERROR_NO_DESERIALIZER, valueInput.toString()));
        }
        return serializer.deserialize(valueInput.child("value").orElse(valueInput));
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "operator");
    }

    @Override
    public IOperatorVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("operator") || !(tag.contains("variableIds") || tag.contains("variableIds"))) {
            return INVALID_FACADE;
        }
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(tag), LOGGER)) {
            ValueInput input = TagValueInput.create(
                    scopedCollector,
                    valueDeseralizationContext.holderLookupProvider(),
                    tag
            );
            IOperator operator;
            try {
                operator = deserialize(input.child("operator").orElseThrow());
            } catch (EvaluationException e) {
                return INVALID_FACADE;
            }
            if(operator == null) {
                return INVALID_FACADE;
            }
            int[] variableIds = input.list("variableIds", Codec.INT).orElseThrow()
                    .stream()
                    .mapToInt(i -> i)
                    .toArray();
            return new OperatorVariableFacade(id, operator, variableIds);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return INVALID_FACADE;
        }
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IOperatorVariableFacade variableFacade) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(tag), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
            serialize(valueOutput, variableFacade.getOperator());
            tag.put("operator", valueOutput.buildResult());
        }
        ListTag variableIds = new ListTag();
        for (int variableId : variableFacade.getVariableIds()) {
            variableIds.add(IntTag.valueOf(variableId));
        }
        tag.put("variableIds", variableIds);
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
