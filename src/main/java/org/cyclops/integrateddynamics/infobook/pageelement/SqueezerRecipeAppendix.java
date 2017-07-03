package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.AdvancedButton;
import org.cyclops.cyclopscore.infobook.GuiInfoBook;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

/**
 * Squeezer recipes.
 * @author rubensworks
 */
public class SqueezerRecipeAppendix extends RecipeAppendix<IRecipe<IngredientRecipeComponent, IngredientAndFluidStackRecipeComponent, DummyPropertiesComponent>> {

    private static final int SLOT_INPUT_OFFSET_X = 16;
    private static final int SLOT_OFFSET_Y = 23;
    private static final int SLOT_OUTPUT_OFFSET_X = 68;

    private static final AdvancedButton.Enum INPUT_ITEM = AdvancedButton.Enum.create();
    private static final AdvancedButton.Enum RESULT_ITEM = AdvancedButton.Enum.create();
    private static final AdvancedButton.Enum RESULT_FLUID = AdvancedButton.Enum.create();

    public SqueezerRecipeAppendix(IInfoBook infoBook, IRecipe<IngredientRecipeComponent, IngredientAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) {
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
        return "tile.blocks.integrateddynamics.squeezer.name";
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        renderItemHolders.put(INPUT_ITEM, new ItemButton(getInfoBook()));
        renderItemHolders.put(RESULT_ITEM, new ItemButton(getInfoBook()));
        renderItemHolders.put(RESULT_FLUID, new FluidButton(getInfoBook()));
        super.bakeElement(infoSection);
    }

    @Override
    public void drawElementInner(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my) {
        int middle = (width - SLOT_SIZE) / 2;
        gui.drawArrowRight(x + middle - 3, y + 2);

        // Prepare items
        int tick = getTick(gui);
        ItemStack inputItem = recipe.getInput().getIngredient() == null ? null : prepareItemStacks(recipe.getInput().getItemStacks(), tick);
        ItemStack resultItem = recipe.getOutput().getIngredient() == null ? null : prepareItemStacks(recipe.getOutput().getItemStacks(), tick);
        FluidStack resultFluid = recipe.getOutput().getFluidStack();

        // Items
        renderItem(gui, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        renderItem(gui, x + SLOT_OUTPUT_OFFSET_X, y, resultItem, mx, my, RESULT_ITEM);
        renderFluid(gui, x + SLOT_OUTPUT_OFFSET_X, y + SLOT_OFFSET_Y, resultFluid, mx, my, RESULT_FLUID);

        renderItem(gui, x + middle, y, new ItemStack(BlockSqueezer.getInstance()), mx, my, false, null);
    }

}
