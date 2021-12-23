package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.ITickingTile;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.TickingTileComponent;

/**
 * A part entity whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectable extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    @Getter
    private final ICable cable;

    public TileCableConnectable(TileEntityType<?> type) {
        super(type);
        cable = new CableTile<TileCableConnectable>(this) {

            @Override
            protected boolean isForceDisconnectable() {
                return false;
            }

            @Override
            protected EnumFacingMap<Boolean> getForceDisconnected() {
                return null;
            }

            @Override
            protected EnumFacingMap<Boolean> getConnected() {
                return tile.connected;
            }
        };
        addCapabilityInternal(CableConfig.CAPABILITY, LazyOptional.of(() -> cable));
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, LazyOptional.of(NetworkCarrierDefault::new));
        addCapabilityInternal(PathElementConfig.CAPABILITY, LazyOptional.of(() -> new PathElementTile<>(this, cable)));
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        connected.clear();
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
        if (getLevel() != null && !getLevel().isClientSide) {
            NetworkHelpers.revalidateNetworkElements(getLevel(), getBlockPos());
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (getLevel() != null && !getLevel().isClientSide) {
            NetworkHelpers.invalidateNetworkElements(getLevel(), getBlockPos(), this);
        }
    }
}
