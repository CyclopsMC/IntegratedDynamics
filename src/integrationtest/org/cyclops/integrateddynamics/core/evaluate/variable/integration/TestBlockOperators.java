package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSponge;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
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
    private DummyVariableBlock bDarkOakLeaves;
    private DummyVariableBlock bLogicProgrammer;
    private DummyVariableBlock bLeaves;
    private DummyVariableBlock bReed;
    private DummyVariableBlock bSand;
    private DummyVariableBlock bFarmLand;
    private DummyVariableBlock bCarrot;
    private DummyVariableBlock bCarrotGrown;

    private DummyVariableItemStack iApple;
    private DummyVariableItemStack iSeedWheat;

    private DummyVariable<ValueTypeString.ValueString> sSponge;
    private DummyVariable<ValueTypeString.ValueString> sSpongeWet;

    @IntegrationBefore
    public void before() {
        bAir = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.AIR.getDefaultState()));
        bCoal = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.COAL_BLOCK.getDefaultState()));
        bDarkOakLeaves = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK)));
        bLogicProgrammer = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(BlockLogicProgrammer.getInstance().getDefaultState()));
        bLeaves = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.LEAVES.getDefaultState()));
        bReed = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.REEDS.getDefaultState()));
        bSand = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.SAND.getDefaultState()));
        bFarmLand = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.FARMLAND.getDefaultState()));
        bCarrot = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.CARROTS.getDefaultState()));
        bCarrotGrown = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.CARROTS.getDefaultState().withProperty(BlockCrops.AGE, 1)));

        iApple = new DummyVariableItemStack(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)));
        iSeedWheat = new DummyVariableItemStack(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.WHEAT_SEEDS)));

        sSponge = new DummyVariable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("minecraft:sponge"));
        sSpongeWet = new DummyVariable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("minecraft:sponge 1"));
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

        IValue res3 = Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bDarkOakLeaves});
        TestHelpers.assertEqual(((ValueObjectTypeItemStack.ValueItemStack) res3).getRawValue().isItemEqual(new ItemStack(Blocks.LEAVES2, 1, 1)), true, "itemstack(dark_oak_leaves) = dark_oak_leaves");
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

    /**
     * ----------------------------------- ISSHEARABLE -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsShearable() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_ISSHEARABLE.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isshearable(air) = false");

        IValue res2 = Operators.OBJECT_BLOCK_ISSHEARABLE.evaluate(new IVariable[]{bLeaves});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "isshearable(leaves) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsShearableLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISSHEARABLE.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsShearableSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISSHEARABLE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsShearable() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISSHEARABLE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISPLANTABLE -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsPlantable() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_ISPLANTABLE.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isplantable(air) = false");

        IValue res2 = Operators.OBJECT_BLOCK_ISPLANTABLE.evaluate(new IVariable[]{bReed});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "isplantable(reed) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsPlantableLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISPLANTABLE.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsPlantableSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISPLANTABLE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsPlantable() throws EvaluationException {
        Operators.OBJECT_BLOCK_ISPLANTABLE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PLANTTYPE -----------------------------------
     */

    @IntegrationTest
    public void testBlockPlantType() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_PLANTTYPE.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), "None", "planttype(air) = None");

        IValue res2 = Operators.OBJECT_BLOCK_PLANTTYPE.evaluate(new IVariable[]{bReed});
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), "Beach", "planttype(reed) = Beach");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantTypeLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTTYPE.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantTypeSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTTYPE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypePlantType() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTTYPE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PLANT -----------------------------------
     */

    @IntegrationTest
    public void testBlockPlant() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_PLANT.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueObjectTypeBlock.ValueBlock, "result is a block");
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res1).getRawValue().isPresent(), false, "plant(air) = null");

        IValue res2 = Operators.OBJECT_BLOCK_PLANT.evaluate(new IVariable[]{bReed});
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res2).getRawValue().get().getBlock() == Blocks.REEDS, true, "plant(reed) = reed");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANT.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypePlant() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PLANTAGE -----------------------------------
     */

    @IntegrationTest
    public void testBlockPlantAge() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{bAir});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 0, "plantage(air) = 0");

        IValue res2 = Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{bCarrot});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 0, "plantage(bCarrot) = 0");

        IValue res3 = Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{bCarrotGrown});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res3).getRawValue(), 1, "plantage(bCarrotGrown) = 1");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantAgeLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{bAir, bAir});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizePlantAgeSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypePlantAge() throws EvaluationException {
        Operators.OBJECT_BLOCK_PLANTAGE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- BLOCKBYNAME -----------------------------------
     */

    @IntegrationTest
    public void testBlockBlockByName() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_BY_NAME.evaluate(new IVariable[]{sSponge});
        Asserts.check(res1 instanceof ValueObjectTypeBlock.ValueBlock, "result is a block");
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res1).getRawValue().get(), Blocks.SPONGE.getDefaultState(), "blockbyname(minecraft:sponge) = sponge");

        IValue res2 = Operators.OBJECT_BLOCK_BY_NAME.evaluate(new IVariable[]{sSpongeWet});
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res2).getRawValue().get(),
                Blocks.SPONGE.getDefaultState().withProperty(BlockSponge.WET, true), "blockbyname(minecraft:sponge 1) = sponge_wet");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockByNameLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_BY_NAME.evaluate(new IVariable[]{sSponge, sSponge});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockByNameSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_BY_NAME.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeBlockByName() throws EvaluationException {
        Operators.OBJECT_BLOCK_BY_NAME.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
