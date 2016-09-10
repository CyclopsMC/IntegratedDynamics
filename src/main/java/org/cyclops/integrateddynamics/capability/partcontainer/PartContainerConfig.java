package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.part.IPartContainer;

/**
 * Config for the part container capability.
 * @author rubensworks
 *
 */
public class PartContainerConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static PartContainerConfig _instance;

    @CapabilityInject(IPartContainer.class)
    public static Capability<IPartContainer> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public PartContainerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "partContainer",
                "A container that can hold parts.",
                IPartContainer.class,
                new DefaultCapabilityStorage<IPartContainer>(),
                PartContainerDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    /**
     * Get the part container at the given position.
     * @param pos The position.
     * @return The container or null.
     */
    public static IPartContainer get(DimPos pos) {
        return get(pos.getWorld(), pos.getBlockPos());
    }

    /**
     * Get the part container at the given position.
     * @param world The world.
     * @param pos The block position.
     * @return The container or null.
     */
    public static IPartContainer get(World world, BlockPos pos) {
        return TileHelpers.getCapability(world, pos, null, PartContainerConfig.CAPABILITY);
    }
}
