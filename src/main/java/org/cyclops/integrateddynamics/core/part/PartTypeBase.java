package org.cyclops.integrateddynamics.core.part;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;
import org.cyclops.integrateddynamics.item.ItemEnhancement;
import org.cyclops.integrateddynamics.item.ItemWrench;

import java.util.*;
import java.util.function.Consumer;

/**
 * An abstract {@link IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>>
        extends PartTypeAdapter<P, S> {

    private Item item;
    private Block block;
    private final String name;
    private final PartRenderPosition partRenderPosition;
    private final Map<Class<? extends INetworkEvent>, IEventAction> networkEventActions;

    public Item getItem() {
        return item;
    }

    public Block getBlock() {
        return block;
    }

    public PartRenderPosition getPartRenderPosition() {
        return partRenderPosition;
    }

    public PartTypeBase(String name, PartRenderPosition partRenderPosition) {
        this.name = name;
        this.partRenderPosition = partRenderPosition;

        networkEventActions = constructNetworkEventActions();

        registerBlock();
    }

    @Override
    public Identifier getUniqueName() {
        return Identifier.fromNamespaceAndPath(getModId(), this.name);
    }

    protected ModBaseNeoForge getMod() {
        return IntegratedDynamics._instance;
    }

    protected String getModId() {
        return getMod().getModId();
    }

    /**
     * Creates and registers a block instance for this part type.
     * This is mainly used for the block facadeModel.
     */
    protected void registerBlock() {
        BlockConfigCommon blockConfig = new BlockConfigCommon<>(getMod(), "part_" + this.name,
                (eConfig, properties) -> block = createBlock(eConfig, properties),
                (eConfig, block) -> item  = createItem(eConfig, block)) {
            @Override
            public String getFullTranslationKey() {
                return PartTypeBase.this.getTranslationKey();
            }

            @Override
            public Collection<java.util.function.Supplier<ItemStack>> getDefaultCreativeTabEntries() {
                return Collections.singleton(() -> new ItemStack(getItemInstance()));
            }
        };
        getMod().getConfigHandler().addConfigurable(blockConfig);
    }

    /**
     * Factory method for creating a block instance.
     *
     * @param blockConfig The config to register the block for.
     * @param properties
     * @return The block instance.
     */
    protected Block createBlock(BlockConfigCommon<?> blockConfig, BlockBehaviour.Properties properties) {
        return new IgnoredBlock(properties);
    }

    /**
     * Factory method for creating a item instance.
     * @param blockConfig The block config to register the item for.
     * @param block The block corresponding to the item.
     * @return The item instance.
     */
    protected Item createItem(BlockConfigCommon<?> blockConfig, Block block) {
        return new ItemPart<>(blockConfig.createDefaultItemProperties()
                .overrideDescription(this.getTranslationKey()), this);
    }

    @Override
    public Identifier getBlockModelPath() {
        return Identifier.fromNamespaceAndPath(getModId(), "part_" + this.name);
    }

    @Override
    protected String createTranslationKey() {
        return "parttype." + getModId() + "." + this.name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public INetworkElement createNetworkElement(IPartContainer partContainer, DimPos pos, Direction side) {
        return new PartNetworkElement(this, PartPos.of(pos, side));
    }

    @Override
    public InteractionResult onPartActivated(S partState, BlockPos pos, Level world, Player player, InteractionHand hand,
                                            ItemStack heldItem, BlockHitResult hit) {
        // Drop through if the player is sneaking
        if(player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        // Consume enhancement
        if (heldItem.getItem() instanceof ItemEnhancement itemEnhancement) {
            InteractionResult result = itemEnhancement.applyEnhancement(this, partState, heldItem, player, hand);
            if (result.consumesAction()) {
                return result;
            }
        }

        // Set offset and side
        PartPos partPos = PartPos.of(world, pos, hit.getDirection());
        if (heldItem.getItem() instanceof ItemWrench itemWrench) {
            InteractionResult result = itemWrench.performPartAction(hit, this, partState, heldItem, player, hand, partPos);
            if (result.consumesAction()) {
                return result;
            }
        }

        if(getContainerProvider(partPos).isPresent()) {
            if (!world.isClientSide()) {
                return PartHelpers.openContainerPart((ServerPlayer) player, partPos, this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        super.addDrops(target, state, itemStacks, dropMainElement, saveState);

        // Save enhancements
        if (!saveState && state.getMaxOffset() > 0) {
            // Drop Part Offset items each with as maximum the GeneralConfig.enchancementOffsetPartDropValue offset value.
            int remainingOffset = state.getMaxOffset();
            while (remainingOffset > 0) {
                int offset;
                if (remainingOffset < GeneralConfig.enchancementOffsetPartDropValue) {
                    offset = remainingOffset;
                } else {
                    offset = GeneralConfig.enchancementOffsetPartDropValue;
                }
                remainingOffset -= offset;

                ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_ENHANCEMENT_OFFSET);
                RegistryEntries.ITEM_ENHANCEMENT_OFFSET.get().setEnhancementValue(itemStack, offset);
                itemStacks.add(itemStack);
            }
            state.setMaxOffset(0);
        }
    }

    @Override
    public BlockState getBlockState(IPartContainer partContainer, Direction side) {
        return getBlock().defaultBlockState()
                .setValue(IgnoredBlock.FACING, side);
    }

    @Override
    public BlockState getBaseBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public void loadTooltip(S state, List<Component> lines) {
        if(!state.isEnabled()) {
            lines.add(Component.translatable(L10NValues.PART_TOOLTIP_DISABLED));
        }
        lines.add(Component.translatable(L10NValues.GENERAL_ITEM_ID, state.getId()));

        if (state.getMaxOffset() > 0) {
            lines.add(Component.translatable(L10NValues.PART_TOOLTIP_MAXOFFSET, state.getMaxOffset()));
        }
    }

    @Override
    public void loadTooltip(ItemStack itemStack, Consumer<Component> tooltipAdder) {
        if(itemStack.has(RegistryEntries.DATACOMPONENT_PART_STATE)) {
            CompoundTag tag = itemStack.get(RegistryEntries.DATACOMPONENT_PART_STATE);
            if(tag.contains("id")) {
                int id = tag.getInt("id").orElseThrow();
                tooltipAdder.accept(Component.translatable(L10NValues.GENERAL_ITEM_ID, id));
            }
            if(tag.contains("maxOffset")) {
                int maxOffset = tag.getInt("maxOffset").orElseThrow();
                tooltipAdder.accept(Component.translatable(L10NValues.PART_TOOLTIP_MAXOFFSET, maxOffset));
            }
        }

        super.loadTooltip(itemStack, tooltipAdder);
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
    public void writeExtraGuiData(RegistryFriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        packetBuffer.writeUtf(this.getUniqueName().toString());
    }

    public interface IEventAction<P extends IPartType<P, S>, S extends IPartState<P>, E extends INetworkEvent> {

        public void onAction(INetwork network, PartTarget target, S state, E event);

    }

}
