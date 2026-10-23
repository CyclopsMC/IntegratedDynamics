package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionHistory;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionTextField;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Base container for the logic programmer.
 * @author rubensworks
 */
public abstract class ContainerLogicProgrammerBase extends ScrollingInventoryContainer<ILogicProgrammerElement> implements IDirtyMarkListener {

    public static final int OUTPUT_X = 232;
    public static final int OUTPUT_Y = 110;

    public static final int BASE_X = 88;
    public static final int BASE_Y = 18;
    public static final int MAX_WIDTH = 160;
    public static final int MAX_HEIGHT = 87;

    /**
     * The expression side panel, to the right of the main panel.
     */
    public static final int EXPRESSION_PANEL_X = 252;
    public static final int EXPRESSION_PANEL_Y = 4;
    public static final int EXPRESSION_PANEL_WIDTH = 150;
    public static final int EXPRESSION_PANEL_HEIGHT = 172;

    protected static final IItemPredicate<ILogicProgrammerElement> FILTERER =
            (item, pattern) -> pattern.matcher(item.getMatchString()).matches()
                    || pattern.matcher(item.getSymbol()).matches();

    private final SimpleInventory writeSlot;
    private final SimpleInventory filterSlots;
    private ILogicProgrammerElement activeElement = null;
    private ILogicProgrammerElement temporarySlotsElement = null;
    private SimpleInventory temporaryInputSlots = null;
    private Component lastError;
    private LoadConfigListener loadConfigListener;

    private IValueType filterIn1 = null;
    private IValueType filterIn2 = null;
    private IValueType filterOut = null;
    private IValueType expressionSlotFilter = null;
    // Client-side state of the expression panel, which outlives the config pattern
    @OnlyIn(Dist.CLIENT)
    @Nullable
    private ExpressionTextField expressionTextField;
    private final ExpressionHistory expressionHistory = new ExpressionHistory();
    private boolean settingExpression = false;

    @OnlyIn(Dist.CLIENT)
    private ContainerScreenLogicProgrammerBase gui;

    private String lastLabel = "";

    public ContainerLogicProgrammerBase(@Nullable MenuType<?> type, int id, Inventory playerInventory) {
        super(type, id, playerInventory, new SimpleContainer(0), getElements(), FILTERER);
        this.writeSlot = new SimpleInventory(1, 1);
        this.filterSlots = new SimpleInventory(3, 1);
        this.filterSlots.addDirtyMarkListener(new FilterSlotListener(ValueDeseralizationContext.of(playerInventory.player.level())));
        this.writeSlot.addDirtyMarkListener(this);
        this.writeSlot.addDirtyMarkListener(loadConfigListener = new LoadConfigListener());
        this.temporaryInputSlots = new SimpleInventory(0, 1);
        initializeSlotsPre();
        initializeSlotsPost();
    }

    protected static List<ILogicProgrammerElement> getElements() {
        List<ILogicProgrammerElement> elements = Lists.newLinkedList();
        for(ILogicProgrammerElementType type: LogicProgrammerElementTypes.REGISTRY.getTypes()) {
            elements.addAll(type.createElements());
        }
        return elements;
    }

    @OnlyIn(Dist.CLIENT)
    public void setGui(ContainerScreenLogicProgrammerBase gui) {
        this.gui = gui;
    }

    @OnlyIn(Dist.CLIENT)
    public ContainerScreenLogicProgrammerBase getGui() {
        return this.gui;
    }

    protected void initializeSlotsPre() {
        addSlot(new SlotVariable(writeSlot, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public void setChanged() {
                // We don't call super here to avoid dirty mark listeners to be called twice, which can cause issues with loading and immediate overwriting.
                // This is because SimpleInventory will already call dirty mark listeners when calling setItem.
                // Strictly this behaviour in SimpleInventory is not required, but due to backwards-compat, we keep it.
                // TODO: refactor this in SimpleInventory in next major?
            }
        });
        SlotSingleItem filterSlotIn1 = new SlotVariable(filterSlots, 0, 6, 218);
        SlotSingleItem filterSlotIn2 = new SlotVariable(filterSlots, 1, 24, 218);
        SlotSingleItem filterSlotOut = new SlotVariable(filterSlots, 2, 58, 218);
        filterSlotIn1.setPhantom(true);
        filterSlotIn2.setPhantom(true);
        filterSlotOut.setPhantom(true);
        addSlot(filterSlotIn1);
        addSlot(filterSlotIn2);
        addSlot(filterSlotOut);
    }

    protected void initializeSlotsPost() {
        addPlayerInventory((Inventory) getPlayerIInventory(), 88, 131);
    }

    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    protected boolean isAssertInventorySize() {
        return false;
    }

    @Override
    protected int getSizeInventory() {
        return 1;
    }

