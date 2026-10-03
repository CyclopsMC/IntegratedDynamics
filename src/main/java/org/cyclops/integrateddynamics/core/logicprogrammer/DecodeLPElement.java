package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
import org.cyclops.integrateddynamics.core.logicprogrammer.client.DecodeLPElementClient;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Element for creating a variable from an encoded value,
 * which may have been encoded in another world.
 *
 * @author rubensworks
 */
public class DecodeLPElement implements ILogicProgrammerElement<ISubGuiBox, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase, DecodeLPElementClient> {

    // A text field, and two lines below it to show the pasted value or its error
    private static final IConfigRenderPattern RENDER_PATTERN = new IConfigRenderPattern.Base(150, 44, new Pair[0], null);

    private String inputString = "";
    @Nullable
    private IValue value = null;
    @Nullable
    private Component error = null;

    private DecodeLPElementClient client;

    public DecodeLPElement() {
        if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            this.client = constructClient();
        }
    }

    private DecodeLPElementClient constructClient() {
        return new DecodeLPElementClient(this);
    }

    @Override
    public DecodeLPElementClient getClient() {
        return client;
    }

    /**
     * @return What was pasted into the text field, which is only known client-side.
     */
    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    @Nullable
    public Component getError() {
        return error;
    }

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
        return LogicProgrammerElementTypes.DECODE;
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
        return inputString.isEmpty();
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
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ContainerInput clickType, Player player) {
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
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_DECODE);
    }

    @Override
    public void loadTooltip(Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_DECODE_TOOLTIP));
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return RENDER_PATTERN;
    }

    @Override
    public void activate() {
        clear();
        inputString = "";
    }

    @Override
    public void deactivate() {
        clear();
        inputString = "";
    }

    @Override
    public Component validate() {
        if (error != null) {
            return error;
        }
        if (value == null) {
            return Component.translatable(L10NValues.VALUE_ENCODING_ERROR_NOVALUE);
        }
        return null;
    }

    @Override
    public int getColor() {
        return IModHelpers.get().getBaseHelpers().RGBToInt(170, 170, 170);
    }

    @Override
    public String getSymbol() {
        // This is what the element list shows
        return IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_LOGICPROGRAMMER_DECODE);
    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {

    }

}
