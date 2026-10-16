package org.cyclops.integrateddynamics.core.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IExpressionVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionOperator;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Variable facade for variables defined by an expression tree.
 * @author rubensworks
 */
public class ExpressionVariableFacade extends VariableFacadeBase implements IExpressionVariableFacade {

    @Nullable
    private final Expression expression;
    private final Map<Integer, IValueType<?>> referenceTypes;
    private IVariable<?> variable = null;
    private int lastNetworkHash = -1;
    private IValueType<?> outputType = null;

    // Flags to detect infinite recursion
    private boolean building = false;
    private boolean validating = false;

    public ExpressionVariableFacade(boolean generateId, @Nullable Expression expression) {
        this(generateId, expression, Map.of());
    }

    public ExpressionVariableFacade(int id, @Nullable Expression expression) {
        this(id, expression, Map.of());
    }

    public ExpressionVariableFacade(boolean generateId, @Nullable Expression expression, Map<Integer, IValueType<?>> referenceTypes) {
        super(generateId);
        this.expression = expression;
        this.referenceTypes = Map.copyOf(referenceTypes);
    }

    public ExpressionVariableFacade(int id, @Nullable Expression expression, Map<Integer, IValueType<?>> referenceTypes) {
        super(id);
        this.expression = expression;
        this.referenceTypes = Map.copyOf(referenceTypes);
    }

    @Override
    public Map<Integer, IValueType<?>> getReferenceTypes() {
        return referenceTypes;
    }

    @Nullable
    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean isValid() {
        return expression != null && ExpressionTrees.countEmpty(expression.root()) == 0;
    }

    @Nullable
    protected IVariable<?> getReferencedVariable(INetwork network, IPartNetwork partNetwork, int variableId) {
        if (!partNetwork.hasVariableFacade(variableId)) {
            return null;
        }
        IVariableFacade variableFacade = partNetwork.getVariableFacade(variableId);
        if (variableFacade == null || variableFacade == this || !variableFacade.isValid()) {
            return null;
        }
        return variableFacade.getVariable(network, partNetwork);
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if (!isValid() || building) {
            return null;
        }
        int newNetworkHash = network != null ? network.hashCode() : -1;
        if (variable == null || (variable instanceof IExpression<?> e && e.hasErrored()) || newNetworkHash != this.lastNetworkHash) {
            this.lastNetworkHash = newNetworkHash;
            building = true;
            try {
                variable = buildVariable(network, partNetwork);
            } finally {
                building = false;
            }
        }
        return (IVariable<V>) variable;
    }

