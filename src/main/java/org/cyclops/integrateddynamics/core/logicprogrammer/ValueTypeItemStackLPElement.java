package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ValueTypeItemStackLPElementClient;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Element for a value type that can be derived from an {@link ItemStack}.
 * @author rubensworks
 */
public class ValueTypeItemStackLPElement<V extends IValue> extends ValueTypeLPElementBase<ValueTypeItemStackLPElementClient<V>> {

    private final IItemStackToValue<V> itemStackToValue;
    private ItemStack itemStack = ItemStack.EMPTY;

    public ValueTypeItemStackLPElement(IValueType valueType, IItemStackToValue<V> itemStackToValue) {
        super(valueType);
        this.itemStackToValue = itemStackToValue;
    }

    @Override
    public ValueTypeItemStackLPElementClient<V> constructClient() {
        return new ValueTypeItemStackLPElementClient<>(this);
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.SINGLE_SLOT;
    }

    @Override
    public int getItemStackSizeLimit() {
        return ItemStack.EMPTY.getMaxStackSize();
    }

    @Override
    public void onInputSlotUpdated(Player player, int slotId, ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean canWriteElementPre() {
        return this.itemStackToValue.isNullable() || !this.itemStack.isEmpty();
    }

    @Override
    public void activate() {
        this.itemStack = ItemStack.EMPTY;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public Component validate() {
        if(!this.itemStackToValue.isNullable() && this.itemStack.isEmpty()) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUTITEM);
        }
        return itemStackToValue.validate(itemStack);
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return true;
    }

    @Override
    public IValue getValue() {
        return this.itemStackToValue.getValue(this.itemStack);
    }

    @Override
    public void setValue(IValue value) {
        this.itemStack = this.itemStackToValue.getValueAsItemStack((V) value);
    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {
        container.getTemporaryInputSlots().setItem(0, this.itemStack);
    }

    public static interface IItemStackToValue<V extends IValue> {

        public boolean isNullable();
        public Component validate(ItemStack itemStack);
        public V getValue(ItemStack itemStack);
        public ItemStack getValueAsItemStack(V value);

    }

}
