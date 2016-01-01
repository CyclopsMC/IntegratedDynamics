package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.http.util.Asserts;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;

/**
 * Test the different logical operators.
 * @author rubensworks
 */
public class TestBlockOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableBlock bAir;
    private DummyVariableBlock bCoal;

    @IntegrationBefore
    public void before() {
        bAir = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.air.getDefaultState()));
        bCoal = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.coal_block.getDefaultState()));
    }

    /**
     * ----------------------------------- OPAQUE -----------------------------------
     */

    @IntegrationTest
    public void testBlockOpaque() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_OPAQUE.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isopaque(air) = false");

        IValue res2 = Operators.OBJECT_BLOCK_OPAQUE.evaluate(new IVariable[]{bCoal});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "isopaque(coalblock) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeOpaqueLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_OPAQUE.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeOpaqueSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_OPAQUE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeOpaque() throws EvaluationException {
        Operators.OBJECT_BLOCK_OPAQUE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ITEMSTACK -----------------------------------
     */

    @IntegrationTest
    public void testBlockItemStack() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueObjectTypeItemStack.ValueItemStack, "result is an itemstack");
        TestHelpers.assertEqual(((ValueObjectTypeItemStack.ValueItemStack) res1).getRawValue().isPresent(), false, "itemstack(air) = null");

        IValue res2 = Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bCoal});
        TestHelpers.assertEqual(((ValueObjectTypeItemStack.ValueItemStack) res2).getRawValue().get().isItemEqual(new ItemStack(Blocks.coal_block)), true, "itemstack(coalblock) = coalblock");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeItemStackLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeItemStackSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeItemStack() throws EvaluationException {
        Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
