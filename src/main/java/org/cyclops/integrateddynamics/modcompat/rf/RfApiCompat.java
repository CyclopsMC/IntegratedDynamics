package org.cyclops.integrateddynamics.modcompat.rf;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.modcompat.IApiCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.rf.aspect.RfAspects;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Mod compat for the Charset mod.
 * @author rubensworks
 *
 */
public class RfApiCompat implements IApiCompat {

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Sets.<IAspect>newHashSet(
					RfAspects.Read.Energy.BOOLEAN_ISAPPLICABLE,
					RfAspects.Read.Energy.BOOLEAN_ISRECEIVER,
					RfAspects.Read.Energy.BOOLEAN_ISPROVIDER,
					RfAspects.Read.Energy.BOOLEAN_CANEXTRACT,
					RfAspects.Read.Energy.BOOLEAN_CANINSERT,
					RfAspects.Read.Energy.BOOLEAN_ISFULL,
					RfAspects.Read.Energy.BOOLEAN_ISEMPTY,
					RfAspects.Read.Energy.BOOLEAN_ISNONEMPTY,
					RfAspects.Read.Energy.INTEGER_STORED,
					RfAspects.Read.Energy.INTEGER_CAPACITY,
					RfAspects.Read.Energy.DOUBLE_FILLRATIO
			));
		}
	}

	@Override
	public String getApiID() {
		return Reference.MOD_RF_API;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "RF readers aspects.";
	}

}
