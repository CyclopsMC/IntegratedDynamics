package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.AdvancedButtonEnum;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final List<AdvancedButtonEnum> resultItems;

    public SqueezerRecipeAppendix(IInfoBook infoBook, RecipeHolder<? extends RecipeSqueezer> recipe) {
        super(infoBook, recipe);
        resultItems = Lists.newArrayList();
        for (int i = 0; i < recipe.value().getOutputItems().size(); i++) {
            resultItems.add(AdvancedButtonEnum.create());
        }
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    protected int getHeightInner() {
        return (recipe.value().getOutputFluid().isPresent() ? SLOT_OFFSET_Y : 0) + resultItems.size() * SLOT_OFFSET_Y - 3;
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.squeezer";
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
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
        int tick = getTick(gui);
        ItemStack inputItem = recipe.value().getInputIngredient() == null ? null : prepareItemStacks(recipe.value().getInputIngredient().getItems(), tick);
        List<ItemStack> outputItems = recipe.value().getOutputItems().stream()
                .map(RecipeSqueezer.IngredientChance::getIngredientFirst)
                .collect(Collectors.toList());
        Optional<FluidStack> outputFluid = recipe.value().getOutputFluid();

        // Items
        renderItem(gui, guiGraphics, x + SLOT_INPUT_OFFSET_X, y, inputItem, mx, my, INPUT_ITEM);
        int slotOffset = 0;
        for (int i = 0; i < outputItems.size(); i++) {
            renderItem(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputItems.get(i), mx, my, resultItems.get(i),
                    recipe.value().getOutputItems().get(i).getChance());
            slotOffset += SLOT_OFFSET_Y;
        }
        if (outputFluid.isPresent()) {
            renderFluid(gui, guiGraphics, x + SLOT_OUTPUT_OFFSET_X, y + slotOffset, outputFluid.get(), mx, my, RESULT_FLUID);
        }

        renderItem(gui, guiGraphics, x + middle, y, getCrafter(), mx, my, false, null);
    }

    protected ItemStack getCrafter()  {
        return new ItemStack(RegistryEntries.BLOCK_SQUEEZER.get());
    }


}