    public void setActiveElementById(ResourceLocation typeId, ResourceLocation elementId) {
        ILogicProgrammerElementType type = LogicProgrammerElementTypes.REGISTRY.getType(typeId);
        if (type != null) {
            ILogicProgrammerElement element = type.getByName(elementId);
            if(!LogicProgrammerElementTypes.areEqual(getActiveElement(), element)) {
                setActiveElement(element, 0, 0);
                onDirty();
            }
        } else {
            setActiveElement(null, 0, 0);
        }
    }

    /**
     * Set the new active element.
     * @param activeElement The new element.
     * @param baseX The slots X coordinate
     * @param baseY The slots Y coordinate
     */
    public void setActiveElement(final ILogicProgrammerElement activeElement, int baseX, int baseY) {
        if(this.activeElement != null) {
            this.activeElement.deactivate();
        }
        this.activeElement = activeElement;

        this.lastError = null;
        this.activeElement = activeElement;
        // Another expression, unless a set action moves the expression to the element of its root
        if (!settingExpression) {
            expressionHistory.clear();
            if (MinecraftHelpers.isClientSideThread()) {
                resetExpressionTextField();
            }
        }

        this.setElementInventory(this.activeElement, baseX, baseY);

        if(activeElement != null) {
            activeElement.activate();
        }
    }

    /**
     * Set the new active element.
     * @param element The new element.
     * @param baseX The slots X coordinate
     * @param baseY The slots Y coordinate
     */
    public void setElementInventory(final ILogicProgrammerElement element, int baseX, int baseY) {
        this.lastError = null;

        // This assumes that there is only one other slot, the remaining slots will be erased!
        // (We can do this because they are all ghost slots)
        lastSlots.clear();
        slots.clear();
        remoteSlots.clear();
        initializeSlotsPre();
        this.temporaryInputSlots.removeDirtyMarkListener(this);
        this.temporaryInputSlots = new SimpleInventory(element == null ? 0 : element.getRenderPattern().getSlotPositions().length, element == null ? 0 : element.getItemStackSizeLimit());
        temporaryInputSlots.addDirtyMarkListener(this);
        this.temporarySlotsElement = element;
        if(element != null) {
            Pair<Integer, Integer>[] slotPositions = element.getRenderPattern().getSlotPositions();
            for (int i = 0; i < temporaryInputSlots.getContainerSize(); i++) {
                addSlot(element.createSlot(temporaryInputSlots, i, 1 + baseX + slotPositions[i].getLeft(),
                        1 + baseY + slotPositions[i].getRight()));
            }
        }
        initializeSlotsPost();
        this.lastLabel = "";
    }

    /**
     * Recreate the slots of the given element, for example when the element changes its render pattern.
     * In contrast to {@link #setElementInventory(ILogicProgrammerElement, int, int)}, this keeps the current label.
     * @param element The element.
     * @param baseX The slots X coordinate
     * @param baseY The slots Y coordinate
     */
    public void refreshElementInventory(final ILogicProgrammerElement element, int baseX, int baseY) {
        String label = this.lastLabel;
        this.setElementInventory(element, baseX, baseY);
        this.lastLabel = label;
        this.temporaryInputSlots.removeDirtyMarkListener(this);
        element.setValueInContainer(this);
        this.temporaryInputSlots.addDirtyMarkListener(this);
    }

    /**
     * Apply an expression edit action to the active element.
     * This is called on the server via a packet, and directly on the client.
     * @param action The action.
     */
    public void applyExpressionAction(CompoundTag action) {
        if (ExpressionEditor.ACTION_SET.equals(action.getString("a"))) {
            applySetExpressionAction(action);
            return;
        }
        if (activeElement instanceof OperatorLPElement operatorElement) {
            ExpressionEditor.ActionResult result = operatorElement.applyExpressionAction(this,
                    ValueDeseralizationContext.of(player.level()), action);
            if (result == ExpressionEditor.ActionResult.NAVIGATED) {
                if (MinecraftHelpers.isClientSideThread()) {
                    getGui().onExpressionNavigated();
                } else {
                    refreshElementInventory(operatorElement, 0, 0);
                }
            }
            if (result != ExpressionEditor.ActionResult.NONE) {
                onDirty();
            }
        }
    }

    /**
     * @param factory Creates the text field the first time.
     * @return The text of the expression, which is kept while the panel is recreated.
     */
    @OnlyIn(Dist.CLIENT)
    public ExpressionTextField getExpressionTextField(Supplier<ExpressionTextField> factory) {
        if (expressionTextField == null) {
            expressionTextField = factory.get();
        }
        return expressionTextField;
    }

    /**
     * @return The changes to the expression in the active element, which are only recorded on the client.
     */
    public ExpressionHistory getExpressionHistory() {
        return expressionHistory;
    }

