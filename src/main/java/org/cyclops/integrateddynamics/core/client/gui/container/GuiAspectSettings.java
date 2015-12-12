package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionClient;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeGuiElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeSubGuiRenderPattern;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.logicprogrammer.SubGuiConfigRenderPattern;

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

    private static final int BUTTON_SAVE = 0;
    private static final int BUTTON_LEFT = 1;
    private static final int BUTTON_RIGHT = 2;
    public static final int BUTTON_EXIT = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final IAspect aspect;

    private final List<IAspectPropertyTypeInstance> propertyTypes;
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected ValueTypeGuiElement<GuiAspectSettings, ContainerAspectSettings> guiElement = null;
    protected int activePropertyIndex = 0;
    protected ValueTypeSubGuiRenderPattern propertyConfigPattern = null;
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
        this.propertyTypes = Lists.newArrayList(aspect.getDefaultProperties().getTypes());

        putButtonAction(BUTTON_SAVE, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                if(guiElement != null && lastError == null) {
                    ContainerAspectSettings aspectContainer = (ContainerAspectSettings) container;
                    aspectContainer.setValue(getActiveProperty(), guiElement.getValueType().deserialize(guiElement.getInputString()));
                    try {
                        actionPerformed(buttonExit);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        putButtonAction(BUTTON_LEFT, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                if(getActivePropertyIndex() > 0) {
                    setActiveProperty(getActivePropertyIndex() - 1);
                    refreshButtonEnabled();
                }
            }
        });
        putButtonAction(BUTTON_RIGHT, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                if(getActivePropertyIndex() < propertyTypes.size()) {
                    setActiveProperty(getActivePropertyIndex() + 1);
                    refreshButtonEnabled();
                }
            }
        });
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
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "aspectSettings.png";
    }

    @Override
    public void initGui() {
        super.initGui();
        subGuiHolder.initGui(this.guiLeft, this.guiTop);
        GuiButtonText buttonSave;
        buttonList.add(buttonSave = new GuiButtonText(BUTTON_SAVE, this.guiLeft,  this.guiTop + 88,
                L10NHelpers.localize("item.items.integrateddynamics.labeller.button.write")));
        buttonSave.xPosition += this.getBaseXSize() - buttonSave.width - 9;
        buttonList.add(buttonExit = new GuiButtonText(BUTTON_EXIT, guiLeft + 7, guiTop + 5, 12, 10, "<<", true));
        buttonList.add(buttonLeft = new GuiButtonText(BUTTON_LEFT, guiLeft + 21, guiTop + 5, 10, 10, "<", true));
        buttonList.add(buttonRight = new GuiButtonText(BUTTON_RIGHT, guiLeft + 159, guiTop + 5, 10, 10, ">", true));
        refreshButtonEnabled();

        setActiveProperty(activePropertyIndex);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        subGuiHolder.drawGuiContainerBackgroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRendererObj, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRendererObj, mouseX, mouseY);

        IAspectPropertyTypeInstance activeProperty = getActiveProperty();
        if(activeProperty != null) {
            String label = L10NHelpers.localize(activeProperty.getUnlocalizedName());
            RenderHelpers.drawScaledCenteredString(fontRendererObj, label, 88, 10, 0,
                    1.0F, 140, Helpers.RGBToInt(10, 10, 10));
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
        guiElement = new ValueTypeGuiElement<>(property.getType());
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

    public class SubGuiValueTypeInfo extends ValueTypeGuiElement.SubGuiValueTypeInfo<SubGuiConfigRenderPattern, GuiAspectSettings, ContainerAspectSettings> {

        public SubGuiValueTypeInfo(IGuiInputElement<SubGuiConfigRenderPattern, GuiAspectSettings, ContainerAspectSettings> element) {
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
