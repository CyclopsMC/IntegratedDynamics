package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntList;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.GeneralChoiceOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyMaterialized;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Pretty-prints expression trees, such as {@code item -> size(nbt(item)) > 3}.
 * Operators are named as described in {@link OperatorNames}.
 * Choices are printed as {@code c ? a : b}, operator values as {@code (+)} or {@code (reduce)}.
 * @author rubensworks
 */
public final class ExpressionPrinter {

    private static final int MAX_CONST_LENGTH = 24;
    public static final String EMPTY = "?";

    private ExpressionPrinter() {
    }

    /**
     * @param expression An expression.
     * @param labels Resolves the label of a variable id, may return null.
     * @param refTypes Resolves the type of a variable id, may return null when unknown.
     * @return The pretty-printed expression.
     */
    public static String print(Expression expression, IntFunction<String> labels, IntFunction<IValueType<?>> refTypes) {
        return join(tokenize(expression, labels, ExpressionTypeChecker.check(expression, refTypes)));
    }

    /**
     * @param expression An expression.
     * @param labels Resolves the label of a variable id, may return null.
     * @return The pretty-printed expression, without known reference types.
     */
    public static String print(Expression expression, IntFunction<String> labels) {
        return print(expression, labels, id -> null);
    }

    /**
     * @param expression An expression.
     * @return The pretty-printed expression without labels.
     */
    public static String print(Expression expression) {
        return print(expression, id -> null);
    }

    /**
     * Print the given expression in the text form that {@link ExpressionParser} reads back into the same expression.
     * In contrast to {@link #print}, values are not shortened, parameters are written with their types,
     * and references only use a label if that label leads back to them.
     * @param expression An expression.
     * @param values For serializing values that are written with their type, such as items.
     * @param labels Resolves the label of a variable id, may return null.
     * @param labelIds Resolves the ids of the variables with a label.
     * @param refTypes Resolves the type of a variable id, may return null when unknown.
     * @return The text.
     */
    public static String printExact(Expression expression, ValueDeseralizationContext values, IntFunction<String> labels,
                                    Function<String, IntList> labelIds, IntFunction<IValueType<?>> refTypes) {
        return join(tokenizeExact(expression, values, labels, labelIds, ExpressionTypeChecker.check(expression, refTypes)));
    }

    /**
     * The text of {@link #printExact} as tokens, which tell what node each part of the text belongs to.
     * @param expression An expression.
     * @param values For serializing values that are written with their type, such as items.
     * @param labels Resolves the label of a variable id, may return null.
     * @param labelIds Resolves the ids of the variables with a label.
     * @param types The type checking result of the expression.
     * @return The tokens.
     */
    public static List<Token> tokenizeExact(Expression expression, ValueDeseralizationContext values, IntFunction<String> labels,
                                            Function<String, IntList> labelIds, ExpressionTypeChecker.Result types) {
        List<Token> tokens = new ArrayList<>();
        List<ExpressionParameter> parameters = expression.parameters();
        if (!parameters.isEmpty()) {
            boolean typed = parameters.stream().anyMatch(parameter -> parameter.type() != ValueTypes.CATEGORY_ANY);
            if (parameters.size() == 1 && !typed) {
                tokens.add(new Token(parameters.get(0).name(), null));
            } else {
                tokens.add(new Token("(", null));
                for (int i = 0; i < parameters.size(); i++) {
                    if (i > 0) {
                        tokens.add(new Token(", ", null));
                    }
                    ExpressionParameter parameter = parameters.get(i);
                    tokens.add(new Token(parameter.name(), null));
                    if (parameter.type() != ValueTypes.CATEGORY_ANY) {
                        tokens.add(new Token(": " + ExpressionParser.getTypeName(parameter.type()), null));
                    }
                }
                tokens.add(new Token(")", null));
            }
            tokens.add(new Token(" -> ", null));
        }
        new Tokenizer(expression, labels, types, tokens, new Exact(values, labelIds)).visit(expression.root(), new int[0], false);
        return tokens;
    }

    /**
     * @param value A value.
     * @param values For serializing values that are written with their type.
     * @return The value in the text form, which {@link ExpressionParser} reads back into the same value.
     */
    public static String printLiteral(IValue value, ValueDeseralizationContext values) {
        return switch (value) {
            case ValueTypeBoolean.ValueBoolean bool -> Boolean.toString(bool.getRawValue());
            case ValueTypeInteger.ValueInteger integer -> Integer.toString(integer.getRawValue());
            case ValueTypeLong.ValueLong longValue -> longValue.getRawValue() + "L";
            case ValueTypeDouble.ValueDouble doubleValue -> printDouble(doubleValue.getRawValue());
            case ValueTypeString.ValueString string -> quote(string.getRawValue());
            case ValueTypeOperator.ValueOperator operator when OperatorNames.isRegistered(operator.getRawValue()) ->
                    "(" + OperatorNames.getValueName(operator.getRawValue()) + ")";
            case ValueTypeList.ValueList list when isListLiteral(list) -> printList(list, values);
            // NBT is written as the tag itself, as in commands, and () for none
            case ValueTypeNbt.ValueNbt nbt -> printTyped(value, nbt.getRawValue().map(Object::toString).orElse("()"));
            default -> printTyped(value, ValueHelpers.serializeRaw(values, value).toString());
        };
    }

