package org.cyclops.integrateddynamics.core.logicprogrammer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The editing state of an expression in the logic programmer,
 * together with the edit actions that are applied identically on client and server.
 * @author rubensworks
 */
public class ExpressionEditor {

    public static final String ACTION_CONST = "const";
    public static final String ACTION_OPERATOR = "op";
    public static final String ACTION_PARAM = "param";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_NAVIGATE = "nav";
    public static final String ACTION_ADD_PARAM = "addparam";
    public static final String ACTION_REMOVE_PARAM = "rmparam";
    public static final String ACTION_RENAME_PARAM = "renameparam";
    public static final String ACTION_PARAM_TYPE = "paramtype";

    /**
     * The types a parameter can be given in the logic programmer, in cycling order.
     */
    public static final List<IValueType<?>> PARAMETER_TYPES = List.of(
            ValueTypes.CATEGORY_ANY, ValueTypes.BOOLEAN, ValueTypes.INTEGER, ValueTypes.LONG, ValueTypes.DOUBLE,
            ValueTypes.CATEGORY_NUMBER, ValueTypes.STRING, ValueTypes.LIST, ValueTypes.OPERATOR, ValueTypes.NBT,
            ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK, ValueTypes.OBJECT_FLUIDSTACK, ValueTypes.OBJECT_ENTITY,
            ValueTypes.OBJECT_INGREDIENTS, ValueTypes.OBJECT_RECIPE);

    private static final String[] DEFAULT_PARAM_NAMES = {"x", "y", "z", "a", "b", "c", "d", "e"};

    private Expression expression;
    private int[] editPath = new int[0];
    private final Int2ObjectMap<IValueType<?>> refTypes = new Int2ObjectOpenHashMap<>();
    @Nullable
    private ExpressionTypeChecker.Result lastResult = null;

    public ExpressionEditor(IOperator rootOperator) {
        this.expression = new Expression(ExpressionNode.Operator.ofEmpty(rootOperator));
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
        this.editPath = new int[0];
        this.lastResult = null;
    }

    public int[] getEditPath() {
        return editPath;
    }

    /**
     * @return The operator node that is currently being edited.
     */
    public ExpressionNode.Operator getCurrentNode() {
        return (ExpressionNode.Operator) ExpressionTrees.get(expression.root(), editPath);
    }

    public IOperator getCurrentOperator() {
        return getCurrentNode().operator();
    }

    public ExpressionNode getChild(int slot) {
        return getCurrentNode().children().get(slot);
    }

    public int getSlotCount() {
        return getCurrentNode().children().size();
    }

    /**
     * @param slot A slot of the current node.
     * @return The input type of the current operator at the given slot.
     */
    public IValueType<?> getExpectedType(int slot) {
        IValueType<?>[] inputTypes = getCurrentOperator().getInputTypes();
        return slot < inputTypes.length ? inputTypes[slot] : ValueTypes.CATEGORY_ANY;
    }

    public void setRefType(int variableId, @Nullable IValueType<?> type) {
        if (type != null) {
            refTypes.put(variableId, type);
        }
        lastResult = null;
    }

    public boolean hasRefType(int variableId) {
        return refTypes.containsKey(variableId);
    }

    @Nullable
    public IValueType<?> getRefType(int variableId) {
        return refTypes.get(variableId);
    }

    /**
     * @return The known types of the variables referenced in the expression.
     */
    public Map<Integer, IValueType<?>> getRefTypes() {
        Map<Integer, IValueType<?>> types = new HashMap<>();
        for (int variableId : ExpressionTrees.getRefIds(expression.root())) {
            IValueType<?> type = refTypes.get(variableId);
            if (type != null) {
                types.put(variableId, type);
            }
        }
        return types;
    }

    /**
     * For references of unknown type, take the type of the operator input they are in when that type is not a category.
     * This holds for cards that were written without errors.
     */
    public void inferMissingRefTypes() {
        inferMissingRefTypes(expression.root());
        lastResult = null;
    }

    private void inferMissingRefTypes(ExpressionNode node) {
        if (node instanceof ExpressionNode.Operator op) {
            IValueType<?>[] inputTypes = op.operator().getInputTypes();
            for (int i = 0; i < op.children().size(); i++) {
                ExpressionNode child = op.children().get(i);
                if (child instanceof ExpressionNode.Ref ref && !refTypes.containsKey(ref.variableId())
                        && i < inputTypes.length && !(inputTypes[i] instanceof IValueTypeCategory<?>)) {
                    refTypes.put(ref.variableId(), inputTypes[i]);
                }
                inferMissingRefTypes(child);
            }
        }
    }

