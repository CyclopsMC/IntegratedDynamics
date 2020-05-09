package org.cyclops.integrateddynamics.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Input and Output Variable Transformer.
 * @author rubensworks
 *
 */
public class ItemVariableTransformerConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemVariableTransformerConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemVariableTransformerConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "variable_transformer",
                null,
                null
        );
    }

    @Override
    protected ConfigurableItem initSubInstance() {
        return (ConfigurableItem) new ConfigurableItem(this) {
            @Override
            public String getTranslationKey(ItemStack itemStack) {
                return super.getTranslationKey(itemStack) + (itemStack.getMetadata() == 0 ? ".output" : ".input");
            }

            @Override
            public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
                if (!ItemStackHelpers.isValidCreativeTab(this, tab)) return;
                super.getSubItems(tab, subItems);
                subItems.add(new ItemStack(this, 1, 1));
            }
        }.setHasSubtypes(true).setMaxDamage(0);
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        return super.getModelName(itemStack) + (itemStack.getMetadata() == 0 ? "_output" : "_input");
    }
    
}
