package org.cyclops.integrateddynamics.item;

import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for a liquid chorus bucket.
 * @author rubensworks
 */
public class ItemBucketLiquidChorusConfig extends ItemConfig {

    public ItemBucketLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "bucket_liquid_chorus",
                eConfig -> new BucketItem(() -> RegistryEntries.FLUID_LIQUID_CHORUS, new Item.Properties()
                        .containerItem(Items.BUCKET)
                        .maxStackSize(1)
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
