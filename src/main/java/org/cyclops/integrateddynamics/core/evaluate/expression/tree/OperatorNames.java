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
 * @author rubensworks
 */
public final class OperatorNames {

    // By registration, so the operators of add-ons do not change how text without types is read
    private static final Comparator<IOperator> ORDER = Comparator.comparingInt(OperatorNames::getRegistrationIndex);

    // Read from both the client and server thread
    private static volatile Names names = null;

    private OperatorNames() {
    }

    /**
     * @param operator An operator.
     * @return The symbol to write this operator with between its two inputs, or null.
     *         These are the operators whose symbol is one between two values in the text form.
     */
    @Nullable
    public static String getInfixSymbol(IOperator operator) {
        return getNames().infixSymbols().get(operator);
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
        return choose(getNames().infix().getOrDefault(symbol, List.of()), Arrays.asList(leftType, rightType), true);
    }

    /**
     * @param operator An operator.
     * @return The symbol to write this operator with in front of its single input, or null.
     */
    @Nullable
    public static String getPrefixSymbol(IOperator operator) {
        return getNames().prefixSymbols().get(operator);
    }

    /**
     * @param node An operator node.
     * @param path The path of the node.
     * @param types The type checking result of the expression that contains the node.
     * @return The symbol to write the node with in front of its single input, or null if the symbol would be read
     *         as another operator, or if the operator has no symbol.
     */
    @Nullable
    public static String getPrefixSymbol(ExpressionNode.Operator node, int[] path, ExpressionTypeChecker.Result types) {
        String symbol = getPrefixSymbol(node.operator());
        if (symbol == null || node.children().size() != 1) {
            return null;
        }
        return getPrefixOperator(symbol, getChildType(types, path, 0)) == node.operator() ? symbol : null;
    }

    /**
     * @param symbol A symbol.
     * @return The operator written with this symbol between its two inputs when their types are unknown, or null.
     *         + is addition here, see {@link #getInfixOperator(String, IValueType, IValueType)}.
     */
    @Nullable
    public static IOperator getInfixOperator(String symbol) {
        return getInfixOperator(symbol, null, null);
    }

    /**
     * @param symbol A symbol.
     * @param type The type of the input, or null if unknown.
     * @return The operator written with this symbol in front of its single input of this type, see {@link #choose}, or null.
     */
    @Nullable
    public static IOperator getPrefixOperator(String symbol, @Nullable IValueType<?> type) {
        return choose(getNames().prefix().getOrDefault(symbol, List.of()), Collections.singletonList(type), true);
    }

    /**
     * @param symbol A symbol.
     * @return The operator that this symbol in brackets stands for as a value, as in {@code (+)}, or null.
     */
    @Nullable
    public static IOperator getSymbolValue(String symbol) {
        IOperator infix = getInfixOperator(symbol);
        return infix != null ? infix : getPrefixOperator(symbol, null);
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
        // Only if the symbol is read back as this operator, as (+) is addition
        return symbol != null && getSymbolValue(symbol) == operator ? symbol : getName(operator, null);
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
            symbol = getPrefixSymbol(node, path, types);
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
     * Choose the operator that a shared name or symbol stands for:
     * <ol>
     *     <li>the operators that take as many inputs as there are arguments,</li>
     *     <li>of those, the ones whose inputs fit the arguments whose type is known best, where an input of exactly
     *     the type of an argument fits better than one of a category that holds it, such as Number or Any,</li>
     *     <li>and of those, the one that was registered first, so those of Integrated Dynamics before those of add-ons,
     *     whose operators do not change how text without types is read.</li>
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
        return getNames().scoped();
    }

    private static int getRegistrationIndex(IOperator operator) {
        return getNames().registrationIndex().getOrDefault(operator, Integer.MAX_VALUE);
    }

    private static Names getNames() {
        // Rebuilt when operators were registered since
        int operatorCount = Operators.REGISTRY.getOperators().size();
        Names current = names;
        if (current == null || current.operatorCount() != operatorCount) {
            Map<String, List<IOperator>> scoped = new HashMap<>();
            for (Map<String, IOperator> operators : Operators.REGISTRY.getScopedInteractOperators().values()) {
                operators.forEach((name, operator) -> scoped.computeIfAbsent(name, n -> new ArrayList<>()).add(operator));
            }
            Map<String, List<IOperator>> infix = new HashMap<>();
            Map<String, List<IOperator>> prefix = new HashMap<>();
            Map<IOperator, String> infixSymbols = new HashMap<>();
            Map<IOperator, String> prefixSymbols = new HashMap<>();
            Map<IOperator, Integer> registrationIndex = new HashMap<>();
            for (IOperator operator : Operators.REGISTRY.getOperators()) {
                registrationIndex.putIfAbsent(operator, registrationIndex.size());
                String symbol = operator.getSymbol();
                if (!isRegistered(operator)) {
                    continue;
                }
                if (operator.getRequiredInputLength() == 2 && ExpressionParser.INFIX_SYMBOLS.contains(symbol)) {
                    infix.computeIfAbsent(symbol, s -> new ArrayList<>()).add(operator);
                    infixSymbols.put(operator, symbol);
                } else if (operator.getRequiredInputLength() == 1 && ExpressionParser.PREFIX_SYMBOLS.contains(symbol)) {
                    prefix.computeIfAbsent(symbol, s -> new ArrayList<>()).add(operator);
                    prefixSymbols.put(operator, symbol);
                }
            }
            current = new Names(operatorCount, scoped, infix, prefix, infixSymbols, prefixSymbols, registrationIndex);
            names = current;
        }
        return current;
    }

    // The operators of each shared scoped name and symbol, and the order of registration
    private static record Names(int operatorCount, Map<String, List<IOperator>> scoped, Map<String, List<IOperator>> infix,
                                Map<String, List<IOperator>> prefix, Map<IOperator, String> infixSymbols,
                                Map<IOperator, String> prefixSymbols, Map<IOperator, Integer> registrationIndex) {}

}
