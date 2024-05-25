package org.cyclops.integrateddynamics.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventoryState;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import javax.annotation.Nullable;

/**
 * A part entity with inventory whose block can connect with cables.
 * @author rubensworks
 */
public class BlockEntityCableConnectableInventory extends CyclopsBlockEntity {

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    private final ICable cable;
    private final INetworkCarrier networkCarrier;
    private final SimpleInventory inventory;

    public BlockEntityCableConnectableInventory(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int inventorySize, int stackSize) {
        super(type, blockPos, blockState);
        inventory = createInventory(inventorySize, stackSize);
        cable = new CableTile<BlockEntityCableConnectableInventory>(this) {

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
        networkCarrier = new NetworkCarrierDefault();
    }

    public static void registerCableConnectableInventoryCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityCableConnectableInventory> blockEntityType) {
        event.registerBlockEntity(
                Capabilities.Cable.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getCable()
        );
        event.registerBlockEntity(
                Capabilities.NetworkCarrier.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getNetworkCarrier()
        );
        event.registerBlockEntity(
                Capabilities.PathElement.BLOCK,
                blockEntityType,
                (blockEntity, context) -> new PathElementTile<>(blockEntity, blockEntity.getCable())
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getInventory().getItemHandler()
        );
        event.registerBlockEntity(
                org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK,
                blockEntityType,
                (blockEntity, context) -> new SimpleInventoryState(blockEntity.getInventory())
        );
    }

    public EnumFacingMap<Boolean> getConnected() {
        return connected;
    }

    public ICable getCable() {
        return cable;
    }

    public INetworkCarrier getNetworkCarrier() {
        return networkCarrier;
    }

    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize);
    }


    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        connected.clear();
        inventory.readFromNBT(tag, "inventory");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        inventory.writeToNBT(tag, "inventory");
        super.saveAdditional(tag);
    }

    /**
     * Called after the network has been fully initialized
     */
    public void afterNetworkReAlive() {

    }

    @Nullable
    public INetwork getNetwork() {
        return this.networkCarrier.getNetwork();
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (getLevel() != null && !getLevel().isClientSide) {
            NetworkHelpers.invalidateNetworkElements(getLevel(), getBlockPos(), this);
        }
    }

    public static class Ticker<T extends BlockEntityCableConnectableInventory> extends BlockEntityTickerDelayed<T> {
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
