package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;

import javax.annotation.Nullable;

/**
 * Config for the part container capability.
 * @author rubensworks
 *
 */
public class PartContainerConfig extends CapabilityConfig<IPartContainer> {

    @CapabilityInject(IPartContainer.class)
    public static Capability<IPartContainer> CAPABILITY = null;

    public PartContainerConfig() {
        super(
                CommonCapabilities._instance,
                "part_container",
                IPartContainer.class,
                new DefaultCapabilityStorage<IPartContainer>(),
                () -> new PartContainerDefault() {
                    @Nullable
                    @Override
                    public Direction getWatchingSide(World world, BlockPos pos, PlayerEntity player) {
                        return null;
                    }

                    @Override
                    protected void setChanged() {

                    }

                    @Override
                    protected void sendUpdate() {

                    }

                    @Override
                    protected World getLevel() {
                        return null;
                    }

                    @Override
                    protected BlockPos getPos() {
                        return null;
                    }

                    @Override
                    protected INetwork getNetwork() {
                        return null;
                    }
                }
        );
    }

}
