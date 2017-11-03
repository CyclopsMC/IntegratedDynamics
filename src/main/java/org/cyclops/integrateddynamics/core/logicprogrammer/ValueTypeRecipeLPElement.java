package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.client.gui.component.input.GuiTextFieldExtended;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeLists;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeRecipeValueChangedPacket;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Element for recipes.
 * This is hardcoded to only support items, fluids and energy
 * @author rubensworks
 */
public class ValueTypeRecipeLPElement extends ValueTypeLPElementBase {

    @SideOnly(Side.CLIENT)
    private SubGuiRenderPattern lastGui;

    private NonNullList<ItemStack> inputStacks;
    private ItemStack inputFluid;
    @Getter
    @Setter
    private String inputFluidAmount = "0";
    @Getter
    @Setter
    private String inputEnergy = "0";
    private NonNullList<ItemStack> outputStacks;
    private ItemStack outputFluid;
    @Getter
    @Setter
    private String outputFluidAmount = "0";
    @Getter
    @Setter
    private String outputEnergy = "0";

    public ValueTypeRecipeLPElement() {
        super(ValueTypes.OBJECT_RECIPE);
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.RECIPE;
    }

    @Override
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {
        if (slotId >= 0 && slotId < 9) {
            inputStacks.set(slotId, itemStack.copy());
        }
        if (slotId == 9) {
            inputFluid = itemStack.copy();
            if (inputFluidAmount.equalsIgnoreCase("0")) {
                int amount = FluidHelpers.getAmount(Helpers.getFluidStack(inputFluid));
                inputFluidAmount = Integer.toString(amount);
                if (MinecraftHelpers.isClientSide() && lastGui != null) {
                    this.lastGui.getInputFluidAmountBox().setText(inputFluidAmount);
                }
            }
        }
        if (slotId > 9 && slotId < 13) {
            outputStacks.set(slotId - 10, itemStack.copy());
        }
        if (slotId == 13) {
            outputFluid = itemStack.copy();
            if (outputFluidAmount.equalsIgnoreCase("0")) {
                int amount = FluidHelpers.getAmount(Helpers.getFluidStack(outputFluid));
                outputFluidAmount = Integer.toString(amount);
                if (MinecraftHelpers.isClientSide() && lastGui != null) {
                    this.lastGui.getOutputFluidAmountBox().setText(outputFluidAmount);
                }
            }
        }
    }

    @Override
    public boolean canWriteElementPre() {
        boolean inputValid = inputStacks.stream().anyMatch(stack -> !stack.isEmpty())
                || !inputFluid.isEmpty() || !inputFluidAmount.equalsIgnoreCase("0")
                || !inputEnergy.equalsIgnoreCase("0");
        boolean outputValid = outputStacks.stream().anyMatch(stack -> !stack.isEmpty())
                || !outputFluid.isEmpty() || !outputFluidAmount.equalsIgnoreCase("0")
                || !outputEnergy.equalsIgnoreCase("0");
        return inputValid && outputValid;
    }

    @Override
    public void activate() {
        inputStacks = NonNullList.withSize(9, ItemStack.EMPTY);
        inputFluid = ItemStack.EMPTY;
        inputFluidAmount = "0";
        inputEnergy = "0";
        outputStacks = NonNullList.withSize(3, ItemStack.EMPTY);
        outputFluid = ItemStack.EMPTY;
        outputFluidAmount = "0";
        outputEnergy = "0";
    }

