package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ValueTypeIngredientsLPElementClient;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeIngredientsValueChangedPacket;

import java.util.List;
import java.util.Map;

/**
 * Element for the ingredients value type.
 *
 * @author rubensworks
 */
public class ValueTypeIngredientsLPElement extends ValueTypeLPElementBase<ValueTypeIngredientsLPElementClient> {

    public static final int OFFSET_X = 20;
    public static final int OFFSET_Y = 21;

    private IngredientComponent currentType = IngredientComponent.ITEMSTACK;
    private Map<IngredientComponent, Integer> lengths = Maps.newHashMap();
    private Map<IngredientComponent, Map<Integer, IValueTypeLogicProgrammerElement>> subElements = Maps.newHashMap();
    private int activeElement = -1;

    private ValueObjectTypeIngredients.ValueIngredients serverValue = null;

    public ValueTypeIngredientsLPElement() {
        super(ValueTypes.OBJECT_INGREDIENTS);
    }

    public void setServerValue(ValueObjectTypeIngredients.ValueIngredients serverValue) {
        this.serverValue = serverValue;
    }

    @Override
    public ValueTypeIngredientsLPElementClient constructClient() {
        return new ValueTypeIngredientsLPElementClient(this);
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE_CANVAS_WIDE;
    }

    @Override
    public boolean canWriteElementPre() {
        return true;
    }

    protected IMixedIngredients constructValues() {
        Map<IngredientComponent<?, ?>, List<?>> lists = Maps.newIdentityHashMap();
        for (IngredientComponent<?, ?> component : IngredientComponentHandlers.REGISTRY.getComponents()) {
            List values = Lists.newArrayListWithExpectedSize(lengths.get(component));
            subElements.get(component).entrySet().forEach(entry -> {
                IIngredientComponentHandler componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                try {
                    values.add(componentHandler.toInstance(entry.getValue().getValue()));
                } catch (Exception e) {
                    values.add(component.getMatcher().getEmptyInstance());
                }
            });
            if (!values.isEmpty()) {
                lists.put(component, values);
            }
        }
        return new MixedIngredients(lists);
    }

    @Override
    public IValue getValue() {
        return IModHelpers.get().getMinecraftHelpers().isClientSideThread()
                ? ValueObjectTypeIngredients.ValueIngredients.of(constructValues()) : serverValue;
    }

    @Override
    public void setValue(IValue value) {
        ValueObjectTypeIngredients.ValueIngredients valueIngredients = (ValueObjectTypeIngredients.ValueIngredients) value;
        if (!IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            setServerValue(valueIngredients);
        }

        valueIngredients.getRawValue().ifPresent(ingredients -> {
            // Select itemstack by default if it has instances
            if (ingredients.getComponents().contains(IngredientComponent.ITEMSTACK)) {
                currentType = IngredientComponent.ITEMSTACK;
            } else {
                currentType = null;
            }

            for (IngredientComponent<?, ?> ingredientComponent : ingredients.getComponents()) {
                IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(ingredientComponent);

                // If no itemstacks in ingredient, select any other
                if (currentType == null) {
                    currentType = ingredientComponent;
                }

                // Save length per ingredient component
                lengths.put(ingredientComponent, ingredients.getInstances(ingredientComponent).size());

                // Initialize LP elements for all instances of this ingredient component
                Map<Integer, IValueTypeLogicProgrammerElement> entries = Maps.newHashMap();
                List<?> instances = ingredients.getInstances(ingredientComponent);
                for (int i = 0; i < instances.size(); i++) {
                    initializeElementFromInstanceValue(entries, handler, instances.get(i), i);
                }
                subElements.put(ingredientComponent, entries);
            }
        });
    }

