package org.cyclops.integrateddynamics.capability.path;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Config for the path element capability.
 * @author rubensworks
 *
 */
public class PathElementConfig extends CapabilityConfig<IPathElement> {

    /**
     * The unique instance.
     */
    public static PathElementConfig _instance;

    @CapabilityInject(IPathElement.class)
    public static Capability<IPathElement> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public PathElementConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "path_element_provider",
                "Capability for elements used for path construction",
                IPathElement.class,
                new DefaultCapabilityStorage<IPathElement>(),
                PathElementDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
