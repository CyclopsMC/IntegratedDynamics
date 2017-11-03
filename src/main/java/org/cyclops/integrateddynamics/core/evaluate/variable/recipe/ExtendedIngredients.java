package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A lazy extension for ingredients.
 * @author rubensworks
 */
public class ExtendedIngredients extends WrappedIngredients {

    private final int targetIndex;

    private final RecipeComponent component;
    private final ValueTypeList.ValueList list;
    private final ValueTypeOperator.ValueOperator predicate;

    public ExtendedIngredients(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                               RecipeComponent component, ValueTypeList.ValueList list,
                               ValueTypeOperator.ValueOperator predicate) {
        super(IIngredients.orEmpty(base.getRawValue()));
        this.targetIndex = targetIndex;
        this.component = component;
        this.list = list;
        this.predicate = predicate;
    }

    protected boolean forComponent(RecipeComponent component) {
        return component == this.component;
    }

    protected int getTargetSize(int parentSize) {
        if (targetIndex < parentSize) {
            return parentSize;
        } else {
            return targetIndex + 1;
        }
    }

    protected <T extends IValueType<V>, V extends IValue> Optional<Predicate<V>>
    getTargetPredicate(int index, int size, @Nullable ValueTypeList.ValueList<T, V> list,
                       @Nullable ValueTypeOperator.ValueOperator predicate) {
        if (list == null && predicate == null) {
            return Optional.empty();
        }
        if (index == targetIndex) {
            if (list != null) {
                return Optional.of((V input) -> {
                    for (V value : list.getRawValue()) {
                        if (input.equals(value)) {
                            return true;
                        }
                    }
                    return false;
                });
            }
            if (predicate != null) {
                return Optional.of((V value) -> {
                    try {
                        IValue result = predicate.getRawValue().evaluate(new Variable(value));
                        return result instanceof ValueTypeBoolean.ValueBoolean
                                && ((ValueTypeBoolean.ValueBoolean) result).getRawValue();
                    } catch (EvaluationException e) {
                        return false;
                    }
                });
            }
        }
        if (index >= size && index < targetIndex) {
            return Optional.of(Predicates.alwaysFalse());
        }
        return Optional.empty();
    }

    protected <T extends IValueType<V>, V extends IValue> Optional<List<V>>
    getTargetList(int index, int size, @Nullable ValueTypeList.ValueList<T, V> list,
                  @Nullable ValueTypeOperator.ValueOperator predicate) {
        if (list == null && predicate == null) {
            return Optional.empty();
        }
        if (index == targetIndex) {
            return Optional.of(list != null ? Lists.newArrayList(list.getRawValue()) : Collections.emptyList());
        }
        if (index >= size && index < targetIndex) {
            return Optional.of(Collections.emptyList());
        }
        return Optional.empty();
    }

    protected <T extends IValueType<V>, V extends IValue> List<List<V>>
    getTargetListRaw(int sizeThis, Function<Integer, List<V>> listGetter) {
        return Lists.transform(ContiguousSet.create(Range.closedOpen(0, sizeThis),
                DiscreteDomain.integers()).asList(), listGetter);
    }

    @Override
    public int getIngredients(RecipeComponent<?, ?> component) {
        return forComponent(component) ? getTargetSize(super.getIngredients(component)) : super.getIngredients(component);
    }

    @Override
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index) {
        if (forComponent(component)) {
            return getTargetPredicate(index, super.getIngredients(component), (ValueTypeList.ValueList<?, V>) list,
                    predicate).orElseGet(() -> super.getPredicate(component, index));
        }
        return super.getPredicate(component, index);
    }

    @Override
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index) {
        if (forComponent(component)) {
            return getTargetList(index, super.getIngredients(component), (ValueTypeList.ValueList<?, V>) list,
                    predicate).orElseGet(() -> super.getList(component, index));
        }
        return super.getList(component, index);
    }

    @Override
    public <V extends IValue, T, R> List<List<V>> getRaw(RecipeComponent<T, R> component) {
        return forComponent(component) ? getTargetListRaw(this.getIngredients(component),
                (index) -> this.getList(component, index)) : super.getRaw(component);
    }

    public static ExtendedIngredients forList(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                               RecipeComponent<?, ?> component, ValueTypeList.ValueList<?, ?> list) {
        return new ExtendedIngredients(base, targetIndex, component,
                list, null);
    }

    public static ExtendedIngredients forPredicate(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                                   RecipeComponent<?, ?> component, ValueTypeOperator.ValueOperator itemPredicate) {
        return new ExtendedIngredients(base, targetIndex,
                component, null, itemPredicate);
    }
}
