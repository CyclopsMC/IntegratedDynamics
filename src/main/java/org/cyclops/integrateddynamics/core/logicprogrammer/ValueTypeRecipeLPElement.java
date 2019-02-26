package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.fluidhandler.FluidMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesList;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.component.input.GuiTextFieldExtended;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchType;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeRecipeValueChangedPacket;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

    private NonNullList<Pair<ItemStack, ItemMatchType>> inputStacks;
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

    public static ItemMatchType getDefaultItemMatch() {
        return ItemMatchType.ITEMMETA;
    }

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
        if (inputStacks == null) {
            return;
        }

        if (slotId >= 0 && slotId < 9) {
            inputStacks.set(slotId, Pair.of(itemStack.copy(), inputStacks.get(slotId).getRight()));
        }
        if (slotId == 9) {
            inputFluid = itemStack.copy();
            if (inputFluidAmount.equalsIgnoreCase("0")) {
                int amount = FluidHelpers.getAmount(Helpers.getFluidStack(inputFluid));
                inputFluidAmount = Integer.toString(amount);
                if (MinecraftHelpers.isClientSide() && lastGui != null) {
                    refreshInputFluidAmountBox();
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
                    refreshOutputFluidAmountBox();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void refreshInputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.getInputFluidAmountBox() != null) {
            this.lastGui.getInputFluidAmountBox().setText(inputFluidAmount);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void refreshOutputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.getOutputFluidAmountBox() != null) {
            this.lastGui.getOutputFluidAmountBox().setText(outputFluidAmount);
        }
    }

    // Used by ID-Compat for JEI recipe transfer handler
    public boolean isValidForRecipeGrid(List<ItemStack> itemInputs, List<FluidStack> fluidInputs,
                                        List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
        return itemInputs.size() <= 9 && itemOutputs.size() <= 3
                && fluidInputs.size() <= 1 && fluidOutputs.size() <= 1;
    }

    protected void putStackInContainer(ContainerLogicProgrammerBase container, int slot, ItemStack itemStack) {
        // Offset: Player inventory, recipe grid slots
        container.putStackInSlot(container.inventorySlots.size() - (36 + 14) + slot, itemStack);
    }

    // Used by ID-Compat for JEI recipe transfer handler
    public void setRecipeGrid(ContainerLogicProgrammerBase container,
                              List<ItemStack> itemInputs, List<FluidStack> fluidInputs,
                              List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
        int slot = 0;

        // Fill input item slots
        for (ItemStack itemInput : itemInputs) {
            putStackInContainer(container, slot, itemInput);
            slot++;
        }
        while (slot < 9) {
            putStackInContainer(container, slot, ItemStack.EMPTY);
            slot++;
        }

        // Fill input fluid slot
        slot = 9;
        FluidStack fluidStackInput = null;
        if (fluidInputs.size() > 0) {
            fluidStackInput = fluidInputs.get(0);
        }
        putStackInContainer(container, slot, fluidStackInput == null ? ItemStack.EMPTY : getFluidBucket(fluidStackInput));
        inputFluidAmount = String.valueOf(FluidHelpers.getAmount(fluidStackInput));
        if (MinecraftHelpers.isClientSide()) {
            refreshInputFluidAmountBox();
        }

        // Fill input output slots
        slot = 10;
        for (ItemStack itemOutput : itemOutputs) {
            putStackInContainer(container, slot, itemOutput);
            slot++;
        }
        while (slot < 13) {
            putStackInContainer(container, slot, ItemStack.EMPTY);
            slot++;
        }

        // Fill output fluid slot
        slot = 13;
        FluidStack fluidStackOutput = null;
        if (fluidOutputs.size() > 0) {
            fluidStackOutput = fluidOutputs.get(0);
        }
        putStackInContainer(container, slot, fluidStackOutput == null ? ItemStack.EMPTY : getFluidBucket(fluidStackOutput));
        outputFluidAmount = String.valueOf(FluidHelpers.getAmount(fluidStackOutput));
        if (MinecraftHelpers.isClientSide()) {
            refreshOutputFluidAmountBox();
        }
    }

    protected ItemStack getFluidBucket(FluidStack fluidStack) {
        ItemStack itemStack = new ItemStack(Items.BUCKET);
        IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        fluidHandler.fill(new FluidStack(fluidStack, Fluid.BUCKET_VOLUME), true);
        return fluidHandler.getContainer();
    }

    @Override
    public boolean canWriteElementPre() {
        boolean inputValid = inputStacks.stream().anyMatch(stack -> !stack.getLeft().isEmpty())
                || !inputFluid.isEmpty() || !inputFluidAmount.equalsIgnoreCase("0")
                || !inputEnergy.equalsIgnoreCase("0");
        boolean outputValid = outputStacks.stream().anyMatch(stack -> !stack.isEmpty())
                || !outputFluid.isEmpty() || !outputFluidAmount.equalsIgnoreCase("0")
                || !outputEnergy.equalsIgnoreCase("0");
        return inputValid && outputValid;
    }

    @Override
    public void activate() {
        inputStacks = NonNullList.withSize(9, Pair.of(ItemStack.EMPTY, getDefaultItemMatch()));
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
    public Slot createSlot(IInventory temporaryInputSlots, int slotId, int x, int y) {
        Slot slot = ILogicProgrammerElement.createSlotDefault(this, temporaryInputSlots, slotId, x, y);
        if (slotId < 9) {
            slot.setBackgroundName(getDefaultItemMatch().getSlotSpriteName().toString());
        }
        return slot;
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, EntityPlayer player) {
        if (slotId >= 4 && slotId < 13 && mouseButton == 0 && clickType == ClickType.QUICK_MOVE) {
            int id = slotId - 4;
            this.inputStacks.set(id, Pair.of(this.inputStacks.get(id).getLeft(), this.inputStacks.get(id).getRight().next()));
            slot.setBackgroundName(this.inputStacks.get(id).getRight().getSlotSpriteName().toString());
            return true;
        }

        return super.slotClick(slotId, slot, mouseButton, clickType, player);
    }

    @Override
    public int getItemStackSizeLimit() {
        return 64;
    }

    protected Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> getInputs(List<Pair<ItemStack, ItemMatchType>> itemStacks,
                                                                                                      ItemStack fluid, int fluidAmount,
                                                                                                      int energy) {
        // Cut of itemStacks list until last non-empty stack
        int lastNonEmpty = 0;
        for (int i = 0; i < itemStacks.size(); i++) {
            if (!itemStacks.get(i).getLeft().isEmpty()) {
                lastNonEmpty = i + 1;
            }
        }
        itemStacks = itemStacks.subList(0, lastNonEmpty);

        // Override fluid amount
        FluidStack fluidStack = Helpers.getFluidStack(fluid);
        if (fluidStack != null) {
            fluidStack.amount = fluidAmount;
        }

        Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
        List<IPrototypedIngredientAlternatives<ItemStack, Integer>> items = itemStacks.stream()
                .map(stack -> stack.getRight().getPrototypeHandler().getPrototypesFor(stack.getLeft()))
                .collect(Collectors.toList());
        List<IPrototypedIngredientAlternatives<FluidStack, Integer>> fluids = fluidStack != null
                ? Collections.singletonList(new PrototypedIngredientAlternativesList<>(
                        Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.FLUIDSTACK, fluidStack, FluidMatch.NBT))))
                : Collections.emptyList();
        List<IPrototypedIngredientAlternatives<Integer, Boolean>> energies = energy > 0 ?
                Collections.singletonList(new PrototypedIngredientAlternativesList<>(
                        Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ENERGY, energy, false))))
                : Collections.emptyList();
        if (!items.isEmpty()) {
            inputs.put(IngredientComponent.ITEMSTACK, (List) items);
        }
        if (!fluids.isEmpty()) {
            inputs.put(IngredientComponent.FLUIDSTACK, (List) fluids);
        }
        if (!energies.isEmpty()) {
            inputs.put(IngredientComponent.ENERGY, (List) energies);
        }

        return inputs;
    }

    protected Map<IngredientComponent<?, ?>, List<?>> getOutputs(List<ItemStack> itemStacks,
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

        Map<IngredientComponent<?, ?>, List<?>> outputs = Maps.newIdentityHashMap();
        if (!itemStacks.isEmpty()) {
            outputs.put(IngredientComponent.ITEMSTACK, itemStacks);
        }
        if (fluidStack != null) {
            outputs.put(IngredientComponent.FLUIDSTACK, Collections.singletonList(fluidStack));
        }
        if (energy > 0) {
            outputs.put(IngredientComponent.ENERGY, Collections.singletonList(energy));
        }

        return outputs;
    }

    @Override
    public IValue getValue() {
        return ValueObjectTypeRecipe.ValueRecipe.of(
                new RecipeDefinition(getInputs(this.inputStacks, this.inputFluid,
                        Integer.parseInt(this.inputFluidAmount), Integer.parseInt(this.inputEnergy)),
                new MixedIngredients(getOutputs(this.outputStacks, this.outputFluid,
                        Integer.parseInt(this.outputFluidAmount), Integer.parseInt(this.outputEnergy)))));
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
            Pair<ItemStack, ItemMatchType> entry = this.inputStacks.get(i);
            slots.setInventorySlotContents(i, entry.getLeft());
        }
        slots.setInventorySlotContents(9, this.inputFluid);
        if (gui.getInputFluidAmountBox() != null) {
            gui.getInputFluidAmountBox().setText(this.inputFluidAmount);
            gui.getInputEnergyBox().setText(this.inputEnergy);
            for (int i = 0; i < this.outputStacks.size(); i++) {
                slots.setInventorySlotContents(10 + i, this.outputStacks.get(i));
                // No need to set slot type, as this can't be changed for output stacks
            }
            slots.setInventorySlotContents(13, this.outputFluid);
            gui.getOutputFluidAmountBox().setText(this.outputFluidAmount);
            gui.getOutputEnergyBox().setText(this.outputEnergy);
        }
    }

    @SideOnly(Side.CLIENT)
    protected static class SubGuiRenderPattern extends RenderPattern<ValueTypeRecipeLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase>
            implements IRenderPatternValueTypeTooltip {

        @Getter
        @Setter
        private boolean renderTooltip = true;
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

            // Output type tooltip
            this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());

            // Render the overlay of the input item slots
            for (int slotId = 0; slotId < this.gui.inventorySlots.inventorySlots.size(); ++slotId) {
                Slot slot = this.gui.inventorySlots.inventorySlots.get(slotId);
                if (slotId >= 4 && slotId < 13) {
                    int slotX = slot.xPos;
                    int slotY = slot.yPos;
                    // Only render if the slot has a stack, otherwise vanilla will already render the overlay.
                    if (slot.getHasStack() && slot.isEnabled()) {
                        TextureAtlasSprite textureatlassprite = slot.getBackgroundSprite();
                        if (textureatlassprite != null) {
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.color(1, 1, 1);
                            this.gui.mc.getTextureManager().bindTexture(slot.getBackgroundLocation());
                            this.drawTexturedModalRect(slotX, slotY, textureatlassprite, 16, 16);
                            GlStateManager.enableDepth();
                        }
                    }

                    // Draw tooltips
                    if (gui.isPointInRegion(slotX, slotY, 16, 16, mouseX, mouseY)) {
                        String name = "valuetype.valuetypes.integrateddynamics.ingredients.match."
                                + this.element.inputStacks.get(slot.getSlotIndex()).getRight().name().toLowerCase(Locale.ENGLISH);
                        gui.drawTooltip(Lists.newArrayList(
                                L10NHelpers.localize(name + ".desc") + " "
                                        + TextFormatting.RESET + TextFormatting.ITALIC
                                        + L10NHelpers.localize("valuetype.valuetypes.integrateddynamics.ingredients.info")
                        ), mouseX - guiLeft, mouseY - guiTop - 15);
                    }
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
