package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.cyclops.commoncapabilities.api.capability.fluidhandler.FluidMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesList;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket;

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

    public static final int SLOT_OFFSET = 4;
    public static final int TICK_DELAY = 30;

    @OnlyIn(Dist.CLIENT)
    public ValueTypeRecipeLPElementMasterSubGui lastGui;

    @Getter
    private NonNullList<ItemMatchProperties> inputStacks;
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
    public void onInputSlotUpdated(Player player, int slotId, ItemStack itemStack) {
        if (inputStacks == null) {
            return;
        }

        if (slotId >= 0 && slotId < 9) {
            ItemStack itemStackOld = inputStacks.get(slotId).getItemStack();
            if (itemStackOld.getItem() != itemStack.getItem()) {
                inputStacks.set(slotId, new ItemMatchProperties(itemStack.copy()));
                if (MinecraftHelpers.isClientSideThread()) {
                    refreshPropertiesGui(slotId);
                }
            }
        }
        if (slotId == 9) {
            inputFluid = itemStack.copy();
            if (inputFluidAmount.equalsIgnoreCase("0")) {
                int amount = FluidHelpers.getAmount(Helpers.getFluidStack(inputFluid));
                inputFluidAmount = Integer.toString(amount);
                if (MinecraftHelpers.isClientSideThread() && lastGui != null) {
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
                if (MinecraftHelpers.isClientSideThread() && lastGui != null) {
                    refreshOutputFluidAmountBox();
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void refreshPropertiesGui(int slot) {
        if (this.lastGui != null && this.lastGui.isPropertySubGuiActive(slot)) {
            this.lastGui.propertiesSubGuis.get(slot).loadStateToGui();
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void refreshInputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.subGuiRecipe.getInputFluidAmountBox() != null) {
            this.lastGui.subGuiRecipe.getInputFluidAmountBox().setValue(inputFluidAmount);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void refreshOutputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.subGuiRecipe.getOutputFluidAmountBox() != null) {
            this.lastGui.subGuiRecipe.getOutputFluidAmountBox().setValue(outputFluidAmount);
        }
    }

    public void sendSlotPropertiesToServer(int slotId, ItemMatchProperties props) {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(
                new LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket(
                        slotId, props.isNbt(), props.getItemTag() == null ? "" : props.getItemTag(), props.getTagQuantity()));
    }

    // Used by ID-Compat for JEI recipe transfer handler
    public boolean isValidForRecipeGrid(List<ItemMatchProperties> itemInputs, List<FluidStack> fluidInputs,
                                        List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
        return itemInputs.size() <= 9 && itemOutputs.size() <= 3
                && fluidInputs.size() <= 1 && fluidOutputs.size() <= 1;
    }

    protected void putItemPropertiesInContainer(ContainerLogicProgrammerBase container, int slot, ItemMatchProperties props) {
        putStackInContainer(container, slot, props.getItemStack());
        getInputStacks().set(slot, props);
    }

    protected void putStackInContainer(ContainerLogicProgrammerBase container, int slot, ItemStack itemStack) {
        // Offset: Player inventory, recipe grid slots
        container.setItem(container.getItems().size() - (36 + 14) + slot, 0, itemStack);
    }

    // Used by ID-Compat for JEI recipe transfer handler
    public void setRecipeGrid(ContainerLogicProgrammerBase container,
                              List<ItemMatchProperties> itemInputs, List<FluidStack> fluidInputs,
                              List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
        int slot = 0;

        // Fill input item slots
        for (ItemMatchProperties itemInput : itemInputs) {
            putItemPropertiesInContainer(container, slot, itemInput);
            slot++;
        }
        while (slot < 9) {
            putItemPropertiesInContainer(container, slot, new ItemMatchProperties(ItemStack.EMPTY));
            slot++;
        }

        // Fill input fluid slot
        slot = 9;
        FluidStack fluidStackInput = FluidStack.EMPTY;
        if (fluidInputs.size() > 0) {
            fluidStackInput = fluidInputs.get(0);
        }
        putStackInContainer(container, slot, fluidStackInput.isEmpty() ? ItemStack.EMPTY : getFluidBucket(fluidStackInput));
        inputFluidAmount = String.valueOf(FluidHelpers.getAmount(fluidStackInput));
        if (MinecraftHelpers.isClientSideThread()) {
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
        FluidStack fluidStackOutput = FluidStack.EMPTY;
        if (fluidOutputs.size() > 0) {
            fluidStackOutput = fluidOutputs.get(0);
        }
        putStackInContainer(container, slot, fluidStackOutput.isEmpty() ? ItemStack.EMPTY : getFluidBucket(fluidStackOutput));
        outputFluidAmount = String.valueOf(FluidHelpers.getAmount(fluidStackOutput));
        if (MinecraftHelpers.isClientSideThread()) {
            refreshOutputFluidAmountBox();
        }
    }

    protected ItemStack getFluidBucket(FluidStack fluidStack) {
        ItemStack itemStack = new ItemStack(Items.BUCKET);
        IFluidHandlerItem fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        fluidHandler.fill(new FluidStack(fluidStack, FluidHelpers.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
        return fluidHandler.getContainer();
    }

    protected boolean isInputValid() {
        return inputStacks.stream().anyMatch(ItemMatchProperties::isValid)
                || !inputFluid.isEmpty() || !inputFluidAmount.equalsIgnoreCase("0")
                || !inputEnergy.equalsIgnoreCase("0");
    }

    protected boolean isOutputValid() {
        return outputStacks.stream().anyMatch(stack -> !stack.isEmpty())
                || !outputFluid.isEmpty() || !outputFluidAmount.equalsIgnoreCase("0")
                || !outputEnergy.equalsIgnoreCase("0");
    }

    @Override
    public boolean canWriteElementPre() {
        return isInputValid() == isOutputValid(); // Not &&, because we also allow fully blank recipes
    }

    @Override
    public void activate() {
        inputStacks = NonNullList.withSize(9, new ItemMatchProperties(ItemStack.EMPTY));
        for (int i = 0; i < 9; i++) {
            inputStacks.set(i, new ItemMatchProperties(ItemStack.EMPTY));
        }
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
    public Component validate() {
        if (!inputFluid.isEmpty() && Helpers.getFluidStack(inputFluid).isEmpty()) {
            return Component.translatable(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
        }
        if (!outputFluid.isEmpty() && Helpers.getFluidStack(outputFluid).isEmpty()) {
            return Component.translatable(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
        }
        try {
            Integer.parseInt(inputFluidAmount);
        } catch (NumberFormatException e) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, inputFluidAmount);
        }
        try {
            Integer.parseInt(outputFluidAmount);
        } catch (NumberFormatException e) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, outputFluidAmount);
        }
        try {
            Long.parseLong(inputEnergy);
        } catch (NumberFormatException e) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, inputEnergy);
        }
        try {
            Long.parseLong(outputEnergy);
        } catch (NumberFormatException e) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, outputEnergy);
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, Player player) {
        if (slotId >= SLOT_OFFSET && slotId < 9 + SLOT_OFFSET) {
            if (clickType == ClickType.QUICK_MOVE && mouseButton == 0) {
                if (player.level.isClientSide()) {
                    int id = slotId - SLOT_OFFSET;
                    lastGui.setPropertySubGui(id);
                }
                return true;
            } else {
                // Similar logic as ContainerExtended.adjustPhantomSlot
                ItemMatchProperties props = getInputStacks().get(slotId - SLOT_OFFSET);
                int quantityCurrent = props.getTagQuantity();
                int quantityNew;
                if (clickType == ClickType.QUICK_MOVE) {
                    quantityNew = mouseButton == 0 ? (quantityCurrent + 1) / 2 : quantityCurrent * 2;
                } else {
                    quantityNew = mouseButton == 0 ? quantityCurrent - 1 : quantityCurrent + 1;
                }

                if (quantityNew > slot.getMaxStackSize()) {
                    quantityNew = slot.getMaxStackSize();
                }

                props.setTagQuantity(quantityNew);
                if (!props.getItemStack().isEmpty()) {
                    props.getItemStack().setCount(quantityNew);
                }

                if (quantityNew <= 0) {
                    props.setItemTag(null);
                    props.setTagQuantity(1);
                    if (MinecraftHelpers.isClientSideThread()) {
                        refreshPropertiesGui(slotId - SLOT_OFFSET);
                    }
                }
            }
        }

        return super.slotClick(slotId, slot, mouseButton, clickType, player);
    }

    @Override
    public Slot createSlot(Container temporaryInputSlots, int slotId, int x, int y) {
        SlotExtended slot = new SlotExtended(temporaryInputSlots, slotId, x, y) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return ValueTypeRecipeLPElement.this.isItemValidForSlot(slotId, itemStack);
            }

            @Override
            public ItemStack getItem() {
                if (MinecraftHelpers.isClientSideThread() && slotId < 9) {
                    ItemMatchProperties props = getInputStacks().get(slotId);
                    String tagName = props.getItemTag();
                    if (tagName != null) {
                        try {
                            ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Registries.ITEM, new ResourceLocation(tagName)));
                            if (!tag.isEmpty()) {
                                List<Item> items = tag.stream().toList();
                                int tick = ((int) Minecraft.getInstance().level.getGameTime()) / TICK_DELAY;
                                Item item = items.get(tick % items.size());
                                return new ItemStack(item, props.getTagQuantity());
                            }
                        } catch (ResourceLocationException e) {
                            // Ignore invalid tags
                        }
                    }
                }
                return super.getItem();
            }
        };
        slot.setPhantom(true);
        return slot;
    }

    @Override
    public int getItemStackSizeLimit() {
        return 64;
    }

    protected Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> getInputs(List<ItemMatchProperties> itemStacks,
                                                                                                      ItemStack fluid, int fluidAmount,
                                                                                                      long energy) {
        // Cut of itemStacks list until last non-empty stack
        int lastNonEmpty = 0;
        for (int i = 0; i < itemStacks.size(); i++) {
            if (itemStacks.get(i).isValid()) {
                lastNonEmpty = i + 1;
            }
        }
        itemStacks = itemStacks.subList(0, lastNonEmpty);

        // Override fluid amount
        FluidStack fluidStack = Helpers.getFluidStack(fluid);
        if (!fluidStack.isEmpty()) {
            fluidStack.setAmount(fluidAmount);
        }

        Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
        List<IPrototypedIngredientAlternatives<ItemStack, Integer>> items = itemStacks.stream()
                .map(ItemMatchProperties::createPrototypedIngredient)
                .collect(Collectors.toList());
        List<IPrototypedIngredientAlternatives<FluidStack, Integer>> fluids = !fluidStack.isEmpty()
                ? Collections.singletonList(new PrototypedIngredientAlternativesList<>(
                        Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.FLUIDSTACK, fluidStack, FluidMatch.FLUID | FluidMatch.TAG))))
                : Collections.emptyList();
        List<IPrototypedIngredientAlternatives<Long, Boolean>> energies = energy > 0 ?
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

    protected Map<IngredientComponent<?, ?>, List<?>> getOutputs(List<ItemStack> itemStacksIn,
                                                                 ItemStack fluid, int fluidAmount,
                                                                 long energy) {
        // Cut of itemStacks list until last non-empty stack
        List<ItemStack> itemStacks = Lists.newArrayList();
        for (int i = 0; i < itemStacksIn.size(); i++) {
            if (!itemStacksIn.get(i).isEmpty()) {
                itemStacks.add(itemStacksIn.get(i));
            }
        }

        // Override fluid amount
        FluidStack fluidStack = Helpers.getFluidStack(fluid);
        if (!fluidStack.isEmpty()) {
            fluidStack.setAmount(fluidAmount);
        }

        Map<IngredientComponent<?, ?>, List<?>> outputs = Maps.newIdentityHashMap();
        if (!itemStacks.isEmpty()) {
            outputs.put(IngredientComponent.ITEMSTACK, itemStacks);
        }
        if (!fluidStack.isEmpty()) {
            outputs.put(IngredientComponent.FLUIDSTACK, Collections.singletonList(fluidStack));
        }
        if (energy > 0) {
            outputs.put(IngredientComponent.ENERGY, Collections.singletonList(energy));
        }

        return outputs;
    }

    @Override
    public IValue getValue() {
        if (!isInputValid() && !isOutputValid()) {
            return ValueObjectTypeRecipe.ValueRecipe.of(null);
        }
        return ValueObjectTypeRecipe.ValueRecipe.of(
                new RecipeDefinition(getInputs(this.inputStacks, this.inputFluid,
                        Integer.parseInt(this.inputFluidAmount), Long.parseLong(this.inputEnergy)),
                new MixedIngredients(getOutputs(this.outputStacks, this.outputFluid,
                        Integer.parseInt(this.outputFluidAmount), Long.parseLong(this.outputEnergy)))));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return lastGui = new ValueTypeRecipeLPElementMasterSubGui(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {
        ValueTypeRecipeLPElementRecipeSubGui gui = ((ValueTypeRecipeLPElementMasterSubGui) subGui).getSubGuiRecipe();
        Container slots = gui.container.getTemporaryInputSlots();
        for (int i = 0; i < this.inputStacks.size(); i++) {
            ItemMatchProperties entry = this.inputStacks.get(i);
            slots.setItem(i, entry.getItemStack());
        }
        slots.setItem(9, this.inputFluid);
        if (gui.getInputFluidAmountBox() != null) {
            gui.getInputFluidAmountBox().setValue(this.inputFluidAmount);
            gui.getInputEnergyBox().setValue(this.inputEnergy);
            for (int i = 0; i < this.outputStacks.size(); i++) {
                slots.setItem(10 + i, this.outputStacks.get(i));
                // No need to set slot type, as this can't be changed for output stacks
            }
            slots.setItem(13, this.outputFluid);
            gui.getOutputFluidAmountBox().setValue(this.outputFluidAmount);
            gui.getOutputEnergyBox().setValue(this.outputEnergy);
        }
    }

}
