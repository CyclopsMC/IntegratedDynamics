package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.AdvancedButtonEnum;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplaySqueezer;

import java.util.List;
import java.util.function.Supplier;

/**
 * Squeezer recipes.
 * @author rubensworks
 */
public class SqueezerRecipeAppendix extends RecipeAppendix<SqueezerRecipeAppendixClient> {

    public static final int SLOT_INPUT_OFFSET_X = 16;
    public static final int SLOT_OFFSET_Y = 23;
    public static final int SLOT_OUTPUT_OFFSET_X = 68;

    public static final AdvancedButtonEnum INPUT_ITEM = AdvancedButtonEnum.create();
    public static final AdvancedButtonEnum RESULT_FLUID = AdvancedButtonEnum.create();

    private List<AdvancedButtonEnum> resultItems;

    public SqueezerRecipeAppendix(IInfoBook infoBook, Supplier<RecipeDisplayEntry> recipeDisplaySupplier) throws InfoBookParser.InvalidAppendixException {
        super(infoBook, recipeDisplaySupplier);
    }

    public List<AdvancedButtonEnum> getResultItems() {
        return resultItems;
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    public SqueezerRecipeAppendixClient constructSectionAppendixClient() throws InfoBookParser.InvalidAppendixException {
        return new SqueezerRecipeAppendixClient(this);
    }

    @Override
    protected int getHeightInner() {
        RecipeDisplayEntry recipeDisplay = getRecipeDisplay();
        if (recipeDisplay == null) {
            return 10;
        }
        return (!((RecipeDisplaySqueezer) recipeDisplay.display()).outputFluid().isEmpty() ? SLOT_OFFSET_Y : 0) + ((RecipeDisplaySqueezer) recipeDisplay.display()).outputItems().size() * SLOT_OFFSET_Y - 3;
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

        if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            getSectionAppendixClient().bakeElement(infoSection);
        }
        super.bakeElement(infoSection);
    }

    protected ItemStack getCrafter()  {
        return new ItemStack(RegistryEntries.BLOCK_SQUEEZER.get());
    }


}