    @OnlyIn(Dist.CLIENT)
    protected void resetExpressionTextField() {
        if (expressionTextField != null) {
            expressionTextField.reset();
        }
    }

    /**
     * @param operator An operator.
     * @return The element for building expressions with this operator as root, or null if there is none.
     */
    @Nullable
    public OperatorLPElement getOperatorElement(IOperator operator) {
        for (ILogicProgrammerElement element : getUnfilteredItems()) {
            if (element instanceof OperatorLPElement operatorElement && operatorElement.getOperator() == operator) {
                return operatorElement;
            }
        }
        return null;
    }

    /**
     * Replace the whole expression of the active element.
     * If the root operator changes, the element of that operator becomes active, as when loading a card.
     * This also happens if no operator element is active, such as for an expression that was typed in the empty panel.
     * @param action A set action.
     */
    protected void applySetExpressionAction(CompoundTag action) {
        Expression expression = ExpressionEditor.readSetAction(ValueDeseralizationContext.of(player.level()), action);
        if (expression == null) {
            return;
        }
        OperatorLPElement current = activeElement instanceof OperatorLPElement operatorElement && operatorElement.getEditor() != null
                ? operatorElement : null;
        IOperator root = ((ExpressionNode.Operator) expression.root()).operator();
        OperatorLPElement target = current;
        if (current == null || current.getOperator() != root) {
            target = getOperatorElement(root);
            if (target == null) {
                return;
            }
            // Activating the target deactivates the current element, which forgets its references
            OperatorLPElement.References references = current != null ? current.getReferences() : null;
            // The history and text continue in the other element
            settingExpression = true;
            try {
                if (MinecraftHelpers.isClientSideThread()) {
                    getGui().handleElementActivation(target, false);
                } else {
                    setActiveElement(target, 0, 0);
                }
            } finally {
                settingExpression = false;
            }
            if (references != null) {
                target.addReferences(references);
            }
        }
        // Stay at the open operator if it is still there, such as after typing a change inside it
        target.setExpression(expression, target == current ? current.getEditor().getEditPath() : new int[0],
                ExpressionEditor.readSetActionText(action));
        target.findReferencedCards(player);
        if (MinecraftHelpers.isClientSideThread()) {
            getGui().onExpressionNavigated();
        } else {
            refreshElementInventory(target, 0, 0);
        }
        onDirty();
    }

    public boolean canWriteActiveElementPre() {
        if(activeElement != null) {
            return activeElement.canWriteElementPre();
        }
        return false;
    }

    public boolean canWriteActiveElement() {
        if(!canWriteActiveElementPre()) {
            return false;
        }
        lastError = activeElement.validate();
        return lastError == null;
    }

