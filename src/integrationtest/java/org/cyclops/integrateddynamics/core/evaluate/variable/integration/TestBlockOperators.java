package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
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

    private DummyVariable<ValueTypeNbt.ValueNbt> nbtCarrotGrown;

    @IntegrationBefore
    public void before() {
        bAir = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.AIR.defaultBlockState()));
        bCoal = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.COAL_BLOCK.defaultBlockState()));
        bDarkOakLeaves = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.DARK_OAK_LEAVES.defaultBlockState()));
        bLogicProgrammer = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState()));
        bLeaves = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.OAK_LEAVES.defaultBlockState()));
        bReed = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.SUGAR_CANE.defaultBlockState()));
        bSand = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.SAND.defaultBlockState()));
        bFarmLand = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.FARMLAND.defaultBlockState()));
        bCarrot = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.CARROTS.defaultBlockState()));
        bCarrotGrown = new DummyVariableBlock(ValueObjectTypeBlock.ValueBlock.of(Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 1)));

        iApple = new DummyVariableItemStack(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)));
        iSeedWheat = new DummyVariableItemStack(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.WHEAT_SEEDS)));

        sSponge = new DummyVariable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("minecraft:sponge"));

        CompoundTag tag = new CompoundTag();
        tag.putString("age", "1");
        nbtCarrotGrown = new DummyVariable<>(ValueTypes.NBT, ValueTypeNbt.ValueNbt.of(tag));
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
        TestHelpers.assertEqual(ItemStack.isSameItem(((ValueObjectTypeItemStack.ValueItemStack) res2).getRawValue(), new ItemStack(Blocks.COAL_BLOCK)), true, "itemstack(coalblock) = coalblock");

        IValue res3 = Operators.OBJECT_BLOCK_ITEMSTACK.evaluate(new IVariable[]{bDarkOakLeaves});
        TestHelpers.assertEqual(ItemStack.isSameItem(((ValueObjectTypeItemStack.ValueItemStack) res3).getRawValue(), new ItemStack(Blocks.DARK_OAK_LEAVES, 1)), true, "itemstack(dark_oak_leaves) = dark_oak_leaves");
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
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), "IntegratedDynamics", "modname(logicprogrammer) = IntegratedDynamics");
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
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), SoundEvents.STONE_BREAK.getLocation().toString(), "placesound(coal) = inecraft:block.stone.break");

        IValue res2 = Operators.OBJECT_BLOCK_PLACESOUND.evaluate(new IVariable[]{bCoal});
        Asserts.check(res2 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), SoundEvents.STONE_PLACE.getLocation().toString(), "placesound(coal) = inecraft:block.stone.place");

        IValue res3 = Operators.OBJECT_BLOCK_STEPSOUND.evaluate(new IVariable[]{bCoal});
        Asserts.check(res3 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res3).getRawValue(), SoundEvents.STONE_STEP.getLocation().toString(), "placesound(coal) = inecraft:block.stone.step");
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
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res1).getRawValue().get(), Blocks.SPONGE.defaultBlockState(), "blockbyname(minecraft:sponge) = sponge");
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

    /**
     * ----------------------------------- BLOCK_PROPERTIES -----------------------------------
     */

    @IntegrationTest
    public void testBlockBlockProperties() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_PROPERTIES.evaluate(new IVariable[]{bCarrotGrown});
        Asserts.check(res1 instanceof ValueTypeNbt.ValueNbt, "result is an nbt tag");
        CompoundTag tag = new CompoundTag();
        tag.putString("age", "1");
        TestHelpers.assertEqual(((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), tag, "blockproperties(minecraft:carrot) = {...}");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockPropertiesLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_PROPERTIES.evaluate(new IVariable[]{bLeaves, bLeaves});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockPropertiesSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_PROPERTIES.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeBlockProperties() throws EvaluationException {
        Operators.OBJECT_BLOCK_PROPERTIES.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- BLOCK_WITH_PROPERTIES -----------------------------------
     */

    @IntegrationTest
    public void testBlockBlockWithProperties() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_WITH_PROPERTIES.evaluate(new IVariable[]{bCarrot, nbtCarrotGrown});
        Asserts.check(res1 instanceof ValueObjectTypeBlock.ValueBlock, "result is a block");
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res1).getRawValue().get(), bCarrotGrown.getValue().getRawValue().get(), "blockwithproperties(minecraft:carrot, ...) = {...}");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockWithPropertiesLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_WITH_PROPERTIES.evaluate(new IVariable[]{bLeaves, nbtCarrotGrown, bLeaves});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockWithPropertiesSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_WITH_PROPERTIES.evaluate(new IVariable[]{bLeaves});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeBlockWithProperties() throws EvaluationException {
        Operators.OBJECT_BLOCK_WITH_PROPERTIES.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- BLOCK_POSSIBLE_PROPERTIES -----------------------------------
     */

    @IntegrationTest
    public void testBlockBlockPossibleProperties() throws EvaluationException {
        IValue res1 = Operators.OBJECT_BLOCK_POSSIBLE_PROPERTIES.evaluate(new IVariable[]{bCarrotGrown});
        Asserts.check(res1 instanceof ValueTypeNbt.ValueNbt, "result is an nbt tag");
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        list.add(StringTag.valueOf("0"));
        list.add(StringTag.valueOf("1"));
        list.add(StringTag.valueOf("2"));
        list.add(StringTag.valueOf("3"));
        list.add(StringTag.valueOf("4"));
        list.add(StringTag.valueOf("5"));
        list.add(StringTag.valueOf("6"));
        list.add(StringTag.valueOf("7"));
        tag.put("age", list);
        TestHelpers.assertEqual(((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), tag, "blockpossibleproperties(minecraft:carrot) = {...}");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockPossiblePropertiesLarge() throws EvaluationException {
        Operators.OBJECT_BLOCK_POSSIBLE_PROPERTIES.evaluate(new IVariable[]{bLeaves, bLeaves});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockPossiblePropertiesSmall() throws EvaluationException {
        Operators.OBJECT_BLOCK_POSSIBLE_PROPERTIES.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeBlockPossibleProperties() throws EvaluationException {
        Operators.OBJECT_BLOCK_POSSIBLE_PROPERTIES.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
