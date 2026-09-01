package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueTypeClient;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.SubGuiValueTypeInfoBase;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.RenderPattern;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Gui for aspect settings.
 * @author rubensworks
 */
public class ContainerScreenAspectSettings extends ContainerScreenExtended<ContainerAspectSettings> {

    private static final int VARIABLE_SIGNAL_X = 100;
    private static final int VARIABLE_SIGNAL_Y = 112;

    private static final int ERROR_WIDTH = 13;
    private static final int ERROR_HEIGHT = 13;
    private static final int OK_WIDTH = 14;
    private static final int OK_HEIGHT = 12;

    private final List<IAspectPropertyTypeInstance> propertyTypes;
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected IGuiInputElementValueType<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings, IGuiInputElementValueTypeClient<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings>> guiElement = null;
    protected int activePropertyIndex = 0;
    protected RenderPattern propertyConfigPattern = null;
    protected SubGuiValueTypeInfo propertyInfo = null;
    private ButtonText buttonLeft = null;
    private ButtonText buttonRight = null;
    private ButtonText buttonExit = null;
    private Component lastError;

    public ContainerScreenAspectSettings(ContainerAspectSettings container, Inventory inventory, Component title) {
        super(container, inventory, title);

        this.propertyTypes = Lists.newArrayList(container.getPropertyTypes());
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/aspect_settings.png");
    }

    public int getActivePropertyIndex() {
        return activePropertyIndex;
    }

    protected void saveSetting() {
        // Don't save while the setting is driven by a variable,
        // as the shown value is the variable's value, and not the statically configured value.
        if(guiElement != null && lastError == null && !container.isPropertyVariableFilled(getActivePropertyIndex())) {
            container.setValue(ValueDeseralizationContext.ofClient(), getActiveProperty(), guiElement.getValue());
        }
    }

    protected void refreshButtonEnabled() {
        buttonLeft.active = getActivePropertyIndex() > 0;
        buttonRight.active = getActivePropertyIndex() < propertyTypes.size() - 1;
    }

    @Override
    protected int getBaseYSize() {
        return 237;
    }

