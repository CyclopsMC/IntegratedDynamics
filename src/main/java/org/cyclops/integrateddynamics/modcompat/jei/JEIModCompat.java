package org.cyclops.integrateddynamics.modcompat.jei;

import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for the JEI integration of this mod.
 * @author rubensworks
 *
 */
public class JEIModCompat implements IModCompat {
	
	/**
	 * If the modcompat can be used.
	 */
	public static boolean canBeUsed = false;

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			canBeUsed = IntegratedDynamics._instance.getModCompatLoader().shouldLoadModCompat(this);
		}
	}

	@Override
	public String getModID() {
		return Reference.MOD_JEI;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Integration for Integrated Dynamics recipes.";
	}

}
