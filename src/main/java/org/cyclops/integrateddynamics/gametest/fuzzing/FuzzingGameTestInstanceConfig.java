package org.cyclops.integrateddynamics.gametest.fuzzing;

import org.cyclops.cyclopscore.config.extendedconfig.GameTestInstanceTypeConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * @author rubensworks
 */
public class FuzzingGameTestInstanceConfig extends GameTestInstanceTypeConfigCommon {
    public FuzzingGameTestInstanceConfig(IModBase mod) {
        super(mod, "fuzzing", (eConfig) -> FuzzingGameTestInstance.CODEC);
    }
}