    protected <VT extends IValueType<V>, V extends IValue, T, M> void initializeElementFromInstanceValue(Map<Integer, IValueTypeLogicProgrammerElement> entries, IIngredientComponentHandler<VT, V, T, M> handler, T instance, int instanceIndex) {
        IValue instanceValue = handler.toValue(instance);
        IValueTypeLogicProgrammerElement lpElement = instanceValue.getType().createLogicProgrammerElement();
        lpElement.setValue(instanceValue);
        entries.put(instanceIndex, lpElement);
    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {
        if (!subElements.get(currentType).isEmpty()) {
            IValueTypeLogicProgrammerElement subElement = setActiveElement(0);
            int x = RenderPatternCommon.calculateX(ContainerLogicProgrammerBase.BASE_X, ContainerLogicProgrammerBase.MAX_WIDTH, subElement.getRenderPattern()) + ContainerLogicProgrammerBase.BASE_X - OFFSET_X;
            int y = RenderPatternCommon.calculateY(ContainerLogicProgrammerBase.BASE_Y, ContainerLogicProgrammerBase.MAX_HEIGHT, subElement.getRenderPattern()) + ContainerLogicProgrammerBase.BASE_Y - OFFSET_Y;
            container.setElementInventory(subElement, x, y);
            subElement.setValueInContainer(container);
        }
    }

    public int getLength() {
        return lengths.get(currentType);
    }

    public void setLength(int length) {
        lengths.put(currentType, length);
        setActiveElement(getLength() - 1);
    }

    public IngredientComponent getCurrentType() {
        return currentType;
    }

    public void setCurrentType(IngredientComponent currentType) {
        this.currentType = currentType;
        setActiveElement(subElements.get(currentType).size() - 1);
    }

    public int getActiveElement() {
        return activeElement;
    }

    public IValueTypeLogicProgrammerElement setActiveElement(int index) {
        activeElement = index;
        IValueTypeLogicProgrammerElement subElement = null;
        if (index >= 0) {
            if (!subElements.get(currentType).containsKey(index)) {
                subElements.get(currentType).put(index, subElement = IngredientComponentHandlers.REGISTRY.getComponentHandler(currentType)
                        .getValueType().createLogicProgrammerElement());
            } else {
                subElement = subElements.get(currentType).get(index);
            }
        }
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            getClient().setActiveElement(activeElement);
        }
        return subElement;
    }

    public void removeElement(int index) {
        Map<Integer, IValueTypeLogicProgrammerElement> oldSubElements = subElements.get(currentType);
        subElements.put(currentType, Maps.newHashMap());
        for (Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : oldSubElements.entrySet()) {
            int i = entry.getKey();
            if (i < index) {
                subElements.get(currentType).put(i, entry.getValue());
            } else if (i > index) {
                subElements.get(currentType).put(i - 1, entry.getValue());
            }
        }
        getClient().removeElement(index);
        setLength(getLength() - 1);
    }

    public Map<IngredientComponent, Map<Integer, IValueTypeLogicProgrammerElement>> getSubElements() {
        return subElements;
    }

    @Override
    public void activate() {
        for (IngredientComponent recipeComponent : IngredientComponentHandlers.REGISTRY.getComponents()) {
            subElements.put(recipeComponent, Maps.newHashMap());
            lengths.put(recipeComponent, 0);
        }
        getClient().activate();
    }

    @Override
    public void deactivate() {

    }

    @Override
    public Component validate() {
        if (!IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            return serverValue == null ? Component.literal("") : null;
        }
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeIngredientsValueChangedPacket(ValueDeseralizationContext.ofClient(),
                            ValueObjectTypeIngredients.ValueIngredients.of(constructValues())));
        }
        for (Map<Integer, IValueTypeLogicProgrammerElement> componentValues : subElements.values()) {
            for (Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : componentValues.entrySet()) {
                Component error = entry.getValue().validate();
                if (error != null) {
                    return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDLISTELEMENT, entry.getKey(), error);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return (slotId == 0 && super.isItemValidForSlot(slotId, itemStack)) ||
                (activeElement >= 0 && subElements.get(currentType).containsKey(activeElement)
                        && subElements.get(currentType).get(activeElement).isItemValidForSlot(slotId, itemStack));
    }

}
