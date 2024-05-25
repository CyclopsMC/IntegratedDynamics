package org.cyclops.integrateddynamics.core.network.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.cyclops.integrateddynamics.api.network.INetwork;

import javax.annotation.Nullable;

/**
 * An event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class NetworkInitializedEvent extends Event {

    private final INetwork network;
    private final Level world;
    private final BlockPos pos;
    private final LivingEntity placer;

    public NetworkInitializedEvent(INetwork network, Level world, BlockPos pos, @Nullable LivingEntity placer) {
        this.network = network;
        this.world = world;
        this.pos = pos;
        this.placer = placer;
    }

    public INetwork getNetwork() {
        return network;
    }

    public Level getLevel() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public LivingEntity getPlacer() {
        return placer;
    }
}
