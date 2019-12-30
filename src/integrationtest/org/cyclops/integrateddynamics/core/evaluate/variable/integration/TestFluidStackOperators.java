package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.http.util.Asserts;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

/**
 * Test the different logical operators.
 * @author rubensworks
 */
public class TestFluidStackOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableFluidStack eBucketLava;
    private DummyVariableFluidStack eBucketWater;
    private DummyVariableFluidStack eWater100;
    private DummyVariableFluidStack eWater100Tag;

    @IntegrationBefore
    public void before() {
        eBucketLava = new DummyVariableFluidStack(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME)));
        eBucketWater = new DummyVariableFluidStack(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME)));
        eWater100 = new DummyVariableFluidStack(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 100)));
        eWater100Tag = new DummyVariableFluidStack(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 100)));
        eWater100Tag.getValue().getRawValue().get().tag = new NBTTagCompound();
        eWater100Tag.getValue().getRawValue().get().tag.setTag("a", new NBTTagString("abc"));
    }

    /**
     * ----------------------------------- AMOUNT -----------------------------------
     */

    @IntegrationTest
    public void testAmount() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), Fluid.BUCKET_VOLUME, "amount(lava:1000) = 1000");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), Fluid.BUCKET_VOLUME, "amount(water:1000) = 1000");

        IValue res3 = Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{eWater100});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res3).getRawValue(), 100, "amount(water:100) = 100");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeAmountLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeAmountSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeAmount() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_AMOUNT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- BLOCK -----------------------------------
     */

    @IntegrationTest
    public void testBlock() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_BLOCK.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueObjectTypeBlock.ValueBlock, "result is a block");
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res1).getRawValue().get(), Blocks.LAVA.getDefaultState(), "block(lava) = lava");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_BLOCK.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueObjectTypeBlock.ValueBlock) res2).getRawValue().get(), Blocks.WATER.getDefaultState(), "block(water) = water");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_BLOCK.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeBlockSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_BLOCK.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeBlock() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_BLOCK.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- LUMINOSITY -----------------------------------
     */

    @IntegrationTest
    public void testLuminosity() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_LUMINOSITY.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 15, "luminosity(lava) = 15");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_LUMINOSITY.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 0, "luminosity(water) = 0");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeLuminosityLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_LUMINOSITY.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeLuminositySmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_LUMINOSITY.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeLuminosity() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_LUMINOSITY.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- DENSITY -----------------------------------
     */

    @IntegrationTest
    public void testDensity() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_DENSITY.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 3000, "density(lava) = 1000");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_DENSITY.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 1000, "density(water) = 1000");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeDensityLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_DENSITY.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeDensitySmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_DENSITY.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeDensity() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_DENSITY.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VISCOSITY -----------------------------------
     */

    @IntegrationTest
    public void testViscosity() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_VISCOSITY.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 6000, "viscosity(lava) = 6000");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_VISCOSITY.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 1000, "viscosity(water) = 1000");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeViscosityLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_VISCOSITY.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeViscositySmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_VISCOSITY.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeViscosity() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_VISCOSITY.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISGASEOUS -----------------------------------
     */

    @IntegrationTest
    public void testIsGaseous() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_ISGASEOUS.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isgaseous(lava) = false");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_ISGASEOUS.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), false, "isgaseous(water) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsGaseousLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISGASEOUS.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsGaseousSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISGASEOUS.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsGaseous() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISGASEOUS.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- RARITY -----------------------------------
     */

    @IntegrationTest
    public void testRarity() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_RARITY.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), EnumRarity.COMMON.rarityName, "rarity(lava) = common");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_RARITY.evaluate(new IVariable[]{eBucketWater});
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res2).getRawValue(), EnumRarity.COMMON.rarityName, "rarity(water) = common");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeRarityLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_RARITY.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeRaritySmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_RARITY.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeRarity() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_RARITY.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISRAWFLUIDEQUAL -----------------------------------
     */

    @IntegrationTest
    public void testIsRawFluidEqual() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{eBucketLava, eBucketWater});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "israwfluidequal(lava, water) = false");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{eBucketLava, eBucketLava});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "israwfluidequal(lava:1000, lava:1000) = true");

        IValue res3 = Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{eBucketWater, eWater100});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), true, "israwfluidequal(water:1000, water:100) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsRawFluidEqualLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{eBucketLava, eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsRawFluidEqualSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsRawFluidEqual() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MODNAME -----------------------------------
     */

    @IntegrationTest
    public void testFluidModName() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_MODNAME.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), "Minecraft", "modname(lava) = Minecraft");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_MODNAME.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_MODNAME.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeModName() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_MODNAME.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- NBT -----------------------------------
     */

    @IntegrationTest
    public void testFluidNbt() throws EvaluationException {
        IValue res1 = Operators.OBJECT_FLUIDSTACK_NBT.evaluate(new IVariable[]{eBucketLava});
        Asserts.check(res1 instanceof ValueTypeNbt.ValueNbt, "result is an nbt tag");
        TestHelpers.assertEqual(((ValueTypeNbt.ValueNbt) res1).getRawValue(), new NBTTagCompound(), "nbt(lava) = null");

        IValue res2 = Operators.OBJECT_FLUIDSTACK_NBT.evaluate(new IVariable[]{eWater100Tag});
        NBTTagCompound tag = new NBTTagCompound();
        tag.putString("a", "abc");
        TestHelpers.assertEqual(((ValueTypeNbt.ValueNbt) res2).getRawValue(), tag, "nbt(watertag) != null");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeNbtLarge() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_NBT.evaluate(new IVariable[]{eBucketLava, eBucketLava});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeNbtSmall() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_NBT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeNbt() throws EvaluationException {
        Operators.OBJECT_FLUIDSTACK_NBT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
