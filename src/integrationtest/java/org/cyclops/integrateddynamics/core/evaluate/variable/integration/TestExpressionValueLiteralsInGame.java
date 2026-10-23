package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tests for how values that need the game's registries are written in the text form of expressions,
 * and that each reads back into the same value.
 * @author rubensworks
 */
public class TestExpressionValueLiteralsInGame {

    private static ValueDeseralizationContext values() {
        return ValueDeseralizationContext.ofAllEnabled();
    }

    private static ExpressionParser.Context context() {
        return ExpressionEditor.getParserContext(values(), id -> null, label -> IntLists.EMPTY_LIST);
    }

    private static Expression parse(String text) throws ExpressionParseException {
        return ExpressionEditor.parse(text, context());
    }

    private static String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, values(), id -> null, label -> IntLists.EMPTY_LIST, id -> null);
    }

    private static IValue read(String text) throws ExpressionParseException {
        Expression expression = parse(text + " == ?");
        return ((ExpressionNode.Const) ((ExpressionNode.Operator) expression.root()).children().get(0)).value();
    }

    /**
     * Write the value in an expression, read that back into the same expression, and write it the same way again.
     * @param value A value.
     * @return How the value is written.
     */
    private static String assertRoundTrip(IValue value) {
        Expression expression = new Expression(new ExpressionNode.Operator(Operators.RELATIONAL_EQUALS,
                List.of(new ExpressionNode.Const(value), ExpressionNode.EMPTY)));
        String text = printExact(expression);
        Expression parsed;
        try {
            parsed = parse(text);
        } catch (ExpressionParseException e) {
            throw new IllegalStateException("Could not parse " + text + " at " + e.getPosition() + ": " + e.getComponent().getString(), e);
        }
        TestHelpers.assertEqual(parsed, expression, "Reads back the same: " + text);
        IValue read = ((ExpressionNode.Const) ((ExpressionNode.Operator) parsed.root()).children().get(0)).value();
        TestHelpers.assertEqual(read.getType(), value.getType(), "Reads back the same type: " + text);
        if (value instanceof ValueTypeList.ValueList<?, ?> list) {
            TestHelpers.assertEqual((IValueType) ((ValueTypeList.ValueList<?, ?>) read).getRawValue().getValueType(),
                    (IValueType) list.getRawValue().getValueType(), "Reads back the same element type: " + text);
        }
        TestHelpers.assertEqual(printExact(parsed), text, "Is written the same again: " + text);
        return ExpressionPrinter.printLiteral(value, values());
    }

    private static void assertError(String text, String key) {
        try {
            parse(text);
            throw new IllegalStateException("Expected an error for " + text);
        } catch (ExpressionParseException e) {
            TestHelpers.assertEqual(((TranslatableContents) e.getComponent().getContents()).getKey(), key, "Error for " + text);
        }
    }

    private static ValueObjectTypeItemStack.ValueItemStack item(ItemStack itemStack) {
        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
    }

    private static ValueTypeList.ValueList list(IValueType<?> type, IValue... values) {
        return ValueTypeList.ValueList.ofList((IValueType) type, List.of(values));
    }

    // --------------- Items ---------------

    @IntegrationTest
    public void testRoundTripItems() {
        TestHelpers.assertEqual(assertRoundTrip(item(new ItemStack(Items.DIAMOND))), "@item{count:1,id:\"minecraft:diamond\"}", "Diamond");
        assertRoundTrip(item(new ItemStack(Items.COBBLESTONE, 64)));
        // More than fits a stack
        assertRoundTrip(item(new ItemStack(Items.COBBLESTONE, 1000)));
        // With data components
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(10);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Blade \"one\""));
        // From the registries the values are written with
        sword.enchant(values().holderLookupProvider().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 3);
        assertRoundTrip(item(sword));
        TestHelpers.assertEqual(assertRoundTrip(item(ItemStack.EMPTY)), "@item{}", "Empty item");
    }

    @IntegrationTest
    public void testReadItems() throws ExpressionParseException {
        // Without a count, it is a single one, and ids can leave out the minecraft namespace
        TestHelpers.assertEqual(read("@item{id: \"minecraft:diamond\"}"), item(new ItemStack(Items.DIAMOND)), "Without count");
        TestHelpers.assertEqual(read("@item{id: \"diamond\", count: 3}"), item(new ItemStack(Items.DIAMOND, 3)), "Without namespace");
        // Unknown items are not read as air
        assertError("@item{id: \"minecraft:diamnd\"} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED);
        assertError("@item{id: \"minecraft:diamond\", count: 0} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED);
    }

    // --------------- Blocks ---------------

    @IntegrationTest
    public void testRoundTripBlocks() {
        TestHelpers.assertEqual(assertRoundTrip(ValueObjectTypeBlock.ValueBlock.of(Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X))),
                "@block{Name:\"minecraft:oak_log\",Properties:{axis:\"x\"}}", "Oak log");
        TestHelpers.assertEqual(assertRoundTrip(ValueObjectTypeBlock.ValueBlock.of(Blocks.STONE.defaultBlockState())),
                "@block{Name:\"minecraft:stone\"}", "Stone");
        assertRoundTrip(ValueObjectTypeBlock.ValueBlock.of(Blocks.AIR.defaultBlockState()));
    }

    @IntegrationTest
    public void testReadBlocks() throws ExpressionParseException {
        TestHelpers.assertEqual(read("@block{Name: \"stone\"}"), ValueObjectTypeBlock.ValueBlock.of(Blocks.STONE.defaultBlockState()), "Without namespace");
        assertError("@block{Name: \"minecraft:stoen\"} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED);
        assertError("@block{Name: \"minecraft:oak_log\", Properties: {axis: \"q\"}} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED);
    }

    // --------------- Fluids ---------------

    @IntegrationTest
    public void testRoundTripFluids() {
        TestHelpers.assertEqual(assertRoundTrip(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 1000))),
                "@fluid{amount:1000,id:\"minecraft:water\"}", "Water");
        assertRoundTrip(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.LAVA, 250)));
        assertRoundTrip(ValueObjectTypeFluidStack.ValueFluidStack.of(FluidStack.EMPTY));
    }

    @IntegrationTest
    public void testReadFluids() throws ExpressionParseException {
        TestHelpers.assertEqual(read("@fluid{id: \"water\", amount: 500}"),
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 500)), "Without namespace");
    }

    // --------------- Ingredients and recipes ---------------

    private static MixedIngredients mixedIngredients() {
        Map<IngredientComponent<?, ?>, List<?>> ingredients = Maps.newIdentityHashMap();
        ingredients.put(IngredientComponent.ENERGY, Lists.newArrayList(777L));
        ingredients.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new FluidStack(Fluids.WATER, 123)));
        ingredients.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(new ItemStack(Items.OAK_BOAT), new ItemStack(Items.STONE, 2)));
        return new MixedIngredients(ingredients);
    }

    @IntegrationTest
    public void testRoundTripIngredients() {
        assertRoundTrip(ValueObjectTypeIngredients.ValueIngredients.of(mixedIngredients()));
        assertRoundTrip(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK,
                Lists.newArrayList(new ItemStack(Items.DIAMOND_PICKAXE), ItemStack.EMPTY))));
        TestHelpers.assertEqual(assertRoundTrip(ValueObjectTypeIngredients.ValueIngredients.of(null)), "@ingredients{}", "No ingredients");
    }

    @IntegrationTest
    public void testRoundTripRecipes() {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.OAK_BOAT), ItemMatch.EXACT)));
        inputs.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        inputs.add(Lists.newArrayList(
                new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.STONE), ItemMatch.ITEM),
                new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.COBBLESTONE), ItemMatch.ITEM)));
        assertRoundTrip(ValueObjectTypeRecipe.ValueRecipe.of(RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK, inputs, mixedIngredients())));
        TestHelpers.assertEqual(assertRoundTrip(ValueObjectTypeRecipe.ValueRecipe.of(null)), "@recipe{}", "No recipe");
    }

    /**
     * A recipe can be written with operators, which is easier than its serialized form.
     */
    @IntegrationTest
    public void testRecipeFromOperators() throws ExpressionParseException, EvaluationException {
        Expression expression = parse("withInputOutput(withItems(@ingredients{}, [@item{id: \"oak_log\"}]), "
                + "withItems(@ingredients{}, [@item{id: \"oak_planks\", count: 4}]))");
        IValue value = ExpressionEvaluator.buildLazy(expression.root(), 0, new ExpressionEvaluator.LocalValueCache(), id -> null).getValue();
        Asserts.check(value instanceof ValueObjectTypeRecipe.ValueRecipe recipe && recipe.getRawValue().isPresent(), "Is a recipe");
        IValue output = Operators.RECIPE_OUTPUT.evaluate(new org.cyclops.integrateddynamics.api.evaluate.variable.IVariable[]{
                new org.cyclops.integrateddynamics.core.evaluate.variable.Variable<>(value)});
        TestHelpers.assertEqual(output, ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK,
                Lists.newArrayList(new ItemStack(Items.OAK_PLANKS, 4)))), "Output");
        assertRoundTrip(value);
    }

    // --------------- Lists ---------------

    @IntegrationTest
    public void testRoundTripListsOfObjects() {
        TestHelpers.assertEqual(assertRoundTrip(list(ValueTypes.OBJECT_ITEMSTACK, item(new ItemStack(Items.DIAMOND)), item(new ItemStack(Items.STONE, 2)))),
                "[@item{count:1,id:\"minecraft:diamond\"}, @item{count:2,id:\"minecraft:stone\"}]", "Items");
        assertRoundTrip(list(ValueTypes.OBJECT_BLOCK, ValueObjectTypeBlock.ValueBlock.of(Blocks.STONE.defaultBlockState())));
        assertRoundTrip(list(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 1))));
        assertRoundTrip(list(ValueTypes.OBJECT_INGREDIENTS, ValueObjectTypeIngredients.ValueIngredients.of(mixedIngredients())));
        // Of different types
        assertRoundTrip(list(ValueTypes.CATEGORY_ANY, item(new ItemStack(Items.DIAMOND)), ValueObjectTypeBlock.ValueBlock.of(Blocks.STONE.defaultBlockState())));
        // An empty list of items is written with its type
        assertRoundTrip(list(ValueTypes.OBJECT_ITEMSTACK));
    }

}
