package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.junit.Test;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the logic programmer expression editor.
 * @author rubensworks
 */
public class TestExpressionEditor {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static ExpressionEditor.ActionResult apply(ExpressionEditor editor, CompoundTag action) {
        return editor.apply(ValueDeseralizationContextMocked.get(), action);
    }

    @Test
    public void testBuildExpression() {
        // item -> size(nbt(item)) > 3, built with editor actions
        ExpressionEditor editor = new ExpressionEditor(Operators.RELATIONAL_GT);
        assertThat(apply(editor, ExpressionEditor.actionAddParam()), is(ExpressionEditor.ActionResult.CHANGED));
        apply(editor, ExpressionEditor.actionRenameParam(0, "item"));
        assertThat(apply(editor, ExpressionEditor.actionConst(ValueDeseralizationContextMocked.get(), new int[0], 1,
                ValueTypeInteger.ValueInteger.of(3))), is(ExpressionEditor.ActionResult.SLOT_CHANGED));
        apply(editor, ExpressionEditor.actionOperator(new int[0], 0, Operators.NBT_COMPOUND_SIZE));
        assertThat(apply(editor, ExpressionEditor.actionNavigate(new int[]{0})), is(ExpressionEditor.ActionResult.NAVIGATED));
        apply(editor, ExpressionEditor.actionOperator(new int[]{0}, 0, Operators.OBJECT_ITEMSTACK_DATA));
        apply(editor, ExpressionEditor.actionNavigate(new int[]{0, 0}));
        assertThat(editor.getCurrentOperator(), is(Operators.OBJECT_ITEMSTACK_DATA));
        apply(editor, ExpressionEditor.actionParam(new int[]{0, 0}, 0, 0));

        assertThat(ExpressionPrinter.print(editor.getExpression()), is("item -> size(nbt(item)) > 3"));
        // The parameter type was inferred from the slot it was dropped in
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.OBJECT_ITEMSTACK));
        assertThat(editor.check().isValid(), is(true));
        assertThat(editor.check().outputType(), is((IValueType) ValueTypes.BOOLEAN));
        assertThat(editor.getOperatorsAlongPath(editor.getEditPath()).size(), is(3));

        // Back to the root
        assertThat(apply(editor, ExpressionEditor.actionNavigate(new int[0])), is(ExpressionEditor.ActionResult.NAVIGATED));
        assertThat(editor.getCurrentOperator(), is(Operators.RELATIONAL_GT));
    }

    @Test
    public void testSlotActionsOnlyForCurrentNode() {
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionOperator(new int[0], 0, Operators.ARITHMETIC_INCREMENT));
        // Not navigated into [0] yet
        assertThat(apply(editor, ExpressionEditor.actionClear(new int[]{0}, 0)), is(ExpressionEditor.ActionResult.NONE));
        // Out of range slot
        assertThat(apply(editor, ExpressionEditor.actionClear(new int[0], 5)), is(ExpressionEditor.ActionResult.NONE));
        // Invalid navigation
        assertThat(apply(editor, ExpressionEditor.actionNavigate(new int[]{1})), is(ExpressionEditor.ActionResult.NONE));
        // Clearing removes the nested operator
        assertThat(apply(editor, ExpressionEditor.actionClear(new int[0], 0)), is(ExpressionEditor.ActionResult.SLOT_CHANGED));
        assertThat(editor.getChild(0), is((ExpressionNode) ExpressionNode.EMPTY));
    }

    @Test
    public void testOperatorWithoutRenderPatternRejected() {
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        CompoundTag action = ExpressionEditor.actionOperator(new int[0], 0, Operators.ARITHMETIC_INCREMENT);
        action.putString("operator", "integrateddynamics:does_not_exist");
        assertThat(apply(editor, action), is(ExpressionEditor.ActionResult.NONE));
    }

    @Test
    public void testRemoveParameter() {
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionParam(new int[0], 0, 0));
        apply(editor, ExpressionEditor.actionParam(new int[0], 1, 1));
        assertThat(ExpressionPrinter.print(editor.getExpression()), is("(x, y) -> x + y"));
        apply(editor, ExpressionEditor.actionRemoveParam(0));
        assertThat(ExpressionPrinter.print(editor.getExpression()), is("y -> ? + y"));
    }

    @Test
    public void testParameterTypes() {
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionAddParam());
        assertThat(apply(editor, ExpressionEditor.actionParamType(0, ValueTypes.INTEGER)), is(ExpressionEditor.ActionResult.CHANGED));
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.INTEGER));
        // Types outside the list are ignored
        apply(editor, ExpressionEditor.actionParamType(0, ValueTypes.CATEGORY_NAMED));
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.INTEGER));
        // Cycling wraps around
        assertThat(ExpressionEditor.cycleParameterType(ValueTypes.CATEGORY_ANY, -1), is((IValueType) ExpressionEditor.PARAMETER_TYPES.get(ExpressionEditor.PARAMETER_TYPES.size() - 1)));
        assertThat(ExpressionEditor.cycleParameterType(ValueTypes.CATEGORY_ANY, 1), is((IValueType) ValueTypes.BOOLEAN));
    }

    @Test
    public void testParameterTypeFromCategory() {
        // Number slots give a Number parameter
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionParam(new int[0], 0, 0));
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.CATEGORY_NUMBER));
        assertThat(editor.check().errors().isEmpty(), is(true));

        // Any slots leave the parameter untyped
        editor = new ExpressionEditor(Operators.RELATIONAL_EQUALS);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionParam(new int[0], 0, 0));
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.CATEGORY_ANY));
    }

    @Test
    public void testPickOperatorAsValue() {
        // In operator slots, operators that don't give an operator are values, others are nested unless forced
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.OPERATOR, Operators.STRING_LENGTH, false), is(true));
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.OPERATOR, Operators.OPERATOR_APPLY, false), is(false));
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.OPERATOR, Operators.OPERATOR_FLIP, false), is(false));
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.OPERATOR, Operators.OPERATOR_FLIP, true), is(true));
        // Other slots only take operator values when forced and when an operator fits
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.CATEGORY_ANY, Operators.STRING_LENGTH, false), is(false));
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.CATEGORY_ANY, Operators.STRING_LENGTH, true), is(true));
        assertThat(ExpressionEditor.isPickedAsValue(ValueTypes.INTEGER, Operators.STRING_LENGTH, true), is(false));

        // l -> map((stringLength), l)
        ExpressionEditor editor = new ExpressionEditor(Operators.OPERATOR_MAP);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionRenameParam(0, "l"));
        assertThat(apply(editor, ExpressionEditor.actionConst(ValueDeseralizationContextMocked.get(), new int[0], 0,
                ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH))), is(ExpressionEditor.ActionResult.SLOT_CHANGED));
        apply(editor, ExpressionEditor.actionParam(new int[0], 1, 0));
        assertThat(ExpressionPrinter.print(editor.getExpression()), is("l -> map((stringLength), l)"));
        assertThat(editor.check().isValid(), is(true));
        assertThat(editor.getExpression().parameters().get(0).type(), is((IValueType) ValueTypes.LIST));
    }

    @Test
    public void testSetAction() {
        Expression expression = new Expression(new ExpressionNode.Operator(Operators.STRING_LENGTH, List.of(new ExpressionNode.Param(0))),
                List.of(new ExpressionParameter("s", ValueTypes.STRING)));
        CompoundTag action = ExpressionEditor.actionSet(ValueDeseralizationContextMocked.get(), expression);
        assertThat(ExpressionEditor.readSetAction(ValueDeseralizationContextMocked.get(), action), is(expression));
        // Not a set action
        assertThat(ExpressionEditor.readSetAction(ValueDeseralizationContextMocked.get(), ExpressionEditor.actionAddParam()), nullValue());
    }

    @Test
    public void testSetActionRejectsWhatCannotBeEdited() {
        // The root must be an operator
        assertThat(readBack(new Expression(new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(1)))), nullValue());
        // Parameters need a name that can be written as text, and a type that can be chosen
        assertThat(readBack(new Expression(ExpressionNode.Operator.ofEmpty(Operators.STRING_LENGTH),
                List.of(new ExpressionParameter("true", ValueTypes.STRING)))), nullValue());
        assertThat(readBack(new Expression(ExpressionNode.Operator.ofEmpty(Operators.STRING_LENGTH),
                List.of(new ExpressionParameter("x", ValueTypes.CATEGORY_NAMED)))), nullValue());
        assertThat(readBack(new Expression(ExpressionNode.Operator.ofEmpty(Operators.STRING_LENGTH),
                List.of(new ExpressionParameter("x", ValueTypes.STRING), new ExpressionParameter("x", ValueTypes.STRING)))), nullValue());
        // Every operator needs a slot for each input
        Operators.REGISTRY.getOperators().stream().filter(operator -> !ExpressionEditor.isEditable(operator)).findFirst()
                .ifPresent(operator -> assertThat(readBack(new Expression(ExpressionNode.Operator.ofEmpty(operator))), nullValue()));
        // Not too many nodes
        ExpressionNode node = ExpressionNode.EMPTY;
        for (int i = 0; i < 7; i++) {
            node = new ExpressionNode.Operator(Operators.GENERAL_CHOICE, List.of(node, node, node));
        }
        assertThat(readBack(new Expression(node)), nullValue());
    }

    private static Expression readBack(Expression expression) {
        return ExpressionEditor.readSetAction(ValueDeseralizationContextMocked.get(),
                ExpressionEditor.actionSet(ValueDeseralizationContextMocked.get(), expression));
    }

    @Test
    public void testParameterNames() {
        assertThat(ExpressionEditor.sanitizeParameterName("my item!"), is("myitem"));
        assertThat(ExpressionEditor.sanitizeParameterName("abcdefghijklmnopqrstuvwxyz").length(), is(16));
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionRenameParam(0, "!!!"));
        assertThat(editor.getExpression().parameters().get(0).name(), is("x"));
        // Names that would not read back as a parameter from the text form
        apply(editor, ExpressionEditor.actionRenameParam(0, "true"));
        apply(editor, ExpressionEditor.actionRenameParam(0, "1x"));
        assertThat(editor.getExpression().parameters().get(0).name(), is("x"));
    }

    @Test
    public void testParseValue() {
        assertThat(ExpressionEditor.parseValue(ValueTypes.INTEGER, "3"), is((IValue) ValueTypeInteger.ValueInteger.of(3)));
        assertThat(ExpressionEditor.parseValue(ValueTypes.INTEGER, "abc"), nullValue());
        assertThat(ExpressionEditor.parseValue(ValueTypes.CATEGORY_NUMBER, "3"), is((IValue) ValueTypeInteger.ValueInteger.of(3)));
        assertThat(ExpressionEditor.parseValue(ValueTypes.CATEGORY_NUMBER, "1.5"), is((IValue) ValueTypeDouble.ValueDouble.of(1.5)));
        assertThat(ExpressionEditor.parseValue(ValueTypes.CATEGORY_ANY, "true"), is((IValue) ValueTypeBoolean.ValueBoolean.of(true)));
        assertThat(ExpressionEditor.parseValue(ValueTypes.CATEGORY_ANY, "hello"), is((IValue) ValueTypeString.ValueString.of("hello")));
        assertThat(ExpressionEditor.parseValue(ValueTypes.BOOLEAN, "yes"), nullValue());
        assertThat(ExpressionEditor.parseValue(ValueTypes.STRING, ""), is((IValue) ValueTypeString.ValueString.of("")));
        assertThat(ExpressionEditor.parseValue(ValueTypes.INTEGER, ""), nullValue());
    }

}
