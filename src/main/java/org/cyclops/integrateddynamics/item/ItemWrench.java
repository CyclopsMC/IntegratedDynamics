package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.inventory.InventoryLocationPlayer;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartConfigHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartConfigApplyResult;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.core.part.PartConfigSnapshot;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.inventory.container.ContainerWrenchConfig;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The default wrench for this mod.
 * @author rubensworks
 */
public class ItemWrench extends Item {

    private static final Map<String, Mode> NAMED_MODES = Maps.newHashMap();
    private static final Map<Integer, Mode> INT_MODES = Maps.newHashMap();

    public ItemWrench(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && !world.isClientSide()) {
            incrementMode(itemStack);
            player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode", Component.translatable(getMode(itemStack).getLabel())));
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
        }
        // Show what is inside the Wrench, so that parts of it can be switched off before pasting
        if (!player.isSecondaryUseActive() && getMode(itemStack).isConfig()
                && itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_PART_CONFIG)) {
            if (!world.isClientSide()) {
                ItemLocation itemLocation = InventoryLocationPlayer.getInstance()
                        .handToLocation(player, hand, player.getInventory().getSelectedSlot());
                IModHelpers.get().getMinecraftHelpers().openMenu((ServerPlayer) player,
                        new NamedContainerProviderItem(itemLocation,
                                Component.translatable("gui.integrateddynamics.wrench_config"),
                                ContainerWrenchConfig::new),
                        packetBuffer -> ItemLocation.writeToPacketBuffer(packetBuffer, itemLocation));
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            switch (getMode(itemStack)) {
                case OFFSET -> {
                    // Save offset
                    itemStack.set(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS, context.getClickedPos());
                    context.getPlayer().sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.saved", context.getClickedPos().toShortString()));
                    return InteractionResult.SUCCESS;
                }
                case OFFSET_SIDE -> {
                    // Save offset and side
                    itemStack.set(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS, context.getClickedPos());
                    itemStack.set(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION, context.getClickedFace());
                    context.getPlayer().sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.saved", context.getClickedPos().toShortString(), context.getClickedFace().getSerializedName()));
                    return InteractionResult.SUCCESS;
                }
                case DEFAULT -> {
                    if (!CableHelpers.getCable(context.getLevel(), context.getClickedPos(), context.getClickedFace()).isPresent()) {
                        return InteractionResult.FAIL;
                    }
                }
                case CONFIG, CONFIG_SETTINGS, CONFIG_ASPECT -> {
                    // Let the click through to the part, so that its configuration can be copied
                    return InteractionResult.PASS;
                }
            }
        }

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            return super.useOn(context);
        }

        ItemStack itemStack = context.getItemInHand();
        switch (getMode(itemStack)) {
            case DEFAULT -> {
                // Rotate block
                if (context.getClickedFace().getAxis() == Direction.Axis.Y
                        && blockState.hasProperty(BlockStateProperties.FACING)) {
                    // If pointing top or bottom, and we can rotate to UP and DOWN, rotate to that direction or opposite
                    blockState = blockState.setValue(BlockStateProperties.FACING, blockState.getValue(BlockStateProperties.FACING) == Direction.UP ? Direction.DOWN : Direction.UP);
                } else if (context.getClickedFace().getAxis() != Direction.Axis.Y
                        && blockState.hasProperty(BlockStateProperties.FACING)
                        && blockState.getValue(BlockStateProperties.FACING).getAxis() == Direction.Axis.Y) {
                    // If not pointing top or bottom, and rotation is UP or DOWN, rotate to facing
                    blockState = blockState.setValue(BlockStateProperties.FACING, context.getClickedFace());
                } else {
                    // Otherwise, just call rotate method
                    blockState = blockState.rotate(context.getLevel(), context.getClickedPos(), Rotation.CLOCKWISE_90);
                }
                context.getLevel().setBlockAndUpdate(context.getClickedPos(), blockState);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public Mode getMode(ItemStack itemStack) {
        return Objects.requireNonNullElse(itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_MODE), Mode.DEFAULT);
    }

    public void setMode(ItemStack itemStack, Mode mode) {
        itemStack.set(RegistryEntries.DATACOMPONENT_WRENCH_MODE, mode);
    }

    public void incrementMode(ItemStack itemStack) {
        Mode mode = getMode(itemStack);
        int modeId = mode.ordinal();
        Mode nextMode = Mode.values()[(modeId + 1) % Mode.values().length];
        setMode(itemStack, nextMode);

        itemStack.remove(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS);
        itemStack.remove(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, tooltipAdder, flag);

        Mode mode = getMode(itemStack);
        tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode", Component.translatable(mode.getLabel())));
        if (itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS)) {
            tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.offset.pos", itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS).toShortString()).withStyle(ChatFormatting.GRAY));
        }
        if (itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION)) {
            tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.side", itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION).getSerializedName()).withStyle(ChatFormatting.GRAY));
        }
        CompoundTag configTag = itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_PART_CONFIG);
        // A configuration stays in the Wrench when switching modes, but only says something in the modes that paste it
        if (mode.isConfig() && configTag != null && context.registries() != null) {
            PartConfigSnapshot.fromNBT(context.registries(), configTag).ifPresent(snapshot -> {
                // Only the sections that the current mode pastes are relevant
                Set<PartConfigSection> sections = Sets.intersection(snapshot.getSections(), mode.getConfigSections());
                if (sections.isEmpty()) {
                    // This mode has nothing to paste from this configuration, so it has nothing to say about it
                    return;
                }
                tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.config.source",
                        Component.translatable(getSourcePartTypeName(snapshot))).withStyle(ChatFormatting.GRAY));
                tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.config.sections",
                        sections.stream()
                                .map(section -> Component.translatable(section.getTranslationKey()).getString())
                                .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GRAY));
                int requiredBlanks = snapshot.getRequiredBlankVariables(sections);
                if (requiredBlanks > 0) {
                    tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.config.requires",
                            requiredBlanks).withStyle(ChatFormatting.GOLD));
                }
                int requiredMaxOffset = snapshot.getRequiredMaxOffset(sections);
                if (requiredMaxOffset > 0) {
                    tooltipAdder.accept(Component.translatable("item.integrateddynamics.wrench.mode.config.requires_enhancements",
                            requiredMaxOffset).withStyle(ChatFormatting.GOLD));
                }
                // Whatever the part type stored itself can need something from the player as well
                IPartType<?, ?> sourcePartType = PartTypes.REGISTRY.getPartType(snapshot.sourcePartType());
                if (sourcePartType != null) {
                    for (PartConfigSection section : sections) {
                        sourcePartType.getConfigExtraRequirements(snapshot, section)
                                .forEach(requirement -> tooltipAdder.accept(requirement.copy().withStyle(ChatFormatting.GOLD)));
                    }
                }
            });
        }
        // Hidden behind the same shift that reveals the item info, to keep the resting tooltip short
        if (IModHelpers.get().getMinecraftClientHelpers().isShifted()) {
            tooltipAdder.accept(Component.translatable(mode.getLabel() + ".info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }

    public <P extends IPartType<P, S>, S extends IPartState<P>> InteractionResult performPartAction(BlockHitResult hit, IPartType<P, S> partType, IPartState<P> partState, ItemStack itemStack, Player player, InteractionHand hand, PartPos center) {
        Mode mode = getMode(itemStack);
        switch (mode) {
            case OFFSET -> {
                if (itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS)) {
                    Vec3i offset = determineOffset(hit, itemStack);
                    if (((IPartType) partType).setTargetOffset(partState, center, offset)) {
                        player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.success"));
                    } else {
                        player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"));
                    }
                } else {
                    player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.incomplete"));
                }
                return InteractionResult.SUCCESS;
            }
            case CONFIG, CONFIG_SETTINGS, CONFIG_ASPECT -> {
                pastePartConfig(partType, partState, itemStack, player, center);
                return InteractionResult.SUCCESS;
            }
            case OFFSET_SIDE -> {
                if (itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS) && itemStack.has(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION)) {
                    Vec3i offset = determineOffset(hit, itemStack);
                    Direction side = itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_DIRECTION);
                    if (((IPartType) partType).setTargetOffset(partState, center, offset)) {
                        ((IPartType) partType).setTargetSideOverride(partState, side);
                        player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.success"));
                    } else {
                        player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"));
                    }
                } else {
                    player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.incomplete"));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Copy the configuration of the part at the given position into the given Wrench.
     * @param itemStack The Wrench.
     * @param player The player.
     * @param center The position of the part.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyPartConfig(ItemStack itemStack, Player player, PartPos center) {
        if (player.level().isClientSide()) {
            return;
        }
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(center);
        if (partStateHolder == null) {
            return;
        }
        IPartType partType = partStateHolder.getPart();
        Level level = center.getPos().getLevel(true);
        PartConfigSnapshot snapshot = ((IPartType) partType).snapshotConfig(ValueDeseralizationContext.of(level),
                partStateHolder.getState(), getMode(itemStack).getConfigSections());
        if (snapshot.isEmpty()) {
            // Nothing was configured on this part, so there is nothing to paste onto another one
            itemStack.remove(RegistryEntries.DATACOMPONENT_WRENCH_PART_CONFIG);
            player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.config.nothing"));
            return;
        }
        PartConfigHelpers.setSnapshot(level.registryAccess(), itemStack, snapshot);
        player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.config.copied",
                Component.translatable(partType.getTranslationKey())));
    }

    /**
     * Paste the configuration inside the given Wrench onto the given part.
     * @param partType The part type.
     * @param partState The part state.
     * @param itemStack The Wrench.
     * @param player The player.
     * @param center The position of the part.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void pastePartConfig(IPartType<?, ?> partType, IPartState<?> partState, ItemStack itemStack,
                                   Player player, PartPos center) {
        if (player.level().isClientSide()) {
            return;
        }
        Level level = center.getPos().getLevel(true);
        Set<PartConfigSection> sections = getMode(itemStack).getConfigSections();
        PartConfigSnapshot snapshot = PartConfigHelpers.getSnapshot(level.registryAccess(), itemStack).orElse(null);
        if (snapshot == null || sections.stream().noneMatch(snapshot::hasSection)) {
            player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.config.empty"));
            return;
        }
        // Only a whole configuration has to come from the same part type,
        // as the separate sections are matched by aspect and inventory instead.
        if (getMode(itemStack).isFullConfig() && !snapshot.sourcePartType().equals(partType.getUniqueName())) {
            player.sendOverlayMessage(Component.translatable("item.integrateddynamics.wrench.mode.config.mismatch",
                    Component.translatable(getSourcePartTypeName(snapshot))));
            return;
        }

        INetwork network = NetworkHelpers.getNetwork(center).orElse(null);
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
        PartTarget target = ((IPartType) partType).getTarget(center, partState);
        PartConfigApplyResult result = ((IPartType) partType).applyConfig(ValueDeseralizationContext.of(level),
                network, partNetwork, target, partState, snapshot, sections, player);
        player.sendOverlayMessage(result.getMessage());
        // Warnings go to the chat, as they are too long for the action bar and are worth keeping around
        result.getWarnings().forEach(warning -> player.sendSystemMessage(warning));
    }

    protected static String getSourcePartTypeName(PartConfigSnapshot snapshot) {
        IPartType<?, ?> partType = PartTypes.REGISTRY.getPartType(snapshot.sourcePartType());
        return partType == null ? snapshot.sourcePartType().toString() : partType.getTranslationKey();
    }

    protected Vec3i determineOffset(BlockHitResult hit, ItemStack itemStack) {
        BlockPos source = hit.getBlockPos().relative(hit.getDirection());
        BlockPos targetAbs = itemStack.get(RegistryEntries.DATACOMPONENT_WRENCH_TARGET_BLOCKPOS);
        return new Vec3i(targetAbs.getX() - source.getX(), targetAbs.getY() - source.getY(), targetAbs.getZ() - source.getZ());
    }

    public static enum Mode implements StringRepresentable {
        DEFAULT("integrateddynamics:default", "item.integrateddynamics.wrench.mode.default"),
        OFFSET("integrateddynamics:offset", "item.integrateddynamics.wrench.mode.offset"),
        OFFSET_SIDE("integrateddynamics:offset_side", "item.integrateddynamics.wrench.mode.offset_side"),
        CONFIG("integrateddynamics:config", "item.integrateddynamics.wrench.mode.config",
                PartConfigSection.ALL),
        CONFIG_SETTINGS("integrateddynamics:config_settings", "item.integrateddynamics.wrench.mode.config_settings",
                Sets.immutableEnumSet(PartConfigSection.PART_SETTINGS)),
        CONFIG_ASPECT("integrateddynamics:config_aspect", "item.integrateddynamics.wrench.mode.config_aspect",
                Sets.immutableEnumSet(PartConfigSection.ASPECT));

        public static final StringRepresentable.EnumCodec<Mode> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.idMapper(INT_MODES::get, Mode::ordinal);

        private final String name;
        private final String label;
        private final Set<PartConfigSection> configSections;

        private Mode(String name, String label) {
            this(name, label, Set.of());
        }

        private Mode(String name, String label, Set<PartConfigSection> configSections) {
            this.name = name;
            this.label = label;
            this.configSections = configSections;
            NAMED_MODES.put(name, this);
            INT_MODES.put(ordinal(), this);
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        /**
         * @return The configuration sections that this mode copies and pastes.
         *         Empty for modes that do not deal with part configurations.
         */
        public Set<PartConfigSection> getConfigSections() {
            return configSections;
        }

        /**
         * @return If this mode copies and pastes part configurations.
         */
        public boolean isConfig() {
            return !this.configSections.isEmpty();
        }

        /**
         * @return If this mode copies and pastes the whole configuration of a part,
         *         which is only allowed between parts of the same type.
         */
        public boolean isFullConfig() {
            return this.configSections.equals(PartConfigSection.ALL);
        }

        @Override
        public String getSerializedName() {
            return getName();
        }
    }

}
