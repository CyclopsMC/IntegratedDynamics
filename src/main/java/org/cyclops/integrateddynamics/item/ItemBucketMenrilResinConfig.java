package org.cyclops.integrateddynamics.item;

import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for a menril resin bucket.
 * @author rubensworks
 */
public class ItemBucketMenrilResinConfig extends ItemConfig {

    public ItemBucketMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "bucket_menril_resin",
                eConfig -> new BucketItem(() -> RegistryEntries.FLUID_MENRIL_RESIN, new Item.Properties()
                        .containerItem(Items.BUCKET)
                        .maxStackSize(1)
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
