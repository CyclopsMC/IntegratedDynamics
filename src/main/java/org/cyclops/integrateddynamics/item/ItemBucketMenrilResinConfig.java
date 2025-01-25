package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for a menril resin bucket.
 * @author rubensworks
 */
public class ItemBucketMenrilResinConfig extends ItemConfigCommon<IModBase> {

    public ItemBucketMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "bucket_menril_resin",
                (eConfig, properties) -> new BucketItem(RegistryEntries.FLUID_MENRIL_RESIN.get(), properties
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1))
        );
    }

}
