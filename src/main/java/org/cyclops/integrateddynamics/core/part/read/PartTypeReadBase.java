package org.cyclops.integrateddynamics.core.part.read;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract {@link IPartTypeReader}.
 * @author rubensworks
 */
public abstract class PartTypeReadBase<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>>
        extends PartTypeAspects<P, S> implements IPartTypeReader<P, S> {

    private List<IAspectRead> aspectsRead = null;
    private EnumMap<AspectUpdateType, Set<IAspectRead>> updateAspects = null;

    public PartTypeReadBase(String name) {
        this(name, new PartRenderPosition(0.1875F, 0.3125F, 0.625F, 0.625F));
    }

    public PartTypeReadBase(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    protected Set<IAspectRead> getUpdateAspects(AspectUpdateType updateType) {
        if (updateAspects == null) {
            updateAspects = new EnumMap<>(AspectUpdateType.class);
            for (AspectUpdateType aspectUpdateType : AspectUpdateType.values()) {
                updateAspects.put(aspectUpdateType, Sets.newLinkedHashSet());
            }
            for (IAspect aspect : getAspects()) {
                if (aspect instanceof IAspectRead) {
                    IAspectRead aspectRead = (IAspectRead) aspect;
                    updateAspects.get(aspectRead.getUpdateType()).add(aspectRead);
                }
            }
        }

        return updateAspects.get(updateType);
    }

    @Override
    public boolean isSolid(S state) {
        return true;
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.update(network, partNetwork, target, state);
        for(IAspect aspect : getUpdateAspects(AspectUpdateType.NETWORK_TICK)) {
            aspect.update(network, partNetwork, this, target, state);
        }
    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, S state,
                                      BlockGetter world, Block neighbourBlock, BlockPos neighbourBlockPos) {
        super.onBlockNeighborChange(network, partNetwork, target, state, world, neighbourBlock, neighbourBlockPos);
        for(IAspect aspect : getUpdateAspects(AspectUpdateType.BLOCK_UPDATE)) {
            aspect.update(network, partNetwork, this, target, state);
        }
    }

    @Override
    public List<IAspectRead> getReadAspects() {
        if (aspectsRead == null) {
            aspectsRead = Aspects.REGISTRY.getReadAspects(this);
        }
        return aspectsRead;
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(PartTarget target, S partState,
                                                                                      IAspectRead<V, T> aspect) {
        IAspectVariable<V> variable = partState.getVariable(aspect);
        if(variable == null) {
            if(!getAspects().contains(aspect)) {
                throw new IllegalArgumentException(String.format("Tried to get the variable for the aspect %s that did not exist within the " +
                        "part type %s.", aspect.getUniqueName(), this));
            }
            variable = aspect.createNewVariable(target);
            partState.setVariable(aspect, variable);
        }
        return variable;
    }

    @Override
    public void setTargetSideOverride(S state, @Nullable Direction side) {
        Direction lastSide = getTargetSideOverride(state);
        super.setTargetSideOverride(state, side);
        if (lastSide != side) {
            state.resetVariables();
        }
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(getTranslationKey());
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerPartReader<>(id, playerInventory, new SimpleContainer(0),
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeReadBase) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        // Write part position
        PacketCodec.write(packetBuffer, pos);

        super.writeExtraGuiData(packetBuffer, pos, player);
    }

}
