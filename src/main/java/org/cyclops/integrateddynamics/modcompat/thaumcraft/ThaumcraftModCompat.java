package org.cyclops.integrateddynamics.modcompat.thaumcraft;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyNBTFactory;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.client.render.valuetype.AspectValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueTypeListProxyPositionedAspectContainer;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.logicprogrammer.ValueObjectTypeAspectElementType;

/**
 * Compatibility plugin for Thaumcraft.
 * @author rubensworks
 *
 */
public class ThaumcraftModCompat implements IModCompat {

	public static ValueObjectTypeAspect OBJECT_ASPECT;
	public static ValueTypeListProxyNBTFactory<ValueObjectTypeAspect, ValueObjectTypeAspect.ValueAspect, ValueTypeListProxyPositionedAspectContainer> POSITIONED_ASPECTCONTAINER;
	public static ValueObjectTypeAspectElementType OBJECT_ASPECT_ELEMENTTYPE;

    @Override
    public String getModID() {
        return Reference.MOD_THAUMCRAFT;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.PREINIT) {
			// Part types
			PartTypes.REGISTRY.register(new PartTypeThaumcraftReader("thaumcraftReader"));

			// Value types
			OBJECT_ASPECT = ValueTypes.REGISTRY.register(new ValueObjectTypeAspect());

			// List proxy factories
			POSITIONED_ASPECTCONTAINER = ValueTypeListProxyFactories.REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedAspectContainer", ValueTypeListProxyPositionedAspectContainer.class));

			// Logic programmer aspect value type creator
			OBJECT_ASPECT_ELEMENTTYPE = LogicProgrammerElementTypes.REGISTRY.addType(new ValueObjectTypeAspectElementType());

			if(MinecraftHelpers.isClientSide()) {
				initClient();
			}
		}
    }

	@SideOnly(Side.CLIENT)
	protected void initClient() {
		ValueTypeWorldRenderers.REGISTRY.register(ThaumcraftModCompat.OBJECT_ASPECT, new AspectValueTypeWorldRenderer());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Thaumcraft reader.";
	}

}
