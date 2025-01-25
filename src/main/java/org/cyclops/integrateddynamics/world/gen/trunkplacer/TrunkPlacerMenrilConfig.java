package org.cyclops.integrateddynamics.world.gen.trunkplacer;

import org.cyclops.cyclopscore.config.extendedconfig.TrunkPlacerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link TrunkPlacerMenril}.
 * @author rubensworks
 *
 */
public class TrunkPlacerMenrilConfig extends TrunkPlacerConfigCommon<TrunkPlacerMenril, IntegratedDynamics> {

    public TrunkPlacerMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "menril",
                eConfig -> TrunkPlacerMenril.CODEC
        );
    }

}
