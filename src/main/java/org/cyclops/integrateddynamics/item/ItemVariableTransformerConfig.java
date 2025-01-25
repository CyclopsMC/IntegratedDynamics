package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Input and Output Variable Transformer.
 * @author rubensworks
 *
 */
public class ItemVariableTransformerConfig extends ItemConfigCommon<IModBase> {

    public ItemVariableTransformerConfig(boolean input) {
        super(
                IntegratedDynamics._instance,
                "variable_transformer_" + (input ? "input" : "output"),
                (eConfig, properties) -> new Item(properties)
        );
    }

}
