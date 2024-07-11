package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class BlockEntityVariablestore extends BlockEntityCableConnectableInventory
        implements IDirtyMarkListener, INetworkEventListener<VariablestoreNetworkElement>, MenuProvider {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int INVENTORY_SIZE = ROWS * COLS;

    private final IVariableContainer variableContainer;

    private boolean shouldSendUpdateEvent = false;

    public BlockEntityVariablestore(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_VARIABLE_STORE.get(), blockPos, blockState, BlockEntityVariablestore.INVENTORY_SIZE, 1);
        getInventory().addDirtyMarkListener(this);
        variableContainer = new VariableContainerDefault();
    }

    public static class CapabilityRegistrar extends BlockEntityCableConnectableInventory.CapabilityRegistrar<BlockEntityVariablestore> {
        public CapabilityRegistrar(Supplier<BlockEntityType<? extends BlockEntityVariablestore>> blockEntityType) {
            super(blockEntityType);
        }

        @Override
        public void populate() {
            super.populate();

            add(
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    (blockEntity, context) -> blockEntity.getInventory().getItemHandler()
            );
            add(
                    Capabilities.NetworkElementProvider.BLOCK,
                    (blockEntity, context) -> blockEntity.getNetworkElementProvider()
            );
            add(
                    Capabilities.VariableContainer.BLOCK,
                    (blockEntity, context) -> blockEntity.getVariableContainer()
            );
        }
    }

    @Override
    public INetworkElementProvider getNetworkElementProvider() {
        return new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new VariablestoreNetworkElement(DimPos.of(world, blockPos));
            }
        };
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack itemStack) {
                return super.canPlaceItem(slot, itemStack)
                        && (itemStack.isEmpty() || itemStack.getCapability(Capabilities.VariableFacade.ITEM) != null);
            }
        };
    }

    public IVariableContainer getVariableContainer() {
        return variableContainer;
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider provider) {
        super.read(tag, provider);
        shouldSendUpdateEvent = true;
    }

    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        variableContainer.refreshVariables(getNetwork(), getInventory(), sendVariablesUpdateEvent, ValueDeseralizationContext.of(getLevel()));
    }

    @Override
    public void onDirty() {
        if(!level.isClientSide()) {
            refreshVariables(true);
        }
    }

    // Make sure that when this TE is loaded, and after the network has been set,
    // that we trigger a variable update event in the network.

    @Override
    public void onLoad() {
        super.onLoad();
        if(!MinecraftHelpers.isClientSide()) {
            shouldSendUpdateEvent = true;
        }
    }

    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Sets.<Class<? extends INetworkEvent>>newHashSet(VariableContentsUpdatedEvent.class);
    }

    @Override
    public void onEvent(INetworkEvent event, VariablestoreNetworkElement networkElement) {
        if(event instanceof VariableContentsUpdatedEvent) {
            refreshVariables(false);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerVariablestore(id, playerInventory, this.getInventory());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integrateddynamics.variablestore");
    }

    public static class Ticker extends BlockEntityCableConnectableInventory.Ticker<BlockEntityVariablestore> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityVariablestore blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.shouldSendUpdateEvent && blockEntity.getNetwork() != null) {
                blockEntity.shouldSendUpdateEvent = false;
                blockEntity.refreshVariables(true);
            }
        }
    }
}
