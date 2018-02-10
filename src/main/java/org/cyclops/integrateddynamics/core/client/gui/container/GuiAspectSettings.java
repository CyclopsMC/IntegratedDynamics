package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionClient;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;

import java.io.IOException;
import java.util.List;

/**
 * Gui for aspect settings.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class GuiAspectSettings extends GuiContainerExtended {

    private static final int ERROR_WIDTH = 13;
    private static final int ERROR_HEIGHT = 13;
    private static final int OK_WIDTH = 14;
    private static final int OK_HEIGHT = 12;

    private static final int BUTTON_LEFT = 0;
    private static final int BUTTON_RIGHT = 1;
    public static final int BUTTON_EXIT = 2;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final IAspect aspect;

    private final List<IAspectPropertyTypeInstance> propertyTypes;
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected GuiElementValueTypeString<GuiAspectSettings, ContainerAspectSettings> guiElement = null;
    protected int activePropertyIndex = 0;
    protected GuiElementValueTypeStringRenderPattern propertyConfigPattern = null;
    protected SubGuiValueTypeInfo propertyInfo = null;
    private GuiButtonText buttonLeft = null;
    private GuiButtonText buttonRight = null;
    private GuiButtonText buttonExit = null;
    private L10NHelpers.UnlocalizedString lastError;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param aspect The aspect.
     */
    public GuiAspectSettings(EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType, IAspect aspect) {
        super(new ContainerAspectSettings(player, target, partContainer, partType, aspect));
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.aspect = aspect;

        aspect.getProperties(getPartType(), getTarget(), ((ContainerAspectSettings) container).getPartState());
        //noinspection deprecation
        this.propertyTypes = Lists.newArrayList(aspect.getDefaultProperties().getTypes());

        putButtonAction(BUTTON_LEFT, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                saveSetting();
                if(getActivePropertyIndex() > 0) {
                    setActiveProperty(getActivePropertyIndex() - 1);
                    refreshButtonEnabled();
                }
            }
        });
        putButtonAction(BUTTON_RIGHT, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                saveSetting();
                if(getActivePropertyIndex() < propertyTypes.size()) {
                    setActiveProperty(getActivePropertyIndex() + 1);
                    refreshButtonEnabled();
                }
            }
        });
        putButtonAction(BUTTON_EXIT, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                saveSetting();
                IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide());
            }
        });
    }

    protected void saveSetting() {
        if(guiElement != null && lastError == null) {
            ContainerAspectSettings aspectContainer = (ContainerAspectSettings) container;
            aspectContainer.setValue(getActiveProperty(), guiElement.getValueType().deserialize(guiElement.getInputString()));
        }
    }

    @Override
    public void onGuiClosed() {
        saveSetting();
        super.onGuiClosed();
    }

    protected void refreshButtonEnabled() {
        buttonLeft.enabled = getActivePropertyIndex() > 0;
        buttonRight.enabled = getActivePropertyIndex() < propertyTypes.size() - 1;
    }

    @Override
    protected int getBaseYSize() {
        return 213;
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getModGui().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "aspect_settings.png";
    }

    @Override
    public void initGui() {
        super.initGui();
        subGuiHolder.initGui(this.guiLeft, this.guiTop);
        buttonList.add(buttonExit = new GuiButtonText(BUTTON_EXIT, guiLeft + 7, guiTop + 5, 12, 10, "<<", true));
        buttonList.add(buttonLeft = new GuiButtonText(BUTTON_LEFT, guiLeft + 21, guiTop + 5, 10, 10, "<", true));
        buttonList.add(buttonRight = new GuiButtonText(BUTTON_RIGHT, guiLeft + 159, guiTop + 5, 10, 10, ">", true));
        refreshButtonEnabled();

        setActiveProperty(activePropertyIndex);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        subGuiHolder.drawGuiContainerBackgroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRenderer, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRenderer, mouseX, mouseY);

        IAspectPropertyTypeInstance activeProperty = getActiveProperty();
        if(activeProperty != null) {
            String label = L10NHelpers.localize(activeProperty.getUnlocalizedName());
            RenderHelpers.drawScaledCenteredString(fontRenderer, label, 88, 10, 0,
                    1.0F, 140, Helpers.RGBToInt(10, 10, 10));
            if (RenderHelpers.isPointInRegion(this.guiLeft + 40, this.guiTop, 110, 20, mouseX, mouseY)) {
                String unlocalizedInfo = activeProperty.getUnlocalizedName().replaceFirst("\\.name$", ".info");
                if (I18n.hasKey(unlocalizedInfo)) {
                    drawTooltip(Lists.newArrayList(TextFormatting.GRAY.toString()
                            + L10NHelpers.localize(unlocalizedInfo)), mouseX - this.guiLeft, mouseY - this.guiTop + 20);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(!subGuiHolder.keyTyped(this.checkHotbarKeys(keyCode), typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        } else {
            if(guiElement != null) {
                onValueChanged();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void onValueChanged() {
        lastError = guiElement.validate();
    }

    protected IAspectPropertyTypeInstance getActiveProperty() {
        return propertyTypes.get(Math.max(0, Math.min(propertyTypes.size() - 1, activePropertyIndex)));
    }

    protected void setActiveProperty(int index) {
        onActivateElement(propertyTypes.get(activePropertyIndex = index));
    }

    protected void onActivateElement(IAspectPropertyTypeInstance property) {
        if(guiElement != null) {
            guiElement.deactivate();
            subGuiHolder.removeSubGui(propertyConfigPattern);
            subGuiHolder.removeSubGui(propertyInfo);
        }
        guiElement = new GuiElementValueTypeString<>(property.getType(), IConfigRenderPattern.NONE);
        guiElement.setValidator(property.getValidator());
        subGuiHolder.addSubGui(propertyConfigPattern = guiElement.createSubGui(8, 17, 160, 91, this, (ContainerAspectSettings) getContainer()));
        subGuiHolder.addSubGui(propertyInfo = new SubGuiValueTypeInfo(guiElement));
        propertyConfigPattern.initGui(guiLeft, guiTop);
        guiElement.activate();
        syncInputValue();
        lastError = guiElement.validate();
    }

    protected void syncInputValue() {
        IAspectPropertyTypeInstance property = getActiveProperty();
        IValue value = ((ContainerAspectSettings) container).getPropertyValue(property);
        if(value != null) {
            guiElement.setInputString(property.getType().toCompactString(value), propertyConfigPattern);
        }
    }


    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        IAspectPropertyTypeInstance property = ((ContainerAspectSettings) container).getPropertyIds().get(valueId);
        if(property != null && getActiveProperty() == property) {
            syncInputValue();
        }
    }

    public class SubGuiValueTypeInfo extends GuiElementValueTypeString.SubGuiValueTypeInfo<RenderPattern, GuiAspectSettings, ContainerAspectSettings> {

        public SubGuiValueTypeInfo(IGuiInputElement<RenderPattern, GuiAspectSettings, ContainerAspectSettings> element) {
            super(GuiAspectSettings.this, (ContainerAspectSettings) GuiAspectSettings.this.container, element, 8, 105, 160, 20);
        }

        @Override
        protected boolean showError() {
            return true;
        }

        @Override
        protected L10NHelpers.UnlocalizedString getLastError() {
            return lastError;
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

    }

}
