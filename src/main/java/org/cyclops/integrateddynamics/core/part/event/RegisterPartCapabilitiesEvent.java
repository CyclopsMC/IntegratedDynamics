package org.cyclops.integrateddynamics.core.part.event;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * An event to link block capabilities to part capabilities.
 * This assumes part can expose volatile capabilities.
 * @author rubensworks
 */
public class RegisterPartCapabilitiesEvent extends Event implements IModBusEvent {

    protected final RegisterCapabilitiesEvent registerCapabilitiesEvent;
    protected final BlockEntityType<? extends BlockEntityMultipartTicking> blockEntityType;

    public RegisterPartCapabilitiesEvent(
            RegisterCapabilitiesEvent registerCapabilitiesEvent,
            BlockEntityType<? extends BlockEntityMultipartTicking> blockEntityType
    ) {
        this.registerCapabilitiesEvent = registerCapabilitiesEvent;
        this.blockEntityType = blockEntityType;
    }

    public <T> void register(BlockCapability<T, Direction> blockCapability, PartCapability<T> partCapability) {
        this.registerCapabilitiesEvent.registerBlockEntity(
                blockCapability,
                blockEntityType,
                (blockEntity, context) -> {
                    INetwork network = blockEntity.getNetwork();
                    if (network != null) {
                        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
                        return blockEntity.getPartContainer()
                                .getCapability(partCapability, network, partNetwork, PartTarget.fromCenter(PartPos.of(blockEntity.getLevel(), blockEntity.getBlockPos(), context)))
                                .orElse(null);
                    }
                    return null;
                }
        );
    }
}
