package org.cyclops.integrateddynamics.modcompat.tesla;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.modcompat.IApiCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.tesla.aspect.TeslaAspects;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Mod compat for the Tesla API.
 * @author rubensworks
 *
 */
public class TeslaApiCompat implements IApiCompat {

	@Override
	public void onInit(final Step initStep) {
		if(initStep == Step.PREINIT) {
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Sets.<IAspect>newHashSet(
					TeslaAspects.Read.Energy.BOOLEAN_ISAPPLICABLE,
					TeslaAspects.Read.Energy.BOOLEAN_ISRECEIVER,
					TeslaAspects.Read.Energy.BOOLEAN_ISPROVIDER,
					TeslaAspects.Read.Energy.BOOLEAN_CANEXTRACT,
					TeslaAspects.Read.Energy.BOOLEAN_CANINSERT,
					TeslaAspects.Read.Energy.BOOLEAN_ISFULL,
					TeslaAspects.Read.Energy.BOOLEAN_ISEMPTY,
					TeslaAspects.Read.Energy.BOOLEAN_ISNONEMPTY,
					TeslaAspects.Read.Energy.LONG_STORED,
					TeslaAspects.Read.Energy.LONG_CAPACITY,
					TeslaAspects.Read.Energy.DOUBLE_FILLRATIO,
					TeslaAspects.Read.Energy.STRING_STORED,
					TeslaAspects.Read.Energy.STRING_CAPACITY
			));
		}
	}

	@Override
	public String getApiID() {
		return Reference.MOD_TESLA_API;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Tesla readers aspects.";
	}

}
