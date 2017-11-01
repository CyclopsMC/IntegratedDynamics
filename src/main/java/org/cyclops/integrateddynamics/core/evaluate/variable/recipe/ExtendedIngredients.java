package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
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

    private final ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> items;
    private final ValueTypeOperator.ValueOperator itemPredicate;

    private final ValueTypeList.ValueList<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> fluids;
    private final ValueTypeOperator.ValueOperator fluidPredicate;

    private final ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger> energies;
    private final ValueTypeOperator.ValueOperator energypredicate;

    public ExtendedIngredients(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                               ValueTypeList.ValueList<ValueObjectTypeItemStack,
                                       ValueObjectTypeItemStack.ValueItemStack> items,
                               ValueTypeOperator.ValueOperator itemPredicate,
                               ValueTypeList.ValueList<ValueObjectTypeFluidStack,
                                       ValueObjectTypeFluidStack.ValueFluidStack> fluids,
                               ValueTypeOperator.ValueOperator fluidPredicate,
                               ValueTypeList.ValueList<ValueTypeInteger,
                                       ValueTypeInteger.ValueInteger> energies,
                               ValueTypeOperator.ValueOperator energypredicate) {
        super(IIngredients.orEmpty(base.getRawValue()));
        this.targetIndex = targetIndex;
        this.items = items;
        this.itemPredicate = itemPredicate;
        this.fluids = fluids;
        this.fluidPredicate = fluidPredicate;
        this.energies = energies;
        this.energypredicate = energypredicate;
    }

    protected boolean forItems() {
        return this.items != null || this.itemPredicate != null;
    }

    protected boolean forFluids() {
        return this.fluids != null || this.fluidPredicate != null;
    }

    protected boolean forEnergy() {
        return this.energies != null || this.energypredicate != null;
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
    public int getItemStackIngredients() {
        return forItems() ? getTargetSize(super.getItemStackIngredients()) : super.getItemStackIngredients();
    }

    @Override
    public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
        return getTargetPredicate(index, getItemStackIngredients(), items, itemPredicate)
                .orElseGet(() -> super.getItemStackPredicate(index));
    }

    @Override
    public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
        return getTargetList(index, super.getItemStackIngredients(), items, itemPredicate)
                .orElseGet(() -> super.getItemStacks(index));
    }

    @Override
    public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
        return forItems() ? getTargetListRaw(this.getItemStackIngredients(), this::getItemStacks) : super.getItemStacksRaw();
    }

    @Override
    public int getFluidStackIngredients() {
        return forFluids() ? getTargetSize(super.getFluidStackIngredients()) : super.getFluidStackIngredients();
    }

    @Override
    public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
        return getTargetPredicate(index, super.getFluidStackIngredients(), fluids, fluidPredicate)
                .orElseGet(() -> super.getFluidStackPredicate(index));
    }

    @Override
    public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
        return getTargetList(index, super.getFluidStackIngredients(), fluids, fluidPredicate)
                .orElseGet(() -> super.getFluidStacks(index));
    }
    @Override
    public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
        return forFluids() ? getTargetListRaw(this.getFluidStackIngredients(), this::getFluidStacks) : super.getFluidStacksRaw();
    }

    @Override
    public int getEnergyIngredients() {
        return forEnergy() ? getTargetSize(super.getEnergyIngredients()) : super.getEnergyIngredients();
    }

    @Override
    public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
        return getTargetPredicate(index, super.getEnergyIngredients(), energies, energypredicate)
                .orElseGet(() -> super.getEnergiesPredicate(index));
    }

    @Override
    public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
        return getTargetList(index, super.getEnergyIngredients(), energies, energypredicate)
                .orElseGet(() -> super.getEnergies(index));
    }

    @Override
    public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
        return forEnergy() ? getTargetListRaw(this.getEnergyIngredients(), this::getEnergies) : super.getEnergiesRaw();
    }

    public static ExtendedIngredients forItems(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                               ValueTypeList.ValueList<ValueObjectTypeItemStack,
                                                       ValueObjectTypeItemStack.ValueItemStack> items) {
        return new ExtendedIngredients(base, targetIndex,
                items, null, 
                null, null, 
                null, null);
    }

    public static ExtendedIngredients forItemPredicate(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                                       ValueTypeOperator.ValueOperator itemPredicate) {
        return new ExtendedIngredients(base, targetIndex,
                null, itemPredicate,
                null, null,
                null, null);
    }

    public static ExtendedIngredients forFluids(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                               ValueTypeList.ValueList<ValueObjectTypeFluidStack,
                                                       ValueObjectTypeFluidStack.ValueFluidStack> fluids) {
        return new ExtendedIngredients(base, targetIndex,
                null, null,
                fluids, null,
                null, null);
    }

    public static ExtendedIngredients forFluidPredicate(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                                       ValueTypeOperator.ValueOperator fluidPredicate) {
        return new ExtendedIngredients(base, targetIndex,
                null, null,
                null, fluidPredicate,
                null, null);
    }

    public static ExtendedIngredients forEnergies(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                                ValueTypeList.ValueList<ValueTypeInteger,
                                                        ValueTypeInteger.ValueInteger> energies) {
        return new ExtendedIngredients(base, targetIndex,
                null, null,
                null, null,
                energies, null);
    }

    public static ExtendedIngredients forEnergyPredicate(ValueObjectTypeIngredients.ValueIngredients base, int targetIndex,
                                                        ValueTypeOperator.ValueOperator energyPredicate) {
        return new ExtendedIngredients(base, targetIndex,
                null, null,
                null, null,
                null, energyPredicate);
    }
}
