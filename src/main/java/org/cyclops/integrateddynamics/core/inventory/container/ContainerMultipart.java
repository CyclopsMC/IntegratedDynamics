package org.cyclops.integrateddynamics.core.inventory.container;

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
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Container for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>, A extends IAspect>
        extends ScrollingInventoryContainer<A> implements IDirtyMarkListener {

    private static final int PAGE_SIZE = 3;
    public static final int ASPECT_BOX_HEIGHT = 36;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;
    private final World world;
    private final BlockPos pos;

    protected final IInventory inputSlots;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param partState The part state.
     */
    public ContainerMultipart(EntityPlayer player, PartTarget target, IPartContainer partContainer, P partType, S partState, List<A> items) {
        super(player.inventory, partType, items, new IItemPredicate<A>() {
            @Override
            public boolean apply(A item, Pattern pattern) {
                // We could cache this if this would prove to be a bottleneck.
                // But we have a small amount of aspects, so this shouldn't be a problem.
                return pattern.matcher(L10NHelpers.localize(item.getUnlocalizedName()).toLowerCase()).matches();
            }
        });
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.partState = partState;
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();

        this.inputSlots = constructInputSlotsInventory();
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotSingleItem(inputSlots, i, 96, 27 + ASPECT_BOX_HEIGHT * i, ItemVariable.getInstance()));
            disableSlot(i);
        }
    }

    protected IInventory constructInputSlotsInventory() {
        SimpleInventory inventory = new SimpleInventory(getUnfilteredItemCount(), "temporaryInputSlots", 1);
        inventory.addDirtyMarkListener(this);
        return inventory;
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = Integer.MIN_VALUE;
        slot.yDisplayPosition = Integer.MIN_VALUE;
    }

    protected void enableSlot(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex);
        slot.xDisplayPosition = 96;
        slot.yDisplayPosition = 27 + ASPECT_BOX_HEIGHT * row;
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    protected void onScroll() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i);
        }
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, A element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlot(elementIndex, row);
    }

    @Override
    protected int getSizeInventory() {
        return getUnfilteredItemCount(); // Input and output slots per item
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public ItemStack writeAspectInfo(ItemStack itemStack, IAspect aspect) {
        return Aspects.REGISTRY.writeAspect(itemStack, getPartState().getId(), aspect);
    }

}
