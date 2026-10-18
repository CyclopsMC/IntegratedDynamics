package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Parses the text form of expressions, as written by {@link ExpressionPrinter#printExact}.
 *
 * <pre>
 * expression     := [parameters "=&gt;"] ternary
 * parameters     := NAME | "(" parameter ("," parameter)* ")"
 * parameter      := NAME [":" [NAME ":"] NAME]
 * ternary        := or ["?" ternary ":" ternary]
 * or             := and ("||" and)*
 * and            := equality ("&amp;&amp;" equality)*
 * equality       := relational (("==" | "!=") relational)*
 * relational     := additive (("&lt;" | "&lt;=" | "&gt;" | "&gt;=") additive)*
 * additive       := multiplicative (("+" | "-") multiplicative)*
 * multiplicative := unary (("*" | "/" | "%") unary)*
 * unary          := "!" unary | ("-" NUMBER | primary) ("." NAME arguments)*
 * primary        := NUMBER | STRING | "true" | "false" | "@" TYPE SNBT | "#" (ID | NAME | STRING) | "?"
 *                 | "[" [ternary ("," ternary)* [","]] "]" | NAME arguments | NAME | "(" (NAME | SYMBOL) ")" | "(" ternary ")"
 * arguments      := "(" [ternary ("," ternary)*] ")"
 * </pre>
 *
 * Operator names are resolved by {@link OperatorNames}, using the type of the first argument.
 * A {@code ?} is an empty slot, and {@code (name)} an operator used as a value.
 * As in Integrated Scripting, {@code a.f(b)} is {@code f(a, b)}, calling a parameter as in {@code f(a)} is {@code apply(f, a)},
 * + joins strings, strings can be in single quotes, and {@code ->}, {@code ===} and {@code !==}
 * can be written for {@code =>}, {@code ==} and {@code !=}.
 *
 * @author rubensworks
 */
public final class ExpressionParser {

    public static final int MAX_LENGTH = 8192;
    private static final int MAX_NESTING = ExpressionTrees.MAX_DEPTH * 2;
    private static final List<List<String>> BINARY_LEVELS = List.of(
            List.of("||"), List.of("&&"), List.of("==", "!="), List.of("<", "<=", ">", ">="), List.of("+", "-"), List.of("*", "/", "%"));
    private static final List<String> SYMBOLS = List.of(
            "===", "!==", "=>", "->", "==", "!=", "<=", ">=", "&&", "||", "(", ")", "[", "]", ",", "?", ":", ".",
            "+", "-", "*", "/", "%", "<", ">", "!");
    private static final Map<String, String> SYMBOL_ALIASES = Map.of("->", "=>", "===", "==", "!==", "!=");
    private static final List<IOperator> APPLY_OPERATORS = List.of(
            Operators.OPERATOR_APPLY_0, Operators.OPERATOR_APPLY, Operators.OPERATOR_APPLY_2, Operators.OPERATOR_APPLY_3);
    private static final Set<String> RESERVED_NAMES = Set.of("true", "false", "NaN", "Infinity");

    private final Context context;
    private final List<Token> tokens;
    private int index = 0;
    private int nesting = 0;
    private List<ExpressionParameter> parameters = List.of();
    private ExpressionTypeChecker.Inference types;

    private ExpressionParser(Context context, List<Token> tokens) {
        this.context = context;
        this.tokens = tokens;
    }

    /**
     * @param text The text of an expression.
     * @param context What the parser needs to know about the world.
     * @return The expression.
     * @throws ExpressionParseException If the text is not a valid expression.
     */
    public static Expression parse(String text, Context context) throws ExpressionParseException {
        if (text.length() > MAX_LENGTH) {
            throw new ExpressionParseException(MAX_LENGTH, Component.translatable(L10NValues.EXPRESSION_PARSE_TOO_LONG, MAX_LENGTH));
        }
        return new ExpressionParser(context, new Lexer(text, context).tokenize()).parseExpression();
    }

    /**
     * @param name A parameter name.
     * @return If the name can be written in the text form.
     */
    public static boolean isValidParameterName(String name) {
        return isIdentifier(name) && name.length() <= ExpressionTrees.MAX_PARAMETER_NAME_LENGTH && !RESERVED_NAMES.contains(name);
    }

    /**
     * @param text A text.
     * @return If the text is a single name.
     */
    public static boolean isIdentifier(String text) {
        if (text.isEmpty() || !isNameStart(text.charAt(0))) {
            return false;
        }
        for (int i = 1; i < text.length(); i++) {
            if (!isNamePart(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param valueType A value type.
     * @return The name of the type in the text form.
     */
    public static String getTypeName(IValueType<?> valueType) {
        ResourceLocation id = valueType.getUniqueName();
        return id.getNamespace().equals(Reference.MOD_ID) ? id.getPath() : id.toString();
    }

    /**
     * @param name The name of a type in the text form, which types of add-ons can also leave their namespace out of
     *             when no other type has that name, as in {@code chemicalstack}.
     * @return The type, or null if unknown or ambiguous.
     */
    @Nullable
    public static IValueType<?> getValueType(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name.contains(":") ? name : Reference.MOD_ID + ":" + name);
        IValueType<?> type = id == null ? null : ValueTypes.REGISTRY.getValueType(id);
        if (type != null || name.contains(":")) {
            return type;
        }
        for (IValueType<?> candidate : ValueTypes.REGISTRY.getValueTypes()) {
            if (candidate.getUniqueName().getPath().equals(name)) {
                if (type != null) {
                    return null;
                }
                type = candidate;
            }
        }
        return type;
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // --------------- Grammar ---------------

    private Expression parseExpression() throws ExpressionParseException {
        if (isParameterHeader()) {
            parameters = parseParameters();
            expect("=>");
        }
        types = new ExpressionTypeChecker.Inference(parameters, context.refTypes());
        Token start = peek(0);
        ExpressionNode root = parseTernary();
        Token trailing = peek(0);
        if (trailing.kind() != Kind.END) {
            throw error(trailing, L10NValues.EXPRESSION_PARSE_TRAILING, trailing.text());
        }
        if (!(root instanceof ExpressionNode.Operator)) {
            throw new ExpressionParseException(start.start(), Component.translatable(L10NValues.EXPRESSION_PARSE_ROOT));
        }
        if (ExpressionTrees.depth(root) > ExpressionTrees.MAX_DEPTH) {
            throw new ExpressionParseException(start.start(), Component.translatable(L10NValues.EXPRESSION_PARSE_TOO_DEEP));
        }
        return new Expression(root, parameters);
    }

    private boolean isParameterHeader() {
        if (peek(0).kind() == Kind.NAME && isSymbol(peek(1), "=>")) {
            return true;
        }
        if (!isSymbol(peek(0), "(")) {
            return false;
        }
        int i = 1;
        while (true) {
            if (peek(i).kind() != Kind.NAME) {
                return false;
            }
            i++;
            if (isSymbol(peek(i), ":")) {
                if (peek(i + 1).kind() != Kind.NAME) {
                    return false;
                }
                i += 2;
                // With the namespace of an add-on, as in mod:type
                if (isSymbol(peek(i), ":") && peek(i + 1).kind() == Kind.NAME) {
                    i += 2;
                }
            }
            if (!isSymbol(peek(i), ",")) {
                return isSymbol(peek(i), ")") && isSymbol(peek(i + 1), "=>");
            }
            i++;
        }
    }

    private List<ExpressionParameter> parseParameters() throws ExpressionParseException {
        List<ExpressionParameter> parameters = new ArrayList<>();
        if (peek(0).kind() == Kind.NAME) {
            parameters.add(parseParameter(parameters));
            return parameters;
        }
        expect("(");
        do {
            parameters.add(parseParameter(parameters));
        } while (accept(","));
        expect(")");
        return parameters;
    }

    private ExpressionParameter parseParameter(List<ExpressionParameter> previous) throws ExpressionParseException {
        Token name = next();
        if (!isValidParameterName(name.text())) {
            throw error(name, L10NValues.EXPRESSION_PARSE_PARAMETER_NAME, name.text());
        }
        if (previous.stream().anyMatch(parameter -> parameter.name().equals(name.text()))) {
            throw error(name, L10NValues.EXPRESSION_PARSE_DUPLICATE_PARAMETER, name.text());
        }
        if (previous.size() >= ExpressionTrees.MAX_PARAMETERS) {
            throw error(name, L10NValues.EXPRESSION_PARSE_TOO_MANY_PARAMETERS, ExpressionTrees.MAX_PARAMETERS);
        }
        IValueType<?> type = ValueTypes.CATEGORY_ANY;
        if (accept(":")) {
            Token typeName = next();
            String typeText = typeName.text();
            if (typeName.kind() == Kind.NAME && isSymbol(peek(0), ":") && peek(1).kind() == Kind.NAME) {
                next();
                typeText += ":" + next().text();
            }
            type = getValueType(typeText);
            if (type == null) {
                throw error(typeName, L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, typeText);
            }
            if (!context.parameterTypes().test(type)) {
                throw error(typeName, L10NValues.EXPRESSION_PARSE_PARAMETER_TYPE, typeText);
            }
        }
        return new ExpressionParameter(name.text(), type);
    }

    private ExpressionNode parseTernary() throws ExpressionParseException {
        ExpressionNode condition = parseBinary(0);
        Token question = peek(0);
        if (!accept("?")) {
            return condition;
        }
        enter(question);
        ExpressionNode then = parseTernary();
        expect(":");
        ExpressionNode otherwise = parseTernary();
        nesting--;
        return operator(Operators.GENERAL_CHOICE, List.of(condition, then, otherwise), question);
    }

    private ExpressionNode parseBinary(int level) throws ExpressionParseException {
        if (level == BINARY_LEVELS.size()) {
            return parseUnary();
        }
        ExpressionNode left = parseBinary(level + 1);
        while (peek(0).kind() == Kind.SYMBOL && BINARY_LEVELS.get(level).contains(symbol(peek(0)))) {
            Token symbol = next();
            ExpressionNode right = parseBinary(level + 1);
            IOperator operator = OperatorNames.getInfixOperator(symbol(symbol), types.getType(left), types.getType(right));
            left = operator(operator, List.of(left, right), symbol);
        }
        return left;
    }

    private ExpressionNode parseUnary() throws ExpressionParseException {
        Token token = peek(0);
        if (accept("!")) {
            enter(token);
            ExpressionNode child = parseUnary();
            nesting--;
            return operator(OperatorNames.getPrefixOperator("!"), List.of(child), token);
        }
        if (isSymbol(token, "-") && isNumber(peek(1))) {
            next();
            return parseMethodCalls(new ExpressionNode.Const(parseNumber(next(), true)));
        }
        return parseMethodCalls(parsePrimary());
    }

    /**
     * Read the method calls after a value, where a.f(b) is f(a, b).
     */
    private ExpressionNode parseMethodCalls(ExpressionNode receiver) throws ExpressionParseException {
        int outerNesting = nesting;
        while (isSymbol(peek(0), ".")) {
            Token dot = next();
            Token name = peek(0);
            if (name.kind() != Kind.NAME || !isSymbol(peek(1), "(")) {
                throw error(dot, L10NValues.EXPRESSION_PARSE_METHOD);
            }
            next();
            // Each call is a level deeper
            enter(dot);
            List<ExpressionNode> arguments = new ArrayList<>();
            arguments.add(receiver);
            arguments.addAll(parseArguments());
            receiver = resolveCall(name, arguments);
        }
        nesting = outerNesting;
        return receiver;
    }

    private ExpressionNode parsePrimary() throws ExpressionParseException {
        Token token = next();
        switch (token.kind()) {
            case NUMBER -> {
                return new ExpressionNode.Const(parseNumber(token, false));
            }
            case STRING -> {
                return new ExpressionNode.Const(ValueTypeString.ValueString.of((String) token.value()));
            }
            case TYPED -> {
                return new ExpressionNode.Const((IValue) token.value());
            }
            case REF -> {
                return parseRef(token);
            }
            case NAME -> {
                if (isSymbol(peek(0), "(")) {
                    return parseCall(token);
                }
                return parseName(token);
            }
            case SYMBOL -> {
                if (isSymbol(token, "?")) {
                    return ExpressionNode.EMPTY;
                }
                if (isSymbol(token, "[")) {
                    return new ExpressionNode.Const(parseList(token));
                }
                if (isSymbol(token, "(")) {
                    if (isOperatorValue(peek(0)) && isSymbol(peek(1), ")")) {
                        Token name = next();
                        next();
                        return new ExpressionNode.Const(ValueTypeOperator.ValueOperator.of(resolveOperatorValue(name)));
                    }
                    enter(token);
                    ExpressionNode node = parseTernary();
                    expect(")");
                    nesting--;
                    return node;
                }
                throw error(token, L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE, token.text());
            }
            default -> throw error(token, L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE_END);
        }
    }

    /**
     * Read a list of values after its opening bracket, such as ["a", "b"].
     * A comma after the last value is allowed, as in text that is copied from elsewhere.
     */
    private IValue parseList(Token open) throws ExpressionParseException {
        enter(open);
        List<IValue> values = new ArrayList<>();
        while (!accept("]")) {
            Token start = peek(0);
            if (!(parseTernary() instanceof ExpressionNode.Const constant)) {
                throw error(start, L10NValues.EXPRESSION_PARSE_LIST_ELEMENT);
            }
            values.add(constant.value());
            if (!accept(",")) {
                expect("]");
                break;
            }
        }
        nesting--;
        return ValueTypeList.ValueList.ofList((IValueType) getListType(values), values);
    }

    /**
     * @param values The values of a list.
     * @return The type of a list literal with these values: the type they share, or Any if they differ or there are none.
     */
    public static IValueType<?> getListType(List<? extends IValue> values) {
        IValueType<?> type = values.isEmpty() ? ValueTypes.CATEGORY_ANY : values.get(0).getType();
        for (IValue value : values) {
            if (value.getType() != type) {
                return ValueTypes.CATEGORY_ANY;
            }
        }
        return type;
    }

    /**
     * @param c A character.
     * @return If it separates tokens, also spaces that text copied from elsewhere can contain, such as non-breaking ones.
     */
    static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\u200B' || c == '\uFEFF';
    }

    /**
     * @param c A character.
     * @return If it starts or ends a string, also curly quotes that text copied from elsewhere can contain.
     */
    static boolean isQuote(char c) {
        return c == '"' || (c >= '\u201C' && c <= '\u201F');
    }

    /**
     * @param c A character.
     * @return If it starts or ends a string in single quotes, also curly ones.
     */
    static boolean isSingleQuote(char c) {
        return c == '\'' || (c >= '\u2018' && c <= '\u201B');
    }

    /**
     * @param typed A tag as it was typed.
     * @param written The tag of the value that was read from it.
     * @return If everything that was typed is kept, where the written tag can add defaults.
     */
    static boolean isKept(Tag typed, Tag written) {
        if (typed instanceof CompoundTag typedCompound) {
            if (!(written instanceof CompoundTag writtenCompound)) {
                return false;
            }
            for (String key : typedCompound.getAllKeys()) {
                if (!writtenCompound.contains(key) || !isKept(typedCompound.get(key), writtenCompound.get(key))) {
                    return false;
                }
            }
            return true;
        }
        // Lists and arrays, which can be written as each other, as in [1, 2] for [I; 1, 2]
        if (typed instanceof CollectionTag<?> typedList) {
            if (!(written instanceof CollectionTag<?> writtenList) || typedList.size() != writtenList.size()) {
                return false;
            }
            for (int i = 0; i < typedList.size(); i++) {
                if (!isKept(typedList.get(i), writtenList.get(i))) {
                    return false;
                }
            }
            return true;
        }
        // Numbers of another type with the same value, as in 1000 for 1000L
        if (typed instanceof NumericTag typedNumber && written instanceof NumericTag writtenNumber) {
            return isSameNumber(typedNumber.getAsNumber(), writtenNumber.getAsNumber());
        }
        // Ids can be written without the minecraft namespace, as in commands
        if (typed instanceof StringTag && written instanceof StringTag) {
            ResourceLocation typedId = ResourceLocation.tryParse(typed.getAsString());
            if (typedId != null && typedId.equals(ResourceLocation.tryParse(written.getAsString()))) {
                return true;
            }
        }
        return typed.equals(written);
    }

    private static boolean isSameNumber(Number a, Number b) {
        if (isIntegral(a) && isIntegral(b)) {
            return a.longValue() == b.longValue();
        }
        return Double.compare(a.doubleValue(), b.doubleValue()) == 0;
    }

    private static boolean isIntegral(Number number) {
        return number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long;
    }

    private ExpressionNode parseName(Token token) throws ExpressionParseException {
        String name = token.text();
        int parameter = getParameterIndex(name);
        if (parameter >= 0) {
            return new ExpressionNode.Param(parameter);
        }
        if (name.equals("true") || name.equals("false")) {
            return new ExpressionNode.Const(ValueTypeBoolean.ValueBoolean.of(name.equals("true")));
        }
        if (isNumber(token)) {
            return new ExpressionNode.Const(parseNumber(token, false));
        }
        throw error(token, L10NValues.EXPRESSION_PARSE_UNKNOWN_NAME, name);
    }

    private ExpressionNode parseCall(Token name) throws ExpressionParseException {
        List<ExpressionNode> arguments = parseArguments();
        int parameter = getParameterIndex(name.text());
        if (parameter < 0) {
            return resolveCall(name, arguments);
        }
        // A parameter that holds an operator is called with apply, where f(a) is apply(f, a)
        if (arguments.size() >= APPLY_OPERATORS.size()) {
            throw error(name, L10NValues.EXPRESSION_PARSE_PARAMETER_CALL, name.text(), APPLY_OPERATORS.size() - 1);
        }
        List<ExpressionNode> children = new ArrayList<>();
        children.add(new ExpressionNode.Param(parameter));
        children.addAll(arguments);
        return operator(APPLY_OPERATORS.get(arguments.size()), children, name);
    }

    private List<ExpressionNode> parseArguments() throws ExpressionParseException {
        Token open = next();
        enter(open);
        List<ExpressionNode> arguments = new ArrayList<>();
        if (!accept(")")) {
            do {
                arguments.add(parseTernary());
            } while (accept(","));
            expect(")");
        }
        nesting--;
        return arguments;
    }

    private ExpressionNode resolveCall(Token name, List<ExpressionNode> arguments) throws ExpressionParseException {
        IValueType<?> firstArgumentType = arguments.isEmpty() ? null : types.getType(arguments.get(0));
        IOperator operator = OperatorNames.resolve(name.text(), firstArgumentType);
        if (operator == null) {
            throw unknownOperator(name);
        }
        return operator(operator, arguments, name);
    }

    private ExpressionNode parseRef(Token token) throws ExpressionParseException {
        if (token.value() instanceof Integer variableId) {
            return new ExpressionNode.Ref(variableId);
        }
        String label = (String) token.value();
        IntList variableIds = context.labelIds().apply(label);
        if (variableIds.isEmpty()) {
            throw error(token, L10NValues.EXPRESSION_PARSE_UNKNOWN_LABEL, label);
        }
        if (variableIds.size() > 1) {
            throw error(token, L10NValues.EXPRESSION_PARSE_AMBIGUOUS_LABEL, label,
                    variableIds.intStream().mapToObj(id -> "#" + id).collect(Collectors.joining(", ")));
        }
        return new ExpressionNode.Ref(variableIds.getInt(0));
    }

    private IValue parseNumber(Token token, boolean negative) throws ExpressionParseException {
        String text = (negative ? "-" : "") + token.text();
        try {
            switch (token.text()) {
                case "NaN" -> {
                    return ValueTypeDouble.ValueDouble.of(Double.NaN);
                }
                case "Infinity" -> {
                    return ValueTypeDouble.ValueDouble.of(negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                default -> {
                }
            }
            char suffix = Character.toUpperCase(text.charAt(text.length() - 1));
            if (suffix == 'L') {
                return ValueTypeLong.ValueLong.of(Long.parseLong(text.substring(0, text.length() - 1)));
            }
            if (suffix == 'D') {
                return ValueTypeDouble.ValueDouble.of(Double.parseDouble(text.substring(0, text.length() - 1)));
            }
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return ValueTypeDouble.ValueDouble.of(Double.parseDouble(text));
            }
            long value = Long.parseLong(text);
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw error(token, L10NValues.EXPRESSION_PARSE_INTEGER_TOO_LARGE, text, text);
            }
            return ValueTypeInteger.ValueInteger.of((int) value);
        } catch (NumberFormatException e) {
            throw error(token, L10NValues.EXPRESSION_PARSE_BAD_NUMBER, text);
        }
    }

    // --------------- Resolution ---------------

    private ExpressionNode operator(IOperator operator, List<ExpressionNode> children, Token token) throws ExpressionParseException {
        if (children.size() != operator.getRequiredInputLength()) {
            throw error(token, L10NValues.EXPRESSION_PARSE_INPUT_COUNT, token.text(), operator.getRequiredInputLength(), children.size());
        }
        if (!context.operators().test(operator)) {
            throw error(token, L10NValues.EXPRESSION_PARSE_NOT_EDITABLE, token.text());
        }
        return new ExpressionNode.Operator(operator, List.copyOf(children));
    }

    private boolean isOperatorValue(Token token) {
        if (token.kind() == Kind.SYMBOL) {
            return OperatorNames.getInfixOperator(symbol(token)) != null || OperatorNames.getPrefixOperator(symbol(token)) != null;
        }
        return token.kind() == Kind.NAME && getParameterIndex(token.text()) < 0 && !RESERVED_NAMES.contains(token.text());
    }

    private IOperator resolveOperatorValue(Token token) throws ExpressionParseException {
        if (token.kind() == Kind.SYMBOL) {
            IOperator infix = OperatorNames.getInfixOperator(symbol(token));
            return infix != null ? infix : OperatorNames.getPrefixOperator(symbol(token));
        }
        IOperator operator = OperatorNames.resolve(token.text(), null);
        if (operator == null) {
            throw unknownOperator(token);
        }
        return operator;
    }

    private ExpressionParseException unknownOperator(Token name) {
        List<IOperator> candidates = OperatorNames.getScopedNameOperators(name.text());
        if (candidates.size() > 1) {
            return error(name, L10NValues.EXPRESSION_PARSE_AMBIGUOUS_OPERATOR, name.text(),
                    candidates.stream().map(IOperator::getGlobalInteractName).sorted().collect(Collectors.joining(", ")));
        }
        return error(name, L10NValues.EXPRESSION_PARSE_UNKNOWN_OPERATOR, name.text());
    }

    private int getParameterIndex(String name) {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // --------------- Tokens ---------------

    private Token peek(int offset) {
        return tokens.get(Math.min(index + offset, tokens.size() - 1));
    }

    private Token next() {
        Token token = peek(0);
        if (index < tokens.size() - 1) {
            index++;
        }
        return token;
    }

    private boolean accept(String symbol) {
        if (isSymbol(peek(0), symbol)) {
            index++;
            return true;
        }
        return false;
    }

    private void expect(String symbol) throws ExpressionParseException {
        Token token = peek(0);
        if (token.kind() == Kind.END) {
            throw new ExpressionParseException(token.start(), Component.translatable(L10NValues.EXPRESSION_PARSE_UNEXPECTED_END, symbol));
        }
        if (!accept(symbol)) {
            throw error(token, L10NValues.EXPRESSION_PARSE_EXPECTED, symbol, token.text());
        }
    }

    private void enter(Token token) throws ExpressionParseException {
        if (++nesting > MAX_NESTING) {
            throw error(token, L10NValues.EXPRESSION_PARSE_TOO_DEEP);
        }
    }

    private static boolean isSymbol(Token token, String symbol) {
        return token.kind() == Kind.SYMBOL && symbol(token).equals(symbol);
    }

    // The symbol of a symbol token, where other ways to write it are replaced, such as -> by =>
    private static String symbol(Token token) {
        return SYMBOL_ALIASES.getOrDefault(token.text(), token.text());
    }

    private static boolean isNumber(Token token) {
        return token.kind() == Kind.NUMBER || (token.kind() == Kind.NAME && (token.text().equals("NaN") || token.text().equals("Infinity")));
    }

    private static ExpressionParseException error(Token token, String key, Object... args) {
        return new ExpressionParseException(token.start(), Component.translatable(key, args));
    }

    /**
     * What the parser needs to know about the world.
     * @param values For deserializing values that are written with their type, such as items.
     * @param refTypes Resolves the type of a variable id, may return null when unknown.
     * @param labelIds Resolves the ids of the variables with a label.
     * @param parameterTypes The types that parameters can have.
     * @param operators The operators that can be used, not counting operators used as values.
     */
    public static record Context(ValueDeseralizationContext values, IntFunction<IValueType<?>> refTypes,
                                 Function<String, IntList> labelIds, Predicate<IValueType<?>> parameterTypes,
                                 Predicate<IOperator> operators) {
        public static Context of(ValueDeseralizationContext values) {
            return new Context(values, id -> null, label -> IntLists.EMPTY_LIST, type -> true, operator -> true);
        }
    }

    private static enum Kind {
        NAME,
        NUMBER,
        STRING,
        REF,
        TYPED,
        SYMBOL,
        END
    }

    /**
     * @param kind The kind of token.
     * @param text The text of the token, without quotes for strings.
     * @param start The offset of the first character.
     * @param value The parsed value of strings, references and typed values.
     */
    private static record Token(Kind kind, String text, int start, @Nullable Object value) {}

    private static class Lexer {
        private final String text;
        private final Context context;
        private final List<Token> tokens = new ArrayList<>();
        private int position = 0;

        private Lexer(String text, Context context) {
            this.text = text;
            this.context = context;
        }

        private List<Token> tokenize() throws ExpressionParseException {
            while (position < text.length()) {
                char c = text.charAt(position);
                int start = position;
                if (isWhitespace(c)) {
                    position++;
                } else if (isNameStart(c)) {
                    tokens.add(new Token(Kind.NAME, readName(), start, null));
                } else if (Character.isDigit(c)) {
                    tokens.add(new Token(Kind.NUMBER, readNumber(), start, null));
                } else if (isQuote(c) || isSingleQuote(c)) {
                    String string = readString();
                    tokens.add(new Token(Kind.STRING, string, start, string));
                } else if (c == '#') {
                    tokens.add(readRef());
                } else if (c == '@') {
                    tokens.add(readTyped());
                } else {
                    String symbol = SYMBOLS.stream().filter(s -> text.startsWith(s, start)).findFirst().orElse(null);
                    if (symbol == null) {
                        throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_UNKNOWN_CHARACTER, String.valueOf(c)));
                    }
                    position += symbol.length();
                    tokens.add(new Token(Kind.SYMBOL, symbol, start, null));
                }
            }
            tokens.add(new Token(Kind.END, "", text.length(), null));
            return tokens;
        }

        private String readName() {
            int start = position;
            while (position < text.length() && isNamePart(text.charAt(position))) {
                position++;
            }
            return text.substring(start, position);
        }

        private String readNumber() {
            int start = position;
            skipDigits();
            if (position + 1 < text.length() && text.charAt(position) == '.' && Character.isDigit(text.charAt(position + 1))) {
                position++;
                skipDigits();
            }
            if (position < text.length() && (text.charAt(position) == 'e' || text.charAt(position) == 'E')) {
                int exponent = position + 1;
                if (exponent < text.length() && (text.charAt(exponent) == '+' || text.charAt(exponent) == '-')) {
                    exponent++;
                }
                if (exponent < text.length() && Character.isDigit(text.charAt(exponent))) {
                    position = exponent;
                    skipDigits();
                }
            }
            if (position < text.length() && "LlDd".indexOf(text.charAt(position)) >= 0) {
                position++;
            }
            return text.substring(start, position);
        }

        private void skipDigits() {
            while (position < text.length() && Character.isDigit(text.charAt(position))) {
                position++;
            }
        }

        private String readString() throws ExpressionParseException {
            int start = position;
            boolean single = isSingleQuote(text.charAt(position++));
            StringBuilder sb = new StringBuilder();
            while (position < text.length()) {
                char c = text.charAt(position++);
                if (single ? isSingleQuote(c) : isQuote(c)) {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (position >= text.length()) {
                    break;
                }
                char escaped = text.charAt(position++);
                switch (escaped) {
                    case '\\', '"', '\'' -> sb.append(escaped);
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'u' -> {
                        if (position + 4 > text.length()) {
                            throw new ExpressionParseException(position - 2, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_ESCAPE, "u"));
                        }
                        try {
                            sb.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        } catch (NumberFormatException e) {
                            throw new ExpressionParseException(position - 2, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_ESCAPE, "u"));
                        }
                        position += 4;
                    }
                    default -> throw new ExpressionParseException(position - 2,
                            Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_ESCAPE, String.valueOf(escaped)));
                }
            }
            throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_UNTERMINATED_STRING));
        }

        private Token readRef() throws ExpressionParseException {
            int start = position;
            position++;
            if (position < text.length() && Character.isDigit(text.charAt(position))) {
                int digitsStart = position;
                skipDigits();
                String digits = text.substring(digitsStart, position);
                try {
                    return new Token(Kind.REF, text.substring(start, position), start, Integer.parseInt(digits));
                } catch (NumberFormatException e) {
                    throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_NUMBER, digits));
                }
            }
            if (position < text.length() && isNameStart(text.charAt(position))) {
                String label = readName();
                return new Token(Kind.REF, "#" + label, start, label);
            }
            if (position < text.length() && (isQuote(text.charAt(position)) || isSingleQuote(text.charAt(position)))) {
                String label = readString();
                return new Token(Kind.REF, text.substring(start, position), start, label);
            }
            throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_REF));
        }

        private Token readTyped() throws ExpressionParseException {
            int start = position;
            position++;
            int nameStart = position;
            while (position < text.length() && (isNamePart(text.charAt(position)) || ".:/-".indexOf(text.charAt(position)) >= 0)) {
                position++;
            }
            String typeName = text.substring(nameStart, position);
            IValueType<?> valueType = getValueType(typeName);
            if (valueType == null) {
                throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, typeName));
            }
            StringReader reader = new StringReader(text);
            reader.setCursor(position);
            reader.skipWhitespace();
            int valueStart = reader.getCursor();
            // No NBT at all, as an empty NBT value field gives
            if (valueType == ValueTypes.NBT && text.startsWith("()", valueStart)) {
                position = valueStart + 2;
                return new Token(Kind.TYPED, text.substring(start, position), start, ValueTypeNbt.ValueNbt.of());
            }
            Tag tag;
            try {
                tag = new TagParser(reader).readValue();
            } catch (CommandSyntaxException e) {
                throw new ExpressionParseException(e.getCursor() >= 0 ? e.getCursor() : valueStart,
                        badValue(typeName, e.getRawMessage().getString()));
            }
            position = reader.getCursor();
            // NBT is written as the tag itself, as in commands
            if (valueType == ValueTypes.NBT) {
                return new Token(Kind.TYPED, text.substring(start, position), start, ValueTypeNbt.ValueNbt.of(tag));
            }
            // An item without a count is a single one
            if (valueType == ValueTypes.OBJECT_ITEMSTACK && tag instanceof CompoundTag compound && !compound.isEmpty() && !compound.contains("count")) {
                compound.putInt("count", 1);
            }
            IValue value;
            Tag written;
            try {
                value = ValueHelpers.deserializeRaw(context.values(), valueType, tag);
                written = value == null ? null : ValueHelpers.serializeRaw(context.values(), value);
            } catch (RuntimeException e) {
                throw new ExpressionParseException(valueStart, badValue(typeName, String.valueOf(e.getMessage())));
            }
            if (value == null) {
                throw new ExpressionParseException(valueStart, badValue(typeName, tag.toString()));
            }
            // Values fall back to a default for what they cannot read, such as an unknown item, which must not go unnoticed
            if (!isKept(tag, written)) {
                throw new ExpressionParseException(valueStart, Component.translatable(L10NValues.EXPRESSION_PARSE_VALUE_CHANGED, typeName, written.toString()));
            }
            return new Token(Kind.TYPED, text.substring(start, position), start, value);
        }

        private static MutableComponent badValue(String typeName, String reason) {
            return Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_VALUE, typeName, reason);
        }
    }

}
