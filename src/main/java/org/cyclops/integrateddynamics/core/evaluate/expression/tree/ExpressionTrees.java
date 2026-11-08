package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for expression trees: serialization, path navigation and queries.
 * @author rubensworks
 */
public final class ExpressionTrees {

    /**
     * Maximum tree depth accepted when deserializing, to protect against malicious or corrupt data.
     */
    public static final int MAX_DEPTH = 64;
    /**
     * Maximum length of a parameter name.
     */
    public static final int MAX_PARAMETER_NAME_LENGTH = 16;

    private static final String KEY_KIND = "k";
    private static final String KEY_OPERATOR = "op";
    private static final String KEY_CHILDREN = "c";
    private static final String KEY_ID = "id";
    private static final String KEY_VALUE_TYPE = "t";
    private static final String KEY_VALUE = "v";
    private static final String KEY_INDEX = "i";
    private static final String KEY_ROOT = "root";
    private static final String KEY_PARAMETERS = "params";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";

    private static final byte KIND_OPERATOR = 0;
    private static final byte KIND_REF = 1;
    private static final byte KIND_CONST = 2;
    private static final byte KIND_PARAM = 3;
    private static final byte KIND_EMPTY = 4;

    private ExpressionTrees() {
    }

    // --------------- Serialization ---------------

