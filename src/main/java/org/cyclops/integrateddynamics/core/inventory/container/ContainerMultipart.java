package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.regex.Pattern;

/**
 * Container for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ScrollingInventoryContainer<IAspect> {

    private static final int PAGE_SIZE = 3;
    public static final int ASPECT_BOX_HEIGHT = 36;

    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;
    private final World world;
    private final BlockPos pos;
    private final IInventory inputSlots;
    private final IInventory outputSlots;

    /**
     * Make a new instance.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param partState The part state.
     */
    public ContainerMultipart(EntityPlayer player, IPartContainer partContainer, P partType, S partState) {
        super(player.inventory, partType, Lists.newArrayList(partType.getAspects()), new IItemPredicate<IAspect>() {
            @Override
            public boolean apply(IAspect item, Pattern pattern) {
                // We could cache this if this would prove to be a bottleneck.
                // But we have a small amount of aspects, so this shouldn't be a problem.
                return pattern.matcher(L10NHelpers.localize(item.getUnlocalizedName())).matches();
            }
        });
        this.partContainer = partContainer;
        this.partType = partType;
        this.partState = partState;
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();

        this.inputSlots = new SimpleInventory(getUnfilteredItemCount(), "temporaryInputSlots", 1) {
            @Override
            public void onInventoryChanged() {
                processInput();
            }
        };
        this.outputSlots = new SimpleInventory(getUnfilteredItemCount(), "temporaryOutputSlots", 1);

        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotSingleItem(inputSlots, i, 96, 27 + ASPECT_BOX_HEIGHT * i, ItemVariable.getInstance()));
            disableSlot(i, true);
        }
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotRemoveOnly(outputSlots, i, 144, 27 + ASPECT_BOX_HEIGHT * i));
            disableSlot(i, false);
        }

        addPlayerInventory(player.inventory, 9, 131);
    }

    protected void disableSlot(int slotIndex, boolean input) {
        Slot slot = getSlot(slotIndex + (input ? 0 : getUnfilteredItemCount()));
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = Integer.MIN_VALUE;
        slot.yDisplayPosition = Integer.MIN_VALUE;
    }

    protected void enableSlot(int slotIndex, int row, boolean input) {
        Slot slot = getSlot(slotIndex + (input ? 0 : getUnfilteredItemCount()));
        slot.xDisplayPosition = input ? 96 : 144;
        slot.yDisplayPosition = 27 + ASPECT_BOX_HEIGHT * row;
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    protected void onScroll() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i, true);
            disableSlot(i, false);
        }
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, IAspect element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlot(elementIndex, row, true);
        enableSlot(elementIndex, row, false);
    }

    @Override
    protected int getSizeInventory() {
        return getUnfilteredItemCount() * 2; // Input and output slots per item
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!world.isRemote) {
            for (int i = 0; i < getUnfilteredItemCount(); ++i) {
                ItemStack itemstack;
                itemstack = inputSlots.getStackInSlotOnClosing(i);
                if (itemstack != null) {
                    player.dropPlayerItemWithRandomChoice(itemstack, false);
                }
                itemstack = outputSlots.getStackInSlotOnClosing(i);
                if (itemstack != null) {
                    player.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    protected ItemStack writeAspectInfo(ItemStack itemStack, IAspect aspect) {
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class)
                .writeAspect(itemStack, getPartState().getId(), aspect);
    }

    protected void processInput() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            ItemStack itemStack = inputSlots.getStackInSlot(i);
            if(itemStack != null && outputSlots.getStackInSlot(i) == null) {
                ItemStack outputStack = writeAspectInfo(itemStack.copy(), getUnfilteredItems().get(i));
                outputSlots.setInventorySlotContents(i, outputStack);
                inputSlots.decrStackSize(i, 1);
            }
        }
    }

}
