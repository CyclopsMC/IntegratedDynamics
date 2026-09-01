package org.cyclops.integrateddynamics.api.part;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Default implementation of {@link IPartType}.
 * @author rubensworks
 */
public abstract class PartTypeAdapter<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private String translationKey = null;

    @Override
    public final String getTranslationKey() {
        return translationKey != null ? translationKey : (translationKey = createTranslationKey());
    }

    protected abstract String createTranslationKey();

    @Override
    public boolean isSolid(S state) {
        return false;
    }

    @Override
    public void serializeState(ValueOutput valueOutput, S partState) {
        partState.serialize(valueOutput);
    }

    @Override
    public S deserializeState(ValueInput valueInput) {
        S partState = constructDefaultState();
        partState.deserialize(valueInput);
        partState.gatherCapabilities((P) this);
        return partState;
    }

    @Override
    public void setUpdateInterval(S state, int updateInterval) {
        state.setUpdateInterval(updateInterval);
    }

    @Override
    public int getUpdateInterval(S state) {
        return Math.max(getMinimumUpdateInterval(state), state.getUpdateInterval());
    }

    @Override
    public int getMinimumUpdateInterval(S state) {
        return Math.max(GeneralConfig.partsMinimumUpdateInterval, GeneralConfig.partMinimumUpdateIntervalsMap.getOrDefault(this, 1));
    }

    @Override
    public void setPriorityAndChannel(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, int priority, int channel) {
        //noinspection deprecation
        state.setPriority(priority);
        state.setChannel(channel);
    }

    @Override
    public int getPriority(S state) {
        return state.getPriority();
    }

    @Override
    public int getChannel(S state) {
        return state.getChannel();
    }

    @Override
    public Vec3i getTargetOffset(S state) {
        return state.getTargetOffset();
    }

    @Override
    public boolean setTargetOffset(S state, PartPos center, Vec3i offset) {
        int max = state.getMaxOffset();
        if (offset.getX() >= -max && offset.getY() >= -max && offset.getZ() >= -max
                && offset.getX() <= max && offset.getY() <= max && offset.getZ() <= max) {
            state.setTargetOffset(offset);
            return true;
        }
        return false;
    }

    @Override
    public void setTargetSideOverride(S state, @Nullable Direction side) {
        state.setTargetSideOverride(side);
    }

    @Nullable
    @Override
    public Direction getTargetSideOverride(S state) {
        return state.getTargetSideOverride();
    }

    @Override
    public PartTarget getTarget(PartPos pos, S state) {
        PartTarget target = PartTarget.fromCenter(pos);
        Direction sideOverride = getTargetSideOverride(state);
        if (sideOverride != null) {
            target = target.forTargetSide(sideOverride);
        }
        Vec3i offset = getTargetOffset(state);
        if (offset.compareTo(Vec3i.ZERO) != 0) {
            target = target.forOffset(offset);
        }
        return target;
    }

    protected boolean hasOffsetVariables(S state) {
        NonNullList<ItemStack> inventory = state.getInventoryNamed("offsetVariablesInventory");
        return inventory != null && inventory.stream().anyMatch(item -> !item.isEmpty());
    }

    @Override
    public void onOffsetVariablesChanged(PartTarget target, S state) {
        state.markOffsetVariablesChanged();
    }

    @Override
    public void onAspectVariablesChanged(PartTarget target, S state) {
        state.markAspectVariablesChanged();
    }

    protected boolean hasAspectVariables(S state) {
        return state.getInventoriesNamed().entrySet().stream()
                .anyMatch(entry -> entry.getKey().startsWith(PartStateAspectVariablesHandler.INVENTORY_NAME_PREFIX)
                        && entry.getValue().stream().anyMatch(item -> !item.isEmpty()));
    }

    @Override
    public boolean isUpdate(S state) {
        return hasOffsetVariables(state) || hasAspectVariables(state);
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        state.updateOffsetVariables((P) this, network, partNetwork, target);
        state.updateAspectVariables((P) this, network, partNetwork, target);
    }

    @Override
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkReAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        // This resets any errored offset and aspect variables and forces them to reload.
        state.markOffsetVariablesChanged();
        state.markAspectVariablesChanged();
    }

    @Override
    public ItemStack getItemStack(ValueDeseralizationContext valueDeseralizationContext, ProblemReporter.PathElement problemPath, S state, boolean saveState) {
        ItemStack itemStack = new ItemStack(getItem());
        if (saveState) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(problemPath, LOGGER)) {
                TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
                serializeState(valueOutput, state);
                itemStack.set(RegistryEntries.DATACOMPONENT_PART_STATE, valueOutput.buildResult());
            }
        }
        return itemStack;
    }

    @Override
    public ItemStack getCloneItemStack(Level world, BlockPos pos, S state) {
        return getItemStack(ValueDeseralizationContext.of(world), new PartPathElement(pos), state, false);
    }

    @Override
    public S getState(ValueDeseralizationContext valueDeseralizationContext, ProblemReporter.PathElement problemPath, ItemStack itemStack) {
        S partState = null;
        if(!itemStack.isEmpty() && itemStack.has(RegistryEntries.DATACOMPONENT_PART_STATE)) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(problemPath, LOGGER)) {
                ValueInput input = TagValueInput.create(
                        scopedCollector,
                        valueDeseralizationContext.holderLookupProvider(),
                        itemStack.get(RegistryEntries.DATACOMPONENT_PART_STATE)
                );
                partState = deserializeState(input);
            }
        }
        if(partState == null) {
            partState = defaultBlockState();
        }
        return partState;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public S defaultBlockState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        defaultState.gatherCapabilities((P) this);
        return defaultState;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        if(dropMainElement) {
            itemStacks.add(getItemStack(ValueDeseralizationContext.of(target.getCenter().getPos().getLevel(true)), new PartPathElement(target.getCenter().getPos().getBlockPos()), state, saveState));
        }

        // Drop contents of named inventories
        for (Map.Entry<String, NonNullList<ItemStack>> entry : state.getInventoriesNamed().entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                if (!itemStack.isEmpty()) {
                    itemStacks.add(itemStack);
                }
            }
        }
        state.clearInventoriesNamed();
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        state.initializeOffsets(target);
    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public InteractionResult onPartActivated(S partState, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public void updateTick(Level world, BlockPos pos, S partState, RandomSource random) {

    }

    @Override
    public void onPreRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, S state,
                                      BlockGetter world, @Nullable Direction side) {

    }

    @Override
    public int getConsumptionRate(S state) {
        return 0;
    }

    @Override
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, boolean updated) {
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

    @Override
    public void loadTooltip(S state, List<Component> lines) {

    }

    @Override
    public void loadTooltip(ItemStack itemStack, Consumer<Component> tooltipAdder) {

    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return oldPartState == null || newPartState == null || oldPartState.isForceBlockRenderUpdateAndReset();
    }

    @Override
    public boolean hasEventSubscriptions() {
        return false;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Collections.emptySet();
    }

    @Override
    public void onEvent(INetworkEvent event, IPartNetworkElement<P, S> networkElement) {

    }
}
