package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * Config for the cable capability.
 * @author rubensworks
 *
 */
public class CableConfig extends CapabilityConfig<ICable> {

    @CapabilityInject(ICable.class)
    public static Capability<ICable> CAPABILITY = null;

    public CableConfig() {
        super(
                CommonCapabilities._instance,
                "cable",
                ICable.class,
                new DefaultCapabilityStorage<ICable>(),
                () -> new CableDefault() {
                    @Override
                    public void destroy() {

                    }

                    @Override
                    protected boolean isForceDisconnectable() {
                        return false;
                    }

                    @Override
                    protected EnumFacingMap<Boolean> getForceDisconnected() {
                        return null;
                    }

                    @Override
                    protected EnumFacingMap<Boolean> getConnected() {
                        return null;
                    }

                    @Override
                    protected void markDirty() {

                    }

                    @Override
                    protected void sendUpdate() {

                    }

                    @Override
                    protected World getWorld() {
                        return null;
                    }

                    @Override
                    protected BlockPos getPos() {
                        return null;
                    }
                }
        );
    }

}
