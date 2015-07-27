package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Container for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammer extends ScrollingInventoryContainer<IOperator> implements IDirtyMarkListener {

    protected static final IItemPredicate<IOperator> FILTERER = new IItemPredicate<IOperator>(){

        @Override
        public boolean apply(IOperator item, Pattern pattern) {
            return pattern.matcher(L10NHelpers.localize(item.getUnlocalizedName()).toLowerCase()).matches();
        }
    };

    private final SimpleInventory temporarySlots;

    /**
     * Make a new instance.
     * @param inventory   The player inventory.
     */
    public ContainerLogicProgrammer(InventoryPlayer inventory) {
        super(inventory, BlockLogicProgrammer.getInstance(), getOperators(), FILTERER);

        this.temporarySlots = new SimpleInventory(1, "temporarySlots", 1);
        this.temporarySlots.addDirtyMarkListener(this);
        addSlotToContainer(new SlotRemoveOnly(temporarySlots, 0, 232, 110));

        addPlayerInventory(inventory, 88, 131);
    }

    protected static List<IOperator> getOperators() {
        return Operators.REGISTRY.getOperators();
    }

    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    protected int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void onDirty() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            ItemStack itemStack = temporarySlots.getStackInSlot(i);
            if(itemStack != null && temporarySlots.getStackInSlot(i) == null) {
                // TODO: write operator to item
                //ItemStack outputStack = writeAspectInfo(!getWorld().isRemote, itemStack.copy(), getUnfilteredItems().get(i));
            }
        }
    }

}
