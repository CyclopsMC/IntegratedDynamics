package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    public ContainerLabeller(int id, Inventory inventory, FriendlyByteBuf packetBuffer) {
        this(id, inventory, readItemIndex(packetBuffer), readHand(packetBuffer));
    }

    public ContainerLabeller(int id, Inventory inventory, int itemIndex, InteractionHand hand) {
        super(RegistryEntries.CONTAINER_LABELLER, id, inventory, itemIndex, hand);
        this.temporaryInputSlots = new SimpleInventory(1, 1);
        addSlot(new SlotExtended(temporaryInputSlots, 0, 8, 8));
        this.addPlayerInventory(player.getInventory(), 8, 31);

        if(inventory.player.level.isClientSide()) {
            temporaryInputSlots.addDirtyMarkListener(() -> {
                ItemStack itemStack = temporaryInputSlots.getItem(0);
                IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
                IVariableFacade variableFacade = registry.handle(itemStack);
                String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableFacade.getId());
                if(label == null && !itemStack.isEmpty() && itemStack.hasCustomHoverName()) {
                    label = itemStack.getHoverName().getString();
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
        return temporaryInputSlots.getItem(0);
    }

    @Override
    protected int getSizeInventory() {
        return 1;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level.isClientSide()) {
            ItemStack itemStack = temporaryInputSlots.getItem(0);
            if(!itemStack.isEmpty()) {
                player.drop(itemStack, false);
            }
        }
    }

    public void setItemStackName(String name) {
        ItemStack itemStack = getItemStack();
        if(!itemStack.isEmpty()) {
            if (StringUtils.isBlank(name)) {
                itemStack.resetHoverName();
            } else {
                itemStack.setHoverName(new TextComponent(name));
            }
        }
    }
}
