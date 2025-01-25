package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
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
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplaySqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import java.util.List;
import java.util.function.Supplier;

/**
 * Squeezer recipes.
 * @author rubensworks
 */
public class SqueezerRecipeAppendix extends RecipeAppendix<RecipeSqueezer> {

    private static final int SLOT_INPUT_OFFSET_X = 16;
    private static final int SLOT_OFFSET_Y = 23;
    private static final int SLOT_OUTPUT_OFFSET_X = 68;

    private static final AdvancedButtonEnum INPUT_ITEM = AdvancedButtonEnum.create();
    private static final AdvancedButtonEnum RESULT_FLUID = AdvancedButtonEnum.create();

    private List<AdvancedButtonEnum> resultItems;

    public SqueezerRecipeAppendix(IInfoBook infoBook, Supplier<RecipeDisplayEntry> recipeDisplaySupplier) {
        super(infoBook, recipeDisplaySupplier);
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    protected int getHeightInner() {
        RecipeDisplayEntry recipeDisplay = getRecipeDisplay();
        if (recipeDisplay == null) {
            return 10;
        }
        return (!((RecipeDisplaySqueezer) recipeDisplay.display()).outputFluid().isEmpty() ? SLOT_OFFSET_Y : 0) + resultItems.size() * SLOT_OFFSET_Y - 3;
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.squeezer";
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        resultItems = Lists.newArrayList();

        RecipeDisplayEntry recipeDisplay = getRecipeDisplay();
        if (recipeDisplay == null) {
            return;
        }

        RecipeDisplaySqueezer display = ((RecipeDisplaySqueezer) recipeDisplay.display());
        for (int i = 0; i < display.outputItems().size(); i++) {
            resultItems.add(AdvancedButtonEnum.create());
        }

        renderItemHolders.put(INPUT_ITEM, new ItemButton(getInfoBook()));
        for (AdvancedButtonEnum resultItem : resultItems) {
            renderItemHolders.put(resultItem, new ItemButton(getInfoBook()));
        }
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
        RecipeDisplaySqueezer display = ((RecipeDisplaySqueezer) recipeDisplay.display());
        ItemStack inputItem = prepareItemStacks(display.inputIngredient().resolveForStacks(contextMap), tick);
        List<ItemStack> outputItems = display.outputItems().stream()
                .map(pair -> pair.getLeft().resolveForFirstStack(contextMap))
                .toList();
        FluidStack outputFluid = display.outputFluid();

        // Items
        renderItem(gui, guiGraphics, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        int slotOffset = 0;
        for (int i = 0; i < outputItems.size(); i++) {
            renderItem(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputItems.get(i), mx, my, resultItems.get(i),
                    display.outputItems().get(i).getRight());
            slotOffset += SLOT_OFFSET_Y;
        }
        if (!outputFluid.isEmpty()) {
            renderFluid(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputFluid, mx, my, RESULT_FLUID);
        }

        renderItem(gui, guiGraphics, x + middle, y, getCrafter(), mx, my, false, null);
    }

    protected ItemStack getCrafter()  {
        return new ItemStack(RegistryEntries.BLOCK_SQUEEZER.get());
    }


}
