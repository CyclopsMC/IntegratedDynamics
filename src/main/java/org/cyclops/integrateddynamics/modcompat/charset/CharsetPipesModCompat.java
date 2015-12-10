package org.cyclops.integrateddynamics.modcompat.charset;

import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.part.PartTypes;

/**
 * Mod compat for the Version Checker mod.
 * @author rubensworks
 *
 */
public class CharsetPipesModCompat implements IModCompat {

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			PartTypes.REGISTRY.register(new PartTypeCharsetReader("charsetReader"));
		}
	}

	@Override
	public String getModID() {
		return Reference.MOD_CHARSETPIPES;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Charset Pipes reader.";
	}

}
