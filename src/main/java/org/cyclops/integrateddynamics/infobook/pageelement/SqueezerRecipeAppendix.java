package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.AdvancedButton;
import org.cyclops.cyclopscore.infobook.GuiInfoBook;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Squeezer recipes.
 * @author rubensworks
 */
public class SqueezerRecipeAppendix extends RecipeAppendix<IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>> {

    private static final int SLOT_INPUT_OFFSET_X = 16;
    private static final int SLOT_OFFSET_Y = 23;
    private static final int SLOT_OUTPUT_OFFSET_X = 68;

    private static final AdvancedButton.Enum INPUT_ITEM = AdvancedButton.Enum.create();
    private static final AdvancedButton.Enum RESULT_FLUID = AdvancedButton.Enum.create();

    private final List<AdvancedButton.Enum> resultItems;

    public SqueezerRecipeAppendix(IInfoBook infoBook, IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) {
        super(infoBook, recipe);
        resultItems = Lists.newArrayList();
        for (int i = 0; i < recipe.getOutput().getIngredients().size(); i++) {
            resultItems.add(AdvancedButton.Enum.create());
        }
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    protected int getHeightInner() {
        return 19 + resultItems.size() * SLOT_OFFSET_Y;
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "tile.blocks.integrateddynamics.squeezer.name";
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        renderItemHolders.put(INPUT_ITEM, new ItemButton(getInfoBook()));
        for (AdvancedButton.Enum resultItem : resultItems) {
            renderItemHolders.put(resultItem, new ItemButton(getInfoBook()));
        }
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
        List<ItemStack> outputItems = recipe.getOutput().getSubIngredientComponents().stream()
                .map(component -> component.getIngredient() == null ? null : prepareItemStacks(component.getItemStacks(), tick))
                .collect(Collectors.toList());
        FluidStack outputFluid = recipe.getOutput().getFluidStack();

        // Items
        renderItem(gui, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        int slotOffset = 0;
        for (int i = 0; i < outputItems.size(); i++) {
            renderItem(gui, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputItems.get(i), mx, my, resultItems.get(i),
                    recipe.getOutput().getSubIngredientComponents().get(i).getChance());
            slotOffset += SLOT_OFFSET_Y;
        }
        renderFluid(gui, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputFluid, mx, my, RESULT_FLUID);

        renderItem(gui, x + middle, y, new ItemStack(BlockSqueezer.getInstance()), mx, my, false, null);
    }

}
