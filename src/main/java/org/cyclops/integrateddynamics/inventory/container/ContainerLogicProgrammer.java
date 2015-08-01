package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.item.ItemVariable;

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

    private final SimpleInventory writeSlot;
    private IOperator activeOperator = null;
    private IVariableFacade[] inputVariables = new IVariableFacade[0];
    private SimpleInventory temporaryInputSlots = null;

    @SideOnly(Side.CLIENT)
    private GuiLogicProgrammer gui;

    /**
     * Make a new instance.
     * @param inventory   The player inventory.
     */
    public ContainerLogicProgrammer(InventoryPlayer inventory) {
        super(inventory, BlockLogicProgrammer.getInstance(), getOperators(), FILTERER);
        this.writeSlot = new SimpleInventory(1, "writeSlot", 1);
        this.writeSlot.addDirtyMarkListener(this);
        this.writeSlot.addDirtyMarkListener(new IDirtyMarkListener() {
            @Override
            public void onDirty() {
                if (temporaryInputSlots == null || temporaryInputSlots.isEmpty()) {
                    ItemStack itemStack = writeSlot.getStackInSlot(0);
                    if (itemStack != null) {
                        ContainerLogicProgrammer.this.loadConfigFrom(itemStack);
                    }
                }
            }
        });
        this.temporaryInputSlots = new SimpleInventory(0, "temporaryInput", 1);
        initializeSlots();
    }

    @SideOnly(Side.CLIENT)
    public void setGui(GuiLogicProgrammer gui) {
        this.gui = gui;
    }

    @SideOnly(Side.CLIENT)
    public GuiLogicProgrammer getGui() {
        return this.gui;
    }

    protected void initializeSlots() {
        addSlotToContainer(new SlotSingleItem(writeSlot, 0, 232, 110, ItemVariable.getInstance()));
        addPlayerInventory((InventoryPlayer) getPlayerIInventory(), 88, 131);
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

    /**
     * Set the new active operator.
     * @param activeOperator The new operator.
     * @param baseX The slots X coordinate
     * @param baseY The slots Y coordinate
     */
    public void setActiveOperator(IOperator activeOperator, int baseX, int baseY) {
        this.activeOperator = activeOperator;
        this.inputVariables = new IVariableFacade[activeOperator == null ? 0 : activeOperator.getRequiredInputLength()];

        // This assumes that there is only one other slot, the remaining slots will be erased!
        // (We can do this because they are all ghost slots)
        inventoryItemStacks = Lists.newArrayList();
        inventorySlots = Lists.newArrayList();
        initializeSlots();
        this.temporaryInputSlots.removeDirtyMarkListener(this);
        this.temporaryInputSlots = new SimpleInventory(inputVariables.length, "temporaryInput", 1);
        // Don't add 'this', or we'll have infinite loops
        temporaryInputSlots.addDirtyMarkListener(this);
        if(activeOperator != null) {
            Pair<Integer, Integer>[] slotPositions = activeOperator.getRenderPattern().getSlotPositions();
            for (int i = 0; i < temporaryInputSlots.getSizeInventory(); i++) {
                SlotSingleItem slot = new SlotSingleItem(temporaryInputSlots, i, 1 + baseX + slotPositions[i].getLeft(),
                        1 + baseY + slotPositions[i].getRight(), ItemVariable.getInstance());
                slot.setPhantom(true);
                addSlotToContainer(slot);
            }
        }
    }

    public boolean canWriteActiveOperator() {
        if(activeOperator != null) {
            for (IVariableFacade inputVariable : inputVariables) {
                if (inputVariable == null || !inputVariable.isValid()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public IOperator getActiveOperator() {
        return activeOperator;
    }

    public ItemStack writeOperatorInfo(boolean generateId, ItemStack itemStack, IVariableFacade[] inputVariables, final IOperator operator) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        final int[] variableIds = new int[inputVariables.length];
        for(int i = 0; i < inputVariables.length; i++) {
            variableIds[i] = inputVariables[i].getId();
        }
        return registry.writeVariableFacade(generateId, itemStack, Operators.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<OperatorVariableFacade>() {
            @Override
            public OperatorVariableFacade create(boolean generateId) {
                return new OperatorVariableFacade(generateId, operator, variableIds);
            }

            @Override
            public OperatorVariableFacade create(int id) {
                return new OperatorVariableFacade(id, operator, variableIds);
            }
        });
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote) {
            ItemStack itemStack = writeSlot.getStackInSlot(0);
            if(itemStack != null) {
                player.dropPlayerItemWithRandomChoice(itemStack, false);
            }
        }
    }

    @Override
    public void onDirty() {
        for(int i = 0; i < temporaryInputSlots.getSizeInventory(); i++) {
            ItemStack itemStack = temporaryInputSlots.getStackInSlot(i);
            IVariableFacade variableFacade = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).handle(itemStack);
            inputVariables[i] = variableFacade;
        }

        ItemStack itemStack = writeSlot.getStackInSlot(0);
        if(itemStack != null && canWriteActiveOperator()) {
            ItemStack outputStack = writeOperatorInfo(!MinecraftHelpers.isClientSide(), itemStack.copy(), inputVariables, getActiveOperator());
            writeSlot.removeDirtyMarkListener(this);
            writeSlot.setInventorySlotContents(0, outputStack);
            writeSlot.addDirtyMarkListener(this);
        }
    }

    protected void loadConfigFrom(ItemStack itemStack) {
        // Only do this client-side, a packet will be sent to do the same server-side.
        if(MinecraftHelpers.isClientSide()) {
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(itemStack);
            if (variableFacade instanceof OperatorVariableFacade) {
                OperatorVariableFacade operatorFacade = (OperatorVariableFacade) variableFacade;
                if (operatorFacade.isValid()) {
                    IOperator operator = operatorFacade.getOperator();
                    getGui().handleOperatorActivation(operator);
                }
            }
        }
    }

}
