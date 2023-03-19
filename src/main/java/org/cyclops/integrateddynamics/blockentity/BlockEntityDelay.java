package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.evaluate.DelayVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.DelayVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerDelay;
import org.cyclops.integrateddynamics.network.DelayNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Queue;

/**
 * A part entity for the variable delay.
 *
 * @author rubensworks
 */
public class BlockEntityDelay extends BlockEntityProxy implements MenuProvider {

    public static final int INVENTORY_SIZE = 3;

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
    private Player lastPlayer = null;
    private EvaluationException lastError = null;

    public BlockEntityDelay(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_DELAY, blockPos, blockState, BlockEntityDelay.INVENTORY_SIZE);
        this.variable = new VariableAdapter<ValueTypeList.ValueList>() {

            @Override
            public ValueTypeList getType() {
                return ValueTypes.LIST;
            }

            @Override
            public ValueTypeList.ValueList getValue() throws EvaluationException {
                if (lastError != null) {
                    throw lastError;
                }
                return list;
            }
        };

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new DelayNetworkElement(DimPos.of(world, blockPos));
            }
        }));
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
        }, lastPlayer, getBlockState());
    }

    @Override
    public IVariable<?> getVariable(IPartNetwork network) {
        return variable;
    }

    public IVariable<?> getVariableSuper(IPartNetwork network) {
        return super.getVariable(network);
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.values = Queues.newArrayBlockingQueue(this.capacity);
    }

    public Queue<IValue> getValues() {
        if (values == null) {
            values = Queues.newArrayBlockingQueue(this.capacity);
        }
        return values;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag valueList = new ListTag();
        for (IValue value : getValues()) {
            valueList.add(ValueHelpers.serialize(value));
        }
        tag.put("values", valueList);
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        if (this.capacity <= 0) this.capacity = 1;
        values = Queues.newArrayBlockingQueue(this.capacity);

        ListTag valueList = tag.getList("values", Tag.TAG_COMPOUND);
        for (int i = 0; i < valueList.size(); i++) {
            IValue value = ValueHelpers.deserialize(valueList.getCompound(i));
            if (value != null) {
                this.values.add(value);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerDelay(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.integrateddynamics.delay");
    }

    public static class Ticker extends BlockEntityProxy.Ticker<BlockEntityDelay> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityDelay blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (!level.isClientSide && blockEntity.updateInterval > 0 && level.getGameTime() % blockEntity.updateInterval == 0) {
                // Remove oldest elements from the queue until we have room for a new one.
                while (blockEntity.getValues().size() >= blockEntity.capacity) {
                    blockEntity.getValues().poll();
                }

                IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(blockEntity.getNetwork()).orElse(null);
                if (partNetwork == null) {
                    return;
                }

                // Add new value to the queue
                IVariable<?> variable = blockEntity.getVariableSuper(partNetwork);
                IValue value = null;
                if (variable != null) {
                    try {
                        value = variable.getValue();
                        blockEntity.lastError = null;
                    } catch (EvaluationException e) {
                        blockEntity.getEvaluator().addError(e.getErrorMessage());
                        blockEntity.lastError = e;
                    }
                    if (value != null) {
                        try {
                            if (blockEntity.list.getRawValue().getLength() > 0 && blockEntity.list.getRawValue().getValueType() != value.getType()) {
                                blockEntity.getValues().clear();
                            }
                        } catch (EvaluationException e) {}
                        blockEntity.getValues().add(value);

                        // Update variable with as value the materialized queue list
                        blockEntity.list = ValueTypeList.ValueList.ofList(value.getType(), Lists.newArrayList(blockEntity.values));
                    }
                } else {
                    blockEntity.getValues().clear();
                    blockEntity.list = ValueTypes.LIST.getDefault();
                }
                blockEntity.variable.invalidate();
            }
        }
    }
}
