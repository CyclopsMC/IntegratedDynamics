package org.cyclops.integrateddynamics.blockentity;

import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityActiveVariableBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.network.MaterializerNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the variable materializer.
 * @author rubensworks
 */
public class BlockEntityMaterializer extends BlockEntityActiveVariableBase<MaterializerNetworkElement> implements MenuProvider {

    public static final int INVENTORY_SIZE = 3;
    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    @Setter
    private Player lastPlayer = null;
    private boolean writeVariable;

    public BlockEntityMaterializer(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_MATERIALIZER.get(), blockPos, blockState, BlockEntityMaterializer.INVENTORY_SIZE);
    }

    public static <E> void registerMaterializerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityMaterializer> blockEntityType) {
        BlockEntityActiveVariableBase.registerActiveVariableBaseCapabilities(event, blockEntityType);

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                blockEntityType,
                (blockEntity, direction) -> {
                    int slot = -1;
                    switch (direction) {
                        case DOWN ->  slot = SLOT_WRITE_OUT;
                        case UP ->    slot = SLOT_WRITE_IN;
                        case NORTH -> slot = SLOT_READ;
                        case SOUTH -> slot = SLOT_READ;
                        case WEST ->  slot = SLOT_READ;
                        case EAST ->  slot = SLOT_READ;
                    }
                    return new ItemHandlerSlotMasked(blockEntity.getInventory(), slot);
                }
        );
        event.registerBlockEntity(
                Capabilities.NetworkElementProvider.BLOCK,
                blockEntityType,
                (blockEntity, direction) -> new NetworkElementProviderSingleton() {
                    @Override
                    public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                        return new MaterializerNetworkElement(DimPos.of(world, blockPos));
                    }
                }
        );
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack itemStack) {
                return slot != SLOT_WRITE_OUT && super.canPlaceItem(slot, itemStack);
            }
        };
    }

    @Override
    public int getSlotRead() {
        return SLOT_READ;
    }

    protected boolean canWrite() {
        return NetworkHelpers.getPartNetwork(getNetwork())
                .map(partNetwork -> getVariable(partNetwork) != null && getEvaluator().getErrors().isEmpty())
                .orElse(false);
    }

    @Override
    public void onDirty() {
        super.onDirty();
        if (!level.isClientSide()) {
            this.writeVariable = true;
        }
    }

    public ItemStack writeMaterialized(boolean generateId, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        IVariable variable = getVariable(NetworkHelpers.getPartNetworkChecked(getNetwork()));
        try {
            final IValue value = variable.getType().materialize(variable.getValue());
            final IValueType valueType = value.getType();
            return registry.writeVariableFacadeItem(generateId, itemStack, ValueTypes.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, valueType, value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, valueType, value);
                }
            }, getLevel(), lastPlayer, getBlockState());
        } catch (EvaluationException e) {
            getEvaluator().addError(Component.translatable(e.getMessage()));
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerMaterializer(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integrateddynamics.materializer");
    }

    public static class Ticker extends BlockEntityTickerDelayed<BlockEntityMaterializer> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityMaterializer blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.writeVariable && !blockEntity.getInventory().getItem(SLOT_WRITE_IN).isEmpty() && blockEntity.canWrite() && blockEntity.getInventory().getItem(SLOT_WRITE_OUT).isEmpty()) {
                blockEntity.writeVariable = false;

                // Write proxy reference
                ItemStack outputStack = blockEntity.writeMaterialized(!blockEntity.getLevel().isClientSide, blockEntity.getInventory().getItem(SLOT_WRITE_IN));
                if(!outputStack.isEmpty()) {
                    blockEntity.getInventory().setItem(SLOT_WRITE_OUT, outputStack);
                    blockEntity.getInventory().removeItemNoUpdate(SLOT_WRITE_IN);
                }
            }
        }
    }
}
