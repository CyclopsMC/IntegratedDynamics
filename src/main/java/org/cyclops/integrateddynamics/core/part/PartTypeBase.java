package org.cyclops.integrateddynamics.core.part;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurabletypeaction.BlockAction;
import org.cyclops.cyclopscore.config.configurabletypeaction.ItemAction;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.PartTypeAdapter;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract {@link IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>>
        extends PartTypeAdapter<P, S> implements IGuiContainerProvider {

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
    private final PartRenderPosition partRenderPosition;
    private final Map<Class<? extends INetworkEvent>, IEventAction> networkEventActions;

    public PartTypeBase(String name, PartRenderPosition partRenderPosition) {
        if(hasGui()) {
            this.guiID = Helpers.getNewId(getModGui(), Helpers.IDType.GUI);
            getModGui().getGuiHandler().registerGUI(this, ExtendedGuiHandler.PART);
        } else {
            this.guiID = -1;
        }
        this.name = name;
        this.block = registerBlock();
        this.item = registerItem();
        this.partRenderPosition = partRenderPosition;

        networkEventActions = constructNetworkEventActions();
    }

    protected ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    /**
     * Get the part type class.
     * This is used for doing dynamic construction of guis.
     * @return The actual class for this part type.
     */
    public abstract Class<? super P> getPartTypeClass();

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
        BlockConfig blockConfig = new BlockConfig(getMod(), true, "part_" + getName() + "_block", null, null) {
            @Override
            public boolean isDisableable() {
                return false;
            }

            @Override
            public Block getBlockInstance() {
                return PartTypeBase.this.getBlock();
            }
        };
        Block block = createBlock(blockConfig);
        BlockAction.register(block, blockConfig, blockConfig.getTargetTab());
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
        itemConfig = new ItemConfig(getMod(), true, "part_" + getName() + "_item", null, null) {
            @Override
            public boolean isDisableable() {
                return false;
            }

            @Override
            public String getFullTranslationKey() {
                return PartTypeBase.this.getTranslationKey();
            }

            @Override
            public Item getItemInstance() {
                return PartTypeBase.this.getItem();
            }
        };
        Item item = createItem(itemConfig);
        ItemAction.register(item, itemConfig, itemConfig.getTargetTab());
        if(MinecraftHelpers.isClientSide()) {
            ItemAction.handleItemModel(itemConfig);
        }
        getMod().getConfigHandler().addToConfigDictionary(itemConfig);
        return item;
    }

    @Override
    public ResourceLocation getBlockModelPath() {
        return new ResourceLocation(getMod().getModId(), "part_" + getName() + "_block");
    }

    @Override
    public String getTranslationKeyBase() {
        return "parttype.parttypes." + getMod().getModId() + "." + getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public INetworkElement createNetworkElement(IPartContainer partContainer, DimPos pos, EnumFacing side) {
        return new PartNetworkElement(this, getTarget(PartPos.of(pos, side), (S) partContainer.getPartState(side)));
    }

    protected boolean hasGui() {
        return true;
    }

    @Override
    public ModBase getModGui() {
        return getMod();
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, S partState, EntityPlayer player, EnumHand hand,
                                   ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        // Drop through if the player is sneaking
        if(player.isSneaking()) {
            return false;
        }

        if(hasGui()) {
            getModGui().getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, side); // Pass the side as extra data to the gui
            if (!world.isRemote && hasGui()) {
                player.openGui(getModGui().getModId(), getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getBlockState(IPartContainer partContainer, EnumFacing side) {
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

    @Override
    public BlockStateContainer getBaseBlockState() {
        return getBlock().getBlockState();
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {
        if(!state.isEnabled()) {
            lines.add(L10NHelpers.localize(L10NValues.PART_TOOLTIP_DISABLED));
        }
        lines.add(L10NHelpers.localize(L10NValues.GENERAL_ITEM_ID, state.getId()));
    }
    /**
     * Override this to register your network event actions.
     * @return The event actions.
     */
    protected Map<Class<? extends INetworkEvent>, IEventAction> constructNetworkEventActions() {
        return new IdentityHashMap<>();
    }

    @Override
    public final boolean hasEventSubscriptions() {
        return !networkEventActions.isEmpty();
    }

    @Override
    public final Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return networkEventActions.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onEvent(INetworkEvent event, IPartNetworkElement<P, S> networkElement) {
        if (networkElement.getTarget().getCenter().getPos().isLoaded()) {
            networkEventActions.get(event.getClass()).onAction(event.getNetwork(), networkElement.getTarget(),
                    networkElement.getPartState(), event);
        }
    }

    @Override
    public boolean forceLightTransparency(S state) {
        return false;
    }

    public interface IEventAction<P extends IPartType<P, S>, S extends IPartState<P>, E extends INetworkEvent> {

        public void onAction(INetwork network, PartTarget target, S state, E event);

    }

}
