package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLabeller;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.item.ItemLabeller;

/**
 * Container for the labeller.
 * @author rubensworks
 */
public class ContainerLabeller extends ItemInventoryContainer<ItemLabeller> {

    private SimpleInventory temporaryInputSlots = null;

    @OnlyIn(Dist.CLIENT)
    private ContainerScreenLabeller gui;

    public ContainerLabeller(int id, PlayerInventory inventory, PacketBuffer packetBuffer) {
        this(id, inventory, readItemIndex(packetBuffer), readHand(packetBuffer));
    }

    public ContainerLabeller(int id, PlayerInventory inventory, int itemIndex, Hand hand) {
        super(RegistryEntries.CONTAINER_LABELLER, id, inventory, itemIndex, hand);
        this.temporaryInputSlots = new SimpleInventory(1, 1);
        addSlot(new SlotExtended(temporaryInputSlots, 0, 8, 8));
        this.addPlayerInventory(player.inventory, 8, 31);

        if(inventory.player.world.isRemote()) {
            temporaryInputSlots.addDirtyMarkListener(() -> {
                ItemStack itemStack = temporaryInputSlots.getStackInSlot(0);
                IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
                IVariableFacade variableFacade = registry.handle(itemStack);
                String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableFacade.getId());
                if(label == null && !itemStack.isEmpty() && itemStack.hasDisplayName()) {
                    label = itemStack.getDisplayName().getString();
                }
                if(label != null) {
                    ContainerLabeller.this.getGui().setText(label);
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void setGui(ContainerScreenLabeller gui) {
        this.gui = gui;
    }

    @OnlyIn(Dist.CLIENT)
    public ContainerScreenLabeller getGui() {
        return this.gui;
    }

    public ItemStack getItemStack() {
        return temporaryInputSlots.getStackInSlot(0);
    }

    @Override
    protected int getSizeInventory() {
        return 1;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote()) {
            ItemStack itemStack = temporaryInputSlots.getStackInSlot(0);
            if(!itemStack.isEmpty()) {
                player.dropItem(itemStack, false);
            }
        }
    }

    public void setItemStackName(String name) {
        ItemStack itemStack = getItemStack();
        if(!itemStack.isEmpty()) {
            if (StringUtils.isBlank(name)) {
                itemStack.clearCustomName();
            } else {
                itemStack.setDisplayName(new StringTextComponent(name));
            }
        }
    }
}
