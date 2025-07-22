package org.cyclops.integrateddynamics.gametest.integration;

import org.cyclops.cyclopscore.config.extendedconfig.GameTestInstanceTypeConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * @author rubensworks
 */
public class IntegrationMethodGameTestInstanceConfig extends GameTestInstanceTypeConfigCommon {
    public IntegrationMethodGameTestInstanceConfig(IModBase mod) {
        super(mod, "integration_method", (eConfig) -> IntegrationMethodGameTestInstance.CODEC);
    }
}
