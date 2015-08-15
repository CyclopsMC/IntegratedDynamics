package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

import java.util.List;

/**
 * Base class for aspects.
 * @author rubensworks
 */
public abstract class AspectBase<V extends IValue, T extends IValueType<V>> implements IAspect<V, T> {

    private final AspectProperties defaultProperties;

    public AspectBase() {
        this.defaultProperties = createDefaultProperties();
    }

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

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean hasProperties() {
        return getDefaultProperties() != null;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> AspectProperties getProperties(Network network, P partType, PartTarget target, S state) {
        AspectProperties properties = state.getAspectProperties(this);
        if(properties == null) {
            properties = getDefaultProperties().clone();
        }
        setProperties(network, partType, target, state, properties);
        return properties;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setProperties(Network network, P partType, PartTarget target, S state, AspectProperties properties) {
        state.setAspectProperties(this, properties);
    }

    public final AspectProperties getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * Creates the default properties for this aspect, only called once.
     * @return The default properties.
     */
    protected AspectProperties createDefaultProperties() {
        return null;
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