    /**
     * @param value A value.
     * @param serialized Its serialized form.
     * @return The value written with its type, separated by a space when the type name would otherwise continue into it.
     */
    private static String printTyped(IValue value, String serialized) {
        char first = serialized.isEmpty() ? ' ' : serialized.charAt(0);
        boolean separate = Character.isLetterOrDigit(first) || "_.:/-+".indexOf(first) >= 0;
        return "@" + ExpressionParser.getTypeName(value.getType()) + (separate ? " " : "") + serialized;
    }

    /**
     * @param list A list.
     * @return If it can be written as [a, b], which {@link ExpressionParser} reads back into the same list,
     *         so if it is not lazy and its type is the one its values give it.
     */
    private static boolean isListLiteral(ValueTypeList.ValueList<?, ?> list) {
        if (!(list.getRawValue() instanceof ValueTypeListProxyMaterialized<?, ?> proxy)) {
            return false;
        }
        List<IValue> elements = new ArrayList<>();
        proxy.forEach(elements::add);
        return ExpressionParser.getListType(elements) == proxy.getValueType();
    }

    private static String printList(ValueTypeList.ValueList<?, ?> list, ValueDeseralizationContext values) {
        List<String> elements = new ArrayList<>();
        for (IValue element : list.getRawValue()) {
            elements.add(printLiteral(element, values));
        }
        return "[" + String.join(", ", elements) + "]";
    }

