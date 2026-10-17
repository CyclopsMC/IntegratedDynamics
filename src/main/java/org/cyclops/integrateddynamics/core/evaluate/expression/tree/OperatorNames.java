package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Names of operators in printed expressions.
 *
 * Operators are named by their scoped interact name, such as {@code length},
 * as Integrated Scripting does for the methods of values.
 * Some scoped names are shared by operators with different first input types,
 * so a scoped name is only used when the type of the first argument tells which operator it is.
 * Otherwise, the globally unique interact name is used, such as {@code stringLength}.
 * A few common operators are written as symbols instead, such as {@code a + b} and {@code !a}.
 *
 * @author rubensworks
 */
public final class OperatorNames {

    private static final Map<IOperator, String> INFIX_SYMBOLS = Map.ofEntries(
            Map.entry(Operators.ARITHMETIC_ADDITION, "+"),
            Map.entry(Operators.ARITHMETIC_SUBTRACTION, "-"),
            Map.entry(Operators.ARITHMETIC_MULTIPLICATION, "*"),
            Map.entry(Operators.ARITHMETIC_DIVISION, "/"),
            Map.entry(Operators.ARITHMETIC_MODULUS, "%"),
            Map.entry(Operators.RELATIONAL_EQUALS, "=="),
            Map.entry(Operators.RELATIONAL_NOTEQUALS, "!="),
            Map.entry(Operators.RELATIONAL_LT, "<"),
            Map.entry(Operators.RELATIONAL_LE, "<="),
            Map.entry(Operators.RELATIONAL_GT, ">"),
            Map.entry(Operators.RELATIONAL_GE, ">="),
            Map.entry(Operators.LOGICAL_AND, "&&"),
            Map.entry(Operators.LOGICAL_OR, "||"));
    private static final Map<IOperator, String> PREFIX_SYMBOLS = Map.of(Operators.LOGICAL_NOT, "!");

    // Read from both the client and server thread
    private static volatile ScopedNames scopedNames = null;

    private OperatorNames() {
    }

    /**
     * @param operator An operator.
     * @return The symbol to write this operator with between its two inputs, or null.
     */
    @Nullable
    public static String getInfixSymbol(IOperator operator) {
        return INFIX_SYMBOLS.get(operator);
    }

    /**
     * @param operator An operator.
     * @return The symbol to write this operator with in front of its single input, or null.
     */
    @Nullable
    public static String getPrefixSymbol(IOperator operator) {
        return PREFIX_SYMBOLS.get(operator);
    }

    /**
     * @param operator An operator.
     * @param firstArgumentType The type of the first argument, or null if unknown.
     * @return The name to call the operator with.
     */
    public static String getName(IOperator operator, @Nullable IValueType<?> firstArgumentType) {
        if (!isRegistered(operator)) {
            return operator.getSymbol();
        }
        String scopedName = operator.getScopedInteractName();
        return resolve(scopedName, firstArgumentType) == operator ? scopedName : operator.getGlobalInteractName();
    }

    /**
     * @param operator An operator that is used as a value.
     * @return Its name, without an argument to tell shared names apart.
     */
    public static String getValueName(IOperator operator) {
        String symbol = getSymbol(operator);
        return symbol != null ? symbol : getName(operator, null);
    }

    /**
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return The name of the node's operator, as it is printed.
     */
    public static String getNodeName(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        String symbol = getSymbol(node.operator());
        if (symbol != null) {
            return symbol;
        }
        IValueType<?> firstArgumentType = node.children().isEmpty() ? null : types.getType(ExpressionTrees.append(path, 0));
        return getName(node.operator(), firstArgumentType);
    }

    /**
     * Find the operator with the given name, as written by {@link #getName(IOperator, IValueType)}.
     * Global names always match exactly. Scoped names match by the type of the first argument,
     * or match on their own if only one operator has them.
     * @param name A global or scoped name.
     * @param firstArgumentType The type of the first argument, or null if unknown.
     * @return The operator, or null if the name is unknown or ambiguous.
     */
    @Nullable
    public static IOperator resolve(String name, @Nullable IValueType<?> firstArgumentType) {
        IOperator global = Operators.REGISTRY.getGlobalInteractOperators().get(name);
        if (global != null) {
            return global;
        }
        List<IOperator> owners = getScopedNameOwners().getOrDefault(name, List.of());
        if (firstArgumentType != null) {
            IOperator match = null;
            int matches = 0;
            for (IOperator owner : owners) {
                if (accepts(owner, firstArgumentType)) {
                    match = owner;
                    matches++;
                }
            }
            if (matches == 1) {
                return match;
            }
        }
        return owners.size() == 1 ? owners.get(0) : null;
    }

    @Nullable
    private static String getSymbol(IOperator operator) {
        String symbol = getInfixSymbol(operator);
        return symbol != null ? symbol : getPrefixSymbol(operator);
    }

    // Global names are unique by registration, unique names are not guaranteed to be
    private static boolean isRegistered(IOperator operator) {
        return Operators.REGISTRY.getGlobalInteractOperators().get(operator.getGlobalInteractName()) == operator;
    }

    // Categories only match exactly, as they can hold several types with their own operators
    private static boolean accepts(IOperator operator, IValueType<?> firstArgumentType) {
        IValueType<?> firstInputType = operator.getInputTypes()[0];
        return firstInputType == firstArgumentType
                || (!(firstArgumentType instanceof IValueTypeCategory<?>) && firstInputType.correspondsTo(firstArgumentType));
    }

    private static Map<String, List<IOperator>> getScopedNameOwners() {
        // Rebuilt when operators were registered since
        int operatorCount = Operators.REGISTRY.getOperators().size();
        ScopedNames names = scopedNames;
        if (names == null || names.operatorCount() != operatorCount) {
            Map<String, List<IOperator>> owners = new HashMap<>();
            for (Map<String, IOperator> scoped : Operators.REGISTRY.getScopedInteractOperators().values()) {
                scoped.forEach((name, operator) -> owners.computeIfAbsent(name, n -> new ArrayList<>()).add(operator));
            }
            names = new ScopedNames(operatorCount, owners);
            scopedNames = names;
        }
        return names.owners();
    }

    private static record ScopedNames(int operatorCount, Map<String, List<IOperator>> owners) {}

}
