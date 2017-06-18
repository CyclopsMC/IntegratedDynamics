package org.cyclops.integrateddynamics.modcompat.charset;

import com.google.common.collect.Lists;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.charset.aspect.CharsetAspects;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Mod compat for the Charset mod.
 * @author rubensworks
 *
 */
public class CharsetPipesModCompat implements IModCompat {

	@CapabilityInject(IShifter.class)
	public static Capability<IShifter> SHIFTER = null;

	@CapabilityInject(IPipeView.class)
	public static Capability<IPipeView> PIPE = null;

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			Aspects.REGISTRY.register(PartTypes.INVENTORY_READER, Lists.<IAspect>newArrayList(
					CharsetAspects.Read.Pipe.BOOLEAN_ISAPPLICABLE,
					CharsetAspects.Read.Pipe.BOOLEAN_HASCONTENTS,
					CharsetAspects.Read.Pipe.ITEMSTACK_CONTENT,
					CharsetAspects.Read.Pipe.LIST_CONTENTS
			));
			Aspects.REGISTRY.register(PartTypes.INVENTORY_WRITER, Lists.<IAspect>newArrayList(
					CharsetAspects.Write.Pipe.SHIFTER_BOOLEAN,
					CharsetAspects.Write.Pipe.SHIFTER_ITEMSTACK,
					CharsetAspects.Write.Pipe.SHIFTER_FLUIDSTACK,
					CharsetAspects.Write.Pipe.SHIFTER_LISTITEMSTACK,
					CharsetAspects.Write.Pipe.SHIFTER_LISTFLUIDSTACK,
					CharsetAspects.Write.Pipe.SHIFTER_PREDICATEITEMSTACK,
					CharsetAspects.Write.Pipe.SHIFTER_PREDICATEFLUIDSTACK
			));
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
		return "Charset Pipes aspects.";
	}

}
