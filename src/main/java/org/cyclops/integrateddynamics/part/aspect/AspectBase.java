package org.cyclops.integrateddynamics.part.aspect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Base class for aspects.
 * @author rubensworks
 */
public abstract class AspectBase<V extends IValue, T extends IValueType<V>> implements IAspect<V, T> {

    private final IAspectProperties defaultProperties;

    private final ModBase mod;
    private String translationKey = null;

    public AspectBase(ModBase mod, IAspectProperties defaultProperties) {
        this.mod = mod;
        this.defaultProperties = defaultProperties == null ? createDefaultProperties() : defaultProperties;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(getModId(), getUnlocalizedType().replaceAll("\\.", "_"));
    }

    @Override
    public String getTranslationKey() {
        return translationKey != null ? translationKey : (translationKey = getUnlocalizedPrefix());
    }

    protected String getUnlocalizedPrefix() {
        return "aspect." + getModId() + "." + getUnlocalizedType();
    }

    protected abstract String getUnlocalizedType();

    @Override
    public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo) {
        ITextComponent aspectName = new TranslationTextComponent(getTranslationKey());
        ITextComponent valueTypeName = new TranslationTextComponent(getValueType().getTranslationKey());
        lines.add(new TranslationTextComponent(L10NValues.ASPECT_TOOLTIP_ASPECTNAME, aspectName));
        lines.add(new TranslationTextComponent(L10NValues.ASPECT_TOOLTIP_VALUETYPENAME, valueTypeName)
                .mergeStyle(getValueType().getDisplayColorFormat()));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean hasProperties() {
        return getDefaultProperties() != null;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> IAspectProperties getProperties(P partType, PartTarget target, S state) {
        IAspectProperties properties = state.getAspectProperties(this);
        if(properties == null) {
            properties = getDefaultProperties().clone();
            setProperties(partType, target, state, properties);
        }
        return properties;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setProperties(P partType, PartTarget target, S state, IAspectProperties properties) {
        state.setAspectProperties(this, properties);
    }

    @Override
    public final IAspectProperties getDefaultProperties() {
        return defaultProperties;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<IAspectPropertyTypeInstance> getPropertyTypes() {
        return hasProperties() ? getDefaultProperties().getTypes() : Collections.emptyList();
    }

    @Override
    public INamedContainerProvider getPropertiesContainerProvider(PartPos pos) {
        return new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("gui.integrateddynamics.aspect_settings");
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerAspectSettings(id, playerInventory, new Inventory(0),
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), Optional.of(data.getMiddle()), AspectBase.this);
            }
        };
    }

    /**
     * Creates the default properties for this aspect, only called once.
     * @return The default properties.
     */
    @Deprecated
    protected IAspectProperties createDefaultProperties() {
        return null;
    }

    protected ModBase getMod() {
        return mod;
    }

    protected String getModId() {
        return getMod().getModId();
    }

}
