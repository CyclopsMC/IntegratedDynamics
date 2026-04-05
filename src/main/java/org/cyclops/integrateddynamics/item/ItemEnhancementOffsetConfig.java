package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Config for an offset enhancement.
 * @author rubensworks
 */
public class ItemEnhancementOffsetConfig extends ItemConfigCommon<IModBase> {

    public ItemEnhancementOffsetConfig() {
        super(
                IntegratedDynamics._instance,
                "enhancement_offset",
                (eConfig, properties) -> new ItemEnhancement(ItemEnhancement.Type.OFFSET, properties)
        );
    }

    @Override
    public Collection<Supplier<ItemStack>> getDefaultCreativeTabEntries() {
        return Collections.singleton(() -> {
            ItemStack itemStack = new ItemStack(getInstance());
            ((ItemEnhancement) getInstance()).setEnhancementValue(itemStack, 4);
            return itemStack;
        });
    }
}
