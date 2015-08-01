package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;

import java.util.List;

/**
 * Base class for aspects.
 * @author rubensworks
 */
public abstract class AspectBase<V extends IValue, T extends IValueType<V>> implements IAspect<V, T> {

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedPrefix() + ".name";
    }

    protected String getUnlocalizedPrefix() {
        return "aspect.aspects." + getModId() + "." + getUnlocalizedType();
    }

    protected abstract String getUnlocalizedType();

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        String aspectName = L10NHelpers.localize(getUnlocalizedName());
        String valueTypeName = L10NHelpers.localize(getValueType().getUnlocalizedName());
        lines.add(L10NHelpers.localize("aspect.tooltip.aspectName", aspectName));
        lines.add(L10NHelpers.localize("aspect.tooltip.valueTypeName", getValueType().getDisplayColorFormat() + valueTypeName));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
