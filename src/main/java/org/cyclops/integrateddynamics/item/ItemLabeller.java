package org.cyclops.integrateddynamics.item;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

import javax.annotation.Nullable;

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
    public MenuProvider getContainer(Level world, Player playerEntity, ItemLocation itemLocation) {
        return new NamedContainerProviderItem(itemLocation, itemLocation.getItemStack(playerEntity).getHoverName(), ContainerLabeller::new);
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass(Level world, Player playerEntity, ItemStack itemStack) {
        return ContainerLabeller.class;
    }

}
