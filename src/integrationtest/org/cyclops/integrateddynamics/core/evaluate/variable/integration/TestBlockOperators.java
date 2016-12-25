package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import org.apache.http.util.Asserts;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

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
    private DummyVariableBlock bLogicProgrammer;

    @IntegrationBefore
    public void before() {
        bAir = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.AIR.getDefaultState()));
        bCoal = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.COAL_BLOCK.getDefaultState()));
        bLogicProgrammer = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(BlockLogicProgrammer.getInstance().getDefaultState()));
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
        TestHelpers.assertEqual(!((ValueObjectTypeItemStack.ValueItemStack) res1).getRawValue().isEmpty(), false, "itemstack(air) = null");

        IValue res2 = Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bCoal});
        TestHelpers.assertEqual(((ValueObjectTypeItemStack.ValueItemStack) res2).getRawValue().isItemEqual(new ItemStack(Blocks.COAL_BLOCK)), true, "itemstack(coalblock) = coalblock");
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

    /**
     * ----------------------------------- MODNAME -----------------------------------
     */

    @IntegrationTest
    public void testBlockModName() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_MODNAME.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), "Minecraft", "modname(air) = Minecraft");

        IValue res2 = Operators.OBJECT_BLOCK_MODNAME.evaluate(new IVariable[]{bLogicProgrammer});
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), "Integrated Dynamics", "modname(logicprogrammer) = Integrated Dynamics");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_MODNAME.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_MODNAME.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeModName() throws EvaluationException {
        Operators.OBJECT_BLOCK_MODNAME.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SOUNDS -----------------------------------
     */

    @IntegrationTest
    public void testBlockSound() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_BREAKSOUND.evaluate(new IVariable[]{bCoal});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), SoundEvents.BLOCK_STONE_BREAK.getSoundName().toString(), "placesound(coal) = inecraft:block.stone.break");

        IValue res2 = Operators.OBJECT_BLOCK_PLACESOUND.evaluate(new IVariable[]{bCoal});
        Asserts.check(res2 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), SoundEvents.BLOCK_STONE_PLACE.getSoundName().toString(), "placesound(coal) = inecraft:block.stone.place");

        IValue res3 = Operators.OBJECT_BLOCK_STEPSOUND.evaluate(new IVariable[]{bCoal});
        Asserts.check(res3 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res3).getRawValue(), SoundEvents.BLOCK_STONE_STEP.getSoundName().toString(), "placesound(coal) = inecraft:block.stone.step");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeSoundLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_BREAKSOUND.evaluate(new IVariable[]{bAir, bAir});
        Operators.OBJECT_BLOCK_PLACESOUND.evaluate(new IVariable[]{bAir, bAir});
        Operators.OBJECT_BLOCK_STEPSOUND.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeSoundSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_BREAKSOUND.evaluate(new IVariable[]{});
        Operators.OBJECT_BLOCK_PLACESOUND.evaluate(new IVariable[]{});
        Operators.OBJECT_BLOCK_STEPSOUND.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeSound() throws EvaluationException {
        Operators.OBJECT_BLOCK_BREAKSOUND.evaluate(new IVariable[]{DUMMY_VARIABLE});
        Operators.OBJECT_BLOCK_PLACESOUND.evaluate(new IVariable[]{DUMMY_VARIABLE});
        Operators.OBJECT_BLOCK_STEPSOUND.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
