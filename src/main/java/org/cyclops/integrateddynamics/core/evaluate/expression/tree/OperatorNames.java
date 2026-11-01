package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.GeneralChoiceOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Names of operators in printed expressions.
 *
 * Operators are named by their scoped interact name, such as {@code length},
 * as Integrated Scripting does for the methods of values.
 * Some scoped names are shared by operators with different input types, as + is by addition and concat.
 * Which one a name or symbol stands for is decided in the same way for both, see {@link #choose}.
 * A scoped name is only used when it is read back as the same operator,
 * and otherwise the globally unique interact name is used, such as {@code listLength}.
 * A call with a scoped name is written as a method of its first argument, as in {@code item.size()},
 * as Integrated Scripting does for the methods of values.
 * A few common operators are written as symbols instead, such as {@code a + b} and {@code !a},
 * where + joins strings as well.
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
    // + also joins strings
    private static final List<IOperator> PLUS_OPERATORS = List.of(Operators.ARITHMETIC_ADDITION, Operators.STRING_CONCAT);
    /**
     * The types of Integrated Dynamics in the order of the type selector of parameters,
     * which also decides between the operators of a shared name or symbol, see {@link #choose}.
     */
    public static final List<IValueType<?>> TYPE_ORDER = List.of(
            ValueTypes.CATEGORY_ANY, ValueTypes.BOOLEAN, ValueTypes.INTEGER, ValueTypes.LONG, ValueTypes.DOUBLE,
            ValueTypes.CATEGORY_NUMBER, ValueTypes.STRING, ValueTypes.LIST, ValueTypes.OPERATOR, ValueTypes.NBT,
            ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK, ValueTypes.OBJECT_FLUIDSTACK, ValueTypes.OBJECT_ENTITY,
            ValueTypes.OBJECT_INGREDIENTS, ValueTypes.OBJECT_RECIPE);
    // By the first input type in that order, then by the id of that type, as for those of add-ons, then by global name
    private static final Comparator<IOperator> ORDER = Comparator
            .comparingInt((IOperator operator) -> getOrder(getFirstInputType(operator)))
            .thenComparing(operator -> getFirstInputType(operator) == null ? "" : getFirstInputType(operator).getUniqueName().toString())
            .thenComparing(IOperator::getGlobalInteractName);
    private static final Map<String, IOperator> INFIX_OPERATORS = invert(INFIX_SYMBOLS);
    private static final Map<String, IOperator> PREFIX_OPERATORS = invert(PREFIX_SYMBOLS);

    // Read from both the client and server thread
    private static volatile ScopedNames scopedNames = null;

    private OperatorNames() {
    }

    /**
     * @param operator An operator.
     * @return The symbol to write this operator with between its two inputs, or null.
     *         Concat of strings shares + with addition.
     */
    @Nullable
    public static String getInfixSymbol(IOperator operator) {
        return operator == Operators.STRING_CONCAT ? "+" : INFIX_SYMBOLS.get(operator);
    }

    /**
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return The symbol to write the node with between its two inputs, or null if the symbol would be read
     *         as another operator, such as + for addition of strings, or if the operator has no symbol.
     */
    @Nullable
    public static String getInfixSymbol(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        String symbol = getInfixSymbol(node.operator());
        if (symbol == null || node.children().size() != 2) {
            return null;
        }
        IOperator read = getInfixOperator(symbol, getChildType(types, path, 0), getChildType(types, path, 1));
        return read == node.operator() ? symbol : null;
    }

    /**
     * @param symbol A symbol.
     * @param leftType The type of the first input, or null if unknown.
     * @param rightType The type of the second input, or null if unknown.
     * @return The operator written with this symbol between inputs of these types, see {@link #choose}, or null.
     *         So + is concat if one of the inputs is a string, and addition otherwise.
     */
    @Nullable
    public static IOperator getInfixOperator(String symbol, @Nullable IValueType<?> leftType, @Nullable IValueType<?> rightType) {
        if (symbol.equals("+")) {
            return choose(PLUS_OPERATORS, Arrays.asList(leftType, rightType), true);
        }
        return getInfixOperator(symbol);
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
     * @param symbol A symbol.
     * @return The operator written with this symbol between its two inputs, or null.
     *         + is addition here, see {@link #getInfixOperator(String, IValueType, IValueType)}.
     */
    @Nullable
    public static IOperator getInfixOperator(String symbol) {
        return INFIX_OPERATORS.get(symbol);
    }

    /**
     * @param symbol A symbol.
     * @return The operator written with this symbol in front of its single input, or null.
     */
    @Nullable
    public static IOperator getPrefixOperator(String symbol) {
        return PREFIX_OPERATORS.get(symbol);
    }

    /**
     * @param name A scoped name.
     * @return All operators with this scoped name.
     */
    public static List<IOperator> getScopedNameOperators(String name) {
        return getScopedNameOwners().getOrDefault(name, List.of());
    }

    /**
     * @param operator An operator.
     * @param argumentTypes The types of its arguments, null where unknown, or null for an operator used as a value.
     * @return The name to call the operator with: its scoped name if that is read back as the operator, and its global name otherwise.
     */
    public static String getName(IOperator operator, @Nullable List<IValueType<?>> argumentTypes) {
        if (!isRegistered(operator)) {
            return operator.getSymbol();
        }
        String scopedName = operator.getScopedInteractName();
        return resolve(scopedName, argumentTypes) == operator ? scopedName : operator.getGlobalInteractName();
    }

    /**
     * @param operator An operator that is used as a value.
     * @return Its name, without arguments to tell shared names apart.
     */
    public static String getValueName(IOperator operator) {
        String symbol = getSymbol(operator);
        // (+) is read as addition
        return symbol != null && operator != Operators.STRING_CONCAT ? symbol : getName(operator, null);
    }

    /**
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return The name of the node's operator, as it is printed.
     */
    public static String getNodeName(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        String symbol = getInfixSymbol(node, path, types);
        if (symbol == null) {
            symbol = getPrefixSymbol(node.operator());
        }
        return symbol != null ? symbol : getCallName(node, path, types);
    }

    /**
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return The name of the node's operator, as it is printed when the node is written as a call.
     */
    public static String getCallName(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        List<IValueType<?>> argumentTypes = new ArrayList<>();
        for (int i = 0; i < node.children().size(); i++) {
            argumentTypes.add(getChildType(types, path, i));
        }
        return getName(node.operator(), argumentTypes);
    }

    /**
     * Calls are written as a method of their first argument, as in {@code item.size()}, when they use the scoped name,
     * as the type of that argument tells which operator it is.
     * Operators that take an operator first, such as {@code map(f, l)}, and choices stay calls,
     * and so do calls on an empty slot or a number, which reads as a decimal point, as in {@code 1.max(2)}.
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return If the node is written as a method of its first argument.
     */
    public static boolean isMethodCall(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        IOperator operator = node.operator();
        if (node.children().isEmpty() || !isRegistered(operator) || operator instanceof GeneralChoiceOperator
                || operator.getInputTypes()[0] == ValueTypes.OPERATOR) {
            return false;
        }
        ExpressionNode receiver = node.children().get(0);
        if (receiver instanceof ExpressionNode.Empty || getChildType(types, path, 0) == ValueTypes.OPERATOR
                || (receiver instanceof ExpressionNode.Const constant && ValueTypes.CATEGORY_NUMBER.correspondsTo(constant.value().getType()))) {
            return false;
        }
        return getCallName(node, path, types).equals(operator.getScopedInteractName());
    }

    /**
     * Find the operator with the given name, as written by {@link #getName}.
     * Global names always match exactly, and scoped names that several operators share are chosen between by {@link #choose}.
     * @param name A global or scoped name.
     * @param argumentTypes The types of all arguments, null where unknown, or null for an operator used as a value.
     * @return The operator, or null if the name is unknown.
     */
    @Nullable
    public static IOperator resolve(String name, @Nullable List<IValueType<?>> argumentTypes) {
        return resolve(name, argumentTypes, true);
    }

    /**
     * Find the operator of a call that is still being typed, as {@link #resolve(String, List)} does.
     * @param name A global or scoped name.
     * @param argumentTypes The types of the arguments typed so far, null where unknown.
     * @return The operator, or null if the name is unknown.
     */
    @Nullable
    public static IOperator resolvePartial(String name, List<IValueType<?>> argumentTypes) {
        return resolve(name, argumentTypes, false);
    }

    @Nullable
    private static IOperator resolve(String name, @Nullable List<IValueType<?>> argumentTypes, boolean allArguments) {
        IOperator global = Operators.REGISTRY.getGlobalInteractOperators().get(name);
        if (global != null) {
            return global;
        }
        return choose(getScopedNameOwners().getOrDefault(name, List.of()), argumentTypes, allArguments);
    }

    /**
     * Choose the operator that a shared name or symbol stands for, in the same way for all of them:
     * <ol>
     *     <li>the operators that take as many inputs as there are arguments,</li>
     *     <li>of those, the ones whose inputs fit the arguments whose type is known best, where an input of exactly
     *     the type of an argument fits better than one of a category that holds it, such as Number or Any,</li>
     *     <li>and of those, the one whose first input comes first in {@link #TYPE_ORDER}, the order of the type selector,
     *     where the types of add-ons come last.</li>
     * </ol>
     * So {@code x + y} is addition, unless one of them is a string, {@code concat(x, "a")} joins strings,
     * and {@code length(x)} is the length of a string, unless x is a list.
     * When no operator takes the types, this still gives one, whose type error then tells what does not fit.
     * @param candidates The operators.
     * @param argumentTypes The types of the arguments, null or Any where unknown, or null for an operator used as a value.
     * @param allArguments If these are all arguments, and not only the ones typed so far.
     * @return The operator, or null if there are no candidates.
     */
    @Nullable
    static IOperator choose(List<IOperator> candidates, @Nullable List<IValueType<?>> argumentTypes, boolean allArguments) {
        if (candidates.size() <= 1) {
            return candidates.isEmpty() ? null : candidates.get(0);
        }
        List<IOperator> best = candidates;
        if (argumentTypes != null) {
            if (allArguments) {
                List<IOperator> sameCount = candidates.stream()
                        .filter(operator -> operator.getRequiredInputLength() == argumentTypes.size()).toList();
                if (!sameCount.isEmpty()) {
                    best = sameCount;
                }
            }
            List<IOperator> fitting = new ArrayList<>();
            int bestFit = -1;
            for (IOperator operator : best) {
                int fit = getFit(operator, argumentTypes);
                if (fit > bestFit) {
                    bestFit = fit;
                    fitting.clear();
                }
                if (fit == bestFit) {
                    fitting.add(operator);
                }
            }
            best = fitting;
        }
        return best.stream().min(ORDER).orElse(null);
    }

    /**
     * @return How well the inputs of the operator fit the arguments of a known type:
     *         two for each input of exactly the type of its argument, and one for each input that holds it,
     *         or whose type it holds, as a category such as Number does.
     */
    private static int getFit(IOperator operator, List<IValueType<?>> argumentTypes) {
        IValueType<?>[] inputTypes = operator.getInputTypes();
        int fit = 0;
        for (int i = 0; i < Math.min(inputTypes.length, argumentTypes.size()); i++) {
            IValueType<?> argumentType = argumentTypes.get(i);
            if (argumentType == null || argumentType == ValueTypes.CATEGORY_ANY) {
                continue;
            }
            if (inputTypes[i] == argumentType) {
                fit += 2;
            } else if (inputTypes[i].correspondsTo(argumentType) || (argumentType.isCategory() && argumentType.correspondsTo(inputTypes[i]))) {
                fit += 1;
            }
        }
        return fit;
    }

    /**
     * @param type A value type, or null.
     * @return Its position in {@link #TYPE_ORDER}, with all other types after it.
     */
    private static int getOrder(@Nullable IValueType<?> type) {
        int index = type == null ? -1 : TYPE_ORDER.indexOf(type);
        return index >= 0 ? index : TYPE_ORDER.size();
    }

    @Nullable
    private static IValueType<?> getFirstInputType(IOperator operator) {
        return operator.getInputTypes().length == 0 ? null : operator.getInputTypes()[0];
    }

    /**
     * @param receiverType The type of a value.
     * @return The operators that can be called as a method on values of this type, by the name to call them with,
     *         ordered by name. These are the operators whose first input takes the type,
     *         as for the methods of values in Integrated Scripting.
     */
    public static SortedMap<String, IOperator> getMethods(IValueType<?> receiverType) {
        SortedMap<String, IOperator> methods = new TreeMap<>(String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder()));
        Operators.REGISTRY.getScopedInteractOperators().forEach((firstInputType, operators) -> {
            if (firstInputType.correspondsTo(receiverType)) {
                for (IOperator operator : operators.values()) {
                    // Only the receiver is known
                    List<IValueType<?>> argumentTypes = new ArrayList<>(Collections.nCopies(operator.getRequiredInputLength(), null));
                    argumentTypes.set(0, receiverType);
                    methods.put(getName(operator, argumentTypes), operator);
                }
            }
        });
        return methods;
    }

    private static Map<String, IOperator> invert(Map<IOperator, String> symbols) {
        Map<String, IOperator> operators = new HashMap<>();
        symbols.forEach((operator, symbol) -> operators.put(symbol, operator));
        return Map.copyOf(operators);
    }

    @Nullable
    private static String getSymbol(IOperator operator) {
        String symbol = getInfixSymbol(operator);
        return symbol != null ? symbol : getPrefixSymbol(operator);
    }

    private static IValueType<?> getChildType(ExpressionTypeChecker.Result types, int[] path, int child) {
        IValueType<?> type = types.getType(ExpressionTrees.append(path, child));
        return type != null ? type : ValueTypes.CATEGORY_ANY;
    }

    /**
     * @param operator An operator.
     * @return If the operator is registered, so it can be named, in contrast to curried or combined operators.
     */
    public static boolean isRegistered(IOperator operator) {
        // Global names are unique by registration, unique names are not guaranteed to be
        return Operators.REGISTRY.getGlobalInteractOperators().get(operator.getGlobalInteractName()) == operator;
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