    /**
     * @return The type checking result of the current expression, cached until the next change.
     */
    public ExpressionTypeChecker.Result check() {
        if (lastResult == null) {
            lastResult = ExpressionTypeChecker.check(expression, refTypes::get);
        }
        return lastResult;
    }

    /**
     * @param path A path.
     * @return The names of the operators along the given path, starting at the root.
     */
    public List<IOperator> getOperatorsAlongPath(int[] path) {
        List<IOperator> operators = new ArrayList<>();
        for (int i = 0; i <= path.length; i++) {
            ExpressionNode node = ExpressionTrees.get(expression.root(), Arrays.copyOf(path, i));
            if (node instanceof ExpressionNode.Operator op) {
                operators.add(op.operator());
            }
        }
        return operators;
    }

    // --------------- Direct mutations ---------------

    /**
     * Set a child of the current node.
     * @param slot The slot.
     * @param node The new child.
     */
    public void setChild(int slot, ExpressionNode node) {
        if (slot < 0 || slot >= getSlotCount()) {
            return;
        }
        // Infer the type of an untyped parameter from the slot it is dropped in
        if (node instanceof ExpressionNode.Param param && param.index() < expression.parameters().size()) {
            ExpressionParameter parameter = expression.parameters().get(param.index());
            IValueType<?> expectedType = getExpectedType(slot);
            if (parameter.type() == ValueTypes.CATEGORY_ANY && expectedType != ValueTypes.CATEGORY_ANY
                    && (!(expectedType instanceof IValueTypeCategory<?>) || PARAMETER_TYPES.contains(expectedType))) {
                List<ExpressionParameter> parameters = new ArrayList<>(expression.parameters());
                parameters.set(param.index(), parameter.withType(expectedType));
                expression = expression.withParameters(parameters);
            }
        }
        expression = expression.withRoot(ExpressionTrees.replace(expression.root(), ExpressionTrees.append(editPath, slot), node));
        lastResult = null;
    }

    public boolean navigate(int[] path) {
        if (!ExpressionTrees.isOperatorPath(expression.root(), path) || Arrays.equals(path, editPath)) {
            return false;
        }
        editPath = path;
        return true;
    }

    public int addParameter() {
        List<ExpressionParameter> parameters = new ArrayList<>(expression.parameters());
        if (parameters.size() >= ExpressionTrees.MAX_PARAMETERS) {
            return -1;
        }
        String name = "p" + parameters.size();
        for (String candidate : DEFAULT_PARAM_NAMES) {
            if (parameters.stream().noneMatch(p -> p.name().equals(candidate))) {
                name = candidate;
                break;
            }
        }
        parameters.add(new ExpressionParameter(name, ValueTypes.CATEGORY_ANY));
        expression = expression.withParameters(parameters);
        lastResult = null;
        return parameters.size() - 1;
    }

    public void removeParameter(int index) {
        if (index < 0 || index >= expression.parameters().size()) {
            return;
        }
        List<ExpressionParameter> parameters = new ArrayList<>(expression.parameters());
        parameters.remove(index);
        expression = new Expression(ExpressionTrees.removeParam(expression.root(), index), parameters);
        lastResult = null;
    }

    public void renameParameter(int index, String name) {
        if (index < 0 || index >= expression.parameters().size()) {
            return;
        }
        name = sanitizeParameterName(name);
        if (name.isEmpty()) {
            return;
        }
        List<ExpressionParameter> parameters = new ArrayList<>(expression.parameters());
        parameters.set(index, parameters.get(index).withName(name));
        expression = expression.withParameters(parameters);
        lastResult = null;
    }

    public void setParameterType(int index, IValueType<?> type) {
        if (index < 0 || index >= expression.parameters().size() || !PARAMETER_TYPES.contains(type)) {
            return;
        }
        List<ExpressionParameter> parameters = new ArrayList<>(expression.parameters());
        parameters.set(index, parameters.get(index).withType(type));
        expression = expression.withParameters(parameters);
        lastResult = null;
    }

    /**
     * @param type A parameter type.
     * @param direction 1 for the next type, -1 for the previous one.
     * @return The next or previous parameter type, wrapping around.
     */
    public static IValueType<?> cycleParameterType(IValueType<?> type, int direction) {
        int index = PARAMETER_TYPES.indexOf(type);
        int size = PARAMETER_TYPES.size();
        return PARAMETER_TYPES.get(((index < 0 ? 0 : index + direction) % size + size) % size);
    }

