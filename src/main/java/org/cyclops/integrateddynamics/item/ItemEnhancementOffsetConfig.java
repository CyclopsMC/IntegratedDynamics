package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.List;

/**
 * Config for an offset enhancement.
 * @author rubensworks
 */
public class ItemEnhancementOffsetConfig extends ItemConfig {

    public ItemEnhancementOffsetConfig() {
        super(
                IntegratedDynamics._instance,
                "enhancement_offset",
                eConfig -> new ItemEnhancement(ItemEnhancement.Type.OFFSET, new Item.Properties())
        );
    }

    @Override
    protected Collection<ItemStack> getDefaultCreativeTabEntries() {
        List<ItemStack> itemStacks = Lists.newArrayList();
        ItemStack itemStack = new ItemStack(getInstance());
        ((ItemEnhancement) getInstance()).setEnhancementValue(itemStack, 4);
        itemStacks.add(itemStack);
        return itemStacks;
    }
}
