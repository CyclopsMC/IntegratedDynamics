package org.cyclops.integrateddynamics.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * A part entity whose block can connect with cables.
 * @author rubensworks
 */
public class BlockEntityCableConnectable extends CyclopsBlockEntity {

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    private final ICable cable;

    public BlockEntityCableConnectable(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
        super(type, blockPos, blockState);
        cable = new CableTile<BlockEntityCableConnectable>(this) {

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

    public EnumFacingMap<Boolean> getConnected() {
        return connected;
    }

    public ICable getCable() {
        return cable;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        connected.clear();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (getLevel() != null && !getLevel().isClientSide) {
            NetworkHelpers.invalidateNetworkElements(getLevel(), getBlockPos(), this);
        }
    }

    public static class Ticker<T extends BlockEntityCableConnectable> extends BlockEntityTickerDelayed<T> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, T blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.getConnected().isEmpty()) {
                blockEntity.getCable().updateConnections();
            }
            NetworkHelpers.revalidateNetworkElements(level, pos);
        }
    }
}
