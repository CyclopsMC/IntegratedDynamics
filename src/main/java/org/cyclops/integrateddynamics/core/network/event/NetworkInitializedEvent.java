package org.cyclops.integrateddynamics.core.network.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import org.cyclops.integrateddynamics.api.network.INetwork;

import javax.annotation.Nullable;

/**
 * An event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class NetworkInitializedEvent extends Event {

    private final INetwork network;
    private final World world;
    private final BlockPos pos;
    private final LivingEntity placer;

    public NetworkInitializedEvent(INetwork network, World world, BlockPos pos, @Nullable LivingEntity placer) {
        this.network = network;
        this.world = world;
        this.pos = pos;
        this.placer = placer;
    }

    public INetwork getNetwork() {
        return network;
    }

    public World getWorld() {
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
