package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for a liquid chorus bucket.
 * @author rubensworks
 */
public class ItemBucketLiquidChorusConfig extends ItemConfigCommon<IModBase> {

    public ItemBucketLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "bucket_liquid_chorus",
                (eConfig, properties) -> new BucketItem(RegistryEntries.FLUID_LIQUID_CHORUS.get(), properties
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1))
        );
    }

}