    public ILogicProgrammerElement getActiveElement() {
        return activeElement;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            returnWriteItemToPlayer();
        }
    }

    public void onLabelPacket(String label) {
        this.lastLabel = label;
        labelCurrent();
    }

    protected void labelCurrent() {
        ItemStack itemStack = writeSlot.getItem(0);
        if(!itemStack.isEmpty()) {
            IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.of(player.level()), itemStack);
            if(this.lastLabel != null && variableFacade.isValid()) {
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableFacade.getId(), this.lastLabel);
            }
        }
    }

    protected ItemStack writeElementInfo() {
        ItemStack itemStack = writeSlot.getItem(0);
        ItemStack result = getActiveElement().writeElement(player, itemStack.copy());
        return result;
    }

    @Override
    public void onDirty() {
        ILogicProgrammerElement activeElement = getActiveElement();
        if(activeElement != null) {
            for (int i = 0; i < temporaryInputSlots.getContainerSize(); i++) {
                ItemStack itemStack = temporaryInputSlots.getItem(i);
                temporarySlotsElement.onInputSlotUpdated(player, i, itemStack);
            }
        }

        ItemStack itemStack = writeSlot.getItem(0);
        if(canWriteActiveElement() && !itemStack.isEmpty()) {
            // If the variable has a vanilla custom name, make sure we inherit it as variable label
            if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                this.lastLabel = itemStack.getHoverName().getString();
            }

            ItemStack outputStack = writeElementInfo();
            writeSlot.removeDirtyMarkListener(this);
            writeSlot.removeDirtyMarkListener(loadConfigListener);
            writeSlot.setItem(0, outputStack);
            if(!StringUtil.isNullOrEmpty(this.lastLabel)) {
                labelCurrent();
            }
            writeSlot.addDirtyMarkListener(this);
            writeSlot.addDirtyMarkListener(loadConfigListener);
        }
    }

    protected void loadConfigFrom(ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        IVariableFacade variableFacade = registry.handle(ValueDeseralizationContext.of(player.level()), itemStack);
        for(ILogicProgrammerElement element : getUnfilteredItems()) {
            if(element.isFor(variableFacade)) {
                writeSlot.removeDirtyMarkListener(this);
                writeSlot.removeDirtyMarkListener(loadConfigListener);
                if (MinecraftHelpers.isClientSideThread()) {
                    getGui().handleElementActivation(element, false);
                } else {
                    setActiveElement(element, 0, 0);
                }
                temporaryInputSlots.removeDirtyMarkListener(this);
                element.loadElement(variableFacade);
                if (element instanceof OperatorLPElement operatorElement) {
                    operatorElement.findReferencedCards(player);
                }
                if (MinecraftHelpers.isClientSideThread()) {
                    element.setValueInGui(getGui().getOperatorConfigPattern());
                } else {
                    element.setValueInContainer(this);
                }
                writeSlot.addDirtyMarkListener(this);
                writeSlot.addDirtyMarkListener(loadConfigListener);
                temporaryInputSlots.addDirtyMarkListener(this);
            }
        }
    }

    public Component getLastError() {
        return this.lastError;
    }

    public SimpleInventory getTemporaryInputSlots() {
        return this.temporaryInputSlots;
    }

    public boolean hasWriteItemInSlot() {
        return !this.writeSlot.getItem(0).isEmpty();
    }

    public void returnWriteItemToPlayer() {
        if (hasWriteItemInSlot()) {
            ItemStack itemStack = writeSlot.getItem(0);
            Helpers.returnItemToPlayer(player, itemStack);
            writeSlot.setItem(0, ItemStack.EMPTY);
        }
    }

    /**
     * Only show operators whose output fits the given type, used while an expression slot is selected.
     * For operator slots, all operators are shown.
     * @param type The expected type, or null to disable this filter.
     */
    public void setExpressionSlotFilter(@Nullable IValueType type) {
        if (this.expressionSlotFilter != type) {
            this.expressionSlotFilter = type;
            refreshFilter();
        }
    }

    @Override
    protected boolean additionalApplies(ILogicProgrammerElement item) {
        // In operator slots, every operator can be picked as a value
        if (expressionSlotFilter != null && !(item instanceof OperatorLPElement
                && (expressionSlotFilter == ValueTypes.OPERATOR || item.matchesOutput(expressionSlotFilter))
                && ((OperatorLPElement) item).getOperator().getRenderPattern() != null)) {
            return false;
        }
        return (
                ((filterIn1 == null || item.matchesInput(filterIn1)) && (filterIn2 == null || item.matchesInput(filterIn2))) || (filterIn1 == null && filterIn2 == null))
                && (filterOut == null || item.matchesOutput(filterOut));
    }

    @Override
    public void clicked(int slotId, int mouseButton, ClickType clickType, Player player) {
        // Handle cases where the client may have more (phantom) slots than the server.
        if (slotId >= this.slots.size() || (this.activeElement != null
                && this.slots.size() > slotId && slotId >= 0
                && this.activeElement.slotClick(slotId, this.getSlot(slotId), mouseButton, clickType, player))) {
            return;
        }
        super.clicked(slotId, mouseButton, clickType, player);
    }

    /**
     * Load existing operator data when a variable card is inserted into the write slot
     */
    protected class LoadConfigListener implements IDirtyMarkListener {

        @Override
        public void onDirty() {
            if (activeElement == null) {
                ItemStack itemStack = writeSlot.getItem(0);
                if (!itemStack.isEmpty()) {
                    ContainerLogicProgrammerBase.this.loadConfigFrom(itemStack);
                }
            }
        }

    }

    /**
     * Filter LP elements based on the filter value types.
     */
    protected class FilterSlotListener implements IDirtyMarkListener {

        private final ValueDeseralizationContext valueDeseralizationContext;

        public FilterSlotListener(ValueDeseralizationContext valueDeseralizationContext) {
            this.valueDeseralizationContext = valueDeseralizationContext;
        }

        protected IValueType getValueType(Container inventory, int slot) {
            IVariableFacadeHandlerRegistry handler = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            if(inventory.getItem(slot) != null) {
                IVariableFacade variableFacade = handler.handle(valueDeseralizationContext, inventory.getItem(slot));
                if(variableFacade.isValid()) {
                    return variableFacade.getOutputType();
                }
            }
            return null;
        }

        @Override
        public void onDirty() {
            IValueType filterIn1Prev = filterIn1;
            IValueType filterIn2Prev = filterIn2;
            IValueType filterOutPrev = filterOut;

            filterIn1 = getValueType(filterSlots, 0);
            filterIn2 = getValueType(filterSlots, 1);
            filterOut = getValueType(filterSlots, 2);
            if (filterIn1Prev != filterIn1 || filterIn2Prev != filterIn2 || filterOutPrev != filterOut) {
                refreshFilter();
            }
        }

    }

}
