package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeValueChangedPacket;

import java.io.IOException;
import java.util.List;

/**
 * Element for value type.
 * @author rubensworks
 */
@Data
public class ValueTypeElement implements ILogicProgrammerElement {

    private final IValueType valueType;
    private final String defaultInputString;
    private String inputString;

    public ValueTypeElement(IValueType valueType) {
        this.valueType = valueType;
        defaultInputString = getValueType().toCompactString(getValueType().getDefault());
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public String getMatchString() {
        return getLocalizedNameFull().toLowerCase();
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
    }

    @Override
    public void loadTooltip(List<String> lines) {
        getValueType().loadTooltip(lines, true);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {

    }

    @Override
    public boolean canWriteElementPre() {
        return inputString != null && !inputString.isEmpty();
    }

    protected int[] getVariableIds(IVariableFacade[] inputVariables) {
        int[] variableIds = new int[inputVariables.length];
        for(int i = 0; i < inputVariables.length; i++) {
            variableIds[i] = inputVariables[i].getId();
        }
        return variableIds;
    }

    @Override
    public ItemStack writeElement(ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY, new ValueTypeVariableFacadeFactory(valueType, inputString));
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return this.inputString == null || this.inputString.equals(defaultInputString);
    }

    @Override
    public void activate() {
        this.inputString = new String(defaultInputString);
    }

    @Override
    public void deactivate() {
        this.inputString = null;
    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        return getValueType().canDeserialize(inputString);
    }

    @Override
    public int getColor() {
        return getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof ValueTypeVariableFacade) {
            ValueTypeVariableFacade valueTypeFacade = (ValueTypeVariableFacade) variableFacade;
            if (valueTypeFacade.isValid()) {
                return getValueType() == valueTypeFacade.getValueType();
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
        return new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<ValueTypeVariableFacade> {

        private final IValueType valueType;
        private final String value;

        public ValueTypeVariableFacadeFactory(IValueType valueType, String value) {
            this.valueType = valueType;
            this.value = value;
        }

        @Override
        public ValueTypeVariableFacade create(boolean generateId) {
            return new ValueTypeVariableFacade(generateId, valueType, value);
        }

        @Override
        public ValueTypeVariableFacade create(int id) {
            return new ValueTypeVariableFacade(id, valueType, value);
        }
    }

    @SideOnly(Side.CLIENT)
    protected class SubGuiRenderPattern extends SubGuiConfigRenderPattern<ValueTypeElement> {

        private GuiTextField searchField = null;

        public SubGuiRenderPattern(ValueTypeElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            int searchWidth = 71;
            int searchX = getX() + 14;
            int searchY = getY() + 6;
            this.searchField = new GuiTextField(0, fontRenderer, guiLeft + searchX, guiTop + searchY, searchWidth, fontRenderer.FONT_HEIGHT);
            this.searchField.setMaxStringLength(64);
            this.searchField.setMaxStringLength(15);
            this.searchField.setEnableBackgroundDrawing(false);
            this.searchField.setVisible(true);
            this.searchField.setTextColor(16777215);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setText(getValueType().toCompactString(getValueType().getDefault()));
            inputString = searchField.getText();
            this.searchField.width = searchWidth;
            this.searchField.xPosition = guiLeft + (searchX + searchWidth) - this.searchField.width;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            textureManager.bindTexture(TEXTURE);
            this.drawTexturedModalRect(searchField.xPosition - 1, searchField.yPosition - 1, 21, 0, searchField.width + 1, 12);
            // Textbox
            searchField.drawTextBox();
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            IValueType valueType = element.getValueType();

            // Output type tooltip
            if(!container.hasWriteItemInSlot()) {
                if(gui.isPointInRegion(ContainerLogicProgrammer.OUTPUT_X, ContainerLogicProgrammer.OUTPUT_Y,
                        GuiLogicProgrammer.BOX_HEIGHT, GuiLogicProgrammer.BOX_HEIGHT, mouseX, mouseY)) {
                    List<String> lines = Lists.newLinkedList();
                    lines.add(valueType.getDisplayColorFormat() + L10NHelpers.localize(valueType.getUnlocalizedName()));
                    gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        @Override
        public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
            if (!checkHotbarKeys) {
                if (searchField.textboxKeyTyped(typedChar, keyCode)) {
                    inputString = searchField.getText();
                    container.onDirty();
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeValueChangedPacket(inputString));
                    return true;
                }
            }
            return super.keyTyped(checkHotbarKeys, typedChar, keyCode);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

    }

}
