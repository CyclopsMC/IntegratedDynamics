package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
