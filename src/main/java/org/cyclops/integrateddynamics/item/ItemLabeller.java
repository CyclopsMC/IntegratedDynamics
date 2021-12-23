package org.cyclops.integrateddynamics.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

import javax.annotation.Nullable;

import net.minecraft.item.Item.Properties;

/**
 * A labeller for variables.
 * @author rubensworks
 */
public class ItemLabeller extends ItemGui {

    public ItemLabeller(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public INamedContainerProvider getContainer(World world, PlayerEntity playerEntity, int itemIndex, Hand hand, ItemStack itemStack) {
        return new NamedContainerProviderItem(itemIndex, hand, itemStack.getHoverName(), ContainerLabeller::new);
    }

    @Override
    public Class<? extends Container> getContainerClass(World world, PlayerEntity playerEntity, ItemStack itemStack) {
        return ContainerLabeller.class;
    }

}
