package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.List;
import java.util.Map;

/**
 * The default wrench for this mod.
 * @author rubensworks
 */
public class ItemWrench extends Item {

    private static final Map<String, Mode> NAMED_MODES = Maps.newHashMap();

    public ItemWrench(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && !world.isClientSide()) {
            incrementMode(itemStack);
            player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode", Component.translatable(getMode(itemStack).getLabel())), true);
            return MinecraftHelpers.successAction(itemStack);
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
                    itemStack.getOrCreateTag().putLong("pos", context.getClickedPos().asLong());
                    context.getPlayer().displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.saved", context.getClickedPos().toShortString()), true);
                    return InteractionResult.SUCCESS;
                }
                case OFFSET_SIDE -> {
                    // Save offset and side
                    itemStack.getOrCreateTag().putLong("pos", context.getClickedPos().asLong());
                    itemStack.getOrCreateTag().putLong("side", context.getClickedFace().ordinal());
                    context.getPlayer().displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.saved", context.getClickedPos().toShortString(), context.getClickedFace().getSerializedName()), true);
                    return InteractionResult.SUCCESS;
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
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            Mode mode = NAMED_MODES.get(tag.getString("mode"));
            if (mode != null) {
                return mode;
            }
        }
        return Mode.DEFAULT;
    }

    public void setMode(ItemStack itemStack, Mode mode) {
        itemStack.getOrCreateTag().putString("mode", mode.getName());
    }

    public void incrementMode(ItemStack itemStack) {
        Mode mode = getMode(itemStack);
        int modeId = mode.ordinal();
        Mode nextMode = Mode.values()[(modeId + 1) % Mode.values().length];
        setMode(itemStack, nextMode);

        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            tag.remove("pos");
            tag.remove("side");
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);

        CompoundTag tag = itemStack.getTag();
        Mode mode = getMode(itemStack);
        list.add(Component.translatable("item.integrateddynamics.wrench.mode", Component.translatable(mode.getLabel())));
        if (tag != null) {
            if (tag.contains("pos")) {
                list.add(Component.translatable("item.integrateddynamics.wrench.mode.offset.pos", BlockPos.of(tag.getLong("pos")).toShortString()).withStyle(ChatFormatting.GRAY));
            }
            if (tag.contains("side")) {
                list.add(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.side", Direction.values()[tag.getInt("side")].getSerializedName()).withStyle(ChatFormatting.GRAY));
            }
        }
        list.add(Component.translatable(mode.getLabel() + ".info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }

    public <P extends IPartType<P, S>, S extends IPartState<P>> InteractionResult performPartAction(BlockHitResult hit, IPartType<P, S> partType, IPartState<P> partState, ItemStack itemStack, Player player, InteractionHand hand, PartPos center) {
        Mode mode = getMode(itemStack);
        CompoundTag tag = itemStack.getTag();
        switch (mode) {
            case OFFSET -> {
                if (tag.contains("pos")) {
                    Vec3i offset = determineOffset(hit, tag);
                    if (((IPartType) partType).setTargetOffset(partState, center, offset)) {
                        player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.success"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"), true);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.incomplete"), true);
                }
                return InteractionResult.SUCCESS;
            }
            case OFFSET_SIDE -> {
                if (tag.contains("pos") && tag.contains("side")) {
                    Vec3i offset = determineOffset(hit, tag);
                    Direction side = Direction.values()[tag.getInt("side")];
                    if (((IPartType) partType).setTargetOffset(partState, center, offset)) {
                        ((IPartType) partType).setTargetSideOverride(partState, side);
                        player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset_side.success"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"), true);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("item.integrateddynamics.wrench.mode.offset.incomplete"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    protected Vec3i determineOffset(BlockHitResult hit, CompoundTag tag) {
        BlockPos source = hit.getBlockPos().relative(hit.getDirection());
        BlockPos targetAbs = BlockPos.of(tag.getLong("pos"));
        return new Vec3i(targetAbs.getX() - source.getX(), targetAbs.getY() - source.getY(), targetAbs.getZ() - source.getZ());
    }

    public static enum Mode {
        DEFAULT("integrateddynamics:default", "item.integrateddynamics.wrench.mode.default"),
        OFFSET("integrateddynamics:offset", "item.integrateddynamics.wrench.mode.offset"),
        OFFSET_SIDE("integrateddynamics:offset_side", "item.integrateddynamics.wrench.mode.offset_side");

        private final String name;
        private final String label;

        private Mode(String name, String label) {
            this.name = name;
            this.label = label;
            NAMED_MODES.put(name, this);
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }
    }

}
