package org.cyclops.integrateddynamics.infobook.pageelement;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.AdvancedButtonEnum;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

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

    public DryingBasinRecipeAppendix(IInfoBook infoBook, RecipeDryingBasin recipe) {
        super(infoBook, recipe);
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
    public void drawElementInner(ScreenInfoBook gui, MatrixStack matrixStack, int x, int y, int width, int height, int page, int mx, int my) {
        int middle = (width - SLOT_SIZE) / 2;
        gui.drawArrowRight(matrixStack, x + middle - 3, y + 2);

        // Prepare items
        int tick = getTick(gui);
        ItemStack inputItem = prepareItemStacks(recipe.getInputIngredient().getMatchingStacks(), tick);
        FluidStack inputFluid = recipe.getInputFluid();
        ItemStack resultItem = prepareItemStack(recipe.getOutputItem(), tick);
        FluidStack resultFluid = recipe.getOutputFluid();

        // Items
        renderItem(gui, matrixStack, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        renderFluid(gui, matrixStack, x + SLOT_INPUT_OFFSET_X, y + SLOT_OFFSET_Y, inputFluid, mx, my, INPUT_FLUID);
        renderItem(gui, matrixStack, x + SLOT_OUTPUT_OFFSET_X, y, resultItem, mx, my, RESULT_ITEM);
        renderFluid(gui, matrixStack, x + SLOT_OUTPUT_OFFSET_X, y + SLOT_OFFSET_Y, resultFluid, mx, my, RESULT_FLUID);

        renderItem(gui, matrixStack, x + middle, y, getCrafter(), mx, my, false, null);
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_DRYING_BASIN);
    }

}
