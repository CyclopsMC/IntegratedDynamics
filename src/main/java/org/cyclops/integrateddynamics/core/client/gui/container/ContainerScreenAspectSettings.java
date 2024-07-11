package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
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
    protected IGuiInputElementValueType<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> guiElement = null;
    protected int activePropertyIndex = 0;
    protected RenderPattern propertyConfigPattern = null;
    protected SubGuiValueTypeInfo propertyInfo = null;
    private ButtonText buttonLeft = null;
    private ButtonText buttonRight = null;
    private ButtonText buttonExit = null;
    private Component lastError;

    public ContainerScreenAspectSettings(ContainerAspectSettings container, Inventory inventory, Component title) {
        super(container, inventory, title);

        //noinspection deprecation
        this.propertyTypes = Lists.newArrayList(container.getAspect().getDefaultProperties().getTypes());
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/aspect_settings.png");
    }

    public int getActivePropertyIndex() {
        return activePropertyIndex;
    }

    protected void saveSetting() {
        if(guiElement != null && lastError == null) {
            container.setValue(ValueDeseralizationContext.ofClient(), getActiveProperty(), guiElement.getValue());
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        subGuiHolder.renderBg(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, mouseX, mouseY);

        IAspectPropertyTypeInstance activeProperty = getActiveProperty();
        if(activeProperty != null) {
            String label = L10NHelpers.localize(activeProperty.getTranslationKey());
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font, label, 88, 10, 0,
                    1.0F, 140, Helpers.RGBToInt(10, 10, 10), false, Font.DisplayMode.NORMAL);
            if (RenderHelpers.isPointInRegion(this.leftPos + 40, this.topPos, 110, 20, mouseX, mouseY)) {
                String unlocalizedInfo = activeProperty.getTranslationKey() + ".info";
                if (I18n.exists(unlocalizedInfo)) {
                    drawTooltip(Lists.newArrayList(Component.translatable(unlocalizedInfo)
                            .withStyle(ChatFormatting.GRAY)), guiGraphics.pose(), mouseX - this.leftPos, mouseY - this.topPos + 20);
                }
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(!subGuiHolder.charTyped(typedChar, keyCode)) {
            if (keyCode == 1 || this.getMinecraft().options.keyInventory.getKey().getValue() == keyCode) {
                saveSetting();
                this.getMinecraft().player.closeContainer();
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
            if (this.subGuiHolder.keyPressed(typedChar, keyCode, modifiers)) {
                if(guiElement != null) {
                    onValueChanged();
                }
                return true;
            } else {
                return false;
            }
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
        subGuiHolder.addSubGui(propertyConfigPattern = guiElement.createSubGui(8, 17, 160, 91, this, (ContainerAspectSettings) getMenu()));
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
            guiElement.setValue(value, propertyConfigPattern);
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

    public class SubGuiValueTypeInfo extends GuiElementValueTypeString.SubGuiValueTypeInfo<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> {

        public SubGuiValueTypeInfo(IGuiInputElement<RenderPattern, ContainerScreenAspectSettings, ContainerAspectSettings> element) {
            super(ContainerScreenAspectSettings.this, (ContainerAspectSettings) ContainerScreenAspectSettings.this.container, element, 8, 105, 160, 20);
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
        protected ResourceLocation getTexture() {
            return texture;
        }

    }

}
