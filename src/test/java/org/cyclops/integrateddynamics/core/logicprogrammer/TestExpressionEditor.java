package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the logic programmer expression editor.
 * @author rubensworks
 */
public class TestExpressionEditor {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static ExpressionEditor.ActionResult apply(ExpressionEditor editor, net.minecraft.nbt.CompoundTag action) {
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
        net.minecraft.nbt.CompoundTag action = ExpressionEditor.actionOperator(new int[0], 0, Operators.ARITHMETIC_INCREMENT);
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
    public void testParameterNames() {
        assertThat(ExpressionEditor.sanitizeParameterName("my item!"), is("myitem"));
        assertThat(ExpressionEditor.sanitizeParameterName("abcdefghijklmnopqrstuvwxyz").length(), is(16));
        ExpressionEditor editor = new ExpressionEditor(Operators.ARITHMETIC_ADDITION);
        apply(editor, ExpressionEditor.actionAddParam());
        apply(editor, ExpressionEditor.actionRenameParam(0, "!!!"));
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
        assertThat(ExpressionEditor.parseValue(ValueTypes.STRING, ""), nullValue());
    }

}
