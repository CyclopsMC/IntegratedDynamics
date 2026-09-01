package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;
import org.cyclops.integrateddynamics.core.network.IngredientPositionsIndex;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Game tests for {@link IngredientPositionsIndex}.
 * @author rubensworks
 */
public class GameTestsIngredientPositionsIndex {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";

    protected static PartPos pos(GameTestHelper helper, int x) {
        return PartPos.of(DimPos.of(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, 1))), Direction.UP);
    }

    protected static ItemStack named(ItemStack itemStack, String name) {
        ItemStack copy = itemStack.copy();
        copy.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return copy;
    }

    protected static List<PartPos> list(Iterator<PartPos> iterator) {
        return Lists.newArrayList(iterator);
    }

    protected static Set<PartPos> set(Iterator<PartPos> iterator) {
        return new LinkedHashSet<>(list(iterator));
    }

    /**
     * Determine the expected positions for the given query by naively scanning over all indexed instances.
     */
    protected static <T, M> Set<PartPos> expectedPositions(IngredientComponent<T, M> component,
                                                           List<Pair<T, PrioritizedPartPos>> contents,
                                                           T instance, M matchCondition) {
        IIngredientMatcher<T, M> matcher = component.getMatcher();
        T prototype = matcher.withQuantity(instance, 1);
        Set<PartPos> positions = new LinkedHashSet<>();
        for (Pair<T, PrioritizedPartPos> entry : contents) {
            if (matcher.matches(prototype, matcher.withQuantity(entry.getLeft(), 1), matchCondition)) {
                positions.add(entry.getRight().getPartPos());
            }
        }
        return positions;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPositionLookups(GameTestHelper helper) {
        IngredientPositionsIndex<ItemStack, Integer> index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);

        PartPos pos0 = pos(helper, 0);
        PartPos pos1 = pos(helper, 1);
        PartPos pos2 = pos(helper, 2);

        ItemStack apple = new ItemStack(Items.APPLE);
        ItemStack appleNamed = named(apple, "Fancy apple");
        ItemStack beef = new ItemStack(Items.BEEF);

        index.addPosition(new ItemStack(Items.APPLE, 10), PrioritizedPartPos.of(pos0, 0));
        index.addPosition(appleNamed, PrioritizedPartPos.of(pos1, 0));
        index.addPosition(beef, PrioritizedPartPos.of(pos1, 0));
        index.addPosition(beef, PrioritizedPartPos.of(pos2, 0));

        // Exact matching (without quantity) must only return positions with that exact prototype
        helper.assertValueEqual(set(index.getPositions(apple, IngredientComponent.ITEMSTACK.getMatcher()
                .getExactMatchNoQuantityCondition())), Set.of(pos0), "Exact apple lookup");
        helper.assertValueEqual(set(index.getPositions(appleNamed, IngredientComponent.ITEMSTACK.getMatcher()
                .getExactMatchNoQuantityCondition())), Set.of(pos1), "Exact named apple lookup");

        // Exact matching including quantity: prototypes are stored with quantity one
        helper.assertValueEqual(set(index.getPositions(apple, ItemMatch.EXACT)), Set.of(pos0), "Exact apple lookup with quantity");

        // Item-only matching must return all data variants of that item
        helper.assertValueEqual(set(index.getPositions(apple, ItemMatch.ITEM)), Set.of(pos0, pos1), "Item apple lookup");
        helper.assertValueEqual(set(index.getPositions(beef, ItemMatch.ITEM)), Set.of(pos1, pos2), "Item beef lookup");

        // Any matching must return all positions
        helper.assertValueEqual(set(index.getPositions(ItemStack.EMPTY, ItemMatch.ANY)), Set.of(pos0, pos1, pos2), "Any lookup");
        helper.assertValueEqual(set(index.getNonEmptyPositions()), Set.of(pos0, pos1, pos2), "Non-empty positions");

        // Unknown instances must not be matched
        helper.assertValueEqual(set(index.getPositions(new ItemStack(Items.STONE), ItemMatch.EXACT)), Set.of(), "Unknown exact lookup");
        helper.assertValueEqual(set(index.getPositions(new ItemStack(Items.STONE), ItemMatch.ITEM)), Set.of(), "Unknown item lookup");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPositionLookupsMatchAllConditions(GameTestHelper helper) {
        IngredientPositionsIndex<ItemStack, Integer> index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);

        List<ItemStack> instances = List.of(
                new ItemStack(Items.APPLE),
                named(new ItemStack(Items.APPLE), "Fancy"),
                named(new ItemStack(Items.APPLE), "Fancier"),
                new ItemStack(Items.BEEF),
                named(new ItemStack(Items.BEEF), "Fancy"),
                new ItemStack(Items.STONE),
                named(new ItemStack(Items.STONE), "Fancy"),
                new ItemStack(Items.DIRT)
        );

        List<Pair<ItemStack, PrioritizedPartPos>> contents = Lists.newArrayList();
        for (int i = 0; i < instances.size(); i++) {
            // Spread instances over positions and priorities, with some overlap
            PrioritizedPartPos pos = PrioritizedPartPos.of(pos(helper, i % 3), i % 2);
            contents.add(Pair.of(instances.get(i), pos));
            index.addPosition(instances.get(i), pos);
        }

        // All match conditions must yield the same positions as a naive scan over all indexed instances
        for (int matchCondition : new int[]{ItemMatch.ANY, ItemMatch.ITEM, ItemMatch.DATA, ItemMatch.STACKSIZE,
                ItemMatch.ITEM | ItemMatch.DATA, ItemMatch.ITEM | ItemMatch.STACKSIZE,
                ItemMatch.DATA | ItemMatch.STACKSIZE, ItemMatch.EXACT}) {
            for (ItemStack instance : instances) {
                helper.assertValueEqual(
                        set(index.getPositions(instance, matchCondition)),
                        expectedPositions(IngredientComponent.ITEMSTACK, contents, instance, matchCondition),
                        "Lookup of " + instance + " with match condition " + matchCondition);
            }
        }

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPositionLookupsPriorities(GameTestHelper helper) {
        IngredientPositionsIndex<ItemStack, Integer> index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);

        PartPos posLow = pos(helper, 0);
        PartPos posHigh = pos(helper, 1);
        PartPos posMid = pos(helper, 2);

        ItemStack apple = new ItemStack(Items.APPLE);
        index.addPosition(apple, PrioritizedPartPos.of(posLow, 0));
        index.addPosition(apple, PrioritizedPartPos.of(posHigh, 10));
        index.addPosition(named(apple, "Fancy apple"), PrioritizedPartPos.of(posMid, 5));

        // Higher priorities must be returned first
        helper.assertValueEqual(list(index.getPositions(apple, ItemMatch.EXACT)),
                List.of(posHigh, posLow), "Exact lookup priority order");
        helper.assertValueEqual(list(index.getPositions(apple, ItemMatch.ITEM)),
                List.of(posHigh, posMid, posLow), "Item lookup priority order");
        helper.assertValueEqual(list(index.getNonEmptyPositions()),
                List.of(posHigh, posMid, posLow), "Non-empty positions priority order");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPositionRemoval(GameTestHelper helper) {
        IngredientPositionsIndex<ItemStack, Integer> index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);

        PartPos pos0 = pos(helper, 0);
        PartPos pos1 = pos(helper, 1);

        ItemStack apple = new ItemStack(Items.APPLE);
        ItemStack beef = new ItemStack(Items.BEEF);

        index.addPosition(apple, PrioritizedPartPos.of(pos0, 0));
        index.addPosition(apple, PrioritizedPartPos.of(pos1, 0));
        index.addPosition(beef, PrioritizedPartPos.of(pos1, 0));

        index.removePosition(apple, PrioritizedPartPos.of(pos0, 0));
        helper.assertValueEqual(set(index.getPositions(apple, ItemMatch.EXACT)), Set.of(pos1), "Apple after removal");
        helper.assertValueEqual(set(index.getNonEmptyPositions()), Set.of(pos1), "Non-empty positions after removal");

        index.removePosition(apple, PrioritizedPartPos.of(pos1, 0));
        index.removePosition(beef, PrioritizedPartPos.of(pos1, 0));
        helper.assertValueEqual(set(index.getPositions(apple, ItemMatch.ITEM)), Set.of(), "Apple after full removal");
        helper.assertValueEqual(set(index.getNonEmptyPositions()), Set.of(), "Non-empty positions after full removal");

        // Removing an unknown position must be a no-op
        index.removePosition(beef, PrioritizedPartPos.of(pos0, 0));
        helper.assertValueEqual(set(index.getNonEmptyPositions()), Set.of(), "Non-empty positions after no-op removal");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPositionLookupsSingleCategoryComponent(GameTestHelper helper) {
        // The energy component only has a single category type, so it is not classified internally
        IngredientPositionsIndex<Long, Boolean> index = new IngredientPositionsIndex<>(IngredientComponent.ENERGY);

        PartPos pos0 = pos(helper, 0);
        PartPos pos1 = pos(helper, 1);

        index.addPosition(100L, PrioritizedPartPos.of(pos0, 0));
        index.addPosition(200L, PrioritizedPartPos.of(pos1, 0));

        // Energy instances are all indexed under the same prototype
        helper.assertValueEqual(set(index.getPositions(100L, IngredientComponent.ENERGY.getMatcher()
                .getExactMatchNoQuantityCondition())), Set.of(pos0, pos1), "Energy exact lookup without quantity");
        helper.assertValueEqual(set(index.getNonEmptyPositions()), Set.of(pos0, pos1), "Energy non-empty positions");

        helper.succeed();
    }

}
