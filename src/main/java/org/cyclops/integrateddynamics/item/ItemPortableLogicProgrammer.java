package org.cyclops.integrateddynamics.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerPortable;

import javax.annotation.Nullable;

/**
 * A portable logic programmer.
 * @author rubensworks
 */
public class ItemPortableLogicProgrammer extends ItemGui {

    public ItemPortableLogicProgrammer(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public INamedContainerProvider getContainer(World world, PlayerEntity playerEntity, int itemIndex, Hand hand, ItemStack itemStack) {
        return new NamedContainerProviderItem(itemIndex, hand, itemStack.getDisplayName(), ContainerLogicProgrammerPortable::new);
    }

    @Override
    public Class<? extends Container> getContainerClass(World world, PlayerEntity playerEntity, ItemStack itemStack) {
        return ContainerLogicProgrammerPortable.class;
    }

}
