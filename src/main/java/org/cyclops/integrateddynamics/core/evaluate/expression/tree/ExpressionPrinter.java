package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.GeneralChoiceOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
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
     * @param expression The expression that contains the node.
     * @param path The path of a node.
     * @param labels Resolves the label of a variable id, may return null.
     * @param types The type checking result of the expression.
     * @return The pretty-printed node, without the parameter list.
     */
    public static String printNode(Expression expression, int[] path, IntFunction<String> labels,
                                   ExpressionTypeChecker.Result types) {
        List<Token> tokens = new ArrayList<>();
        new Tokenizer(expression, labels, types, tokens).visit(ExpressionTrees.get(expression.root(), path), path, false);
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
        new Tokenizer(expression, labels, types, tokens).visit(expression.root(), new int[0], false);
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

    private static class Tokenizer {
        private final Expression expression;
        private final IntFunction<String> labels;
        private final ExpressionTypeChecker.Result types;
        private final List<Token> tokens;

        private Tokenizer(Expression expression, IntFunction<String> labels, ExpressionTypeChecker.Result types, List<Token> tokens) {
            this.expression = expression;
            this.labels = labels;
            this.types = types;
            this.tokens = tokens;
        }

        private void visit(ExpressionNode node, int[] path, boolean nested) {
            switch (node) {
                case ExpressionNode.Empty empty -> tokens.add(new Token(EMPTY, path));
                case ExpressionNode.Const constant -> tokens.add(new Token(printValue(constant.value()), path));
                case ExpressionNode.Ref ref -> tokens.add(new Token(printRef(ref.variableId(), labels.apply(ref.variableId())), path));
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

        // Incomplete choices stay in prefix notation, as "? > ? ? a : b" is unreadable
        private static boolean isTernary(ExpressionNode node) {
            return node instanceof ExpressionNode.Operator op && op.operator() instanceof GeneralChoiceOperator
                    && op.children().size() == 3 && ExpressionTrees.countEmpty(op) == 0;
        }
    }

}