    /**
     * @param expectedType The type of the slot the operator is picked for.
     * @param operator The picked operator.
     * @param forceValue If the player asked to insert the operator as a value.
     * @return If the operator is inserted as an operator value instead of as a nested call.
     */
    public static boolean isPickedAsValue(IValueType<?> expectedType, IOperator operator, boolean forceValue) {
        if (!ValueHelpers.correspondsTo(expectedType, ValueTypes.OPERATOR)) {
            return false;
        }
        // Operators that may return an operator are nested by default, as they are usually curried or combined
        return forceValue || (expectedType == ValueTypes.OPERATOR
                && !ValueHelpers.correspondsTo(operator.getOutputType(), ValueTypes.OPERATOR));
    }

    public static String sanitizeParameterName(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            }
        }
        String sanitized = sb.toString();
        return sanitized.length() > ExpressionTrees.MAX_PARAMETER_NAME_LENGTH
                ? sanitized.substring(0, ExpressionTrees.MAX_PARAMETER_NAME_LENGTH) : sanitized;
    }

    // --------------- Actions ---------------

    private static CompoundTag action(String type, int[] path) {
        CompoundTag tag = new CompoundTag();
        tag.putString("a", type);
        tag.putIntArray("path", path);
        return tag;
    }

    public static CompoundTag actionConst(ValueDeseralizationContext context, int[] path, int slot, IValue value) {
        CompoundTag tag = action(ACTION_CONST, path);
        tag.putInt("slot", slot);
        tag.put("value", ValueHelpers.serialize(context, value));
        return tag;
    }

    public static CompoundTag actionOperator(int[] path, int slot, IOperator operator) {
        CompoundTag tag = action(ACTION_OPERATOR, path);
        tag.putInt("slot", slot);
        tag.putString("operator", operator.getUniqueName().toString());
        return tag;
    }

    public static CompoundTag actionParam(int[] path, int slot, int index) {
        CompoundTag tag = action(ACTION_PARAM, path);
        tag.putInt("slot", slot);
        tag.putInt("index", index);
        return tag;
    }

    public static CompoundTag actionClear(int[] path, int slot) {
        CompoundTag tag = action(ACTION_CLEAR, path);
        tag.putInt("slot", slot);
        return tag;
    }

    public static CompoundTag actionNavigate(int[] path) {
        return action(ACTION_NAVIGATE, path);
    }

    public static CompoundTag actionAddParam() {
        return action(ACTION_ADD_PARAM, new int[0]);
    }

    public static CompoundTag actionRemoveParam(int index) {
        CompoundTag tag = action(ACTION_REMOVE_PARAM, new int[0]);
        tag.putInt("index", index);
        return tag;
    }

    public static CompoundTag actionRenameParam(int index, String name) {
        CompoundTag tag = action(ACTION_RENAME_PARAM, new int[0]);
        tag.putInt("index", index);
        tag.putString("name", name);
        return tag;
    }

    public static CompoundTag actionParamType(int index, IValueType<?> type) {
        CompoundTag tag = action(ACTION_PARAM_TYPE, new int[0]);
        tag.putInt("index", index);
        tag.putString("type", type.getUniqueName().toString());
        return tag;
    }

    /**
     * Apply the given action.
     * Actions that edit a slot are only accepted for the node that is currently being edited.
     * @param context The deserialization context.
     * @param action The action.
     * @return The result.
     */
    public ActionResult apply(ValueDeseralizationContext context, CompoundTag action) {
        try {
            return applyUnsafe(context, action);
        } catch (RuntimeException e) {
            // Malformed actions from clients are ignored
            return ActionResult.NONE;
        }
    }

    protected ActionResult applyUnsafe(ValueDeseralizationContext context, CompoundTag action) {
        String type = action.getString("a");
        int[] path = action.getIntArray("path");
        int slot = action.getInt("slot");
        boolean slotAction = type.equals(ACTION_CONST) || type.equals(ACTION_OPERATOR)
                || type.equals(ACTION_PARAM) || type.equals(ACTION_CLEAR);
        if (slotAction && (!Arrays.equals(path, editPath) || slot < 0 || slot >= getSlotCount())) {
            return ActionResult.NONE;
        }
        switch (type) {
            case ACTION_CONST -> {
                IValue value;
                try {
                    value = ValueHelpers.deserialize(context, action.getCompound("value"));
                } catch (IllegalArgumentException e) {
                    return ActionResult.NONE;
                }
                if (value == null) {
                    return ActionResult.NONE;
                }
                setChild(slot, new ExpressionNode.Const(value));
                return ActionResult.SLOT_CHANGED;
            }
            case ACTION_OPERATOR -> {
                if (editPath.length + 1 >= ExpressionTrees.MAX_DEPTH) {
                    return ActionResult.NONE;
                }
                IOperator operator = Operators.REGISTRY.getOperator(ResourceLocation.parse(action.getString("operator")));
                if (operator == null || operator.getRenderPattern() == null || operator.getRenderPattern() == IConfigRenderPattern.NONE
                        || operator.getRenderPattern().getSlotPositions().length != operator.getRequiredInputLength()) {
                    return ActionResult.NONE;
                }
                setChild(slot, ExpressionNode.Operator.ofEmpty(operator));
                return ActionResult.SLOT_CHANGED;
            }
            case ACTION_PARAM -> {
                int index = action.getInt("index");
                if (index < 0 || index >= expression.parameters().size()) {
                    return ActionResult.NONE;
                }
                setChild(slot, new ExpressionNode.Param(index));
                return ActionResult.SLOT_CHANGED;
            }
            case ACTION_CLEAR -> {
                setChild(slot, ExpressionNode.EMPTY);
                return ActionResult.SLOT_CHANGED;
            }
            case ACTION_NAVIGATE -> {
                return navigate(path) ? ActionResult.NAVIGATED : ActionResult.NONE;
            }
            case ACTION_ADD_PARAM -> {
                return addParameter() >= 0 ? ActionResult.CHANGED : ActionResult.NONE;
            }
            case ACTION_REMOVE_PARAM -> {
                removeParameter(action.getInt("index"));
                return ActionResult.CHANGED;
            }
            case ACTION_PARAM_TYPE -> {
                IValueType<?> valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(action.getString("type")));
                if (valueType == null) {
                    return ActionResult.NONE;
                }
                setParameterType(action.getInt("index"), valueType);
                return ActionResult.CHANGED;
            }
            case ACTION_RENAME_PARAM -> {
                if (!action.contains("name", Tag.TAG_STRING)) {
                    return ActionResult.NONE;
                }
                renameParameter(action.getInt("index"), action.getString("name"));
                return ActionResult.CHANGED;
            }
            default -> {
                return ActionResult.NONE;
            }
        }
    }

    /**
     * Parse the given text to a value for the given expected type.
     * @param expectedType The expected type, possibly a category.
     * @param text The text.
     * @return The parsed value, or null if it could not be parsed.
     */
    @Nullable
    public static IValue parseValue(IValueType<?> expectedType, String text) {
        if (text.isEmpty()) {
            return expectedType == ValueTypes.STRING ? ValueTypeString.ValueString.of("") : null;
        }
        List<IValueType<?>> candidates = new ArrayList<>();
        if (expectedType == ValueTypes.CATEGORY_ANY) {
            candidates.add(ValueTypes.BOOLEAN);
            candidates.add(ValueTypes.INTEGER);
            candidates.add(ValueTypes.LONG);
            candidates.add(ValueTypes.DOUBLE);
            candidates.add(ValueTypes.STRING);
        } else if (expectedType == ValueTypes.CATEGORY_NUMBER) {
            candidates.add(ValueTypes.INTEGER);
            candidates.add(ValueTypes.LONG);
            candidates.add(ValueTypes.DOUBLE);
        } else if (expectedType instanceof IValueTypeCategory<?> category) {
            candidates.addAll((java.util.Collection) category.getElements());
        } else {
            candidates.add(expectedType);
        }
        for (IValueType<?> candidate : candidates) {
            if (!isTextEditable(candidate)) {
                continue;
            }
            if (candidate == ValueTypes.BOOLEAN && !(text.equals("true") || text.equals("false"))) {
                continue;
            }
            try {
                return ValueHelpers.parseString(candidate, text);
            } catch (EvaluationException | RuntimeException e) {
                // Try next
            }
        }
        return null;
    }

    /**
     * @param type A value type.
     * @return If constants of this type can be entered as text.
     */
    public static boolean isTextEditable(IValueType<?> type) {
        return type == ValueTypes.BOOLEAN || type == ValueTypes.INTEGER || type == ValueTypes.LONG
                || type == ValueTypes.DOUBLE || type == ValueTypes.STRING || type == ValueTypes.NBT
                || type == ValueTypes.CATEGORY_ANY || type == ValueTypes.CATEGORY_NUMBER;
    }

    public static enum ActionResult {
        NONE,
        CHANGED,
        SLOT_CHANGED,
        NAVIGATED
    }

}
