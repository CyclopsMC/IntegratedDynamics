package org.cyclops.integrateddynamics.world.gen.foliageplacer;

import org.cyclops.cyclopscore.config.extendedconfig.FoliagePlacerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link FoliagePlacerMenril}.
 * @author rubensworks
 *
 */
public class FoliagePlacerMenrilConfig extends FoliagePlacerConfigCommon<FoliagePlacerMenril, IntegratedDynamics> {

    public FoliagePlacerMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "menril",
                eConfig -> FoliagePlacerMenril.CODEC
        );
    }
}
