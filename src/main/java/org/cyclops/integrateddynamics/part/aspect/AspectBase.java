package org.cyclops.integrateddynamics.part.aspect;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo) {
        Component aspectName = new TranslatableComponent(getTranslationKey());
        Component valueTypeName = new TranslatableComponent(getValueType().getTranslationKey());
        lines.add(new TranslatableComponent(L10NValues.ASPECT_TOOLTIP_ASPECTNAME, aspectName));
        lines.add(new TranslatableComponent(L10NValues.ASPECT_TOOLTIP_VALUETYPENAME, valueTypeName)
                .withStyle(getValueType().getDisplayColorFormat()));
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
    public MenuProvider getPropertiesContainerProvider(PartPos pos) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent("gui.integrateddynamics.aspect_settings");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerAspectSettings(id, playerInventory, new SimpleContainer(0),
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
