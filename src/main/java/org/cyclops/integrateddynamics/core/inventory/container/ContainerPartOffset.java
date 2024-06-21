package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Container for part offsets.
 * @author rubensworks
 */
public class ContainerPartOffset extends InventoryContainer {

    public static final String BUTTON_SAVE = "button_save";

    private final PartTarget target;
    private final Optional<IPartContainer> partContainer;
    private final IPartType partType;
    private final Level world;

    private final int lastXValueId;
    private final int lastYValueId;
    private final int lastZValueId;
    private final List<Integer> offsetVariableSlotErrorIds;
    private final List<Integer> offsetVariableSlotFilled;
    private final int maxOffsetId;

    private final SimpleInventory offsetVariablesInventory;
    private boolean dirtyInv = false;

    public ContainerPartOffset(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, new SimpleContainer(0),
                PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartOffset(int id, Inventory playerInventory, Container inventory,
                               PartTarget target, Optional<IPartContainer> partContainer, IPartType partType) {
      this(RegistryEntries.CONTAINER_PART_OFFSET.get(), id, playerInventory, inventory, target, partContainer, partType);
    }

    public ContainerPartOffset(@Nullable MenuType<?> type, int id, Inventory playerInventory, Container inventory,
                               PartTarget target, Optional<IPartContainer> partContainer, IPartType partType) {
        super(type, id, playerInventory, inventory);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getCommandSenderWorld();

        addPlayerInventory(player.getInventory(), 27, getPlayerInventoryOffsetY());

        lastXValueId = getNextValueId();
        lastYValueId = getNextValueId();
        lastZValueId = getNextValueId();
        this.offsetVariableSlotErrorIds = Lists.newArrayList();
        this.offsetVariableSlotFilled = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            this.offsetVariableSlotErrorIds.add(getNextValueId());
            this.offsetVariableSlotFilled.add(getNextValueId());
        }
        this.maxOffsetId = getNextValueId();

        putButtonAction(ContainerPartOffset.BUTTON_SAVE, (s, containerExtended) -> {
            if(!world.isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayer) player, target.getCenter(), getPartType());
            }
        });

        offsetVariablesInventory = new SimpleInventory(3, 1);
        offsetVariablesInventory.addDirtyMarkListener(() -> dirtyInv = true);
        if (!player.level().isClientSide) {
            getPartState().loadInventoryNamed("offsetVariablesInventory", offsetVariablesInventory);
        }
        addSlot(new SlotVariable(offsetVariablesInventory, 0, 45, 51));
        addSlot(new SlotVariable(offsetVariablesInventory, 1, 99, 51));
        addSlot(new SlotVariable(offsetVariablesInventory, 2, 153, 51));
    }

    public IPartType getPartType() {
        return partType;
    }

    public PartTarget getTarget() {
        return target;
    }

    public int getLastXValueId() {
        return lastXValueId;
    }

    public int getLastYValueId() {
        return lastYValueId;
    }

    public int getLastZValueId() {
        return lastZValueId;
    }

    public int getMaxOffsetId() {
        return maxOffsetId;
    }

    protected int getPlayerInventoryOffsetY() {
        return 73;
    }

    @Override
    protected void initializeValues() {
        Vec3i offset = getPartType().getTargetOffset(getPartState());
        ValueNotifierHelpers.setValue(this, lastXValueId, offset.getX());
        ValueNotifierHelpers.setValue(this, lastYValueId, offset.getY());
        ValueNotifierHelpers.setValue(this, lastZValueId, offset.getZ());
        ValueNotifierHelpers.setValue(this, maxOffsetId, getPartState().getMaxOffset());
    }

    public int getLastXValue() {
        return ValueNotifierHelpers.getValueInt(this, lastXValueId);
    }

    public int getLastYValue() {
        return ValueNotifierHelpers.getValueInt(this, lastYValueId);
    }

    public int getLastZValue() {
        return ValueNotifierHelpers.getValueInt(this, lastZValueId);
    }

    public int getMaxOffset() {
        return ValueNotifierHelpers.getValueInt(this, maxOffsetId);
    }

    public IPartState getPartState() {
        return partContainer.get().getPartState(getTarget().getCenter().getSide());
    }

    @Override
    public boolean stillValid(Player player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer.get());
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);
        try {
            if(!world.isClientSide()) {
                updatePartOffset();
            }
        } catch (PartStateException e) {
            player.closeContainer();
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!player.level().isClientSide) {
            IPartState partState = getPartState();

            if (this.dirtyInv) {
                this.dirtyInv = false;
                partState.saveInventoryNamed("offsetVariablesInventory", offsetVariablesInventory);
                getPartType().onOffsetVariablesChanged(getTarget(), partState);
            }

            for (int i = 0; i < 3; i++) {
                ValueNotifierHelpers.setValue(this, this.offsetVariableSlotErrorIds.get(i), partState.getOffsetVariableError(i));
                ValueNotifierHelpers.setValue(this, this.offsetVariableSlotFilled.get(i), !offsetVariablesInventory.getItem(i).isEmpty());
            }

            Vec3i offset = getPartType().getTargetOffset(getPartState());
            ValueNotifierHelpers.setValue(this, lastXValueId, offset.getX());
            ValueNotifierHelpers.setValue(this, lastYValueId, offset.getY());
            ValueNotifierHelpers.setValue(this, lastZValueId, offset.getZ());
        }
    }

    @Nullable
    public Component getOffsetVariableError(int slot) {
        return ValueNotifierHelpers.getValueTextComponent(this, this.offsetVariableSlotErrorIds.get(slot));
    }

    public boolean isOffsetVariableFilled(int slot) {
        return ValueNotifierHelpers.getValueBoolean(this, this.offsetVariableSlotFilled.get(slot));
    }

    protected void updatePartOffset() {
        Vec3i offset = new Vec3i(getLastXValue(), getLastYValue(), getLastZValue());
        getPartType().setTargetOffset(getPartState(), getTarget().getCenter(), offset);
    }
}
