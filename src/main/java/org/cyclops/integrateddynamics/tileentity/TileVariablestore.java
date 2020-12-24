package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileVariablestore extends TileCableConnectableInventory
        implements IDirtyMarkListener, INetworkEventListener<VariablestoreNetworkElement>, INamedContainerProvider {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int INVENTORY_SIZE = ROWS * COLS;

    private final IVariableContainer variableContainer;

    private boolean shouldSendUpdateEvent = false;

    public TileVariablestore() {
        super(RegistryEntries.TILE_ENTITY_VARIABLE_STORE, TileVariablestore.INVENTORY_SIZE, 1);
        getInventory().addDirtyMarkListener(this);

        addCapabilityInternal(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, LazyOptional.of(() -> getInventory().getItemHandler()));
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new VariablestoreNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        variableContainer = new VariableContainerDefault();
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, LazyOptional.of(() -> variableContainer));
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
                return super.isItemValidForSlot(slot, itemStack)
                        && (itemStack.isEmpty() || itemStack.getCapability(VariableFacadeHolderConfig.CAPABILITY, null).isPresent());
            }
        };
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        shouldSendUpdateEvent = true;
    }

    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        variableContainer.refreshVariables(getNetwork(), getInventory(), sendVariablesUpdateEvent);
    }

    @Override
    public void onDirty() {
        if(!world.isRemote()) {
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
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (shouldSendUpdateEvent && getNetwork() != null) {
            shouldSendUpdateEvent = false;
            refreshVariables(true);
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
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerVariablestore(id, playerInventory, this.getInventory());
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.integrateddynamics.variablestore");
    }
}
