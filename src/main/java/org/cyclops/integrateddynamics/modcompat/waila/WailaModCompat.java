package org.cyclops.integrateddynamics.modcompat.waila;

import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;

/**
 * Compatibility plugin for Waila.
 * @author rubensworks
 *
 */
public class WailaModCompat implements IModCompat {

    @Override
    public String getModID() {
        return Reference.MOD_WAILA;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.INIT) {
    		FMLInterModComms.sendMessage(getModID(), "register", Waila.class.getName() + ".callbackRegister");
    	}
    }

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "WAILA tooltips for parts.";
	}

}
