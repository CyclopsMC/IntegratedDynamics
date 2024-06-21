package org.cyclops.integrateddynamics.blockentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityActiveVariableBase;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;
import org.cyclops.integrateddynamics.network.ProxyNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A part entity for the variable proxy.
 * @author rubensworks
 */
public class BlockEntityProxy extends BlockEntityActiveVariableBase<ProxyNetworkElement> implements MenuProvider {

    public static final int INVENTORY_SIZE = 3;
    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    public static final String GLOBALCOUNTER_KEY = "proxy";

    @NBTPersist
    @Getter
    @Setter
    private int proxyId = -1;

    @Setter
    private Player lastPlayer = null;
    private boolean writeVariable;

    public BlockEntityProxy(BlockPos blockPos, BlockState blockState) {
        this(RegistryEntries.BLOCK_ENTITY_PROXY.get(), blockPos, blockState, BlockEntityProxy.INVENTORY_SIZE);
    }

    public BlockEntityProxy(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int inventorySize) {
        super(type, blockPos, blockState, inventorySize);
    }

    public static class CapabilityRegistrar extends BlockEntityActiveVariableBase.CapabilityRegistrar<BlockEntityProxy> {
        public CapabilityRegistrar(Supplier<BlockEntityType<? extends BlockEntityProxy>> blockEntityType) {
            super(blockEntityType);
        }

        @Override
        public void populate() {
            super.populate();

            add(
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    (blockEntity, direction) -> {
                        int slot = -1;
                        switch (direction) {
                            case DOWN ->  slot = SLOT_WRITE_OUT;
                            case UP ->    slot = SLOT_WRITE_IN;
                            case NORTH -> slot = SLOT_READ;
                            case SOUTH -> slot = SLOT_READ;
                            case WEST ->  slot = SLOT_READ;
                            case EAST ->  slot = SLOT_READ;
                        }
                        return new ItemHandlerSlotMasked(blockEntity.getInventory(), slot);
                    }
            );
            add(
                    Capabilities.NetworkElementProvider.BLOCK,
                    (blockEntity, direction) -> blockEntity.getNetworkElementProvider()
            );
        }
    }

    @Override
    public INetworkElementProvider getNetworkElementProvider() {
        return new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new ProxyNetworkElement(DimPos.of(world, blockPos));
            }
        };
    }

    public boolean isWriteVariable() {
        return writeVariable;
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack itemStack) {
                return slot != SLOT_WRITE_OUT && super.canPlaceItem(slot, itemStack);
            }
        };
    }

    @Override
    protected InventoryVariableEvaluator<IValue> createEvaluator() {
        return new InventoryVariableEvaluator<IValue>(this.getInventory(), getSlotRead(), ValueDeseralizationContext.of(getLevel()), ValueTypes.CATEGORY_ANY) {
            @Override
            protected void preValidate() {
                super.preValidate();
                // Hard check to make sure the variable is not directly referring to this proxy.
                if(getVariableFacade() instanceof IProxyVariableFacade) {
                    if(((IProxyVariableFacade) getVariableFacade()).getProxyId() == getProxyId()) {
                        addError(Component.translatable(L10NValues.VARIABLE_ERROR_RECURSION, getVariableFacade().getId()));
                    }
                }
            }
        };
    }

    /**
     * This will generate a new proxy id.
     * Be careful when calling this!
     */
    public void generateNewProxyId() {
        this.proxyId = IntegratedDynamics.globalCounters.getNext(GLOBALCOUNTER_KEY);
        setChanged();
    }

    @Override
    public int getSlotRead() {
        return SLOT_READ;
    }

    protected int getSlotWriteIn() {
        return SLOT_WRITE_IN;
    }

    protected int getSlotWriteOut() {
        return SLOT_WRITE_OUT;
    }

    @Override
    public void onDirty() {
        super.onDirty();
        if (!level.isClientSide()) {
            this.writeVariable = true;
        }
    }

    public ItemStack writeProxyInfo(boolean generateId, ItemStack itemStack, final int proxyId) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, ProxyVariableFacadeHandler.getInstance(), new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IProxyVariableFacade>() {
            @Override
            public IProxyVariableFacade create(boolean generateId) {
                return new ProxyVariableFacade(generateId, proxyId);
            }

            @Override
            public IProxyVariableFacade create(int id) {
                return new ProxyVariableFacade(id, proxyId);
            }
        }, getLevel(), lastPlayer, getBlockState());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerProxy(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integrateddynamics.proxy");
    }

    public static class Ticker<T extends BlockEntityProxy> extends BlockEntityTickerDelayed<T> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, T blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.isWriteVariable()) {
                if (!blockEntity.getInventory().getItem(blockEntity.getSlotWriteIn()).isEmpty() && blockEntity.getInventory().getItem(blockEntity.getSlotWriteOut()).isEmpty()) {
                    // Write proxy reference
                    ItemStack outputStack = blockEntity.writeProxyInfo(!blockEntity.getLevel().isClientSide, blockEntity.getInventory().removeItemNoUpdate(blockEntity.getSlotWriteIn()), blockEntity.getProxyId());
                    blockEntity.getInventory().setItem(blockEntity.getSlotWriteOut(), outputStack);
                }
            }
        }
    }
}
