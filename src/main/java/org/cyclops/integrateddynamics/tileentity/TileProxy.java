package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
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
import org.cyclops.integrateddynamics.network.ProxyNetworkElement;

/**
 * A part entity for the variable proxy.
 * @author rubensworks
 */
public class TileProxy extends TileActiveVariableBase<ProxyNetworkElement> {

    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    public static final String GLOBALCOUNTER_KEY = "proxy";

    @NBTPersist
    @Getter
    private int proxyId = -1;

    @Setter
    private EntityPlayer lastPlayer = null;

    public TileProxy() {
        this(3);

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(SLOT_WRITE_OUT));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(SLOT_WRITE_IN));
    }

    public TileProxy(int inventorySize) {
        super(inventorySize, "proxy");

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new ProxyNetworkElement(DimPos.of(world, blockPos));
            }
        });
    }

    @Override
    protected InventoryVariableEvaluator createEvaluator() {
        return new InventoryVariableEvaluator(this, getSlotRead(), ValueTypes.CATEGORY_ANY) {
            @Override
            protected void preValidate() {
                super.preValidate();
                // Hard check to make sure the variable is not directly referring to this proxy.
                if(getVariableFacade() instanceof IProxyVariableFacade) {
                    if(((IProxyVariableFacade) getVariableFacade()).getProxyId() == getProxyId()) {
                        addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_RECURSION, getVariableFacade().getId()));
                    }
                }
            }
        };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return slot != SLOT_WRITE_OUT && super.canInsertItem(slot, itemStack, side);
    }

    /**
     * This will generate a new proxy id.
     * Be careful when calling this!
     */
    public void generateNewProxyId() {
        this.proxyId = IntegratedDynamics.globalCounters.getNext(GLOBALCOUNTER_KEY);
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!MinecraftHelpers.isClientSide() && this.proxyId == -1) {
            generateNewProxyId();
        }
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
        if(!world.isRemote) {
            if (!getStackInSlot(getSlotWriteIn()).isEmpty() && getStackInSlot(getSlotWriteOut()).isEmpty()) {
                // Write proxy reference
                ItemStack outputStack = writeProxyInfo(!getWorld().isRemote, removeStackFromSlot(getSlotWriteIn()), proxyId);
                setInventorySlotContents(getSlotWriteOut(), outputStack);
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
        }, lastPlayer, getBlock());
    }
}
