package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.function.Consumer;

/**
 * Variable facade for variables determined by a raw value.
 *
 * @author rubensworks
 */
public class ValueTypeVariableFacade<V extends IValue> extends VariableFacadeBase implements IValueTypeVariableFacade<V> {

    private final IValueType<V> valueType;
    private final V value;
    private IVariable<V> variable = null;

    public ValueTypeVariableFacade(boolean generateId, IValueType<V> valueType, V value) {
        super(generateId);
        this.valueType = valueType;
        this.value = value;
    }

    public ValueTypeVariableFacade(int id, IValueType<V> valueType, V value) {
        super(id);
        this.valueType = valueType;
        this.value = value;
    }

    @Override
    public IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if (isValid()) {
            if (variable == null) {
                variable = new Variable<V>(getValueType(), getValue());
            }
            return variable;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return getValueType() != null && getValue() != null;
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else {
            // Check expected aspect type and operator output type
            if (!ValueHelpers.correspondsTo(getValueType(), containingValueType)) {
                validator.addError(Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        Component.translatable(containingValueType.getTranslationKey()),
                        Component.translatable(getValueType().getTranslationKey())));
            }
        }
    }

    @Override
    public IValueType getOutputType() {
        return getValueType();
    }

    @Override
    protected IVariableFacadeClient constructClient() {
        return new ValueTypeVariableFacadeClient<>(this);
    }

    @Override
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        if (isValid()) {
            V value = getValue();
            getValueType().loadTooltip(tooltipAdder, false, value);
            tooltipAdder.accept(Component.translatable(L10NValues.VALUETYPE_TOOLTIP_VALUE, getValueType().toCompactString(value)));
        }
        super.appendHoverText(tooltipAdder, context);
    }

    public IValueType<V> getValueType() {
        return this.valueType;
    }

    public V getValue() {
        return this.value;
    }

    public IVariable<V> getVariable() {
        return this.variable;
    }

    public void setVariable(IVariable<V> variable) {
        this.variable = variable;
    }

    public String toString() {
        return "ValueTypeVariableFacade(valueType=" + this.getValueType() + ", value=" + this.getValue() + ", variable=" + this.getVariable() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ValueTypeVariableFacade)) return false;
        final ValueTypeVariableFacade<?> other = (ValueTypeVariableFacade<?>) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$valueType = this.getValueType();
        final Object other$valueType = other.getValueType();
        if (this$valueType == null ? other$valueType != null : !this$valueType.equals(other$valueType)) return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        final Object this$variable = this.getVariable();
        final Object other$variable = other.getVariable();
        if (this$variable == null ? other$variable != null : !this$variable.equals(other$variable)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ValueTypeVariableFacade;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $valueType = this.getValueType();
        result = result * PRIME + ($valueType == null ? 43 : $valueType.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        final Object $variable = this.getVariable();
        result = result * PRIME + ($variable == null ? 43 : $variable.hashCode());
        return result;
    }
}
