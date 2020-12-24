package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Gui for aspect settings.
 * @author rubensworks
 */
public class ContainerScreenAspectSettings extends ContainerScreenExtended<ContainerAspectSettings> {

    private static final int ERROR_WIDTH = 13;
    private static final int ERROR_HEIGHT = 13;
    private static final int OK_WIDTH = 14;
    private static final int OK_HEIGHT = 12;

    private final List<IAspectPropertyTypeInstance> propertyTypes;
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected GuiElementValueTypeString<ContainerScreenAspectSettings, ContainerAspectSettings> guiElement = null;
    protected int activePropertyIndex = 0;
    protected GuiElementValueTypeStringRenderPattern propertyConfigPattern = null;
    protected SubGuiValueTypeInfo propertyInfo = null;
    private ButtonText buttonLeft = null;
    private ButtonText buttonRight = null;
    private ButtonText buttonExit = null;
    private ITextComponent lastError;

    public ContainerScreenAspectSettings(ContainerAspectSettings container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);

        //noinspection deprecation
        this.propertyTypes = Lists.newArrayList(container.getAspect().getDefaultProperties().getTypes());
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/aspect_settings.png");
    }

    public int getActivePropertyIndex() {
        return activePropertyIndex;
    }

    protected void saveSetting() {
        if(guiElement != null && lastError == null) {
            try {
                container.setValue(getActiveProperty(), ValueHelpers.parseString(guiElement.getValueType(), guiElement.getInputString()));
            } catch (EvaluationException e) {
                // Validation already happened before, so we ignore the error here
            }
        }
    }

    protected void refreshButtonEnabled() {
        buttonLeft.active = getActivePropertyIndex() > 0;
        buttonRight.active = getActivePropertyIndex() < propertyTypes.size() - 1;
    }

    @Override
    protected int getBaseYSize() {
        return 213;
    }

    @Override
    public void init() {
        super.init();
        subGuiHolder.init(this.guiLeft, this.guiTop);
        addButton(buttonExit = new ButtonText(guiLeft + 7, guiTop + 5, 12, 10, new TranslationTextComponent("gui.cyclopscore.up"), new StringTextComponent("<<"), createServerPressable(ContainerAspectSettings.BUTTON_EXIT, (button) -> {
            saveSetting();
        }), true));
        addButton(buttonLeft = new ButtonText(guiLeft + 21, guiTop + 5, 10, 10, new TranslationTextComponent("gui.cyclopscore.left"), new StringTextComponent("<"), (button) -> {
            saveSetting();
            if(getActivePropertyIndex() > 0) {
                setActiveProperty(getActivePropertyIndex() - 1);
                refreshButtonEnabled();
            }
        }, true));
        addButton(buttonRight = new ButtonText(guiLeft + 159, guiTop + 5, 10, 10, new TranslationTextComponent("gui.cyclopscore.right"), new StringTextComponent(">"), (button) -> {
            saveSetting();
            if(getActivePropertyIndex() < propertyTypes.size()) {
                setActiveProperty(getActivePropertyIndex() + 1);
                refreshButtonEnabled();
            }
        }, true));
        refreshButtonEnabled();

        setActiveProperty(activePropertyIndex);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        subGuiHolder.drawGuiContainerBackgroundLayer(matrixStack, this.guiLeft, this.guiTop, getMinecraft().getTextureManager(), font, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(matrixStack, this.guiLeft, this.guiTop, getMinecraft().getTextureManager(), font, mouseX, mouseY);

        IAspectPropertyTypeInstance activeProperty = getActiveProperty();
        if(activeProperty != null) {
            String label = L10NHelpers.localize(activeProperty.getTranslationKey());
            RenderHelpers.drawScaledCenteredString(matrixStack, font, label, 88, 10, 0,
                    1.0F, 140, Helpers.RGBToInt(10, 10, 10));
            if (RenderHelpers.isPointInRegion(this.guiLeft + 40, this.guiTop, 110, 20, mouseX, mouseY)) {
                String unlocalizedInfo = activeProperty.getTranslationKey() + ".info";
                if (I18n.hasKey(unlocalizedInfo)) {
                    drawTooltip(Lists.newArrayList(new TranslationTextComponent(unlocalizedInfo)
                            .mergeStyle(TextFormatting.GRAY)), mouseX - this.guiLeft, mouseY - this.guiTop + 20);
                }
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(!subGuiHolder.charTyped(typedChar, keyCode)) {
            if (keyCode == 1 || this.getMinecraft().gameSettings.keyBindInventory.getKey().getKeyCode() == keyCode) {
                saveSetting();
                this.getMinecraft().player.closeScreen();
            } else {
                return super.charTyped(typedChar, keyCode);
            }
        } else {
            if(guiElement != null) {
                onValueChanged();
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar != GLFW.GLFW_KEY_ESCAPE) {
            return this.subGuiHolder.keyPressed(typedChar, keyCode, modifiers);
        } else {
            saveSetting();
            return super.keyPressed(typedChar, keyCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
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
        propertyConfigPattern.init(guiLeft, guiTop);
        guiElement.activate();
        syncInputValue();
        lastError = guiElement.validate();
    }

    protected void syncInputValue() {
        IAspectPropertyTypeInstance property = getActiveProperty();
        IValue value = container.getPropertyValue(property);
        if(value != null) {
            guiElement.setInputString(ValueHelpers.toString(value), propertyConfigPattern);
        }
    }


    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);
        IAspectPropertyTypeInstance property = container.getPropertyIds().get(valueId);
        if(property != null && getActiveProperty() == property) {
            syncInputValue();
        }
    }

    public class SubGuiValueTypeInfo extends GuiElementValueTypeString.SubGuiValueTypeInfo<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> {

        public SubGuiValueTypeInfo(IGuiInputElement<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> element) {
            super(ContainerScreenAspectSettings.this, (ContainerAspectSettings) ContainerScreenAspectSettings.this.container, element, 8, 105, 160, 20);
        }

        @Override
        protected boolean showError() {
            return true;
        }

        @Override
        protected ITextComponent getLastError() {
            return lastError;
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

    }

}
