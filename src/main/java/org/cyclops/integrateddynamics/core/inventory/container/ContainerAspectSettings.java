package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionServer;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiAspectSettings;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

/**
 * Container for aspect settings.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ContainerAspectSettings extends ExtendedInventoryContainer {

    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final World world;
    private final BlockPos pos;
    private final IAspect aspect;

    private final BiMap<Integer, IAspectPropertyTypeInstance> propertyIds = HashBiMap.create();

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param aspect The aspect.
     */
    public ContainerAspectSettings(final EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType, IAspect aspect) {
        super(player.inventory, (IGuiContainerProvider) partType);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();
        this.aspect = aspect;

        addPlayerInventory(player.inventory, 8, 131);

        for(IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
            propertyIds.put(getNextValueId(), property);
        }

        putButtonAction(GuiAspectSettings.BUTTON_EXIT, new IButtonActionServer<InventoryContainer>() {
            @Override
            public void onAction(int buttonId, InventoryContainer container) {
                if (!world.isRemote) {
                    IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide());
                    BlockPos pos = getTarget().getCenter().getPos().getBlockPos();
                    player.openGui(IntegratedDynamics._instance.getModId(), ((IGuiContainerProvider) getPartType()).getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        });
    }

    @Override
    protected void initializeValues() {
        super.initializeValues();
        IAspectProperties properties = aspect.getProperties(getPartType(), getTarget(), getPartState());
        for(IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
            setValue(property, properties.getValue(property));
        }
    }

    public void setValue(IAspectPropertyTypeInstance property, IValue value) {
        ValueNotifierHelpers.setValue(this, propertyIds.inverse().get(property), ValueHelpers.serializeRaw(value));
    }

    public IPartState getPartState() {
        return partContainer.getPartState(getTarget().getCenter().getSide());
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    public <T extends IValueType<V>, V extends IValue> V getPropertyValue(IAspectPropertyTypeInstance<T, V> property) {
        if(propertyIds.containsValue(property)) {
            String value = ValueNotifierHelpers.getValueString(this, propertyIds.inverse().get(property));
            if(value != null) {
                return ValueHelpers.deserializeRaw(property.getType(), value);
            }
        }
        return null;
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        if(!world.isRemote) {
            IAspectPropertyTypeInstance property = propertyIds.get(valueId);
            if (property != null) {
                IAspectProperties aspectProperties = getAspect().getProperties(getPartType(), getTarget(), getPartState());
                aspectProperties = aspectProperties.clone();
                IValue trueValue = ValueHelpers.deserializeRaw(property.getType(), value.getString(ValueNotifierHelpers.KEY));
                aspectProperties.setValue(property, trueValue);
                getAspect().setProperties(getPartType(), getTarget(), getPartState(), aspectProperties);

                // Changing the properties might cause some erroring variables to become valid again, so trigger an update.
                INetwork network = NetworkHelpers.getNetwork(getTarget().getCenter().getPos().getWorld(), getTarget().getCenter().getPos().getBlockPos(), getTarget().getCenter().getSide());
                if (network != null) {
                    network.getEventBus().post(new VariableContentsUpdatedEvent(network));
                }
            }
        }
    }
}
