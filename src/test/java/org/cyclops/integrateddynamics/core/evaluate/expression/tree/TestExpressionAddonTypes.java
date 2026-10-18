package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeAddonMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for value types of add-ons in expressions, which have their own namespace, such as the chemicals of Integrated Mekanism.
 * @author rubensworks
 */
public class TestExpressionAddonTypes {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final ValueTypeAddonMocked CHEMICAL = ValueTypeAddonMocked.CHEMICAL;

    @BeforeClass
    public static void beforeClass() {
        ValueTypeAddonMocked.load();
    }

    // As in the logic programmer
    private static Expression parse(String text) throws ExpressionParseException {
        return ExpressionEditor.parse(text, ExpressionEditor.getParserContext(ValueDeseralizationContextMocked.get(), id -> null,
                label -> IntLists.EMPTY_LIST));
    }

    private static String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, ValueDeseralizationContextMocked.get(), id -> null, label -> IntLists.EMPTY_LIST, id -> null);
    }

    private static void assertError(String text, String key, int position) {
        try {
            parse(text);
            fail("Expected an error for " + text);
        } catch (ExpressionParseException e) {
            assertThat(text, ((TranslatableContents) e.getComponent().getContents()).getKey(), is(key));
            assertThat(text, e.getPosition(), is(position));
        }
    }

    private static IValue read(String text) throws ExpressionParseException {
        return ((ExpressionNode.Const) ((ExpressionNode.Operator) parse(text + " == ?").root()).children().get(0)).value();
    }

    @Test
    public void testParameterTypes() {
        assertTrue(ExpressionEditor.isParameterType(CHEMICAL));
        assertTrue(ExpressionEditor.isParameterType(ValueTypes.CATEGORY_ANY));
        assertTrue(ExpressionEditor.isParameterType(ValueTypes.CATEGORY_NUMBER));
        assertTrue(!ExpressionEditor.isParameterType(ValueTypes.CATEGORY_NAMED));
        assertTrue(!ExpressionEditor.isParameterType(new ValueTypeAddonMocked("testaddon", "unregistered")));

        // Those of Integrated Dynamics first, then those of add-ons by name
        List<IValueType<?>> types = ExpressionEditor.getParameterTypes();
        assertThat(types.get(0), is(ValueTypes.CATEGORY_ANY));
        int chemical = types.indexOf(CHEMICAL);
        assertTrue(chemical > types.indexOf(ValueTypes.OBJECT_RECIPE));
        assertThat(types.indexOf(ValueTypeAddonMocked.WIDGET), is(chemical + 1));
        assertThat(types.indexOf(ValueTypeAddonMocked.OTHER_WIDGET), is(chemical + 2));
    }

    @Test
    public void testParameters() throws ExpressionParseException {
        Expression expression = parse("(c: testaddon:chemical) => c == c");
        assertThat(expression.parameters(), is(List.of(new ExpressionParameter("c", CHEMICAL))));
        // Printed with the namespace, and read back
        assertThat(printExact(expression), is("(c: testaddon:chemical) => c == c"));
        assertThat(parse(printExact(expression)), is(expression));
        // Without the namespace when no other type has that name
        assertThat(parse("(c: chemical) => c == c"), is(expression));
        assertThat(parse("(w: testother:widget, x) => w == x").parameters(), is(List.of(
                new ExpressionParameter("w", ValueTypeAddonMocked.OTHER_WIDGET), new ExpressionParameter("x", ValueTypes.CATEGORY_ANY))));
        assertError("(w: widget) => w == w", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 4);
        assertError("(w: nothing:widget) => w == w", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 4);
        // The types of Integrated Dynamics go first
        assertThat(parse("(s: string) => s == s").parameters(), is(List.of(new ExpressionParameter("s", ValueTypes.STRING))));
    }

    @Test
    public void testParameterTypeInEditor() {
        ExpressionEditor editor = new ExpressionEditor(Operators.RELATIONAL_EQUALS);
        editor.addParameter();
        editor.setParameterType(0, CHEMICAL);
        assertThat(editor.getExpression().parameters().get(0).type(), is(CHEMICAL));
        assertTrue(ExpressionEditor.isEditable(editor.getExpression()));
        // Not to types that are not registered
        editor.setParameterType(0, new ValueTypeAddonMocked("testaddon", "unregistered"));
        assertThat(editor.getExpression().parameters().get(0).type(), is(CHEMICAL));
    }

    @Test
    public void testValues() throws ExpressionParseException {
        IValue steam = CHEMICAL.of("testaddon:steam", 1000);
        Expression expression = new Expression(new ExpressionNode.Operator(Operators.RELATIONAL_EQUALS,
                List.of(new ExpressionNode.Const(steam), ExpressionNode.EMPTY)));
        String text = printExact(expression);
        assertThat(text, is("@testaddon:chemical{amount:1000L,id:\"testaddon:steam\"} == ?"));
        assertThat(parse(text), is(expression));
        // With the short name and an amount that is not written as a long
        assertThat(read("@chemical{id: \"testaddon:steam\", amount: 1000}"), is(steam));
        // An unknown id that would be read as an empty value
        assertError("@testaddon:chemical{id: \"other:steam\", amount: 5L} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED, 19);
        assertError("@widget{} == ?", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 0);
    }

}
