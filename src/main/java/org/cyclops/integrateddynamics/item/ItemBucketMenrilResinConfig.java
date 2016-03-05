package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockFluidClassic;
import org.cyclops.cyclopscore.config.configurable.ConfigurableFluid;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItemBucket;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemBucketConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockFluidMenrilResin;
import org.cyclops.integrateddynamics.fluid.FluidMenrilResin;

/**
 * Config for the Menril Resin Bucket.
 * @author rubensworks
 *
 */
public class ItemBucketMenrilResinConfig extends ItemBucketConfig {

    /**
     * The unique instance.
     */
    public static ItemBucketMenrilResinConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBucketMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "bucketMenrilResin",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableItemBucket(this, BlockFluidMenrilResin.getInstance());
    }

    @Override
    public ConfigurableFluid getFluidInstance() {
        return FluidMenrilResin.getInstance();
    }

    @Override
    public ConfigurableBlockFluidClassic getFluidBlockInstance() {
        return BlockFluidMenrilResin.getInstance();
    }
    
    @Override
    public boolean isDisableable() {
        return false;
    }
    
}
