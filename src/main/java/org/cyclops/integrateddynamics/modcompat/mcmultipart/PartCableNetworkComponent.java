package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.api.tileentity.ITileCable;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * A component for {@link ICableNetwork} at a part.
 * @author rubensworks
 */
public class PartCableNetworkComponent<C extends PartCable & ICableNetwork<IPartNetwork, ICablePathElement>> extends CableNetworkComponent<C> {

    private final PartCable partCable;

    public PartCableNetworkComponent(C partCable) {
        super(partCable);
        this.partCable = partCable;
    }

    protected ITileCable getTile(World world, BlockPos pos) {
        return partCable;
    }

    protected ITileCableNetwork getTileNetwork(World world, BlockPos pos) {
        return partCable;
    }

    @Override
    public void remove(World world, BlockPos pos, EntityPlayer player) {
        partCable.getContainer().removePart(partCable);
        ItemBlockCable.playBreakSound(world, pos, BlockCable.getInstance().getDefaultState());
        if(player == null) {
            ItemStackHelpers.spawnItemStack(world, pos, partCable.getItemStack());
        } else {
            ItemStackHelpers.spawnItemStackToPlayer(world, pos, partCable.getItemStack(), player);
        }
    }
}
