package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

/**
 * A value type of an add-on, in its own namespace, like the chemicals of Integrated Mekanism.
 * As those, it reads an unknown id as an empty value.
 * @author rubensworks
 */
public class ValueTypeAddonMocked extends ValueObjectTypeBase<ValueTypeAddonMocked.ValueAddon> {

    public static final ValueTypeAddonMocked CHEMICAL = ValueTypes.REGISTRY.register(new ValueTypeAddonMocked("testaddon", "chemical"));
    // Two add-ons with a type of the same name
    public static final ValueTypeAddonMocked WIDGET = ValueTypes.REGISTRY.register(new ValueTypeAddonMocked("testaddon", "widget"));
    public static final ValueTypeAddonMocked OTHER_WIDGET = ValueTypes.REGISTRY.register(new ValueTypeAddonMocked("testother", "widget"));

    private final String modId;

    public ValueTypeAddonMocked(String modId, String typeName) {
        super(typeName, ValueAddon.class);
        this.modId = modId;
    }

    public static void load() {
        // Registers the types above
    }

    @Override
    protected String getModId() {
        return modId;
    }

    @Override
    public ValueAddon getDefault() {
        return new ValueAddon(this, "", 0);
    }

    @Override
    public MutableComponent toCompactString(ValueAddon value) {
        return Component.literal(value.getId() + " " + value.getAmount());
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueAddon value) {
        if (!value.getId().isEmpty()) {
            ValueOutput output = valueOutput.child("v");
            output.putString("id", value.getId());
            output.putLong("amount", value.getAmount());
        }
    }

    @Override
    public ValueAddon deserialize(ValueInput valueInput) {
        ValueInput input = valueInput.childOrEmpty("v");
        String id = input.getStringOr("id", "");
        return id.startsWith(modId + ":") ? new ValueAddon(this, id, input.getLongOr("amount", 0)) : getDefault();
    }

    public ValueAddon of(String id, long amount) {
        return new ValueAddon(this, id, amount);
    }

    public static class ValueAddon extends ValueBase {

        private final String id;
        private final long amount;

        private ValueAddon(ValueTypeAddonMocked type, String id, long amount) {
            super(type);
            this.id = id;
            this.amount = amount;
        }

        public String getId() {
            return id;
        }

        public long getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueAddon other && other.getType() == getType() && other.id.equals(id) && other.amount == amount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), id, amount);
        }

        @Override
        public String toString() {
            return "ValueAddon{" + getType().getUniqueName() + ", " + id + ", " + amount + "}";
        }
    }

}