    /**
     * @param text A text.
     * @return The text between quotes, with quotes, backslashes and characters that pasting drops escaped.
     */
    public static String quote(String text) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\r' -> sb.append("\\r");
                default -> {
                    // Also characters that pasting drops, and curly quotes, which end a string
                    if (c < 0x20 || c == 0x7F || c == '\u00A7' || ExpressionParser.isQuote(c)) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String printDouble(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        return Double.toString(value);
    }

    /**
     * @param expression The expression that contains the node.
     * @param path The path of a node.
     * @param labels Resolves the label of a variable id, may return null.
     * @param types The type checking result of the expression.
     * @return The pretty-printed node, without the parameter list.
     */
    public static String printNode(Expression expression, int[] path, IntFunction<String> labels,
                                   ExpressionTypeChecker.Result types) {
        List<Token> tokens = new ArrayList<>();
        new Tokenizer(expression, labels, types, tokens, null).visit(ExpressionTrees.get(expression.root(), path), path, false);
        return join(tokens);
    }

    /**
     * Pretty-print the given expression as a list of tokens.
     * @param expression An expression.
     * @param labels Resolves the label of a variable id, may return null.
     * @param types The type checking result of the expression.
     * @return The tokens.
     */
    public static List<Token> tokenize(Expression expression, IntFunction<String> labels, ExpressionTypeChecker.Result types) {
        List<Token> tokens = new ArrayList<>();
        List<ExpressionParameter> parameters = expression.parameters();
        if (!parameters.isEmpty()) {
            if (parameters.size() > 1) {
                tokens.add(new Token("(", null));
            }
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    tokens.add(new Token(", ", null));
                }
                tokens.add(new Token(parameters.get(i).name(), null));
            }
            if (parameters.size() > 1) {
                tokens.add(new Token(")", null));
            }
            tokens.add(new Token(" -> ", null));
        }
        new Tokenizer(expression, labels, types, tokens, null).visit(expression.root(), new int[0], false);
        return tokens;
    }

    /**
     * @param value A value.
     * @return A short string representation.
     */
    public static String printValue(IValue value) {
        IValueType type = value.getType();
        String text;
        if (value instanceof ValueTypeString.ValueString string) {
            text = "\"" + string.getRawValue() + "\"";
        } else if (value instanceof ValueTypeOperator.ValueOperator operatorValue) {
            text = "(" + OperatorNames.getValueName(operatorValue.getRawValue()) + ")";
        } else {
            text = type.toCompactString(value).getString();
        }
        if (text.length() > MAX_CONST_LENGTH) {
            text = text.substring(0, MAX_CONST_LENGTH - 1) + "…";
        }
        return text;
    }

    /**
     * @param variableId A variable id.
     * @param label A label or null.
     * @return The display string of a reference.
     */
    public static String printRef(int variableId, @Nullable String label) {
        return label != null && !label.isEmpty() ? "#" + label : "#" + variableId;
    }

    private static String join(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.text());
        }
        return sb.toString();
    }

    /**
     * A printed token.
     * @param text The text.
     * @param path The path of the node this token belongs to, or null for separators.
     */
    public static record Token(String text, @Nullable int[] path) {}

    private static record Exact(ValueDeseralizationContext values, Function<String, IntList> labelIds) {}

    private static class Tokenizer {
        private final Expression expression;
        private final IntFunction<String> labels;
        private final ExpressionTypeChecker.Result types;
        private final List<Token> tokens;
        @Nullable
        private final Exact exact;

        private Tokenizer(Expression expression, IntFunction<String> labels, ExpressionTypeChecker.Result types, List<Token> tokens,
                          @Nullable Exact exact) {
            this.expression = expression;
            this.labels = labels;
            this.types = types;
            this.tokens = tokens;
            this.exact = exact;
        }

        private void visit(ExpressionNode node, int[] path, boolean nested) {
            switch (node) {
                case ExpressionNode.Empty empty -> tokens.add(new Token(EMPTY, path));
                case ExpressionNode.Const constant -> tokens.add(new Token(printConst(constant.value()), path));
                case ExpressionNode.Ref ref -> tokens.add(new Token(printReference(ref.variableId()), path));
                case ExpressionNode.Param param -> tokens.add(new Token(param.index() < expression.parameters().size()
                        ? expression.parameters().get(param.index()).name() : EMPTY, path));
                case ExpressionNode.Operator op -> visitOperator(op, path, nested);
            }
        }

        private void visitOperator(ExpressionNode.Operator node, int[] path, boolean nested) {
            IOperator operator = node.operator();
            List<ExpressionNode> children = node.children();
            String infixSymbol = OperatorNames.getInfixSymbol(operator);
            String prefixSymbol = OperatorNames.getPrefixSymbol(operator);
            if (isTernary(node)) {
                if (nested) {
                    tokens.add(new Token("(", path));
                }
                visit(children.get(0), ExpressionTrees.append(path, 0), isTernary(children.get(0)));
                tokens.add(new Token(" ? ", path));
                visit(children.get(1), ExpressionTrees.append(path, 1), isTernary(children.get(1)));
                tokens.add(new Token(" : ", path));
                visit(children.get(2), ExpressionTrees.append(path, 2), isTernary(children.get(2)));
                if (nested) {
                    tokens.add(new Token(")", path));
                }
            } else if (infixSymbol != null && children.size() == 2) {
                if (nested) {
                    tokens.add(new Token("(", path));
                }
                visit(children.get(0), ExpressionTrees.append(path, 0), true);
                tokens.add(new Token(" " + infixSymbol + " ", path));
                visit(children.get(1), ExpressionTrees.append(path, 1), true);
                if (nested) {
                    tokens.add(new Token(")", path));
                }
            } else if (prefixSymbol != null && children.size() == 1) {
                tokens.add(new Token(prefixSymbol, path));
                visit(children.get(0), ExpressionTrees.append(path, 0), true);
            } else {
                tokens.add(new Token(OperatorNames.getNodeName(node, path, types) + "(", path));
                for (int i = 0; i < children.size(); i++) {
                    if (i > 0) {
                        tokens.add(new Token(", ", path));
                    }
                    visit(children.get(i), ExpressionTrees.append(path, i), false);
                }
                tokens.add(new Token(")", path));
            }
        }

        private String printConst(IValue value) {
            // An operator value with the name of a parameter would read as that parameter
            if (value instanceof ValueTypeOperator.ValueOperator operatorValue && OperatorNames.isRegistered(operatorValue.getRawValue())) {
                IOperator operator = operatorValue.getRawValue();
                String name = OperatorNames.getValueName(operator);
                if (expression.parameters().stream().anyMatch(parameter -> parameter.name().equals(name))) {
                    return "(" + operator.getGlobalInteractName() + ")";
                }
            }
            return exact != null ? printLiteral(value, exact.values()) : printValue(value);
        }

        private String printReference(int variableId) {
            String label = labels.apply(variableId);
            if (exact == null) {
                return printRef(variableId, label);
            }
            // Labels are not unique, so only use one that leads back to this variable
            if (label != null && !label.isEmpty() && exact.labelIds().apply(label).equals(IntList.of(variableId))) {
                return "#" + (ExpressionParser.isIdentifier(label) ? label : quote(label));
            }
            return "#" + variableId;
        }

        // Incomplete choices stay in prefix notation, as "? > ? ? a : b" is unreadable
        private static boolean isTernary(ExpressionNode node) {
            return node instanceof ExpressionNode.Operator op && op.operator() instanceof GeneralChoiceOperator
                    && op.children().size() == 3 && ExpressionTrees.countEmpty(op) == 0;
        }
    }

}
