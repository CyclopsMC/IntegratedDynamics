package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
     * Maximum number of parameters.
     */
    public static final int MAX_PARAMETERS = 8;
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
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_ROOT, serializeNode(context, expression.root()));
        if (expression.hasParameters()) {
            ListTag params = new ListTag();
            for (ExpressionParameter parameter : expression.parameters()) {
                CompoundTag paramTag = new CompoundTag();
                paramTag.putString(KEY_NAME, parameter.name());
                paramTag.putString(KEY_TYPE, parameter.type().getUniqueName().toString());
                params.add(paramTag);
            }
            tag.put(KEY_PARAMETERS, params);
        }
        return tag;
    }

    public static Expression deserialize(ValueDeseralizationContext context, CompoundTag tag) throws EvaluationException {
        if (!tag.contains(KEY_ROOT, Tag.TAG_COMPOUND)) {
            throw deserializeError(tag, "missing root");
        }
        List<ExpressionParameter> parameters = new ArrayList<>();
        ListTag params = tag.getList(KEY_PARAMETERS, Tag.TAG_COMPOUND);
        if (params.size() > MAX_PARAMETERS) {
            throw deserializeError(tag, "too many parameters");
        }
        for (int i = 0; i < params.size(); i++) {
            CompoundTag paramTag = params.getCompound(i);
            IValueType<?> type = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(paramTag.getString(KEY_TYPE)));
            if (type == null) {
                throw deserializeError(paramTag, "unknown parameter type");
            }
            String name = paramTag.getString(KEY_NAME);
            if (name.length() > MAX_PARAMETER_NAME_LENGTH) {
                name = name.substring(0, MAX_PARAMETER_NAME_LENGTH);
            }
            parameters.add(new ExpressionParameter(name, type));
        }
        ExpressionNode root = deserializeNode(context, tag.getCompound(KEY_ROOT), 0, parameters.size());
        return new Expression(root, parameters);
    }

    public static CompoundTag serializeNode(ValueDeseralizationContext context, ExpressionNode node) {
        CompoundTag tag = new CompoundTag();
        switch (node) {
            case ExpressionNode.Operator op -> {
                tag.putByte(KEY_KIND, KIND_OPERATOR);
                tag.put(KEY_OPERATOR, Operators.REGISTRY.serialize(context, op.operator()));
                ListTag children = new ListTag();
                for (ExpressionNode child : op.children()) {
                    children.add(serializeNode(context, child));
                }
                tag.put(KEY_CHILDREN, children);
            }
            case ExpressionNode.Ref ref -> {
                tag.putByte(KEY_KIND, KIND_REF);
                tag.putInt(KEY_ID, ref.variableId());
            }
            case ExpressionNode.Const constant -> {
                tag.putByte(KEY_KIND, KIND_CONST);
                IValue value = constant.value();
                tag.putString(KEY_VALUE_TYPE, value.getType().getUniqueName().toString());
                tag.put(KEY_VALUE, ValueHelpers.serializeRaw(context, value));
            }
            case ExpressionNode.Param param -> {
                tag.putByte(KEY_KIND, KIND_PARAM);
                tag.putInt(KEY_INDEX, param.index());
            }
            case ExpressionNode.Empty empty -> tag.putByte(KEY_KIND, KIND_EMPTY);
        }
        return tag;
    }

    public static ExpressionNode deserializeNode(ValueDeseralizationContext context, CompoundTag tag, int depth, int parameterCount) throws EvaluationException {
        if (depth > MAX_DEPTH) {
            throw deserializeError(tag, "tree too deep");
        }
        switch (tag.getByte(KEY_KIND)) {
            case KIND_OPERATOR -> {
                IOperator operator = Operators.REGISTRY.deserialize(context, tag.get(KEY_OPERATOR));
                if (operator == null) {
                    throw deserializeError(tag, "unknown operator");
                }
                ListTag childrenTag = tag.getList(KEY_CHILDREN, Tag.TAG_COMPOUND);
                if (childrenTag.size() != operator.getRequiredInputLength()) {
                    throw deserializeError(tag, "wrong number of children");
                }
                List<ExpressionNode> children = new ArrayList<>(childrenTag.size());
                for (int i = 0; i < childrenTag.size(); i++) {
                    children.add(deserializeNode(context, childrenTag.getCompound(i), depth + 1, parameterCount));
                }
                return new ExpressionNode.Operator(operator, children);
            }
            case KIND_REF -> {
                return new ExpressionNode.Ref(tag.getInt(KEY_ID));
            }
            case KIND_CONST -> {
                IValueType<?> valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(tag.getString(KEY_VALUE_TYPE)));
                if (valueType == null) {
                    throw deserializeError(tag, "unknown value type");
                }
                try {
                    return new ExpressionNode.Const(ValueHelpers.deserializeRaw(context, valueType, tag.get(KEY_VALUE)));
                } catch (IllegalArgumentException e) {
                    throw deserializeError(tag, e.getMessage());
                }
            }
            case KIND_PARAM -> {
                int index = tag.getInt(KEY_INDEX);
                if (index < 0 || index >= parameterCount) {
                    throw deserializeError(tag, "invalid parameter index");
                }
                return new ExpressionNode.Param(index);
            }
            case KIND_EMPTY -> {
                return ExpressionNode.EMPTY;
            }
            default -> throw deserializeError(tag, "unknown node kind");
        }
    }

    private static EvaluationException deserializeError(Tag tag, String reason) {
        return new EvaluationException(Component.translatable(L10NValues.VALUETYPE_ERROR_DESERIALIZE, tag.toString(), reason));
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
