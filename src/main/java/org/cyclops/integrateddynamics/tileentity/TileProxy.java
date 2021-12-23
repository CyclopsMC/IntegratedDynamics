package org.cyclops.integrateddynamics.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;
import org.cyclops.integrateddynamics.network.ProxyNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the variable proxy.
 * @author rubensworks
 */
public class TileProxy extends TileActiveVariableBase<ProxyNetworkElement> implements INamedContainerProvider {

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
    private PlayerEntity lastPlayer = null;
    private boolean writeVariable;

    public TileProxy() {
        this(RegistryEntries.TILE_ENTITY_PROXY, TileProxy.INVENTORY_SIZE);

        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.SOUTH,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.EAST,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.WEST,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_WRITE_IN)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_WRITE_OUT)));
    }

    public TileProxy(TileEntityType<?> type, int inventorySize) {
        super(type, inventorySize);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new ProxyNetworkElement(DimPos.of(world, blockPos));
            }
        }));
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
        return new InventoryVariableEvaluator<IValue>(this.getInventory(), getSlotRead(), ValueTypes.CATEGORY_ANY) {
            @Override
            protected void preValidate() {
                super.preValidate();
                // Hard check to make sure the variable is not directly referring to this proxy.
                if(getVariableFacade() instanceof IProxyVariableFacade) {
                    if(((IProxyVariableFacade) getVariableFacade()).getProxyId() == getProxyId()) {
                        addError(new TranslationTextComponent(L10NValues.VARIABLE_ERROR_RECURSION, getVariableFacade().getId()));
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

    @Override
    public void tick() {
        super.tick();

        if(!level.isClientSide() && this.writeVariable) {
            if (!getInventory().getStackInSlot(getSlotWriteIn()).isEmpty() && getInventory().getStackInSlot(getSlotWriteOut()).isEmpty()) {
                // Write proxy reference
                ItemStack outputStack = writeProxyInfo(!getLevel().isClientSide, getInventory().removeStackFromSlot(getSlotWriteIn()), proxyId);
                getInventory().setItem(getSlotWriteOut(), outputStack);
            }
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
        }, lastPlayer, getBlockState());
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerProxy(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.integrateddynamics.proxy");
    }
}
