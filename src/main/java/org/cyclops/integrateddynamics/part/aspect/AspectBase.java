package org.cyclops.integrateddynamics.part.aspect;

import lombok.Data;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiAspectSettings;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

import java.util.Collection;
import java.util.List;

/**
 * Base class for aspects.
 * @author rubensworks
 */
public abstract class AspectBase<V extends IValue, T extends IValueType<V>> implements IAspect<V, T> {

    private final AspectProperties defaultProperties;
    @Getter
    private final IGuiContainerProvider propertiesGuiProvider;

    public AspectBase() {
        this.defaultProperties = createDefaultProperties();
        if(hasProperties()) {
            int guiIDSettings = Helpers.getNewId(getMod(), Helpers.IDType.GUI);
            getMod().getGuiHandler().registerGUI((propertiesGuiProvider = constructSettingsGuiProvider(guiIDSettings)), ExtendedGuiHandler.ASPECT);
        } else {
            propertiesGuiProvider = null;
        }
    }

    protected IGuiContainerProvider constructSettingsGuiProvider(int guiId) {
        return new GuiProviderSettings(guiId, getMod());
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
    public <P extends IPartType<P, S>, S extends IPartState<P>> AspectProperties getProperties(P partType, PartTarget target, S state) {
        AspectProperties properties = state.getAspectProperties(this);
        if(properties == null) {
            properties = getDefaultProperties().clone();
        }
        setProperties(partType, target, state, properties);
        return properties;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setProperties(P partType, PartTarget target, S state, AspectProperties properties) {
        state.setAspectProperties(this, properties);
    }

    @Override
    public final AspectProperties getDefaultProperties() {
        return defaultProperties;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<AspectPropertyTypeInstance> getPropertyTypes() {
        return getDefaultProperties().getTypes();
    }

    /**
     * Creates the default properties for this aspect, only called once.
     * @return The default properties.
     */
    protected AspectProperties createDefaultProperties() {
        return null;
    }

    protected ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    protected String getModId() {
        return getMod().getModId();
    }

    @Data
    public static class GuiProviderSettings implements IGuiContainerProvider {

        private final int guiID;
        private final ModBase mod;

        @Override
        public Class<? extends Container> getContainer() {
            return ContainerAspectSettings.class;
        }

        @Override
        public Class<? extends GuiScreen> getGui() {
            return GuiAspectSettings.class;
        }
    }

}