    @Override
    public void init() {
        super.init();
        subGuiHolder.init(this.leftPos, this.topPos);
        addRenderableWidget(buttonExit = new ButtonText(leftPos + 7, topPos + 5, 12, 10, Component.translatable("gui.cyclopscore.up"), Component.literal("<<"), createServerPressable(ContainerAspectSettings.BUTTON_EXIT, (button) -> {
            saveSetting();
        }), true));
        addRenderableWidget(buttonLeft = new ButtonText(leftPos + 21, topPos + 5, 10, 10, Component.translatable("gui.cyclopscore.left"), Component.literal("<"), (button) -> {
            saveSetting();
            if(getActivePropertyIndex() > 0) {
                setActiveProperty(getActivePropertyIndex() - 1);
                refreshButtonEnabled();
            }
        }, true));
        addRenderableWidget(buttonRight = new ButtonText(leftPos + 159, topPos + 5, 10, 10, Component.translatable("gui.cyclopscore.right"), Component.literal(">"), (button) -> {
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
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        subGuiHolder.renderBg(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, partialTicks, mouseX, mouseY);

        if (container.isPropertyVariableFilled(getActivePropertyIndex())) {
            IImage image = container.getPropertyVariableError(getActivePropertyIndex()) == null ? Images.OK : Images.ERROR;
            image.draw(guiGraphics, leftPos + VARIABLE_SIGNAL_X, topPos + VARIABLE_SIGNAL_Y);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshInputEnabled();
    }

    /**
     * The value of a property can not be edited manually while it is driven by a variable.
     */
    protected void refreshInputEnabled() {
        if (propertyConfigPattern instanceof GuiElementValueTypeStringRenderPattern<?, ?, ?> stringPattern) {
            WidgetTextFieldExtended textField = stringPattern.getTextField();
            if (textField != null) {
                textField.setEditable(!container.isPropertyVariableFilled(getActivePropertyIndex()));
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, mouseX, mouseY);

        IAspectPropertyTypeInstance activeProperty = getActiveProperty();
        if(activeProperty != null) {
            String label = IModHelpers.get().getL10NHelpers().localize(activeProperty.getTranslationKey());
            IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font, label, 88, 10, 0,
                    1.0F, 140, IModHelpers.get().getBaseHelpers().RGBAToInt(10, 10, 10, 255), false, Font.DisplayMode.NORMAL);
            if (IModHelpers.get().getRenderHelpers().isPointInRegion(this.leftPos + 40, this.topPos, 110, 20, mouseX, mouseY)) {
                String unlocalizedInfo = activeProperty.getTranslationKey() + ".info";
                if (Language.getInstance().has(unlocalizedInfo)) {
                    drawTooltip(Lists.newArrayList(Component.translatable(unlocalizedInfo)
                            .withStyle(ChatFormatting.GRAY)), guiGraphics, mouseX, mouseY + 20);
                }
            }
        }

        if (container.isPropertyVariableFilled(getActivePropertyIndex())) {
            IModHelpers.get().getGuiHelpers().renderTooltipOptional(this, guiGraphics, VARIABLE_SIGNAL_X, VARIABLE_SIGNAL_Y, 14, 13, mouseX, mouseY,
                    () -> {
                        Component error = container.getPropertyVariableError(getActivePropertyIndex());
                        if (error != null) {
                            return Optional.of(Collections.singletonList(error));
                        }
                        return Optional.of(Collections.singletonList(
                                Component.translatable("gui.integrateddynamics.aspectsettings.variable.driven")
                                        .withStyle(ChatFormatting.GRAY)));
                    });
        }
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if(!subGuiHolder.charTyped(evt)) {
            if (evt.codepoint() == 1 || this.getMinecraft().options.keyInventory.getKey().getValue() == evt.codepoint()) {
                saveSetting();
                this.getMinecraft().player.closeContainer();
            } else {
                return super.charTyped(evt);
            }
        } else {
            if(guiElement != null) {
                onValueChanged();
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (evt.key() != GLFW.GLFW_KEY_ESCAPE) {
            if (this.subGuiHolder.keyPressed(evt)) {
                if(guiElement != null) {
                    onValueChanged();
                }
                return true;
            } else {
                return false;
            }
        } else {
            // Don't close all GUIs, but go back to the part's aspect overview GUI.
            exitToPartGui(evt);
            return true;
        }
    }

    /**
     * Save the current setting and go back to the aspect overview GUI of the part.
     */
    protected void exitToPartGui(InputWithModifiers input) {
        buttonExit.onPress(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return subGuiHolder.mouseClicked(evt, isDoubleClick)
                || super.mouseClicked(evt, isDoubleClick);
    }

    protected void onValueChanged() {
        lastError = guiElement.validate();
    }

    protected IAspectPropertyTypeInstance getActiveProperty() {
        return propertyTypes.get(Math.max(0, Math.min(propertyTypes.size() - 1, activePropertyIndex)));
    }

    protected void setActiveProperty(int index) {
        this.activePropertyIndex = index;
        // Inform the server which property is being configured, so that it knows which variable slot is exposed.
        container.setActivePropertyIndex(index);
        if (getMinecraft().gameMode != null) {
            getMinecraft().gameMode.handleInventoryButtonClick(container.containerId, index);
        }
        onActivateElement(propertyTypes.get(activePropertyIndex));
    }

    protected void onActivateElement(IAspectPropertyTypeInstance property) {
        // Deactivate old element
        if(guiElement != null) {
            guiElement.deactivate();
            subGuiHolder.removeSubGui(propertyConfigPattern);
            subGuiHolder.removeSubGui(propertyInfo);
        }

        // Determine element type
        IValueTypeLogicProgrammerElement lpElement = property.getType().createLogicProgrammerElement();
        guiElement = lpElement.createInnerGuiElement();
        if (guiElement == null) {
            throw new UnsupportedOperationException("Tried to invoke createInnerGuiElement on a value type that does not have an inner gui element: " + property.getType().getTypeName());
        }

        // Create new element
        guiElement.setValidator(property.getValidator());
        subGuiHolder.addSubGui(propertyConfigPattern = guiElement.getClient().createSubGui(8, 17, 160, 91, this, (ContainerAspectSettings) getMenu()));
        subGuiHolder.addSubGui(propertyInfo = new SubGuiValueTypeInfo(guiElement));
        propertyConfigPattern.init(leftPos, topPos);
        guiElement.activate();
        syncInputValue();
        lastError = guiElement.validate();
    }

    protected void syncInputValue() {
        IAspectPropertyTypeInstance property = getActiveProperty();
        IValue value = container.getPropertyValue(ValueDeseralizationContext.of(Minecraft.getInstance().player.level()), property);
        if(value != null) {
            guiElement.setValue(value);
            guiElement.getClient().setValueInGui(propertyConfigPattern, false);
        }
        onValueChanged();
    }


    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);
        IAspectPropertyTypeInstance property = container.getPropertyIds().get(valueId);
        if(property != null && getActiveProperty() == property) {
            syncInputValue();
        }
    }

    public class SubGuiValueTypeInfo extends SubGuiValueTypeInfoBase<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> {

        public SubGuiValueTypeInfo(IGuiInputElement<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings, IGuiInputElementValueTypeClient<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings>> element) {
            super(ContainerScreenAspectSettings.this, (ContainerAspectSettings) ContainerScreenAspectSettings.this.container, element, 8, 129, 160, 20);
        }

        @Override
        protected boolean showError() {
            return true;
        }

        @Override
        protected Component getLastError() {
            return lastError;
        }

        @Override
        protected Identifier getTexture() {
            return texture;
        }

    }

}
