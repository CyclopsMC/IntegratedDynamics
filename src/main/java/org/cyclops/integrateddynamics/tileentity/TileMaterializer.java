package org.cyclops.integrateddynamics.tileentity;

import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.network.MaterializerNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the variable materializer.
 * @author rubensworks
 */
public class TileMaterializer extends TileActiveVariableBase<MaterializerNetworkElement> implements INamedContainerProvider {

    public static final int INVENTORY_SIZE = 3;
    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    @Setter
    private PlayerEntity lastPlayer = null;
    private boolean writeVariable;

    public TileMaterializer() {
        super(RegistryEntries.TILE_ENTITY_MATERIALIZER, TileMaterializer.INVENTORY_SIZE);

        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.SOUTH,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.EAST,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.WEST,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_READ)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_WRITE_IN)));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN,
                LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), SLOT_WRITE_OUT)));

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new MaterializerNetworkElement(DimPos.of(world, blockPos));
            }
        }));
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

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide() && this.writeVariable && !getInventory().getItem(SLOT_WRITE_IN).isEmpty() && canWrite() && getInventory().getItem(SLOT_WRITE_OUT).isEmpty()) {
            this.writeVariable = false;

            // Write proxy reference
            ItemStack outputStack = writeMaterialized(!getLevel().isClientSide, getInventory().getItem(SLOT_WRITE_IN));
            if(!outputStack.isEmpty()) {
                getInventory().setItem(SLOT_WRITE_OUT, outputStack);
                getInventory().removeItemNoUpdate(SLOT_WRITE_IN);
            }
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
            }, lastPlayer, getBlockState());
        } catch (EvaluationException e) {
            getEvaluator().addError(new TranslationTextComponent(e.getMessage()));
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerMaterializer(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.integrateddynamics.materializer");
    }
}
