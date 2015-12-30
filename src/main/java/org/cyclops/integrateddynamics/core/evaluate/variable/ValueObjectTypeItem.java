package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import lombok.ToString;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeItem extends ValueObjectTypeBase<ValueObjectTypeItem.ValueItem> implements IValueTypeNamed<ValueObjectTypeItem.ValueItem> {

    public ValueObjectTypeItem() {
        super("item");
    }

    @Override
    public ValueItem getDefault() {
        return ValueItem.of(null);
    }

    @Override
    public String toCompactString(ValueItem value) {
        Optional<Item> item = value.getRawValue();
        return item.isPresent() ? L10NHelpers.localize(item.get().getUnlocalizedName() + ".name") : "";
    }

    @Override
    public String serialize(ValueItem value) {
        Optional<Item> item = value.getRawValue();
        ResourceLocation resourcelocation = item.isPresent() ? Item.itemRegistry.getNameForObject(item.get()) : null;
        return resourcelocation == null ? "minecraft:air" : resourcelocation.toString();
    }

    @Override
    public ValueItem deserialize(String value) {
        return ValueItem.of(Item.getByNameOrId(value));
    }

    @Override
    public String getName(ValueItem a) {
        return toCompactString(a);
    }

    @ToString
    public static class ValueItem extends ValueOptionalBase<Item> {

        private ValueItem(Item item) {
            super(ValueTypes.OBJECT_ITEM, item);
        }

        public static ValueItem of(Item item) {
            return new ValueItem(item);
        }

        @Override
        protected boolean isEqual(Item a, Item b) {
            return a == b;
        }
    }

}
