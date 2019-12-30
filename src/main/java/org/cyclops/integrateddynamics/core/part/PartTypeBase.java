package org.cyclops.integrateddynamics.core.part;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.init.ModBase;
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
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
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
        extends PartTypeAdapter<P, S> {

    @Getter
    private Item item;
    @Getter
    private Block block;
    @Getter
    private final String name;
    @Getter
    private final PartRenderPosition partRenderPosition;
    private final Map<Class<? extends INetworkEvent>, IEventAction> networkEventActions;

    public PartTypeBase(String name, PartRenderPosition partRenderPosition) {
        this.name = name;
        this.partRenderPosition = partRenderPosition;

        networkEventActions = constructNetworkEventActions();

        registerBlock();
    }

    protected ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    /**
     * Creates and registers a block instance for this part type.
     * This is mainly used for the block model.
     */
    protected void registerBlock() {
        BlockConfig blockConfig = new BlockConfig(getMod(), "part_" + getName() + "_block", this::createBlock, this::createItem) {};
        getMod().getConfigHandler().addConfigurable(blockConfig);
    }

    /**
     * Factory method for creating a block instance.
     * @param blockConfig The config to register the block for.
     * @return The block instance.
     */
    protected Block createBlock(BlockConfig blockConfig) {
        return block = new IgnoredBlock();
    }

    /**
     * Factory method for creating a item instance.
     * @param blockConfig The block config to register the item for.
     * @param block The block corresponding to the item.
     * @return The item instance.
     */
    protected Item createItem(BlockConfig blockConfig, Block block) {
        return item = new ItemPart<>(new Item.Properties().group(blockConfig.getMod().getDefaultItemGroup()), this);
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
    public INetworkElement createNetworkElement(IPartContainer partContainer, DimPos pos, Direction side) {
        return new PartNetworkElement(this, getTarget(PartPos.of(pos, side), (S) partContainer.getPartState(side)));
    }

    @Override
    public boolean onPartActivated(S partState, BlockPos pos, World world, PlayerEntity player, Hand hand,
                                   ItemStack heldItem, BlockRayTraceResult hit) {
        // Drop through if the player is sneaking
        if(player.isSneaking()) {
            return false;
        }

        PartPos partPos = PartPos.of(world, pos, hit.getFace());
        if(getContainerProvider(partPos).isPresent()) {
            if (!world.isRemote()) {
                return PartHelpers.openContainerPart((ServerPlayerEntity) player, partPos, this);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockState getBlockState(IPartContainer partContainer, Direction side) {
        return getBlock().getDefaultState()
                .with(IgnoredBlock.FACING, side);
    }

    @Override
    public BlockState getBaseBlockState() {
        return getBlock().getDefaultState();
    }

    @Override
    public void loadTooltip(S state, List<ITextComponent> lines) {
        if(!state.isEnabled()) {
            lines.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_DISABLED));
        }
        lines.add(new TranslationTextComponent(L10NValues.GENERAL_ITEM_ID, state.getId()));
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

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {
        packetBuffer.writeString(this.getName());
    }

    public interface IEventAction<P extends IPartType<P, S>, S extends IPartState<P>, E extends INetworkEvent> {

        public void onAction(INetwork network, PartTarget target, S state, E event);

    }

}
