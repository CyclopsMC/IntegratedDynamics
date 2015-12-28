package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurabletypeaction.BlockAction;
import org.cyclops.cyclopscore.config.configurabletypeaction.ItemAction;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.IInitListener;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract {@link IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S>,
        IGuiContainerProvider {

    @Getter
    private final Item item;
    private ItemConfig itemConfig;
    @Getter
    private final Block block;
    @Getter
    private final int guiID;
    @Getter
    private final String name;
    @Getter
    private final RenderPosition renderPosition;
    private final Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> networkEventActions;

    public PartTypeBase(String name, RenderPosition renderPosition) {
        if(hasGui()) {
            this.guiID = Helpers.getNewId(getMod(), Helpers.IDType.GUI);
            getMod().getGuiHandler().registerGUI(this, ExtendedGuiHandler.PART);
        } else {
            this.guiID = -1;
        }
        this.name = name;
        this.block = registerBlock();
        this.item = registerItem();
        this.renderPosition = renderPosition;

        networkEventActions = constructNetworkEventActions();
    }

    /**
     * Factory method for creating a block instance.
     * @param blockConfig The config to register the block for.
     * @return The block instance.
     */
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock(blockConfig);
    }

    /**
     * Creates and registers a block instance for this part type.
     * This is mainly used for the block model.
     * @return The corresponding block.
     */
    protected Block registerBlock() {
        BlockConfig blockConfig = new BlockConfig(getMod(), true, "part_" + getName() + "Block", null, null) {
            @Override
            public boolean isDisableable() {
                return false;
            }
        };
        Block block = createBlock(blockConfig);
        BlockAction.register(block, blockConfig.getSubUniqueName(), blockConfig.getTargetTab());
        return block;
    }

    /**
     * Factory method for creating a item instance.
     * @param itemConfig The config to register the item for.
     * @return The item instance.
     */
    protected Item createItem(ItemConfig itemConfig) {
        return new ItemPart<P, S>(itemConfig, this);
    }

    /**
     * Creates and registers a item instance for this part type.
     * This is the item used to place the part with and obtained when broken.
     * @return The corresponding item.
     */
    protected Item registerItem() {
        itemConfig = new ItemConfig(getMod(), true, "part_" + getName() + "Item", null, null) {
            @Override
            public boolean isDisableable() {
                return false;
            }
        };
        Item item = createItem(itemConfig);
        ItemAction.register(item, itemConfig.getSubUniqueName(), itemConfig.getTargetTab());
        return item;
    }

    /**
     * Override this to register your network event actions.
     * @return The event actions.
     */
    protected Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> constructNetworkEventActions() {
        return Maps.newHashMap();
    }

    @Override
    public final boolean hasEventSubscriptions() {
        return !networkEventActions.isEmpty();
    }

    @Override
    public final Set<Class<? extends INetworkEvent<IPartNetwork>>> getSubscribedEvents() {
        return networkEventActions.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onEvent(INetworkEvent<IPartNetwork> event, IPartNetworkElement<P, S> networkElement) {
        networkEventActions.get(event.getClass()).onAction(event.getNetwork(), networkElement.getTarget(), networkElement.getPartState(), event);
    }

    @Override
    public boolean isSolid(S state) {
        return false;
    }

    @Override
    public String getUnlocalizedNameBase() {
        return "parttype.parttypes." + getMod().getModId() + "." + getName();
    }

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedNameBase() + ".name";
    }

    @Override
    public void onInit(IInitListener.Step initStep) {
        if(MinecraftHelpers.isClientSide() && initStep == IInitListener.Step.INIT) {
            ItemAction.handleItemModel(getItem(), itemConfig.getNamedId(), itemConfig.getTargetTab(), getMod().getModId(), itemConfig);
        }
    }

    @Override
    public INetworkElement<IPartNetwork> createNetworkElement(IPartContainerFacade partContainerFacade, DimPos pos, EnumFacing side) {
        return new PartNetworkElement(this, partContainerFacade, PartTarget.fromCenter(pos, side));
    }

    @Override
    public ItemStack getItemStack(S state) {
        NBTTagCompound tag = new NBTTagCompound();
        toNBT(tag, state);
        ItemStack itemStack = new ItemStack(getItem());
        itemStack.setTagCompound(tag);
        return itemStack;
    }

    @Override
    public boolean isUpdate(S state) {
        return false;
    }

    @Override
    public void update(IPartNetwork network, PartTarget target, S state) {

    }

    @Override
    public S getState(ItemStack itemStack) {
        S partState = null;
        if(itemStack != null && itemStack.getTagCompound() != null) {
            partState = fromNBT(itemStack.getTagCompound());
        }
        if(partState == null) {
            partState = getDefaultState();
        }
        return partState;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks) {
        itemStacks.add(getItemStack(state));
    }

    @Override
    public void toNBT(NBTTagCompound tag, S partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public S fromNBT(NBTTagCompound tag) {
        S partState = constructDefaultState();
        partState.readFromNBT(tag);
        return partState;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public final S getDefaultState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        return defaultState;
    }

    @Override
    public void setUpdateInterval(S state, int updateInterval) {
        state.setUpdateInterval(updateInterval);
    }

    @Override
    public int getUpdateInterval(S state) {
        return state.getUpdateInterval();
    }

    @Override
    public void beforeNetworkKill(IPartNetwork network, PartTarget target, S state) {
        System.out.println("killing " + state);
    }

    @Override
    public void afterNetworkAlive(IPartNetwork network, PartTarget target, S state) {
        System.out.println("alive " + state);
    }

    @Override
    public void onNetworkAddition(IPartNetwork network, PartTarget target, S state) {

    }

    @Override
    public void onNetworkRemoval(IPartNetwork network, PartTarget target, S state) {

    }

    protected boolean hasGui() {
        return true;
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, IBlockState state, S partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ) {
        // Drop through if the player is sneaking
        if(player.isSneaking() || !partState.isEnabled()) {
            return false;
        }

        if(hasGui()) {
            getMod().getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, side); // Pass the side as extra data to the gui
            if (!world.isRemote && hasGui()) {
                player.openGui(getMod().getModId(), getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onPreRemoved(IPartNetwork network, PartTarget target, S state) {

    }

    @Override
    public void onBlockNeighborChange(IPartNetwork network, PartTarget target, S state, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public IBlockState getBlockState(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

    @Override
    public int getConsumptionRate(S state) {
        return 0;
    }

    @Override
    public void postUpdate(IPartNetwork network, PartTarget target, S state, boolean updated) {
        setEnabled(state, updated);
    }

    @Override
    public boolean isEnabled(S state) {
        return state.isEnabled();
    }

    @Override
    public void setEnabled(S state, boolean enabled) {
        state.setEnabled(enabled);
    }

    public interface IEventAction<P extends IPartType<P, S>, S extends IPartState<P>, E extends INetworkEvent> {

        public void onAction(IPartNetwork network, PartTarget target, S state, E event);

    }

}
