package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.AdvancedButtonEnum;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplayDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

import java.util.function.Supplier;

/**
 * Drying basin recipes.
 * @author rubensworks
 */
public class DryingBasinRecipeAppendix extends RecipeAppendix<RecipeDryingBasin> {

    private static final int SLOT_INPUT_OFFSET_X = 16;
    private static final int SLOT_OFFSET_Y = 23;
    private static final int SLOT_OUTPUT_OFFSET_X = 68;

    private static final AdvancedButtonEnum INPUT_ITEM = AdvancedButtonEnum.create();
    private static final AdvancedButtonEnum INPUT_FLUID = AdvancedButtonEnum.create();
    private static final AdvancedButtonEnum RESULT_ITEM = AdvancedButtonEnum.create();
    private static final AdvancedButtonEnum RESULT_FLUID = AdvancedButtonEnum.create();

    public DryingBasinRecipeAppendix(IInfoBook infoBook, Supplier<RecipeDisplayEntry> recipeDisplaySupplier) {
        super(infoBook, recipeDisplaySupplier);
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    protected int getHeightInner() {
        return 42;
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.drying_basin";
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        renderItemHolders.put(INPUT_ITEM, new ItemButton(getInfoBook()));
        renderItemHolders.put(INPUT_FLUID, new FluidButton(getInfoBook()));
        renderItemHolders.put(RESULT_ITEM, new ItemButton(getInfoBook()));
        renderItemHolders.put(RESULT_FLUID, new FluidButton(getInfoBook()));
        super.bakeElement(infoSection);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawElementInner(ScreenInfoBook gui, GuiGraphics guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        int middle = (width - SLOT_SIZE) / 2;
        gui.drawArrowRight(guiGraphics, x + middle - 3, y + 2);

        // Prepare items
        RecipeDisplayEntry recipeDisplay = getRecipeDisplay();
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
        renderItem(gui, guiGraphics, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        renderFluid(gui, guiGraphics, x + SLOT_INPUT_OFFSET_X, y + SLOT_OFFSET_Y, inputFluid, mx, my, INPUT_FLUID);
        renderItem(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y, resultItem, mx, my, RESULT_ITEM);
        renderFluid(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y + SLOT_OFFSET_Y, resultFluid, mx, my, RESULT_FLUID);

        renderItem(gui, guiGraphics, x + middle, y, getCrafter(), mx, my, false, null);
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_DRYING_BASIN.get());
    }

}
