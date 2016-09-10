package org.cyclops.integrateddynamics.capability;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IFacadeable;

/**
 * Config for the facadeable capability.
 * @author rubensworks
 *
 */
public class FacadeableConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static FacadeableConfig _instance;

    @CapabilityInject(IFacadeable.class)
    public static Capability<IFacadeable> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public FacadeableConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "facadeable",
                "Can hold a facade",
                IFacadeable.class,
                new DefaultCapabilityStorage<IFacadeable>(),
                FacadeableDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    public static boolean hasFacade(IBlockAccess world, BlockPos pos) {
        IFacadeable facadeable = TileHelpers.getCapability(world, pos, null, CAPABILITY);
        return facadeable != null && facadeable.hasFacade();
    }

    public static IBlockState getFacade(IBlockAccess world, BlockPos pos) {
        IFacadeable facadeable = TileHelpers.getCapability(world, pos, null, CAPABILITY);
        return facadeable != null ? facadeable.getFacade() : null;
    }

}
