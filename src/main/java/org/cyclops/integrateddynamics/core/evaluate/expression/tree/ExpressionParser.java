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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
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
    // Names of the types of Integrated Dynamics by id, as the logic programmer shows them, see ValueObjectTypeItemStack
    private static final Map<String, String> TYPE_NAMES = Map.ofEntries(
            Map.entry("any", "Any"), Map.entry("number", "Number"), Map.entry("named", "Named"), Map.entry("uniquely_named", "UniquelyNamed"),
            Map.entry("boolean", "Boolean"), Map.entry("integer", "Integer"), Map.entry("long", "Long"), Map.entry("double", "Double"),
            Map.entry("string", "String"), Map.entry("list", "List"), Map.entry("operator", "Operator"), Map.entry("nbt", "NBT"),
            Map.entry("itemstack", "Item"), Map.entry("block", "Block"), Map.entry("fluidstack", "Fluid"), Map.entry("entity", "Entity"),
            Map.entry("ingredients", "Ingredients"), Map.entry("recipe", "Recipe"));
    // In lower case, for names that differ from the id
    private static final Map<String, String> TYPE_IDS = Map.of("item", "itemstack", "fluid", "fluidstack", "uniquelynamed", "uniquely_named");

    private final Context context;
    private final List<Token> tokens;
    private int index = 0;
    private int nesting = 0;
    private List<ExpressionParameter> parameters = List.of();
    private ExpressionTypeChecker.Inference types;
    // While completing, the calls whose brackets are open, innermost first, and the methods at the end of the text
    @Nullable
    private final Deque<OpenCall> openCalls;
    @Nullable
    private Methods methods = null;
    // While tokenizing, where each node was read, children before their parents
    @Nullable
    private List<Span> spans = null;

    private ExpressionParser(Context context, List<Token> tokens, boolean completing) {
        this.context = context;
        this.tokens = tokens;
        this.openCalls = completing ? new ArrayDeque<>() : null;
    }

    /**
     * @param text The text of an expression.
     * @param context What the parser needs to know about the world.
     * @return The expression.
     * @throws ExpressionParseException If the text is not a valid expression.
     */
    public static Expression parse(String text, Context context) throws ExpressionParseException {
        return parse(text, context, null);
    }

    private static Expression parse(String text, Context context, @Nullable List<Span> spans) throws ExpressionParseException {
        if (text.length() > MAX_LENGTH) {
            throw new ExpressionParseException(MAX_LENGTH, Component.translatable(L10NValues.EXPRESSION_PARSE_TOO_LONG, MAX_LENGTH));
        }
        ExpressionParser parser = new ExpressionParser(context, new Lexer(text, context).tokenize(), false);
        parser.spans = spans;
        return parser.parseExpression();
    }

    /**
     * Parse the text of an expression, and split the text into tokens that tell what node each part belongs to,
     * as {@link ExpressionPrinter#tokenizeExact} does for printed text, so typed text can be shown as typed.
     * @param text The text of an expression.
     * @param context What the parser needs to know about the world.
     * @return The expression and the tokens of the text.
     * @throws ExpressionParseException If the text is not a valid expression.
     */
    public static Tokenized tokenize(String text, Context context) throws ExpressionParseException {
        List<Span> spans = new ArrayList<>();
        Expression expression = parse(text, context, spans);
        // Empty nodes are one instance, which are read in the same order as their paths are visited
        Map<ExpressionNode, int[]> paths = new IdentityHashMap<>();
        Deque<int[]> emptyPaths = new ArrayDeque<>();
        collectPaths(expression.root(), new int[0], paths, emptyPaths);
        int[][] spanPaths = new int[spans.size()][];
        for (int i = 0; i < spans.size(); i++) {
            ExpressionNode node = spans.get(i).node();
            // Null for nodes that are not in the tree, such as the elements of a list
            spanPaths[i] = node instanceof ExpressionNode.Empty ? emptyPaths.poll() : paths.get(node);
        }
        // Children are read before their parents, so each character ends up with its innermost node
        int[][] owners = new int[text.length()][];
        for (int i = spans.size() - 1; i >= 0; i--) {
            if (spanPaths[i] != null) {
                Arrays.fill(owners, spans.get(i).start(), spans.get(i).end(), spanPaths[i]);
            }
        }
        List<ExpressionPrinter.Token> tokens = new ArrayList<>();
        int start = 0;
        for (int i = 1; i <= text.length(); i++) {
            if (i == text.length() || owners[i] != owners[start]) {
                tokens.add(new ExpressionPrinter.Token(text.substring(start, i), owners[start]));
                start = i;
            }
        }
        return new Tokenized(expression, tokens);
    }

    private static void collectPaths(ExpressionNode node, int[] path, Map<ExpressionNode, int[]> paths, Deque<int[]> emptyPaths) {
        if (node instanceof ExpressionNode.Empty) {
            emptyPaths.add(path);
        } else {
            paths.put(node, path);
        }
        if (node instanceof ExpressionNode.Operator operator) {
            for (int i = 0; i < operator.children().size(); i++) {
                collectPaths(operator.children().get(i), ExpressionTrees.append(path, i), paths, emptyPaths);
            }
        }
    }

    /**
     * Find what can be completed at a cursor, as far as the text before it tells.
     * @param text The text of an expression.
     * @param cursor The position of the cursor.
     * @param context What the parser needs to know about the world.
     * @return What can be completed.
     */
    public static Completion complete(String text, int cursor, Context context) {
        if (cursor < 0 || cursor > Math.min(text.length(), MAX_LENGTH)) {
            return Completion.NONE;
        }
        String before = text.substring(0, cursor);
        Types valueType = getValueTypeAtEnd(before, context);
        if (valueType != null) {
            return new Completion(null, null, valueType);
        }
        List<Token> tokens;
        try {
            tokens = new Lexer(before, context).tokenize();
        } catch (ExpressionParseException e) {
            // Such as within a string
            return Completion.NONE;
        }
        Types parameterType = getParameterTypeAtEnd(tokens);
        if (parameterType != null) {
            return new Completion(null, null, parameterType);
        }
        ExpressionParser parser = new ExpressionParser(context, tokens, true);
        try {
            parser.parseExpression();
            return Completion.NONE;
        } catch (ExpressionParseException e) {
            // Calls are only open at the cursor if the text before it ends within them, and not at an error
            boolean atCursor = parser.methods != null || e.getPosition() == cursor;
            return new Completion(parser.methods, atCursor ? parser.getOpenCall() : null, null);
        }
    }

    /**
     * Find the parameters in front of a text, which does not need to parse as a whole.
     * @param text The text of an expression.
     * @param context What the parser needs to know about the world.
     * @return The parameters, or null if there are none.
     */
    @Nullable
    public static ParameterList getParameterList(String text, Context context) {
        // Only the parameters need to be read, which also works when the rest is not complete, such as an open string
        int arrow = text.indexOf("=>");
        if (arrow < 0 || (text.contains("->") && text.indexOf("->") < arrow)) {
            arrow = text.indexOf("->");
        }
        for (String part : arrow < 0 ? List.of(text) : List.of(text, text.substring(0, arrow + 2))) {
            List<Token> tokens;
            try {
                tokens = new Lexer(part, context).tokenize();
            } catch (ExpressionParseException e) {
                continue;
            }
            ExpressionParser parser = new ExpressionParser(context, tokens, false);
            if (!parser.isParameterHeader()) {
                return null;
            }
            Token first = tokens.get(0);
            if (first.kind() == Kind.NAME) {
                return new ParameterList(List.of(first.text()), first.end(), false);
            }
            // The names follow the opening bracket and each comma
            List<String> names = new ArrayList<>();
            int i = 0;
            while (!isSymbol(tokens.get(i), ")")) {
                names.add(tokens.get(i + 1).text());
                i += 2;
                while (!isSymbol(tokens.get(i), ",") && !isSymbol(tokens.get(i), ")")) {
                    i++;
                }
            }
            return new ParameterList(names, tokens.get(i).start(), true);
        }
        return null;
    }

    /**
     * @param names The names of the parameters.
     * @param end Where the parameters end: at the closing bracket, or after the name of a single one without brackets.
     * @param bracketed If they are in brackets.
     */
    public static record ParameterList(List<String> names, int end, boolean bracketed) {}

    /**
     * @param text The text before the cursor.
     * @param context What the parser needs to know about the world.
     * @return The type of a value that is being typed at the end, as in {@code @item}, or null.
     */
    @Nullable
    private static Types getValueTypeAtEnd(String text, Context context) {
        int start = text.length();
        while (start > 0 && isTypeNamePart(text.charAt(start - 1))) {
            start--;
        }
        if (start == 0 || text.charAt(start - 1) != '@') {
            return null;
        }
        try {
            new Lexer(text.substring(0, start - 1), context).tokenize();
        } catch (ExpressionParseException e) {
            // Such as within a string
            return null;
        }
        return new Types(start, text.substring(start), false);
    }

    /**
     * @param tokens The tokens of the text before the cursor.
     * @return The type of a parameter that is being typed at the end, as in {@code (x: int}, or null.
     */
    @Nullable
    private static Types getParameterTypeAtEnd(List<Token> tokens) {
        int end = tokens.size() - 1;
        if (!isSymbol(tokens.get(0), "(")) {
            return null;
        }
        int i = 1;
        while (true) {
            if (tokens.get(i).kind() != Kind.NAME) {
                return null;
            }
            i++;
            if (isSymbol(tokens.get(i), ",")) {
                i++;
                continue;
            }
            if (!isSymbol(tokens.get(i), ":")) {
                return null;
            }
            int start = ++i;
            while (i < end && !isSymbol(tokens.get(i), ",")) {
                i++;
            }
            if (i == end) {
                return getTypeAtEnd(tokens, start);
            }
            i++;
        }
    }

    /**
     * @param tokens Tokens.
     * @param start The index of the first token of a type that ends at the end of the tokens.
     * @return The type if it is being typed: nothing yet, a name, or a namespace with a colon and maybe a name,
     *         without spaces up to the end, or null.
     */
    @Nullable
    private static Types getTypeAtEnd(List<Token> tokens, int start) {
        int end = tokens.size() - 1;
        int count = end - start;
        if (count > 3 || (count > 0 && tokens.get(start).kind() != Kind.NAME)
                || (count > 1 && !isSymbol(tokens.get(start + 1), ":")) || (count > 2 && tokens.get(start + 2).kind() != Kind.NAME)) {
            return null;
        }
        StringBuilder prefix = new StringBuilder();
        for (int i = start; i < end; i++) {
            Token token = tokens.get(i);
            Token next = tokens.get(i + 1);
            if (token.end() != next.start()) {
                return null;
            }
            prefix.append(token.text());
        }
        return new Types(count > 0 ? tokens.get(start).start() : tokens.get(end).start(), prefix.toString(), true);
    }

    /**
     * @param types A type that is being typed.
     * @param context What the parser needs to know about the world.
     * @return The types whose name starts with what is typed, or whose name without the namespace of an add-on does,
     *         ordered by name, those of Integrated Dynamics first. For parameters, only the types they can have.
     */
    public static List<IValueType<?>> getTypes(Types types, Context context) {
        String prefix = types.prefix().toLowerCase(Locale.ROOT);
        List<IValueType<?>> matches = new ArrayList<>();
        for (IValueType<?> type : ValueTypes.REGISTRY.getValueTypes()) {
            if ((types.parameter() ? context.parameterTypes().test(type) : !type.isCategory())
                    && (getTypeName(type).toLowerCase(Locale.ROOT).startsWith(prefix) || type.getUniqueName().getPath().startsWith(prefix))) {
                matches.add(type);
            }
        }
        matches.sort(Comparator.comparing((IValueType<?> type) -> getTypeName(type).contains(":"))
                .thenComparing(ExpressionParser::getTypeName, String.CASE_INSENSITIVE_ORDER));
        return matches;
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
     * @return The name of the type in the text form: for those of Integrated Dynamics, the name the logic programmer
     *         shows, as in {@code Integer} or {@code Item}, otherwise its id.
     */
    public static String getTypeName(IValueType<?> valueType) {
        ResourceLocation id = valueType.getUniqueName();
        return id.getNamespace().equals(Reference.MOD_ID) ? TYPE_NAMES.getOrDefault(id.getPath(), id.getPath()) : id.toString();
    }

    /**
     * @param name The name of a type in the text form, in any case, as in {@code Integer} or {@code integer}.
     *             Types of add-ons are written by their id, whose namespace can be left out
     *             when no other type has that name, as in {@code chemicalstack}. Ids, such as {@code itemstack}, are read too.
     * @return The type, or null if unknown or ambiguous.
     */
    @Nullable
    public static IValueType<?> getValueType(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if (!name.contains(":")) {
            name = TYPE_IDS.getOrDefault(name, name);
        }
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

    /**
     * @param c A character.
     * @return If it can be part of the name of a type after {@code @}, which can contain a namespace.
     */
    public static boolean isTypeNamePart(char c) {
        return isNamePart(c) || ".:/-".indexOf(c) >= 0;
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
            // Also the namespace of an add-on's type
            int typeEnd = tokens.get(index - 1).end();
            if (type == null) {
                throw new ExpressionParseException(typeName.start(), typeEnd,
                        Component.translatable(L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, typeText));
            }
            if (!context.parameterTypes().test(type)) {
                throw new ExpressionParseException(typeName.start(), typeEnd,
                        Component.translatable(L10NValues.EXPRESSION_PARSE_PARAMETER_TYPE, typeText));
            }
        }
        return new ExpressionParameter(name.text(), type);
    }

    private ExpressionNode parseTernary() throws ExpressionParseException {
        Token first = peek(0);
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
        return span(operator(Operators.GENERAL_CHOICE, List.of(condition, then, otherwise), question), first);
    }

    private ExpressionNode parseBinary(int level) throws ExpressionParseException {
        if (level == BINARY_LEVELS.size()) {
            return parseUnary();
        }
        Token first = peek(0);
        ExpressionNode left = parseBinary(level + 1);
        while (peek(0).kind() == Kind.SYMBOL && BINARY_LEVELS.get(level).contains(symbol(peek(0)))) {
            Token symbol = next();
            ExpressionNode right = parseBinary(level + 1);
            IOperator operator = OperatorNames.getInfixOperator(symbol(symbol), types.getType(left), types.getType(right));
            left = span(operator(operator, List.of(left, right), symbol), first);
        }
        return left;
    }

    private ExpressionNode parseUnary() throws ExpressionParseException {
        Token token = peek(0);
        if (accept("!")) {
            enter(token);
            ExpressionNode child = parseUnary();
            nesting--;
            return span(operator(OperatorNames.getPrefixOperator("!"), List.of(child), token), token);
        }
        if (isSymbol(token, "-") && isNumber(peek(1))) {
            next();
            return parseMethodCalls(span(new ExpressionNode.Const(parseNumber(next(), true)), token), token);
        }
        return parseMethodCalls(parsePrimary(), token);
    }

    /**
     * Read the method calls after a value, where a.f(b) is f(a, b).
     * @param receiver The value.
     * @param first The first token of the value.
     */
    private ExpressionNode parseMethodCalls(ExpressionNode receiver, Token first) throws ExpressionParseException {
        int outerNesting = nesting;
        while (isSymbol(peek(0), ".")) {
            Token dot = next();
            Token name = peek(0);
            if (name.kind() != Kind.NAME || !isSymbol(peek(1), "(")) {
                if (openCalls != null && isTypingMethodName(dot, name)) {
                    methods = new Methods(types.getType(receiver), name.start(), name.text());
                }
                throw error(dot, L10NValues.EXPRESSION_PARSE_METHOD);
            }
            next();
            // Each call is a level deeper
            enter(dot);
            List<ExpressionNode> arguments = new ArrayList<>();
            arguments.add(receiver);
            arguments.addAll(parseArguments(name, receiver));
            receiver = span(resolveCall(name, arguments), first);
        }
        nesting = outerNesting;
        return receiver;
    }

    /**
     * @param dot The point after a value.
     * @param name The token after it.
     * @return If the name of a method is being typed at the end of the text, which is not the point of a decimal number.
     */
    private boolean isTypingMethodName(Token dot, Token name) {
        Token previous = tokens.get(index - 2);
        if (previous.kind() == Kind.NUMBER && previous.end() == dot.start()) {
            return false;
        }
        Token end = tokens.get(tokens.size() - 1);
        return name.kind() == Kind.END
                || (name.kind() == Kind.NAME && peek(1).kind() == Kind.END && name.end() == end.start());
    }

    private ExpressionNode parsePrimary() throws ExpressionParseException {
        Token token = next();
        switch (token.kind()) {
            case NUMBER -> {
                return span(new ExpressionNode.Const(parseNumber(token, false)), token);
            }
            case STRING -> {
                return span(new ExpressionNode.Const(ValueTypeString.ValueString.of((String) token.value())), token);
            }
            case TYPED -> {
                return span(new ExpressionNode.Const((IValue) token.value()), token);
            }
            case REF -> {
                return span(parseRef(token), token);
            }
            case NAME -> {
                if (isSymbol(peek(0), "(")) {
                    return span(parseCall(token), token);
                }
                return span(parseName(token), token);
            }
            case SYMBOL -> {
                if (isSymbol(token, "?")) {
                    return span(ExpressionNode.EMPTY, token);
                }
                if (isSymbol(token, "[")) {
                    return span(new ExpressionNode.Const(parseList(token)), token);
                }
                if (isSymbol(token, "(")) {
                    if (isOperatorValue(peek(0)) && isSymbol(peek(1), ")")) {
                        Token name = next();
                        next();
                        return span(new ExpressionNode.Const(ValueTypeOperator.ValueOperator.of(resolveOperatorValue(name))), token);
                    }
                    enter(token);
                    ExpressionNode node = parseTernary();
                    expect(")");
                    nesting--;
                    // The brackets belong to the value in them, which was read last
                    if (spans != null) {
                        spans.set(spans.size() - 1, new Span(node, token.start(), tokens.get(index - 1).end()));
                    }
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
        List<ExpressionNode> arguments = parseArguments(name, null);
        int parameter = getParameterIndex(name.text());
        if (parameter < 0) {
            return resolveCall(name, arguments);
        }
        // A parameter that holds an operator is called with apply, where f(a) is apply(f, a)
        if (arguments.size() >= APPLY_OPERATORS.size()) {
            throw error(name, L10NValues.EXPRESSION_PARSE_PARAMETER_CALL, name.text(), APPLY_OPERATORS.size() - 1);
        }
        List<ExpressionNode> children = new ArrayList<>();
        children.add(span(new ExpressionNode.Param(parameter), name.start(), name.end()));
        children.addAll(arguments);
        return operator(APPLY_OPERATORS.get(arguments.size()), children, name);
    }

    /**
     * Read the arguments of a call.
     * @param name The name of what is called.
     * @param receiver The value a method is called on, or null.
     */
    private List<ExpressionNode> parseArguments(Token name, @Nullable ExpressionNode receiver) throws ExpressionParseException {
        Token open = next();
        enter(open);
        List<ExpressionNode> arguments = new ArrayList<>();
        OpenCall call = openCalls != null ? new OpenCall(name, receiver, arguments) : null;
        if (call != null) {
            openCalls.push(call);
        }
        if (!accept(")")) {
            arguments.add(parseTernary());
            while (accept(",")) {
                if (call != null) {
                    call.argument++;
                }
                arguments.add(parseTernary());
            }
            expect(")");
        }
        if (call != null) {
            openCalls.pop();
        }
        nesting--;
        return arguments;
    }

    /**
     * @return The innermost call whose brackets are open, or null if there is none or it has no known operator,
     *         such as when calling a parameter.
     */
    @Nullable
    private Call getOpenCall() {
        OpenCall open = openCalls.peek();
        if (open == null || types == null || (open.receiver == null && getParameterIndex(open.name.text()) >= 0)) {
            return null;
        }
        List<IValueType<?>> argumentTypes = new ArrayList<>();
        if (open.receiver != null) {
            argumentTypes.add(types.getType(open.receiver));
        }
        open.arguments.forEach(argument -> argumentTypes.add(types.getType(argument)));
        IOperator operator = OperatorNames.resolvePartial(open.name.text(), argumentTypes);
        if (operator == null || !context.operators().test(operator)) {
            return null;
        }
        boolean method = open.receiver != null;
        return new Call(operator, open.name.text(), method, open.argument + (method ? 1 : 0));
    }

    private ExpressionNode resolveCall(Token name, List<ExpressionNode> arguments) throws ExpressionParseException {
        IOperator operator = OperatorNames.resolve(name.text(), arguments.stream().<IValueType<?>>map(types::getType).toList());
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

    /**
     * Record where a node was read while tokenizing, from the given token up to the last token that was read.
     */
    private ExpressionNode span(ExpressionNode node, Token first) {
        return span(node, first.start(), tokens.get(index - 1).end());
    }

    private ExpressionNode span(ExpressionNode node, int start, int end) {
        if (spans != null) {
            spans.add(new Span(node, start, end));
        }
        return node;
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
        return new ExpressionParseException(token.start(), token.end(), Component.translatable(key, args));
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

    /**
     * What can be completed at a cursor.
     * @param methods The methods that can be called on the value before a point right before the cursor, or null.
     * @param call The innermost call whose brackets are open at the cursor, or null.
     * @param types The type being typed at the cursor, or null.
     */
    public static record Completion(@Nullable Methods methods, @Nullable Call call, @Nullable Types types) {
        public static final Completion NONE = new Completion(null, null, null);
    }

    /**
     * @param start Where the name of the type starts.
     * @param prefix What is typed of the name, up to the cursor.
     * @param parameter If it is the type of a parameter, as in {@code (x: int}, or otherwise of a value, as in {@code @item}.
     */
    public static record Types(int start, String prefix, boolean parameter) {}

    /**
     * @param receiverType The type of the value that the methods are called on.
     * @param start Where the name of the method starts.
     * @param prefix What is typed of the name, up to the cursor.
     */
    public static record Methods(IValueType<?> receiverType, int start, String prefix) {}

    /**
     * @param operator The operator that is called.
     * @param name The name it is called with.
     * @param method If it is called as a method, where its first input is the value before the point.
     * @param input The index of the operator's input at the cursor, which can be beyond its inputs.
     */
    public static record Call(IOperator operator, String name, boolean method, int input) {}

    /**
     * @param expression The expression of a text.
     * @param tokens The text as tokens, which tell what node each part of the text belongs to.
     */
    public static record Tokenized(Expression expression, List<ExpressionPrinter.Token> tokens) {}

    /**
     * @param node A node.
     * @param start The offset of the first character it was read from.
     * @param end The offset after the last character it was read from.
     */
    private static record Span(ExpressionNode node, int start, int end) {}

    private static final class OpenCall {
        private final Token name;
        @Nullable
        private final ExpressionNode receiver;
        private final List<ExpressionNode> arguments;
        // The argument at the cursor, among those in the brackets
        private int argument = 0;

        private OpenCall(Token name, @Nullable ExpressionNode receiver, List<ExpressionNode> arguments) {
            this.name = name;
            this.receiver = receiver;
            this.arguments = arguments;
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
     * @param end The offset after the last character, which is after the quotes of strings.
     * @param value The parsed value of strings, references and typed values.
     */
    private static record Token(Kind kind, String text, int start, int end, @Nullable Object value) {}

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
                    tokens.add(new Token(Kind.NAME, readName(), start, position, null));
                } else if (Character.isDigit(c)) {
                    tokens.add(new Token(Kind.NUMBER, readNumber(), start, position, null));
                } else if (isQuote(c) || isSingleQuote(c)) {
                    String string = readString();
                    tokens.add(new Token(Kind.STRING, string, start, position, string));
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
                    tokens.add(new Token(Kind.SYMBOL, symbol, start, position, null));
                }
            }
            tokens.add(new Token(Kind.END, "", text.length(), text.length(), null));
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
                    return new Token(Kind.REF, text.substring(start, position), start, position, Integer.parseInt(digits));
                } catch (NumberFormatException e) {
                    throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_NUMBER, digits));
                }
            }
            if (position < text.length() && isNameStart(text.charAt(position))) {
                String label = readName();
                return new Token(Kind.REF, "#" + label, start, position, label);
            }
            if (position < text.length() && (isQuote(text.charAt(position)) || isSingleQuote(text.charAt(position)))) {
                String label = readString();
                return new Token(Kind.REF, text.substring(start, position), start, position, label);
            }
            throw new ExpressionParseException(start, Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_REF));
        }

        private Token readTyped() throws ExpressionParseException {
            int start = position;
            position++;
            int nameStart = position;
            while (position < text.length() && isTypeNamePart(text.charAt(position))) {
                position++;
            }
            String typeName = text.substring(nameStart, position);
            IValueType<?> valueType = getValueType(typeName);
            if (valueType == null) {
                throw new ExpressionParseException(start, position, Component.translatable(L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, typeName));
            }
            StringReader reader = new StringReader(text);
            reader.setCursor(position);
            reader.skipWhitespace();
            int valueStart = reader.getCursor();
            // No NBT at all, as an empty NBT value field gives
            if (valueType == ValueTypes.NBT && text.startsWith("()", valueStart)) {
                position = valueStart + 2;
                return new Token(Kind.TYPED, text.substring(start, position), start, position, ValueTypeNbt.ValueNbt.of());
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
                return new Token(Kind.TYPED, text.substring(start, position), start, position, ValueTypeNbt.ValueNbt.of(tag));
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
            return new Token(Kind.TYPED, text.substring(start, position), start, position, value);
        }

        private static MutableComponent badValue(String typeName, String reason) {
            return Component.translatable(L10NValues.EXPRESSION_PARSE_BAD_VALUE, typeName, reason);
        }
    }

}
