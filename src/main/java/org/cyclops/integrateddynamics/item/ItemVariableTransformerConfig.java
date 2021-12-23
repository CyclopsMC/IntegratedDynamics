package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Input and Output Variable Transformer.
 * @author rubensworks
 *
 */
public class ItemVariableTransformerConfig extends ItemConfig {

    public ItemVariableTransformerConfig(boolean input) {
        super(
                IntegratedDynamics._instance,
                "variable_transformer_" + (input ? "input" : "output"),
                eConfig -> new Item(new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }
    
}
