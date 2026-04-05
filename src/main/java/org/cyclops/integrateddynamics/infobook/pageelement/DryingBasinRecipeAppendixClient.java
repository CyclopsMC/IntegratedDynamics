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
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplayDryingBasin;

import java.util.Map;

/**
 * @author rubensworks
 */
public class DryingBasinRecipeAppendixClient extends RecipeAppendixClient<DryingBasinRecipeAppendix> {

    protected static final int SLOT_SIZE = 16;

    protected DryingBasinRecipeAppendixClient(DryingBasinRecipeAppendix sectionAppendix) {
        super(sectionAppendix);
    }

    public void bakeElement(InfoSection infoSection) {
        Map<AdvancedButtonEnum, AdvancedButton> renderItemHolders = getSectionAppendix().getRenderItemHolders();
        IInfoBook infoBook = getSectionAppendix().getInfoBook();
        renderItemHolders.put(DryingBasinRecipeAppendix.INPUT_ITEM, new ItemButton(infoBook));
        renderItemHolders.put(DryingBasinRecipeAppendix.INPUT_FLUID, new FluidButton(infoBook));
        renderItemHolders.put(DryingBasinRecipeAppendix.RESULT_ITEM, new ItemButton(infoBook));
        renderItemHolders.put(DryingBasinRecipeAppendix.RESULT_FLUID, new FluidButton(infoBook));
    }

    @Override
    public void drawElementInner(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        int middle = (width - SLOT_SIZE) / 2;
        gui.drawArrowRight(guiGraphics, x + middle - 3, y + 2);

        // Prepare items
        RecipeDisplayEntry recipeDisplay = getSectionAppendix().getRecipeDisplay();
        if (recipeDisplay == null) {
            return;
        }
        int tick = getTick(gui);
        ContextMap contextMap = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        RecipeDisplayDryingBasin display = ((RecipeDisplayDryingBasin) recipeDisplay.display());
        ItemStack inputItem = prepareItemStacks(display.inputIngredient().resolveForStacks(contextMap), tick);
        FluidStack inputFluid = display.inputFluid();
        ItemStack resultItem = prepareItemStacks(display.outputItem().resolveForStacks(contextMap), tick);
        FluidStack resultFluid = display.outputFluid();

        // Items
        renderItem(gui, guiGraphics, x + DryingBasinRecipeAppendix.SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, DryingBasinRecipeAppendix.INPUT_ITEM);
        renderFluid(gui, guiGraphics, x + DryingBasinRecipeAppendix.SLOT_INPUT_OFFSET_X, y + DryingBasinRecipeAppendix.SLOT_OFFSET_Y, inputFluid, mx, my, DryingBasinRecipeAppendix.INPUT_FLUID);
        renderItem(gui, guiGraphics, x + DryingBasinRecipeAppendix.SLOT_OUTPUT_OFFSET_X, y, resultItem, mx, my, DryingBasinRecipeAppendix.RESULT_ITEM);
        renderFluid(gui, guiGraphics, x + DryingBasinRecipeAppendix.SLOT_OUTPUT_OFFSET_X, y + DryingBasinRecipeAppendix.SLOT_OFFSET_Y, resultFluid, mx, my, DryingBasinRecipeAppendix.RESULT_FLUID);

        renderItem(gui, guiGraphics, x + middle, y, getSectionAppendix().getCrafter(), mx, my, false, null);
    }
}
