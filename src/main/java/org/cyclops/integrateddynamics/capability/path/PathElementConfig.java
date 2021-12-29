package org.cyclops.integrateddynamics.capability.path;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Config for the path element capability.
 * @author rubensworks
 *
 */
public class PathElementConfig extends CapabilityConfig<IPathElement> {

    public static Capability<IPathElement> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public PathElementConfig() {
        super(
                CommonCapabilities._instance,
                "path_element_provider",
                IPathElement.class
        );
    }

}