    @Nullable
    protected IVariable<?> buildVariable(INetwork network, IPartNetwork partNetwork) {
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        for (int variableId : ExpressionTrees.getRefIds(expression.root())) {
            IVariable<?> refVariable = getReferencedVariable(network, partNetwork, variableId);
            if (refVariable == null) {
                return null;
            }
            refs.put(variableId, refVariable);
        }
        if (expression.hasParameters()) {
            return new ExpressionOperatorVariable(ExpressionOperator.of(expression, refs), refs.values());
        }
        return ExpressionEvaluator.buildLazy(expression.root(), getId(), partNetwork, refs::get);
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
            return;
        }
        if (validating) {
            validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_CYCLICREFERENCE, Integer.toString(getId())));
            return;
        }
        validating = true;
        try {
            boolean checkFurther = true;
            Int2ObjectMap<IValueType<?>> refTypes = new Int2ObjectOpenHashMap<>();
            for (int variableId : ExpressionTrees.getRefIds(expression.root())) {
                if (variableId < 0) {
                    validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
                    checkFurther = false;
                } else if (!partNetwork.hasVariableFacade(variableId)) {
                    validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                            Integer.toString(variableId)));
                    checkFurther = false;
                } else {
                    IVariableFacade variableFacade = partNetwork.getVariableFacade(variableId);
                    if (variableFacade == this) {
                        validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                                Integer.toString(variableId)));
                        checkFurther = false;
                    } else if (variableFacade != null) {
                        boolean[] valid = {true};
                        variableFacade.validate(network, partNetwork, error -> {
                            validator.addError(error);
                            valid[0] = false;
                        }, ValueTypes.CATEGORY_ANY);
                        if (valid[0]) {
                            IVariable<?> refVariable = variableFacade.getVariable(network, partNetwork);
                            if (refVariable != null) {
                                refTypes.put(variableId, refVariable.getType());
                            }
                        } else {
                            checkFurther = false;
                        }
                    }
                }
            }
            if (checkFurther) {
                ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, refTypes::get);
                for (ExpressionTypeChecker.Error error : result.errors()) {
                    validator.addError(error.message());
                }
                IValueType<?> actualOutputType = expression.hasParameters() ? ValueTypes.OPERATOR : result.outputType();
                if (result.errors().isEmpty() && !ValueHelpers.correspondsTo(actualOutputType, containingValueType)) {
                    validator.addError(Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                            Component.translatable(containingValueType.getTranslationKey()),
                            Component.translatable(actualOutputType.getTranslationKey())));
                }
            }
        } finally {
            validating = false;
        }
    }

    /**
     * @return The output type of the root, inferred without knowledge of referenced variables.
     */
    public IValueType<?> getRootOutputType() {
        if (outputType == null) {
            outputType = expression == null ? ValueTypes.CATEGORY_ANY : ExpressionTypeChecker.check(expression, id -> null).outputType();
        }
        return outputType;
    }

    @Override
    public IValueType getOutputType() {
        if (expression == null) {
            return null;
        }
        return expression.hasParameters() ? ValueTypes.OPERATOR : getRootOutputType();
    }

    /**
     * @return The expression pretty-printed with labels.
     */
    public String printExpression() {
        if (expression == null) {
            return "";
        }
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);
        return ExpressionPrinter.print(expression, labels::getLabel);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Item.TooltipContext context) {
        if (expression != null) {
            list.add(Component.translatable(L10NValues.EXPRESSION_TOOLTIP_EXPRESSION,
                    Component.literal(printExpression()).withStyle(ChatFormatting.WHITE)));
            MutableComponent signature;
            if (expression.hasParameters()) {
                signature = ValueTypeOperator.getSignature(ExpressionTypeChecker.getInputTypes(expression), getRootOutputType()).copy();
            } else {
                IValueType<?> type = getRootOutputType();
                signature = Component.translatable(type.getTranslationKey()).withStyle(type.getDisplayColorFormat());
            }
            list.add(Component.translatable(L10NValues.EXPRESSION_TOOLTIP_TYPE, signature));
            if (!isValid()) {
                list.add(Component.translatable(L10NValues.EXPRESSION_ERROR_INCOMPLETE).withStyle(ChatFormatting.RED));
            }
        }
        super.appendHoverText(list, context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        IValueType<?> valueType = getOutputType();
        if (valueType != null) {
            BakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType);
            if (bakedModel != null) {
                quads.addAll(bakedModel.getQuads(null, null, random, modelData, null));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpressionVariableFacade that)) return false;
        return getId() == that.getId() && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), expression);
    }

    /**
     * @return The root operator if the root is an operator node, otherwise null.
     */
    @Nullable
    public IOperator getRootOperator() {
        return expression != null && expression.root() instanceof ExpressionNode.Operator op ? op.operator() : null;
    }

    /**
     * Variable that holds an expression operator,
     * and propagates invalidation of referenced variables.
     */
    public static class ExpressionOperatorVariable extends VariableAdapter<ValueTypeOperator.ValueOperator> {

        private final ValueTypeOperator.ValueOperator value;
        private final Collection<IVariable<?>> refs;

        public ExpressionOperatorVariable(ExpressionOperator operator, Collection<IVariable<?>> refs) {
            this.value = ValueTypeOperator.ValueOperator.of(operator);
            this.refs = refs;
        }

        @Override
        public IValueType<ValueTypeOperator.ValueOperator> getType() {
            return ValueTypes.OPERATOR;
        }

        @Override
        public ValueTypeOperator.ValueOperator getValue() throws EvaluationException {
            for (IVariable<?> ref : refs) {
                ref.addInvalidationListener(this);
            }
            return value;
        }
    }

}