    public static CompoundTag serialize(ValueDeseralizationContext context, Expression expression) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.holderLookupProvider());
        serialize(output, expression);
        return output.buildResult();
    }

    public static void serialize(ValueOutput output, Expression expression) {
        serializeNode(output.child(KEY_ROOT), expression.root());
        if (expression.hasParameters()) {
            ValueOutput.ValueOutputList params = output.childrenList(KEY_PARAMETERS);
            for (ExpressionParameter parameter : expression.parameters()) {
                ValueOutput param = params.addChild();
                param.putString(KEY_NAME, parameter.name());
                param.putString(KEY_TYPE, parameter.type().getUniqueName().toString());
            }
        }
    }

    public static Expression deserialize(ValueDeseralizationContext context, CompoundTag tag) throws EvaluationException {
        return deserialize(TagValueInput.create(ProblemReporter.DISCARDING, context.holderLookupProvider(), tag));
    }

    public static Expression deserialize(ValueInput input) throws EvaluationException {
        ValueInput rootInput = input.child(KEY_ROOT).orElseThrow(() -> deserializeError(input, "missing root"));
        List<ExpressionParameter> parameters = new ArrayList<>();
        for (ValueInput param : input.childrenListOrEmpty(KEY_PARAMETERS)) {
            Identifier typeName = Identifier.tryParse(param.getStringOr(KEY_TYPE, ""));
            IValueType<?> type = typeName == null ? null : ValueTypes.REGISTRY.getValueType(typeName);
            if (type == null) {
                throw deserializeError(param, "unknown parameter type");
            }
            String name = param.getStringOr(KEY_NAME, "");
            if (name.length() > MAX_PARAMETER_NAME_LENGTH) {
                name = name.substring(0, MAX_PARAMETER_NAME_LENGTH);
            }
            parameters.add(new ExpressionParameter(name, type));
        }
        ExpressionNode root = deserializeNode(rootInput, 0, parameters.size());
        return new Expression(root, parameters);
    }

    public static void serializeNode(ValueOutput output, ExpressionNode node) {
        switch (node) {
            case ExpressionNode.Operator op -> {
                output.putByte(KEY_KIND, KIND_OPERATOR);
                Operators.REGISTRY.serialize(output.child(KEY_OPERATOR), op.operator());
                ValueOutput.ValueOutputList children = output.childrenList(KEY_CHILDREN);
                for (ExpressionNode child : op.children()) {
                    serializeNode(children.addChild(), child);
                }
            }
            case ExpressionNode.Ref ref -> {
                output.putByte(KEY_KIND, KIND_REF);
                output.putInt(KEY_ID, ref.variableId());
            }
            case ExpressionNode.Const constant -> {
                output.putByte(KEY_KIND, KIND_CONST);
                IValue value = constant.value();
                output.putString(KEY_VALUE_TYPE, value.getType().getUniqueName().toString());
                ValueHelpers.serializeRaw(output.child(KEY_VALUE), value);
            }
            case ExpressionNode.Param param -> {
                output.putByte(KEY_KIND, KIND_PARAM);
                output.putInt(KEY_INDEX, param.index());
            }
            case ExpressionNode.Empty empty -> output.putByte(KEY_KIND, KIND_EMPTY);
        }
    }

    public static ExpressionNode deserializeNode(ValueInput input, int depth, int parameterCount) throws EvaluationException {
        if (depth > MAX_DEPTH) {
            throw deserializeError(input, "tree too deep");
        }
        switch (input.getByteOr(KEY_KIND, (byte) -1)) {
            case KIND_OPERATOR -> {
                IOperator operator;
                try {
                    operator = Operators.REGISTRY.deserialize(input.childOrEmpty(KEY_OPERATOR));
                } catch (RuntimeException e) {
                    throw deserializeError(input, String.valueOf(e.getMessage()));
                }
                if (operator == null) {
                    throw deserializeError(input, "unknown operator");
                }
                List<ExpressionNode> children = new ArrayList<>();
                for (ValueInput child : input.childrenListOrEmpty(KEY_CHILDREN)) {
                    children.add(deserializeNode(child, depth + 1, parameterCount));
                }
                if (children.size() != operator.getRequiredInputLength()) {
                    throw deserializeError(input, "wrong number of children");
                }
                return new ExpressionNode.Operator(operator, children);
            }
            case KIND_REF -> {
                return new ExpressionNode.Ref(input.getIntOr(KEY_ID, 0));
            }
            case KIND_CONST -> {
                Identifier typeName = Identifier.tryParse(input.getStringOr(KEY_VALUE_TYPE, ""));
                IValueType<?> valueType = typeName == null ? null : ValueTypes.REGISTRY.getValueType(typeName);
                if (valueType == null) {
                    throw deserializeError(input, "unknown value type");
                }
                try {
                    return new ExpressionNode.Const(ValueHelpers.deserializeRaw(input.childOrEmpty(KEY_VALUE), valueType));
                } catch (RuntimeException e) {
                    throw deserializeError(input, String.valueOf(e.getMessage()));
                }
            }
            case KIND_PARAM -> {
                int index = input.getIntOr(KEY_INDEX, -1);
                if (index < 0 || index >= parameterCount) {
                    throw deserializeError(input, "invalid parameter index");
                }
                return new ExpressionNode.Param(index);
            }
            case KIND_EMPTY -> {
                return ExpressionNode.EMPTY;
            }
            default -> throw deserializeError(input, "unknown node kind");
        }
    }

    /**
     * @param context For serializing the value.
     * @param value A value.
     * @return The value as its type writes it.
     */
    public static CompoundTag serializeValue(ValueDeseralizationContext context, IValue value) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.holderLookupProvider());
        ValueHelpers.serializeRaw(output, value);
        return output.buildResult();
    }

    /**
     * @param context For deserializing the value.
     * @param valueType The type of the value.
     * @param tag The value as its type writes it.
     * @return The value.
     * @throws RuntimeException If the tag is not a value of the type.
     */
    public static <T extends IValue> T deserializeValue(ValueDeseralizationContext context, IValueType<T> valueType, CompoundTag tag) {
        return ValueHelpers.deserializeRaw(TagValueInput.create(ProblemReporter.DISCARDING, context.holderLookupProvider(), tag), valueType);
    }

    private static EvaluationException deserializeError(Object data, String reason) {
        return new EvaluationException(Component.translatable(L10NValues.VALUETYPE_ERROR_DESERIALIZE, data.toString(), reason));
    }

    // --------------- Roots ---------------

    /**
     * The root of an expression is an operator, so a value, parameter, reference or empty slot as a whole expression
     * is the input of the identity operator, which is not written in its text.
     * @param node A whole expression.
     * @return The root for it.
     */
    public static ExpressionNode.Operator asRoot(ExpressionNode node) {
        return node instanceof ExpressionNode.Operator operator ? operator
                : new ExpressionNode.Operator(Operators.GENERAL_IDENTITY, List.of(node));
    }

    /**
     * @param root The root of an expression.
     * @return The input of the identity operator if the root was made by {@link #asRoot} for a node that is not an operator,
     *         which is the whole expression in its text, otherwise null.
     */
    @Nullable
    public static ExpressionNode getRootValue(ExpressionNode root) {
        return root instanceof ExpressionNode.Operator op && op.operator() == Operators.GENERAL_IDENTITY
                && op.children().size() == 1 && !(op.children().get(0) instanceof ExpressionNode.Operator)
                ? op.children().get(0) : null;
    }

    // --------------- Paths ---------------

    /**
     * @param root A root node.
     * @param path A path of child indexes.
     * @return The node at the given path, or null if the path is invalid.
     */
    @Nullable
    public static ExpressionNode get(ExpressionNode root, int[] path) {
        ExpressionNode node = root;
        for (int index : path) {
            if (!(node instanceof ExpressionNode.Operator op) || index < 0 || index >= op.children().size()) {
                return null;
            }
            node = op.children().get(index);
        }
        return node;
    }

    /**
     * @param root A root node.
     * @param path A valid path of child indexes.
     * @param replacement The node to put at the given path.
     * @return A new root node.
     */
    public static ExpressionNode replace(ExpressionNode root, int[] path, ExpressionNode replacement) {
        return replace(root, path, 0, replacement);
    }

    private static ExpressionNode replace(ExpressionNode node, int[] path, int offset, ExpressionNode replacement) {
        if (offset == path.length) {
            return replacement;
        }
        if (!(node instanceof ExpressionNode.Operator op) || path[offset] < 0 || path[offset] >= op.children().size()) {
            throw new IllegalArgumentException("Invalid expression path");
        }
        return op.withChild(path[offset], replace(op.children().get(path[offset]), path, offset + 1, replacement));
    }

    /**
     * @param root A root node.
     * @param path A path.
     * @return If the path points to an operator node.
     */
    public static boolean isOperatorPath(ExpressionNode root, int[] path) {
        return get(root, path) instanceof ExpressionNode.Operator;
    }

    public static int[] append(int[] path, int index) {
        int[] newPath = new int[path.length + 1];
        System.arraycopy(path, 0, newPath, 0, path.length);
        newPath[path.length] = index;
        return newPath;
    }

    public static int[] parent(int[] path) {
        if (path.length == 0) {
            return path;
        }
        int[] newPath = new int[path.length - 1];
        System.arraycopy(path, 0, newPath, 0, newPath.length);
        return newPath;
    }

    // --------------- Queries ---------------

    public static int countEmpty(ExpressionNode node) {
        if (node instanceof ExpressionNode.Empty) {
            return 1;
        }
        int count = 0;
        if (node instanceof ExpressionNode.Operator op) {
            for (ExpressionNode child : op.children()) {
                count += countEmpty(child);
            }
        }
        return count;
    }

    /**
     * @param node A node.
     * @return The depth, where a leaf has depth 0.
     */
    public static int depth(ExpressionNode node) {
        int depth = 0;
        if (node instanceof ExpressionNode.Operator op) {
            for (ExpressionNode child : op.children()) {
                depth = Math.max(depth, depth(child) + 1);
            }
        }
        return depth;
    }

    public static int countNodes(ExpressionNode node) {
        int count = 1;
        if (node instanceof ExpressionNode.Operator op) {
            for (ExpressionNode child : op.children()) {
                count += countNodes(child);
            }
        }
        return count;
    }

    /**
     * @param node A node.
     * @return The number of operator nodes in it, not counting operators used as values.
     */
    public static int countOperators(ExpressionNode node) {
        int count = 0;
        if (node instanceof ExpressionNode.Operator op) {
            count++;
            for (ExpressionNode child : op.children()) {
                count += countOperators(child);
            }
        }
        return count;
    }

    /**
     * @param node A node.
     * @return All referenced variable ids, in order of first occurrence.
     */
    public static IntList getRefIds(ExpressionNode node) {
        IntSet ids = new IntLinkedOpenHashSet();
        collectRefIds(node, ids);
        return new IntArrayList(ids);
    }

    private static void collectRefIds(ExpressionNode node, IntSet ids) {
        if (node instanceof ExpressionNode.Ref ref) {
            ids.add(ref.variableId());
        } else if (node instanceof ExpressionNode.Operator op) {
            for (ExpressionNode child : op.children()) {
                collectRefIds(child, ids);
            }
        }
    }

    public static boolean containsParam(ExpressionNode node, int index) {
        if (node instanceof ExpressionNode.Param param) {
            return param.index() == index;
        }
        if (node instanceof ExpressionNode.Operator op) {
            for (ExpressionNode child : op.children()) {
                if (containsParam(child, index)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param node A node.
     * @param removedIndex A removed parameter index.
     * @return The node where params referring to the removed index become empty, and higher indexes shift down.
     */
    public static ExpressionNode removeParam(ExpressionNode node, int removedIndex) {
        if (node instanceof ExpressionNode.Param param) {
            if (param.index() == removedIndex) {
                return ExpressionNode.EMPTY;
            }
            return param.index() > removedIndex ? new ExpressionNode.Param(param.index() - 1) : param;
        }
        if (node instanceof ExpressionNode.Operator op) {
            List<ExpressionNode> children = new ArrayList<>(op.children().size());
            for (ExpressionNode child : op.children()) {
                children.add(removeParam(child, removedIndex));
            }
            return new ExpressionNode.Operator(op.operator(), children);
        }
        return node;
    }

    /**
     * Check if the given expression can be stored as a plain operator variable,
     * which is the case for a single operator without parameters and only variable references as inputs.
     * @param expression An expression.
     * @return If it is equivalent to a plain operator variable.
     */
    public static boolean isPlainOperator(Expression expression) {
        if (expression.hasParameters() || !(expression.root() instanceof ExpressionNode.Operator op)) {
            return false;
        }
        for (ExpressionNode child : op.children()) {
            if (!(child instanceof ExpressionNode.Ref)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param expression A plain operator expression, see {@link #isPlainOperator(Expression)}.
     * @return The referenced variable ids.
     */
    public static int[] getPlainOperatorVariableIds(Expression expression) {
        ExpressionNode.Operator op = (ExpressionNode.Operator) expression.root();
        int[] ids = new int[op.children().size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ((ExpressionNode.Ref) op.children().get(i)).variableId();
        }
        return ids;
    }

    /**
     * @param operator An operator.
     * @param variableIds Variable ids.
     * @return An expression for a plain operator variable.
     */
    public static Expression fromPlainOperator(IOperator operator, int[] variableIds) {
        List<ExpressionNode> children = new ArrayList<>(variableIds.length);
        for (int variableId : variableIds) {
            children.add(new ExpressionNode.Ref(variableId));
        }
        return new Expression(new ExpressionNode.Operator(operator, children));
    }

}
