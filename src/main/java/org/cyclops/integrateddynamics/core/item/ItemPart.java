package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.parts.IPart;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.IPartState;

/**
 * An item that can place parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemPart<P extends IPart<P, S>, S extends IPartState<P>> extends ConfigurableItem {

    private final IPart<P, S> part;

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     * @param part    The part this item will place.
     */
    public ItemPart(ExtendedConfig eConfig, IPart<P, S> part) {
        super(eConfig);
        this.part = part;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        if(world.getTileEntity(pos) instanceof IPartContainer) {
            IPartContainer partContainer = (IPartContainer) world.getTileEntity(pos);
            if(!partContainer.hasPart(side)) {
                partContainer.setPart(side, getPart());
                System.out.println("Setting part " + getPart());
            } else {
                System.out.println("Side occupied!");
            }
            return true;
        } else {
            return super.onItemUse(stack, playerIn, world, pos, side, hitX, hitY, hitZ);
        }
    }

}
