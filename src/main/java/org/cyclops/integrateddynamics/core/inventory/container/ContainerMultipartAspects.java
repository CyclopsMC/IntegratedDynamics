package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionServer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Container for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerMultipartAspects<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>, A extends IAspect>
        extends ScrollingInventoryContainer<A> implements IDirtyMarkListener {

    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final World world;
    private final BlockPos pos;
    private final Map<IAspect, Integer> aspectPropertyButtons = Maps.newHashMap();

    protected final IInventory inputSlots;
    protected final EntityPlayer player;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param items The items.
     */
    public ContainerMultipartAspects(EntityPlayer player, PartTarget target, IPartContainer partContainer, P partType, List<A> items) {
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
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();

        this.inputSlots = constructInputSlotsInventory();
        this.player = player;

        putButtonAction(BUTTON_SETTINGS, new IButtonActionServer<InventoryContainer>() {
            @Override
            public void onAction(int buttonId, InventoryContainer container) {
                IGuiContainerProvider gui = ((PartTypeConfigurable) getPartType()).getSettingsGuiProvider();
                IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide()); // Pass the side as extra data to the gui
                if(!MinecraftHelpers.isClientSide()) {
                    BlockPos cPos = getTarget().getCenter().getPos().getBlockPos();
                    ContainerMultipartAspects.this.player.openGui(gui.getMod(), gui.getGuiID(),
                            world, cPos.getX(), cPos.getY(), cPos.getZ());
                }
            }
        });

        int nextButtonId = 2;
        for(final IAspect aspect : getUnfilteredItems()) {
            if(aspect.hasProperties()) {
                aspectPropertyButtons.put(aspect, nextButtonId);
                putButtonAction(nextButtonId, new IButtonActionServer<InventoryContainer>() {
                    @Override
                    public void onAction(int buttonId, InventoryContainer container) {
                        IGuiContainerProvider gui = aspect.getPropertiesGuiProvider();
                        IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.ASPECT, Pair.of(getTarget().getCenter().getSide(), aspect));
                        if (!MinecraftHelpers.isClientSide()) {
                            BlockPos cPos = getTarget().getCenter().getPos().getBlockPos();
                            ContainerMultipartAspects.this.player.openGui(gui.getMod(), gui.getGuiID(),
                                    world, cPos.getX(), cPos.getY(), cPos.getZ());
                        }
                    }
                });
                nextButtonId++;
            }
        }
    }

    public Map<IAspect, Integer> getAspectPropertyButtons() {
        return Collections.unmodifiableMap(this.aspectPropertyButtons);
    }

    @SuppressWarnings("unchecked")
    public S getPartState() {
        return (S) partContainer.getPartState(getTarget().getCenter().getSide());
    }

    public abstract int getAspectBoxHeight();

    protected IInventory constructInputSlotsInventory() {
        SimpleInventory inventory = new SimpleInventory(getUnfilteredItemCount(), "temporaryInputSlots", 1);
        inventory.addDirtyMarkListener(this);
        return inventory;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        if(inputSlots instanceof SimpleInventory) {
            ((SimpleInventory) inputSlots).removeDirtyMarkListener(this);
        }
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = Integer.MIN_VALUE;
        slot.yDisplayPosition = Integer.MIN_VALUE;
    }

    protected abstract void enableSlot(int slotIndex, int row);

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

    public ItemStack writeAspectInfo(boolean generateId, ItemStack itemStack, final IAspect aspect) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, Aspects.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IAspectVariableFacade>() {
            @Override
            public IAspectVariableFacade create(boolean generateId) {
                return new AspectVariableFacade(generateId, getPartState().getId(), aspect);
            }

            @Override
            public IAspectVariableFacade create(int id) {
                return new AspectVariableFacade(id, getPartState().getId(), aspect);
            }
        });
    }

}
