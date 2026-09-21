package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;
import java.util.Set;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableForValue;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getVariableFacade;

/**
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsVariableCopy {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableKeepsLabelOnBothCards(GameTestHelper helper) {
        testCopyVariableKeepsLabel(helper, ClickType.PICKUP);
    }

    /**
     * Shift-clicking the result crafts until the result changes,
     * so the copy must not be left behind in a state where another recipe can consume it.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableKeepsLabelOnBothCardsQuickMove(GameTestHelper helper) {
        testCopyVariableKeepsLabel(helper, ClickType.QUICK_MOVE);
    }

    protected void testCopyVariableKeepsLabel(GameTestHelper helper, ClickType clickType) {
        ServerLevel level = helper.getLevel();
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);

        // Create a variable card, and label it
        ItemStack original = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));
        int originalId = getVariableFacade(level, original).getId();
        labels.put(originalId, "MyLabel");

        // Craft the labelled card together with a blank card
        List<ItemStack> cards = craftVariableCopy(helper, original,
                new ItemStack(RegistryEntries.ITEM_VARIABLE.get()), clickType);

        // Both cards must hold a variable, and both must be labelled
        helper.assertValueEqual(cards.size(), 2, "Number of variable cards after crafting");
        for (ItemStack card : cards) {
            IVariableFacade facade = getVariableFacade(level, card);
            helper.assertTrue(facade.isValid(), "A card lost its variable");
            helper.assertTrue("MyLabel".equals(labels.getLabel(facade.getId())), "A card lost its label");
            helper.assertValueEqual(card.getHoverName().getString(), "MyLabel", "Card name");
        }

        // The copy must refer to a new variable
        helper.assertTrue(getVariableFacade(level, cards.get(0)).getId() != getVariableFacade(level, cards.get(1)).getId(),
                "Both cards refer to the same variable");

        helper.succeed();
    }

    /**
     * Shift-clicking must keep crafting copies for as long as there are blank cards to copy onto.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableMultipleTimes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);

        // Create a variable card, and label it
        ItemStack original = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));
        labels.put(getVariableFacade(level, original).getId(), "MyLabel");

        // Craft the labelled card together with three blank cards
        List<ItemStack> cards = craftVariableCopy(helper, original,
                new ItemStack(RegistryEntries.ITEM_VARIABLE.get(), 3), ClickType.QUICK_MOVE);

        // Each blank card must have become a labelled copy, referring to its own variable
        helper.assertValueEqual(cards.size(), 4, "Number of variable cards after crafting");
        Set<Integer> ids = Sets.newHashSet();
        for (ItemStack card : cards) {
            IVariableFacade facade = getVariableFacade(level, card);
            helper.assertTrue(facade.isValid(), "A card lost its variable");
            helper.assertTrue("MyLabel".equals(labels.getLabel(facade.getId())), "A card lost its label");
            ids.add(facade.getId());
        }
        helper.assertValueEqual(ids.size(), 4, "Number of distinct variables after crafting");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableWithoutLabel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);

        // Create an unlabelled variable card
        ItemStack original = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));

        // Craft the card together with a blank card
        List<ItemStack> cards = craftVariableCopy(helper, original,
                new ItemStack(RegistryEntries.ITEM_VARIABLE.get()), ClickType.PICKUP);

        // Neither card may have gotten a label
        helper.assertValueEqual(cards.size(), 2, "Number of variable cards after crafting");
        for (ItemStack card : cards) {
            helper.assertTrue(labels.getLabel(getVariableFacade(level, card).getId()) == null,
                    "A card unexpectedly has a label");
        }

        helper.succeed();
    }

    /**
     * Craft the given variable cards into a copy, as a player would in a crafting table.
     * @param helper The game test helper.
     * @param withData A variable card holding a variable.
     * @param withoutData A blank variable card.
     * @param clickType How the player takes the result out of the result slot.
     * @return All variable cards the player holds afterwards, including the ones left in the crafting grid.
     */
    protected List<ItemStack> craftVariableCopy(GameTestHelper helper, ItemStack withData, ItemStack withoutData,
                                                ClickType clickType) {
        helper.setBlock(POS, Blocks.CRAFTING_TABLE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            CraftingMenu menu = new CraftingMenu(1, player.getInventory(),
                    ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(POS)));

            // Place both cards in the crafting grid, which makes the game determine the crafting result
            menu.getSlot(1).set(withData);
            menu.getSlot(2).set(withoutData);
            helper.assertTrue(!menu.getSlot(0).getItem().isEmpty(), "No crafting result was produced");

            // Take the crafting result
            menu.clicked(0, 0, clickType, player);

            // Collect all cards, wherever they ended up
            List<ItemStack> cards = Lists.newArrayList();
            collectVariableCards(cards, menu.getCarried());
            for (int i = 1; i < menu.slots.size(); i++) {
                collectVariableCards(cards, menu.getSlot(i).getItem());
            }
            return cards;
        } finally {
            player.getServer().getPlayerList().remove(player);
        }
    }

    protected void collectVariableCards(List<ItemStack> cards, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemVariable) {
            for (int i = 0; i < itemStack.getCount(); i++) {
                cards.add(itemStack);
            }
        }
    }
}
