package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.jetbrains.annotations.Nullable;

/**
 * Config for a wrench.
 * @author rubensworks
 */
public class ItemWrenchConfig extends ItemConfigCommon<IntegratedDynamics> {

    @Nullable
    private ItemWrenchClientConfig clientConfig;

    public ItemWrenchConfig() {
        super(
                IntegratedDynamics._instance,
                "wrench",
                eConfig -> new ItemWrench(new Item.Properties()
                        .stacksTo(1))
        );
    }

    @Override
    @Nullable
    public ItemClientConfig<IntegratedDynamics> getItemClientConfig() {
        if (this.clientConfig == null && getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            this.clientConfig = new ItemWrenchClientConfig(this);
        }
        return this.clientConfig;
    }

}
