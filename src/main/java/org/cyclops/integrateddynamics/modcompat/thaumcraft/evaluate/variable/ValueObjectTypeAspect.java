package org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable;

import com.google.common.base.Optional;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueOptionalBase;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.ThaumcraftModCompat;
import thaumcraft.api.aspects.Aspect;

import java.util.Objects;

/**
 * Value type with values that are thaumcraft aspects.
 * @author rubensworks
 */
public class ValueObjectTypeAspect extends ValueObjectTypeBase<ValueObjectTypeAspect.ValueAspect> implements IValueTypeNamed<ValueObjectTypeAspect.ValueAspect> {

    private static final String DELIMITER = ";";

    public ValueObjectTypeAspect() {
        super("thaumcraftaspect");
    }

    @Override
    public ValueAspect getDefault() {
        return ValueAspect.ofNull();
    }

    @Override
    public String toCompactString(ValueAspect value) {
        Optional<Pair<Aspect, Integer>> aspect = value.getRawValue();
        if(aspect.isPresent()) {
            return aspect.get().getKey().getName() + ":" + aspect.get().getValue();
        }
        return "";
    }

    @Override
    public String serialize(ValueAspect value) {
        Optional<Pair<Aspect, Integer>> aspect = value.getRawValue();
        if(aspect.isPresent()) {
            return aspect.get().getKey().getTag() + DELIMITER + aspect.get().getValue();
        }
        return "";
    }

    @Override
    public ValueAspect deserialize(String value) {
        String[] split = value.split(DELIMITER);
        if(split.length == 2) {
            try {
                return ValueAspect.of(Aspect.getAspect(split[0]), Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {}
        }
        return ValueAspect.ofNull();
    }

    @Override
    public String getName(ValueAspect a) {
        return toCompactString(a);
    }

    @ToString
    public static class ValueAspect extends ValueOptionalBase<Pair<Aspect, Integer>> {

        private ValueAspect(Aspect aspect, int amount) {
            super(ThaumcraftModCompat.OBJECT_ASPECT, Pair.of(aspect, amount));
        }

        public ValueAspect() {
            super(ThaumcraftModCompat.OBJECT_ASPECT, null);
        }

        public static ValueAspect of(Aspect aspect, int amount) {
            return new ValueAspect(aspect, amount);
        }

        public static ValueAspect ofNull() {
            return new ValueAspect();
        }

        @Override
        protected boolean isEqual(Pair<Aspect, Integer> a, Pair<Aspect, Integer> b) {
            return Objects.equals(a.getKey().getTag(), b.getKey().getTag()) && Objects.equals(a.getValue(), b.getValue());
        }
    }

}
