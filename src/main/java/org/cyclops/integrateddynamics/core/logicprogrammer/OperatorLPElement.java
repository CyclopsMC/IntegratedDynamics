package org.cyclops.integrateddynamics.core.logicprogrammer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IExpressionVariableFacade;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.ExpressionVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.ReferencePlaceholderVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.core.item.ReferencePlaceholderVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Element for operator.
 *
 * The element edits an expression tree with this operator at its root.
 * The slots always show the inputs of the operator node that is currently being edited.
 * When the tree is a single operator with only variable references, a plain operator variable is written,
 * otherwise an expression variable is written.
 *
 * @author rubensworks
 */
@Data
public class OperatorLPElement implements ILogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final IOperator operator;
    private IVariableFacade[] inputVariables;
    private boolean focused = false;
    @Nullable
    private ExpressionEditor editor;

    // The actual cards that were placed in slots, so they can be shown again after navigating
    private final Int2ObjectMap<ItemStack> refCards = new Int2ObjectOpenHashMap<>();

    // Client-side UI state
    private int selectedSlot = -1;
    private int armedParameter = -1;

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.OPERATOR;
    }

    @Override
    public String getMatchString() {
        return getOperator().getLocalizedNameFull().getString().toLowerCase();
    }

    @Override
    public boolean matchesInput(IValueType valueType) {
        for (IValueType operatorIn : getOperator().getInputTypes()) {
            if(ValueHelpers.correspondsTo(operatorIn, valueType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchesOutput(IValueType valueType) {
        return ValueHelpers.correspondsTo(getOperator().getOutputType(), valueType);
    }

    @Override
    public Component getName() {
        return getOperator().getLocalizedNameFull();
    }

    @Override
    public void loadTooltip(List<Component> lines) {
        getOperator().loadTooltip(lines, true);
    }

    /**
     * @return The operator of the node that is currently being edited.
     */
    public IOperator getCurrentOperator() {
        return editor != null ? editor.getCurrentOperator() : getOperator();
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        IOperator currentOperator = getCurrentOperator();
        if (currentOperator.getRenderPattern() == null) {
            throw new IllegalStateException("Tried to render a (possibly virtual) operator with a null render pattern: "
                    + currentOperator.getUniqueName());
        }
        return currentOperator.getRenderPattern();
    }

    /**
     * @param slotId A slot of the current node.
     * @return If the given item can be stored as an inline item constant in the given slot.
     */
    protected boolean acceptsItemConstant(int slotId, ItemStack itemStack) {
        return editor != null && !itemStack.isEmpty() && itemStack.getItem() != RegistryEntries.ITEM_VARIABLE.get()
                && slotId < editor.getSlotCount()
                && ValueHelpers.correspondsTo(editor.getExpectedType(slotId), ValueTypes.OBJECT_ITEMSTACK);
    }

    @Override
    public void onInputSlotUpdated(Player player, int slotId, ItemStack itemStack) {
        if (editor == null || slotId >= editor.getSlotCount()) {
            return;
        }
        ExpressionNode child = editor.getChild(slotId);
        if (itemStack.isEmpty()) {
            inputVariables[slotId] = null;
            // Only clear nodes that were backed by an item in the slot
            if (child instanceof ExpressionNode.Ref || isItemConstant(child)) {
                editor.setChild(slotId, ExpressionNode.EMPTY);
            }
        } else if (itemStack.getItem() == RegistryEntries.ITEM_VARIABLE.get()) {
            IVariableFacade variableFacade = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).handle(ValueDeseralizationContext.of(player.level()), itemStack);
            inputVariables[slotId] = variableFacade;
            if (variableFacade.isValid()) {
                if (!(child instanceof ExpressionNode.Ref ref) || ref.variableId() != variableFacade.getId()) {
                    editor.setChild(slotId, new ExpressionNode.Ref(variableFacade.getId()));
                }
                if (variableFacade instanceof ReferencePlaceholderVariableFacade placeholder) {
                    if (!editor.hasRefType(placeholder.getId())) {
                        editor.setRefType(placeholder.getId(), placeholder.getValueType());
                    }
                } else {
                    refCards.put(variableFacade.getId(), itemStack.copyWithCount(1));
                    editor.setRefType(variableFacade.getId(), variableFacade.getOutputType());
                }
            } else {
                editor.setChild(slotId, ExpressionNode.EMPTY);
            }
        } else if (acceptsItemConstant(slotId, itemStack)) {
            inputVariables[slotId] = null;
            ExpressionNode.Const constant = new ExpressionNode.Const(ValueObjectTypeItemStack.ValueItemStack.of(itemStack.copyWithCount(1)));
            if (!constant.equals(child)) {
                editor.setChild(slotId, constant);
            }
        }
    }

    protected static boolean isItemConstant(ExpressionNode node) {
        return node instanceof ExpressionNode.Const constant && constant.value() instanceof ValueObjectTypeItemStack.ValueItemStack;
    }

    @Override
    public boolean canWriteElementPre() {
        if (editor == null) {
            return false;
        }
        if (inputVariables != null) {
            for (IVariableFacade inputVariable : inputVariables) {
                if (inputVariable != null && !inputVariable.isValid()) {
                    return false;
                }
            }
        }
        return ExpressionTrees.countEmpty(editor.getExpression().root()) == 0;
    }

    protected int[] getVariableIds(IVariableFacade[] inputVariables) {
        int[] variableIds = new int[inputVariables.length];
        for (int i = 0; i < inputVariables.length; i++) {
            variableIds[i] = inputVariables[i].getId();
        }
        return variableIds;
    }

    @Override
    public ItemStack writeElement(Player player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        Expression expression = editor.getExpression();
        if (ExpressionTrees.isPlainOperator(expression)) {
            // Write the same facade as before expressions existed, for backwards-compatibility.
            int[] variableIds = ExpressionTrees.getPlainOperatorVariableIds(expression);
            return registry.writeVariableFacadeItem(!player.level().isClientSide(), itemStack, Operators.REGISTRY,
                    new OperatorVariableFacadeFactory(operator, variableIds), player.level(), player, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
        }
        return registry.writeVariableFacadeItem(!player.level().isClientSide(), itemStack, ExpressionVariableFacadeHandler.getInstance(),
                new ExpressionVariableFacadeFactory(expression, editor.getRefTypes()), player.level(), player, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
    }

    @Override
    public void loadElement(IVariableFacade variableFacade) {
        if (variableFacade instanceof OperatorVariableFacade operatorVariableFacade) {
            this.editor.setExpression(ExpressionTrees.fromPlainOperator(operator, operatorVariableFacade.getVariableIds()));
        } else if (variableFacade instanceof IExpressionVariableFacade expressionVariableFacade
                && expressionVariableFacade.getExpression() != null) {
            this.editor.setExpression(expressionVariableFacade.getExpression());
            expressionVariableFacade.getReferenceTypes().forEach(this.editor::setRefType);
        } else {
            return;
        }
        this.editor.inferMissingRefTypes();
        this.inputVariables = new IVariableFacade[editor.getSlotCount()];
    }

    /**
     * Look for the variable cards that are referenced in the loaded expression in the player's inventory,
     * so their actual types are known and the actual cards can be shown in the slots.
     * @param player The player.
     */
    public void findReferencedCards(Player player) {
        if (editor == null) {
            return;
        }
        IntList refIds = ExpressionTrees.getRefIds(editor.getExpression().root());
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() == RegistryEntries.ITEM_VARIABLE.get()) {
                IVariableFacade variableFacade = registry.handle(ValueDeseralizationContext.of(player.level()), itemStack);
                if (variableFacade.isValid() && refIds.contains(variableFacade.getId())
                        && !(variableFacade instanceof ReferencePlaceholderVariableFacade) && !refCards.containsKey(variableFacade.getId())) {
                    refCards.put(variableFacade.getId(), itemStack.copyWithCount(1));
                    editor.setRefType(variableFacade.getId(), variableFacade.getOutputType());
                }
            }
        }
    }

    /**
     * @param variableId A referenced variable id.
     * @return The item to show in a slot for the given reference, the actual card if it is known, otherwise a placeholder.
     */
    protected ItemStack getReferenceItem(int variableId) {
        ItemStack card = refCards.get(variableId);
        if (card != null) {
            return card.copy();
        }
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class)
                .writeVariableFacadeItem(new ItemStack(RegistryEntries.ITEM_VARIABLE),
                        new ReferencePlaceholderVariableFacade(variableId, editor.getRefType(variableId)),
                        ReferencePlaceholderVariableFacadeHandler.getInstance());
    }

    @Override
    public void setValueInGui(RenderPattern subGui) {
        setValueInContainer((ContainerLogicProgrammerBase) subGui.container);
    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {
        SimpleInventory inputSlots = container.getTemporaryInputSlots();
        for (int i = 0; i < inputSlots.getContainerSize() && i < editor.getSlotCount(); i++) {
            ExpressionNode child = editor.getChild(i);
            ItemStack itemStack = ItemStack.EMPTY;
            if (child instanceof ExpressionNode.Ref ref) {
                itemStack = getReferenceItem(ref.variableId());
            } else if (isItemConstant(child)) {
                itemStack = ((ValueObjectTypeItemStack.ValueItemStack) ((ExpressionNode.Const) child).value()).getRawValue().copy();
            }
            inputSlots.setItem(i, itemStack);
        }
    }

    /**
     * Apply an expression edit action, on both client and server.
     * @param container The container.
     * @param valueDeseralizationContext The deserialization context.
     * @param action The action, see {@link ExpressionEditor}.
     * @return The result.
     */
    public ExpressionEditor.ActionResult applyExpressionAction(ContainerLogicProgrammerBase container,
                                                               ValueDeseralizationContext valueDeseralizationContext,
                                                               CompoundTag action) {
        if (editor == null) {
            return ExpressionEditor.ActionResult.NONE;
        }
        ExpressionEditor.ActionResult result = editor.apply(valueDeseralizationContext, action);
        if (result == ExpressionEditor.ActionResult.SLOT_CHANGED) {
            int slot = action.getInt("slot");
            ExpressionNode child = editor.getChild(slot);
            if (!(child instanceof ExpressionNode.Ref) && !isItemConstant(child)) {
                // The slot no longer holds an item-backed node
                inputVariables[slot] = null;
                container.getTemporaryInputSlots().removeDirtyMarkListener(container);
                container.getTemporaryInputSlots().setItem(slot, ItemStack.EMPTY);
                container.getTemporaryInputSlots().addDirtyMarkListener(container);
            }
        } else if (result == ExpressionEditor.ActionResult.NAVIGATED) {
            selectedSlot = -1;
            inputVariables = new IVariableFacade[editor.getSlotCount()];
        }
        return result;
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return true;
    }

    @Override
    public void activate() {
        this.editor = new ExpressionEditor(operator);
        this.refCards.clear();
        this.inputVariables = new IVariableFacade[getRenderPattern().getSlotPositions().length];
        this.selectedSlot = -1;
        this.armedParameter = -1;
    }

    @Override
    public void deactivate() {
        this.inputVariables = null;
        this.refCards.clear();
        this.editor = null;
        this.selectedSlot = -1;
        this.armedParameter = -1;
    }

    @Override
    public Component validate() {
        if (editor == null) {
            return null;
        }
        ExpressionTypeChecker.Result result = editor.check();
        if (!result.errors().isEmpty()) {
            return result.errors().get(0).message();
        }
        if (result.emptySlots() > 0) {
            return Component.translatable(L10NValues.EXPRESSION_ERROR_INCOMPLETE);
        }
        return null;
    }

    @Override
    public int getColor() {
        return getOperator().getOutputType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return getOperator().getSymbol();
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof IOperatorVariableFacade) {
            IOperatorVariableFacade operatorFacade = (IOperatorVariableFacade) variableFacade;
            if (operatorFacade.isValid()) {
                return getOperator() == operatorFacade.getOperator();
            }
        }
        if (variableFacade instanceof ExpressionVariableFacade expressionVariableFacade) {
            return expressionVariableFacade.getExpression() != null && getOperator() == expressionVariableFacade.getRootOperator();
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return itemStack.getItem() == RegistryEntries.ITEM_VARIABLE.get() || acceptsItemConstant(slotId, itemStack);
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, Player player) {
        return false;
    }

    @Override
    public int getItemStackSizeLimit() {
        return 1;
    }

    @Override
    public boolean isFocused(RenderPattern subGui) {
        return focused;
    }

    @Override
    public void setFocused(RenderPattern subGui, boolean focused) {
        this.focused = focused;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                      ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new OperatorLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    public static class OperatorVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IOperatorVariableFacade> {

        private final IOperator operator;
        private final int[] variableIds;

        public OperatorVariableFacadeFactory(IOperator operator, int[] variableIds) {
            this.operator = operator;
            this.variableIds = variableIds;
        }

        @Override
        public IOperatorVariableFacade create(boolean generateId) {
            return new OperatorVariableFacade(generateId, operator, variableIds);
        }

        @Override
        public IOperatorVariableFacade create(int id) {
            return new OperatorVariableFacade(id, operator, variableIds);
        }
    }

    public static class ExpressionVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IExpressionVariableFacade> {

        private final Expression expression;
        private final Map<Integer, IValueType<?>> referenceTypes;

        public ExpressionVariableFacadeFactory(Expression expression) {
            this(expression, Map.of());
        }

        public ExpressionVariableFacadeFactory(Expression expression, Map<Integer, IValueType<?>> referenceTypes) {
            this.expression = expression;
            this.referenceTypes = referenceTypes;
        }

        @Override
        public IExpressionVariableFacade create(boolean generateId) {
            return new ExpressionVariableFacade(generateId, expression, referenceTypes);
        }

        @Override
        public IExpressionVariableFacade create(int id) {
            return new ExpressionVariableFacade(id, expression, referenceTypes);
        }
    }

}
