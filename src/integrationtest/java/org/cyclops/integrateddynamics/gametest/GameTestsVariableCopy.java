package org.cyclops.integrateddynamics.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableForValue;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getVariableFacade;

/**
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsVariableCopy {

    public static final String TEMPLATE_EMPTY = "empty10";

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableKeepsLabelOnBothCards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);

        // Create a variable card, and label it
        ItemStack original = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));
        labels.put(getVariableFacade(level, original).getId(), "MyLabel");

        // Craft the labelled card together with a blank card
        CraftResult craftResult = craftVariableCopy(helper, original, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));

        // The crafted card must be labelled
        IVariableFacade craftedFacade = getVariableFacade(level, craftResult.crafted());
        helper.assertTrue(craftedFacade.isValid(), "Crafted card has no valid variable");
        helper.assertTrue("MyLabel".equals(labels.getLabel(craftedFacade.getId())), "Crafted card has no label");
        helper.assertValueEqual(craftResult.crafted().getHoverName().getString(), "MyLabel", "Crafted card name");

        // The card that remains in the crafting grid must be labelled as well
        IVariableFacade remainingFacade = getVariableFacade(level, craftResult.remaining());
        helper.assertTrue(remainingFacade.isValid(), "Remaining card has no valid variable");
        helper.assertTrue(remainingFacade.getId() != craftedFacade.getId(), "Both cards refer to the same variable");
        helper.assertTrue("MyLabel".equals(labels.getLabel(remainingFacade.getId())), "Remaining card has no label");
        helper.assertValueEqual(craftResult.remaining().getHoverName().getString(), "MyLabel", "Remaining card name");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCopyVariableWithoutLabel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);

        // Create an unlabelled variable card
        ItemStack original = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));

        // Craft the card together with a blank card
        CraftResult craftResult = craftVariableCopy(helper, original, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));

        // Neither card may have gotten a label
        helper.assertTrue(labels.getLabel(getVariableFacade(level, craftResult.crafted()).getId()) == null,
                "Crafted card unexpectedly has a label");
        helper.assertTrue(labels.getLabel(getVariableFacade(level, craftResult.remaining()).getId()) == null,
                "Remaining card unexpectedly has a label");

        helper.succeed();
    }

    /**
     * Craft the given variable cards into a copy, as a player would in a crafting table.
     * @param helper The game test helper.
     * @param withData A variable card holding a variable.
     * @param withoutData A blank variable card.
     * @return The crafted card, and the card that was left behind in the crafting grid.
     */
    protected CraftResult craftVariableCopy(GameTestHelper helper, ItemStack withData, ItemStack withoutData) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CraftingMenu menu = new CraftingMenu(0, player.getInventory());

        // Place both cards in the crafting grid
        menu.getSlot(1).set(withData);
        menu.getSlot(2).set(withoutData);

        // Determine the crafting result, like the server does when the crafting grid changes
        CraftingContainer craftSlots = (CraftingContainer) menu.getSlot(1).container;
        CraftingInput craftingInput = craftSlots.asCraftInput();
        RecipeHolder<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingInput, level)
                .orElse(null);
        helper.assertTrue(recipe != null, "No crafting recipe matched");
        menu.getSlot(0).set(recipe.value().assemble(craftingInput, level.registryAccess()));

        // Take the crafting result
        ItemStack crafted = menu.getSlot(0).getItem().copy();
        helper.assertTrue(!crafted.isEmpty(), "No crafting result was produced");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);

        return new CraftResult(crafted, firstNonEmpty(menu.getSlot(1).getItem(), menu.getSlot(2).getItem()));
    }

    protected ItemStack firstNonEmpty(ItemStack... itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (!itemStack.isEmpty()) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    protected record CraftResult(ItemStack crafted, ItemStack remaining) {
    }
}
