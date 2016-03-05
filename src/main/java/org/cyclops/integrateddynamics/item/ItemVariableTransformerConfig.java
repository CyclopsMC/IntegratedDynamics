package org.cyclops.integrateddynamics.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.List;

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
                "variableTransformer",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return (IConfigurable) new ConfigurableItem(this) {
            @Override
            public String getUnlocalizedName(ItemStack itemStack) {
                return super.getUnlocalizedName(itemStack) + (itemStack.getMetadata() == 0 ? ".output" : ".input");
            }

            @Override
            public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
                super.getSubItems(itemIn, tab, subItems);
                subItems.add(new ItemStack(itemIn, 1, 1));
            }
        }.setHasSubtypes(true).setMaxDamage(0);
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        return super.getModelName(itemStack) + (itemStack.getMetadata() == 0 ? "_output" : "_input");
    }
    
}
