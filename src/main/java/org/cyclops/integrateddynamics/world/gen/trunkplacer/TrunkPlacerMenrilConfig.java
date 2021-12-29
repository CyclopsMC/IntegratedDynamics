package org.cyclops.integrateddynamics.world.gen.trunkplacer;

import org.cyclops.cyclopscore.config.extendedconfig.TrunkPlacerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for {@link TrunkPlacerMenril}.
 * @author rubensworks
 *
 */
public class TrunkPlacerMenrilConfig extends TrunkPlacerConfig<TrunkPlacerMenril> {

    public TrunkPlacerMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "menril",
                eConfig -> TrunkPlacerMenril.CODEC
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        RegistryEntries.TRUNK_PLACER_MENRIL = getInstance();
    }

}
