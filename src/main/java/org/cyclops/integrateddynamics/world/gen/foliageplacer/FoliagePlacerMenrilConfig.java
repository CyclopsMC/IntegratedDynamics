package org.cyclops.integrateddynamics.world.gen.foliageplacer;

import org.cyclops.cyclopscore.config.extendedconfig.FoliagePlacerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link FoliagePlacerMenril}.
 * @author rubensworks
 *
 */
public class FoliagePlacerMenrilConfig extends FoliagePlacerConfig<FoliagePlacerMenril> {

    public FoliagePlacerMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "menril",
                eConfig -> FoliagePlacerMenril.CODEC
        );
    }

}