    @Override
    public void deactivate() {

    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        if (!inputFluid.isEmpty() && Helpers.getFluidStack(inputFluid) == null) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
        }
        if (!outputFluid.isEmpty() && Helpers.getFluidStack(outputFluid) == null) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
        }
        try {
            Integer.parseInt(inputFluidAmount);
        } catch (NumberFormatException e) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, inputFluidAmount);
        }
        try {
            Integer.parseInt(outputFluidAmount);
        } catch (NumberFormatException e) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, outputFluidAmount);
        }
        try {
            Integer.parseInt(inputEnergy);
        } catch (NumberFormatException e) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, inputEnergy);
        }
        try {
            Integer.parseInt(outputEnergy);
        } catch (NumberFormatException e) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, outputEnergy);
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return true;
    }

    @Override
    public int getItemStackSizeLimit() {
        return 64;
    }

    protected ValueObjectTypeIngredients.ValueIngredients getIngredients(List<ItemStack> itemStacks,
                                                                         ItemStack fluid, int fluidAmount,
                                                                         int energy) {
        // Cut of itemStacks list until last non-empty stack
        int lastNonEmpty = 0;
        for (int i = 0; i < itemStacks.size(); i++) {
            if (!itemStacks.get(i).isEmpty()) {
                lastNonEmpty = i + 1;
            }
        }
        itemStacks = itemStacks.subList(0, lastNonEmpty);

        // Override fluid amount
        FluidStack fluidStack = Helpers.getFluidStack(fluid);
        if (fluidStack != null) {
            fluidStack.amount = fluidAmount;
        }

        Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists = Maps.newIdentityHashMap();
        lists.put(RecipeComponent.ITEMSTACK, itemStacks.stream().map(stack -> Collections.singletonList(ValueObjectTypeItemStack
                .ValueItemStack.of(stack))).collect(Collectors.toList()));
        lists.put(RecipeComponent.FLUIDSTACK, fluidStack != null ? Collections.singletonList(
                Collections.singletonList(ValueObjectTypeFluidStack.ValueFluidStack.of(fluidStack))) : Collections.emptyList());
        lists.put(RecipeComponent.ENERGY, energy > 0 ? Collections.singletonList(Collections.singletonList(ValueTypeInteger
                .ValueInteger.of(energy))) : Collections.emptyList());

        return ValueObjectTypeIngredients.ValueIngredients.of(new IngredientsRecipeLists(lists));
    }

    @Override
    public IValue getValue() {
        ValueObjectTypeIngredients.ValueIngredients input = getIngredients(this.inputStacks, this.inputFluid,
                Integer.parseInt(this.inputFluidAmount), Integer.parseInt(this.inputEnergy));
        ValueObjectTypeIngredients.ValueIngredients output = getIngredients(this.outputStacks, this.outputFluid,
                Integer.parseInt(this.outputFluidAmount), Integer.parseInt(this.outputEnergy));
        return ValueObjectTypeRecipe.ValueRecipe.of(new ValueObjectTypeRecipe.Recipe(input, output));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return lastGui = new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {
        ValueTypeRecipeLPElement.SubGuiRenderPattern gui = ((ValueTypeRecipeLPElement.SubGuiRenderPattern) subGui);
        IInventory slots = gui.container.getTemporaryInputSlots();
        for (int i = 0; i < this.inputStacks.size(); i++) {
            slots.setInventorySlotContents(i, this.inputStacks.get(i));
        }
        slots.setInventorySlotContents(9, this.inputFluid);
        gui.getInputFluidAmountBox().setText(this.inputFluidAmount);
        gui.getInputEnergyBox().setText(this.inputEnergy);
        for (int i = 0; i < this.outputStacks.size(); i++) {
            slots.setInventorySlotContents(10 + i, this.outputStacks.get(i));
        }
        slots.setInventorySlotContents(13, this.outputFluid);
        gui.getOutputFluidAmountBox().setText(this.outputFluidAmount);
        gui.getOutputEnergyBox().setText(this.outputEnergy);
    }

    @SideOnly(Side.CLIENT)
    protected static class SubGuiRenderPattern extends RenderPattern<ValueTypeRecipeLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

        @Getter
        private GuiTextFieldExtended inputFluidAmountBox = null;
        @Getter
        private GuiTextFieldExtended inputEnergyBox = null;
        @Getter
        private GuiTextFieldExtended outputFluidAmountBox = null;
        @Getter
        private GuiTextFieldExtended outputEnergyBox = null;

        public SubGuiRenderPattern(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        protected static GuiTextFieldExtended makeTextBox(int componentId, int x, int y, String text) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int searchWidth = 35;

            GuiTextFieldExtended box = new GuiTextFieldExtended(componentId, fontRenderer, x, y,
                    searchWidth, fontRenderer.FONT_HEIGHT + 3, true);
            box.setMaxStringLength(10);
            box.setEnableBackgroundDrawing(false);
            box.setVisible(true);
            box.setTextColor(16777215);
            box.setCanLoseFocus(true);
            box.setText(text);
            box.width = searchWidth;
            return box;
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);

            this.inputFluidAmountBox = makeTextBox(0, guiLeft + getX() + 21, guiTop + getY() + 59, element.getInputFluidAmount());
            this.inputEnergyBox = makeTextBox(1, guiLeft + getX() + 21, guiTop + getY() + 77, element.getInputEnergy());
            this.outputFluidAmountBox = makeTextBox(2, guiLeft + getX() + 101, guiTop + getY() + 59, element.getOutputFluidAmount());
            this.outputEnergyBox = makeTextBox(3, guiLeft + getX() + 101, guiTop + getY() + 77, element.getOutputEnergy());
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            IValueType valueType = element.getValueType();

            // Output type tooltip
            if(!container.hasWriteItemInSlot()) {
                if(gui.isPointInRegion(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                        GuiLogicProgrammerBase.BOX_HEIGHT, GuiLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            // Draw crafting arrow
            this.drawTexturedModalRect(guiLeft + getX() + 66, guiTop + getY() + 21, 0, 38, 22, 15);

            inputFluidAmountBox.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
            fontRenderer.drawString(L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 2, guiTop + getY() + 78, 0);
            inputEnergyBox.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
            outputFluidAmountBox.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
            fontRenderer.drawString(L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 84, guiTop + getY() + 78, 0);
            outputEnergyBox.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        @Override
        public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
            if (!checkHotbarKeys) {
                if (inputFluidAmountBox.textboxKeyTyped(typedChar, keyCode)) {
                    element.setInputFluidAmount(inputFluidAmountBox.getText());
                    container.onDirty();
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputFluidAmount(),
                                    LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_FLUID));
                    return true;
                }
                if (inputEnergyBox.textboxKeyTyped(typedChar, keyCode)) {
                    element.setInputEnergy(inputEnergyBox.getText());
                    container.onDirty();
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputEnergy(),
                                    LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_ENERGY));
                    return true;
                }
                if (outputFluidAmountBox.textboxKeyTyped(typedChar, keyCode)) {
                    element.setOutputFluidAmount(outputFluidAmountBox.getText());
                    container.onDirty();
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputFluidAmount(),
                                    LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_FLUID));
                    return true;
                }
                if (outputEnergyBox.textboxKeyTyped(typedChar, keyCode)) {
                    element.setOutputEnergy(outputEnergyBox.getText());
                    container.onDirty();
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputEnergy(),
                                    LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_ENERGY));
                    return true;
                }
            }
            return super.keyTyped(checkHotbarKeys, typedChar, keyCode);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            inputFluidAmountBox.mouseClicked(mouseX, mouseY, mouseButton);
            inputEnergyBox.mouseClicked(mouseX, mouseY, mouseButton);
            outputFluidAmountBox.mouseClicked(mouseX, mouseY, mouseButton);
            outputEnergyBox.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

}
