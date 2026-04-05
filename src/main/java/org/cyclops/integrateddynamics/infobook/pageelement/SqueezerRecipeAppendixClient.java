package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.*;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendixClient;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplaySqueezer;

import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class SqueezerRecipeAppendixClient extends RecipeAppendixClient<SqueezerRecipeAppendix> {

    protected static final int SLOT_SIZE = 16;

    public SqueezerRecipeAppendixClient(SqueezerRecipeAppendix appendix) {
        super(appendix);
    }

    public void bakeElement(InfoSection infoSection) {
        Map<AdvancedButtonEnum, AdvancedButton> renderItemHolders = getSectionAppendix().getRenderItemHolders();
        IInfoBook infoBook = getSectionAppendix().getInfoBook();
        renderItemHolders.put(SqueezerRecipeAppendix.INPUT_ITEM, new ItemButton(infoBook));
        for (AdvancedButtonEnum resultItem : getSectionAppendix().getResultItems()) {
            renderItemHolders.put(resultItem, new ItemButton(infoBook));
        }
        renderItemHolders.put(SqueezerRecipeAppendix.RESULT_FLUID, new FluidButton(infoBook));
    }

    @Override
    public void drawElementInner(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        int middle = (width - SLOT_SIZE) / 2;
        gui.drawArrowRight(guiGraphics, x + middle - 3, y + 2);

        // Prepare items
        RecipeDisplayEntry recipeDisplay = this.getSectionAppendix().getRecipeDisplay();
        if (recipeDisplay == null) {
            return;
        }
        int tick = getTick(gui);
        ContextMap contextMap = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        RecipeDisplaySqueezer display = ((RecipeDisplaySqueezer) recipeDisplay.display());
        ItemStack inputItem = prepareItemStacks(display.inputIngredient().resolveForStacks(contextMap), tick);
        List<ItemStack> outputItems = display.outputItems().stream()
                .map(pair -> pair.getLeft().resolveForFirstStack(contextMap))
                .toList();
        FluidStack outputFluid = display.outputFluid();

        // Items
        renderItem(gui, guiGraphics, x + SqueezerRecipeAppendix.SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, SqueezerRecipeAppendix.INPUT_ITEM);
        int slotOffset = 0;
        for (int i = 0; i < outputItems.size(); i++) {
            renderItem(gui, guiGraphics, x + SqueezerRecipeAppendix.SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputItems.get(i), mx, my, this.getSectionAppendix().getResultItems().get(i),
                    display.outputItems().get(i).getRight());
            slotOffset += SqueezerRecipeAppendix.SLOT_OFFSET_Y;
        }
        if (!outputFluid.isEmpty()) {
            renderFluid(gui, guiGraphics, x + SqueezerRecipeAppendix.SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputFluid, mx, my, SqueezerRecipeAppendix.RESULT_FLUID);
        }

        renderItem(gui, guiGraphics, x + middle, y, this.getSectionAppendix().getCrafter(), mx, my, false, null);
    }
}
