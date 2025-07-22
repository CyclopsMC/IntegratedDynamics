package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.AdvancedButtonEnum;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.RecipeAppendix;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.function.Supplier;

/**
 * Drying basin recipes.
 * @author rubensworks
 */
public class DryingBasinRecipeAppendix extends RecipeAppendix<DryingBasinRecipeAppendixClient> {

    public static final int SLOT_INPUT_OFFSET_X = 16;
    public static final int SLOT_OFFSET_Y = 23;
    public static final int SLOT_OUTPUT_OFFSET_X = 68;

    public static final AdvancedButtonEnum INPUT_ITEM = AdvancedButtonEnum.create();
    public static final AdvancedButtonEnum INPUT_FLUID = AdvancedButtonEnum.create();
    public static final AdvancedButtonEnum RESULT_ITEM = AdvancedButtonEnum.create();
    public static final AdvancedButtonEnum RESULT_FLUID = AdvancedButtonEnum.create();

    public DryingBasinRecipeAppendix(IInfoBook infoBook, Supplier<RecipeDisplayEntry> recipeDisplaySupplier) throws InfoBookParser.InvalidAppendixException {
        super(infoBook, recipeDisplaySupplier);
    }

    @Override
    protected int getWidth() {
        return SLOT_OUTPUT_OFFSET_X + 32;
    }

    @Override
    public DryingBasinRecipeAppendixClient constructSectionAppendixClient() throws InfoBookParser.InvalidAppendixException {
        return new DryingBasinRecipeAppendixClient(this);
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
        if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            getSectionAppendixClient().bakeElement(infoSection);
        }
        super.bakeElement(infoSection);
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_DRYING_BASIN.get());
    }

}
