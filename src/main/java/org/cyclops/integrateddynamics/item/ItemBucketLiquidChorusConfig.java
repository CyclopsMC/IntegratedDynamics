package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
