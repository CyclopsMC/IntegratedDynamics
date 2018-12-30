package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.http.util.Asserts;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Test the different ingredients operators.
 * @author rubensworks
 */
public class TestIngredientsOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariable<ValueTypeInteger.ValueInteger> i0;
    private DummyVariable<ValueTypeInteger.ValueInteger> i1;
    private DummyVariable<ValueTypeInteger.ValueInteger> i2;
    private DummyVariable<ValueTypeInteger.ValueInteger> i3;

    private DummyVariableIngredients iEmpty;
    private DummyVariableIngredients iItems;
    private DummyVariable<ValueTypeList.ValueList> lItems;
    private DummyVariableIngredients iFluids;
    private DummyVariable<ValueTypeList.ValueList> lFluids;
    private DummyVariableIngredients iEnergies;
    private DummyVariable<ValueTypeList.ValueList> lEnergies;
    private IMixedIngredients inputIngredients;
    private DummyVariableIngredients iMix;

    private DummyVariable<ValueObjectTypeItemStack.ValueItemStack> iItem;
    private Predicate<ValueObjectTypeItemStack.ValueItemStack> pItemRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pItem;
    
    private DummyVariable<ValueObjectTypeFluidStack.ValueFluidStack> iFluid;
    private Predicate<ValueObjectTypeFluidStack.ValueFluidStack> pFluidRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pFluid;

    private DummyVariable<ValueTypeInteger.ValueInteger> iEnergy;
    private Predicate<ValueTypeInteger.ValueInteger> pEnergyRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pEnergy;

    @IntegrationBefore
    public void before() {
        i0 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));
        i3 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));

        iEmpty = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new MixedIngredients(Maps.newIdentityHashMap())));

        iItems = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK, Lists.newArrayList(
                        ItemStack.EMPTY, new ItemStack(Items.BOAT), new ItemStack(Blocks.STONE), ItemStack.EMPTY)
                )));
        lItems = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.BOAT)),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Blocks.STONE)),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY)
        ));

        iFluids = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                MixedIngredients.ofInstances(IngredientComponent.FLUIDSTACK, Lists.newArrayList(
                        new FluidStack(FluidRegistry.LAVA, 1000), new FluidStack(FluidRegistry.WATER, 125))
                )));
        lFluids = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.LAVA, 1000)),
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 125))
        ));

        iEnergies = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                MixedIngredients.ofInstances(IngredientComponent.ENERGY, Lists.newArrayList(
                        666, 777, 0)
                )));
        lEnergies = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(666),
                ValueTypeInteger.ValueInteger.of(777),
                ValueTypeInteger.ValueInteger.of(0)
        ));

        Map<IngredientComponent<?, ?>, List<?>> ingredients = Maps.newIdentityHashMap();
        ingredients.put(IngredientComponent.ENERGY, Lists.newArrayList(777));
        ingredients.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new FluidStack(FluidRegistry.WATER, 125)));
        ingredients.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(new ItemStack(Items.BOAT), new ItemStack(Item.getItemFromBlock(Blocks.STONE))));
        inputIngredients = new MixedIngredients(ingredients);
        iMix = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(inputIngredients));

        iItem = new DummyVariable<>(ValueTypes.OBJECT_ITEMSTACK, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)));
        iFluid = new DummyVariable<>(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)));
        iEnergy = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(123));
    }

    /**
     * ----------------------------------- ITEMS -----------------------------------
     */

    @IntegrationTest
    public void testItems() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 2, "items(mix, 0).size = 2");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack>) res1)
                .getRawValue().get(0).getRawValue().getItem(), Items.BOAT, "items(mix, 0) = boat");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack>) res1)
                .getRawValue().get(1).getRawValue().getItem(), Item.getItemFromBlock(Blocks.STONE), "items(mix, 0) = boat");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSize() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLUIDS -----------------------------------
     */

    @IntegrationTest
    public void testFluids() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iFluids});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 2, "fluids(fluids, 0).size = 2");
        TestHelpers.assertEqual(res1,
                ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_FLUIDSTACK, Lists.newArrayList(
                        ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.LAVA, 1000)),
                        ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 125))
                )), "fluids(fluids) = lava, water");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSize() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ENERGIES -----------------------------------
     */

    @IntegrationTest
    public void testEnergies() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iEnergies});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 3, "energys(energys, 0).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res1)
                .getRawValue().get(0).getRawValue(), 666, "energies(energies, 0) = 666");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res1)
                .getRawValue().get(1).getRawValue(), 777, "energies(energies, 0) = 777");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res1)
                .getRawValue().get(2).getRawValue(), 0, "energies(energies, 0) = 0");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSize() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ITEM -----------------------------------
     */

    @IntegrationTest
    public void testWithItem() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ITEM.evaluate(new IVariable[]{iMix, i0, iItem});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<ItemStack> outputList1 = outputIngredients1.getInstances(IngredientComponent.ITEMSTACK);
        TestHelpers.assertEqual(outputList1.size(), 2, "with_items(mix, 0, items)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0).getItem(), Items.APPLE,
                "with_items(mix, 0, items)[0] = items[0]");
        TestHelpers.assertEqual(outputList1.get(1).getItem(), Item.getItemFromBlock(Blocks.STONE),
                "with_items(mix, 0, items)[1] = items[1]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK).size(), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).size(), "Items size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK).get(0), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).get(0), "Items 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK).get(1), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).get(1), "Items 1 remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ITEM.evaluate(new IVariable[]{iMix, i2, iItem});
        IMixedIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<ItemStack> outputList2 = outputIngredients2.getInstances(IngredientComponent.ITEMSTACK);
        TestHelpers.assertEqual(outputList2.size(), 3, "with_items(mix, 2, items)[0]size = 3");
        TestHelpers.assertEqual(outputList2.get(0).getItem(), Items.BOAT,
                "with_items(mix, 2, items)[0] = items[0]");
        TestHelpers.assertEqual(outputList2.get(1).getItem(), Item.getItemFromBlock(Blocks.STONE),
                "with_items(mix, 2, items)[1] = items[1]");
        TestHelpers.assertEqual(outputList2.get(2).getItem(), Items.APPLE,
                "with_items(mix, 2, items)[2] = items[2]");

        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK).size(), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).size(), "Items size changes");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK).get(0), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).get(0), "Items 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK).get(1), inputIngredients.getInstances(IngredientComponent.ITEMSTACK).get(1), "Items 1 remains the same");
        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK).get(2), Collections.emptyList(), "Items 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM.evaluate(new IVariable[]{iMix, i0, iItem});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLUID -----------------------------------
     */

    @IntegrationTest
    public void testWithFluid() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_FLUID.evaluate(new IVariable[]{iMix, i0, iFluid});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<FluidStack> outputList1 = outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK);
        TestHelpers.assertEqual(outputList1.size(), 1, "with_fluids(mix, 0, fluids)[0]size = 1");
        TestHelpers.assertEqual(outputList1.get(0), new FluidStack(FluidRegistry.WATER, 123),
                "with_fluids(mix, 0, fluids)[0] = fluids[0]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK).size(), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK).size(), "Fluids size remains the same");
        TestHelpers.assertNonEqual(ValueObjectTypeFluidStack.ValueFluidStack.of(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK).get(0)), ValueObjectTypeFluidStack.ValueFluidStack.of(inputIngredients.getInstances(IngredientComponent.FLUIDSTACK).get(0)), "Fluids 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_FLUID.evaluate(new IVariable[]{iMix, i2, iFluid});
        IMixedIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<FluidStack> outputList2 = outputIngredients2.getInstances(IngredientComponent.FLUIDSTACK);
        TestHelpers.assertEqual(outputList2.size(), 3, "with_fluids(mix, 3, fluids)[0]size = 2");
        TestHelpers.assertEqual(outputList2.get(0), new FluidStack(FluidRegistry.WATER, 125),
                "with_fluids(mix, 2, fluids)[0] = fluids[0]");
        TestHelpers.assertEqual(outputList2.get(1), null,
                "with_fluids(mix, 2, fluids)[1] = fluids[1]");
        TestHelpers.assertEqual(outputList2.get(2), new FluidStack(FluidRegistry.WATER, 123),
                "with_fluids(mix, 2, fluids)[2] = fluids[2]");

        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.FLUIDSTACK).size(), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK).size(), "Fluids size changes");
        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.FLUIDSTACK).get(2), Collections.emptyList(), "Fluids 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID.evaluate(new IVariable[]{iMix, i0, iFluid});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ENERGY -----------------------------------
     */

    @IntegrationTest
    public void testWithEnergy() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ENERGY.evaluate(new IVariable[]{iMix, i0, iEnergy});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<Integer> outputList1 = outputIngredients1.getInstances(IngredientComponent.ENERGY);
        TestHelpers.assertEqual(outputList1.size(), 1, "with_energy(mix, 0, energy)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0), 123, "with_energy(mix, 0, energy)[0][0] = energy[0]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY).size(), inputIngredients.getInstances(IngredientComponent.ENERGY).size(), "Energies size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY).get(0), inputIngredients.getInstances(IngredientComponent.ENERGY).get(0), "Energies 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluids remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ENERGY.evaluate(new IVariable[]{iMix, i2, iEnergy});
        IMixedIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<Integer> outputList2 = outputIngredients2.getInstances(IngredientComponent.ENERGY);
        TestHelpers.assertEqual(outputList2.size(), 3, "with_energy(mix, 2, energy)[0]size = 3");
        TestHelpers.assertEqual(outputList2.get(0), 777, "with_energy(mix, 2, energy)[3][0] = energy[0]");
        TestHelpers.assertEqual(outputList2.get(1), 0, "with_energy(mix, 2, energy)[3][0] = energy[0]");
        TestHelpers.assertEqual(outputList2.get(2), 123, "with_energy(mix, 2, energy)[3][0] = energy[0]");

        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.ENERGY).size(), inputIngredients.getInstances(IngredientComponent.ENERGY).size(), "Energies size changes");
        TestHelpers.assertNonEqual(outputIngredients2.getInstances(IngredientComponent.ENERGY).get(2), Collections.emptyList(), "Energies 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluids remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergySizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY.evaluate(new IVariable[]{iMix, i0, iEnergy});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergySizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergySize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ITEMS -----------------------------------
     */

    @IntegrationTest
    public void testWithItems() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, lItems});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<ItemStack> outputList1 = outputIngredients1.getInstances(IngredientComponent.ITEMSTACK);
        TestHelpers.assertEqual(outputList1.size(), 4, "with_items(mix, items)[0]size = 4");
        TestHelpers.assertEqual(outputList1.get(0).getItem(), Items.AIR,
                "with_items(mix, items)[0] = items[0]");
        TestHelpers.assertEqual(outputList1.get(1).getItem(), Items.BOAT,
                "with_items(mix, items)[1] = items[1]");
        TestHelpers.assertEqual(outputList1.get(2).getItem(), Item.getItemFromBlock(Blocks.STONE),
                "with_items(mix, items)[2] = items[2]");
        TestHelpers.assertEqual(outputList1.get(3).getItem(), Items.AIR,
                "with_items(mix, items)[3] = items[3]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, lItems});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLUIDS -----------------------------------
     */

    @IntegrationTest
    public void testWithFluids() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, lFluids});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<FluidStack> outputList1 = outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK);
        TestHelpers.assertEqual(outputList1.size(), 2, "with_fluids(mix, fluids)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0), new FluidStack(FluidRegistry.LAVA, 1000),
                "with_fluids(mix, fluids)[0] = fluids[0]");
        TestHelpers.assertEqual(outputList1.get(1), new FluidStack(FluidRegistry.WATER, 125),
                "with_fluids(mix, fluids)[1] = fluids[1]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Item remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ENERGY), inputIngredients.getInstances(IngredientComponent.ENERGY), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, lFluids});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ENERGIES -----------------------------------
     */

    @IntegrationTest
    public void testWithEnergies() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, lEnergies});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IMixedIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<Integer> outputList1 = outputIngredients1.getInstances(IngredientComponent.ENERGY);
        TestHelpers.assertEqual(outputList1.size(), 3, "with_energies(mix, energies)[0]size = 3");
        TestHelpers.assertEqual(outputList1.get(0), 666,
                "with_energies(mix, energies)[0] = energies[0]");
        TestHelpers.assertEqual(outputList1.get(1), 777,
                "with_energies(mix, energies)[1] = energies[1]");
        TestHelpers.assertEqual(outputList1.get(2), 0,
                "with_energies(mix, energies)[2] = energies[2]");

        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.ITEMSTACK), inputIngredients.getInstances(IngredientComponent.ITEMSTACK), "Item remains the same");
        TestHelpers.assertEqual(outputIngredients1.getInstances(IngredientComponent.FLUIDSTACK), inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), "Fluid remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, lEnergies});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
