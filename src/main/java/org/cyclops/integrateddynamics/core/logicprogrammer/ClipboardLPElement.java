package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Element for creating a variable from a value that was copied to the clipboard,
 * possibly in another world.
 *
 * @author rubensworks
 */
public class ClipboardLPElement implements ILogicProgrammerElement<ISubGuiBox, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    @Nullable
    private IValue value = null;
    @Nullable
    private Component error = null;

    @Nullable
    public IValue getValue() {
        return value;
    }

    public void setValue(@Nullable IValue value) {
        this.value = value;
        this.error = null;
    }

    public void setError(@Nullable Component error) {
        this.value = null;
        this.error = error;
    }

    public void clear() {
        this.value = null;
        this.error = null;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.CLIPBOARD;
    }

    @Override
    public String getMatchString() {
        return getName().getString().toLowerCase();
    }

    @Override
    public boolean matchesInput(IValueType<?> valueType) {
        return false;
    }

    @Override
    public boolean matchesOutput(IValueType<?> valueType) {
        // Without a pasted value, any output type is still possible
        return value == null || ValueHelpers.correspondsTo(value.getType(), valueType);
    }

    @Override
    public void onInputSlotUpdated(Player player, int slotId, ItemStack itemStack) {

    }

    @Override
    public boolean canWriteElementPre() {
        return value != null;
    }

    @Override
    public ItemStack writeElement(Player player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!player.level().isClientSide(), itemStack, ValueTypes.REGISTRY,
                new ValueTypeLPElementBase.ValueTypeVariableFacadeFactory(value.getType(), value), player.level(),
                player, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
    }

    @Override
    public void loadElement(IVariableFacade variableFacade) {

    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return value == null;
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        // Existing variables are loaded into the element of their own value type
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, Player player) {
        return false;
    }

    @Override
    public int getItemStackSizeLimit() {
        return 1;
    }

    @Override
    public Slot createSlot(Container temporaryInputSlots, int slotId, int x, int y) {
        return ILogicProgrammerElement.createSlotDefault(this, temporaryInputSlots, slotId, x, y);
    }

    @Override
    public Component getName() {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_CLIPBOARD);
    }

    @Override
    public void loadTooltip(List<Component> lines) {
        lines.add(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_CLIPBOARD_TOOLTIP));
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public void activate() {
        clear();
    }

    @Override
    public void deactivate() {
        clear();
    }

    @Override
    public Component validate() {
        if (error != null) {
            return error;
        }
        if (value == null) {
            return Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_NOVALUE);
        }
        return null;
    }

    @Override
    public int getColor() {
        return Helpers.RGBToInt(170, 170, 170);
    }

    @Override
    public String getSymbol() {
        // This is what the element list shows
        return L10NHelpers.localize(L10NValues.GUI_LOGICPROGRAMMER_CLIPBOARD);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFocused(ISubGuiBox subGui) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setFocused(ISubGuiBox subGui, boolean focused) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {

    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ClipboardLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
