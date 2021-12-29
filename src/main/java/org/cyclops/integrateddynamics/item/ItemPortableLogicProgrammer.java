package org.cyclops.integrateddynamics.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public MenuProvider getContainer(Level world, Player playerEntity, int itemIndex, InteractionHand hand, ItemStack itemStack) {
        return new NamedContainerProviderItem(itemIndex, hand, itemStack.getHoverName(), ContainerLogicProgrammerPortable::new);
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass(Level world, Player playerEntity, ItemStack itemStack) {
        return ContainerLogicProgrammerPortable.class;
    }

}
