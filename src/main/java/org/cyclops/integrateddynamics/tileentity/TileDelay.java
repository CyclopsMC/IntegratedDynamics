package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.DelayVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.DelayVariableFacade;

import java.util.Queue;

/**
 * A part entity for the variable delay.
 *
 * @author rubensworks
 */
public class TileDelay extends TileProxy {

    protected Queue<IValue> values = null;
    @NBTPersist
    @Getter
    private int capacity = 5;
    @NBTPersist
    @Getter
    @Setter
    private int updateInterval = 1;
    private ValueTypeList.ValueList list = ValueTypes.LIST.getDefault();
    private final IVariable<?> variable;

    @Setter
    private EntityPlayer lastPlayer = null;

    public TileDelay() {
        this.variable = new IVariable<ValueTypeList.ValueList>() {

            @Override
            public ValueTypeList getType() {
                return ValueTypes.LIST;
            }

            @Override
            public ValueTypeList.ValueList getValue() throws EvaluationException {
                return list;
            }
        };
    }

    @Override
    public ItemStack writeProxyInfo(boolean generateId, ItemStack itemStack, final int proxyId) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, DelayVariableFacadeHandler.getInstance(), new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IDelayVariableFacade>() {
            @Override
            public IDelayVariableFacade create(boolean generateId) {
                return new DelayVariableFacade(generateId, proxyId);
            }

            @Override
            public IDelayVariableFacade create(int id) {
                return new DelayVariableFacade(id, proxyId);
            }
        }, lastPlayer, getBlock());
    }

    @Override
    public IVariable<?> getVariable(IPartNetwork network) {
        return variable;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        this.values = Queues.newArrayBlockingQueue(this.capacity);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTTagList valueList = new NBTTagList();
        for (IValue value : values) {
            valueList.appendTag(ValueHelpers.serialize(value));
        }
        tag.setTag("values", valueList);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        values = Queues.newArrayBlockingQueue(this.capacity);

        NBTTagList valueList = tag.getTagList("values", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for (int i = 0; i < valueList.tagCount(); i++) {
            IValue value = ValueHelpers.deserialize(valueList.getCompoundTagAt(i));
            if (value != null) {
                this.values.add(value);
            }
        }
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!getWorld().isRemote && updateInterval > 0 && getWorld().getTotalWorldTime() % updateInterval == 0) {
            // Remove oldest elements from the queue until we have room for a new one.
            while (values.size() >= this.capacity) {
                values.poll();
            }

            // Add new value to the queue
            IVariable<?> variable = super.getVariable(NetworkHelpers.getPartNetwork(getNetwork()));
            IValue value = null;
            if (variable != null) {
                try {
                    value = variable.getValue();
                } catch (EvaluationException e) {
                    addError(new L10NHelpers.UnlocalizedString(e.toString()));
                }
                if (value != null) {
                    values.add(value);

                    // Update variable with as value the materialized queue list
                    this.list = ValueTypeList.ValueList.ofList(value.getType(), Lists.newArrayList(values));
                }
            } else {
                values.clear();
                this.list = ValueTypes.LIST.getDefault();
            }
        }
    }
}
