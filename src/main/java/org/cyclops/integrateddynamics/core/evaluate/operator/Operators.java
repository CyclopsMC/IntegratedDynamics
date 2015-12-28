package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

/**
 * Collection of available operators.
 *
 * @author rubensworks
 */
public final class Operators {

    public static final IOperatorRegistry REGISTRY = constructRegistry();

    private static IOperatorRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IOperatorRegistry.class);
        } else {
            return OperatorRegistry.getInstance();
        }
    }

    public static void load() {}

    /**
     * ----------------------------------- LOGICAL OPERATORS -----------------------------------
     */

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_AND = REGISTRY.register(new LogicalOperator("&&", "and", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if (!a) {
                return ValueTypeBoolean.ValueBoolean.of(false);
            } else {
                return variables[1].getValue();
            }
        }
    }));

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_OR = REGISTRY.register(new LogicalOperator("||", "or", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if (a) {
                return ValueTypeBoolean.ValueBoolean.of(true);
            } else {
                return variables[1].getValue();
            }
        }
    }));

    /**
     * Logical NOT operator with one input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_NOT = REGISTRY.register(new LogicalOperator("!", "not", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(!a);
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * ----------------------------------- ARITHMETIC OPERATORS -----------------------------------
     */

    private static final ValueTypeInteger.ValueInteger ZERO = ValueTypeInteger.ValueInteger.of(0);

    /**
     * Arithmetic ADD operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_ADDITION = REGISTRY.register(new ArithmeticOperator("+", "addition", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.add(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MINUS operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(new ArithmeticOperator("-", "subtraction", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.subtract(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MULTIPLY operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(new ArithmeticOperator("*", "multiplication", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.multiply(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic DIVIDE operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_DIVISION = REGISTRY.register(new ArithmeticOperator("/", "division", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.divide(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MAX operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MAXIMUM = REGISTRY.register(new ArithmeticOperator("max", "maximum", new OperatorBase.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.max(variables[0], variables[1]);
        }
    }, IConfigRenderPattern.PREFIX_2));

    /**
     * Arithmetic MIN operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MINIMUM = REGISTRY.register(new ArithmeticOperator("min", "minimum", new OperatorBase.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.min(variables[0], variables[1]);
        }
    }, IConfigRenderPattern.PREFIX_2));



    /**
     * ----------------------------------- INTEGER OPERATORS -----------------------------------
     */

    /**
     * Integer MODULO operator with two input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_MODULUS = REGISTRY.register(new IntegerOperator("%", "modulus", new OperatorBase.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            if (b == 0) { // You can not divide by zero
                throw new EvaluationException("Division by zero");
            } else if (b == 1) { // If b is neutral element for division
                return ZERO;
            } else {
                int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a % b);
            }
        }
    }));

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_INCREMENT = REGISTRY.register(new IntegerOperator("++", "increment", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a + 1);
        }
    }, IConfigRenderPattern.SUFFIX_1));

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_DECREMENT = REGISTRY.register(new IntegerOperator("--", "decrement", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a - 1);
        }
    }, IConfigRenderPattern.SUFFIX_1));

    /**
     * ----------------------------------- RELATIONAL OPERATORS -----------------------------------
     */

    /**
     * Relational == operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_EQUALS = REGISTRY.register(new RelationalEqualsOperator("==", "equals"));

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_GT = REGISTRY.register(new RelationalOperator(">", "gt", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a > b);
        }
    }));

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_LT = REGISTRY.register(new RelationalOperator("<", "lt", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a < b);
        }
    }));

    /**
     * Relational != operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_NOTEQUALS = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(RELATIONAL_EQUALS).build(
                    "!=", "notequals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &gt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_GE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_GT).build(
                    ">=", "ge", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &lt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_LE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_LT).build(
                    "<=", "le", IConfigRenderPattern.INFIX, "relational"));

    /**
     * ----------------------------------- BINARY OPERATORS -----------------------------------
     */

    /**
     * Binary AND operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_AND = REGISTRY.register(new BinaryOperator("&", "and", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a & b);
        }
    }));

    /**
     * Binary OR operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_OR = REGISTRY.register(new BinaryOperator("|", "or", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a | b);
        }
    }));

    /**
     * Binary XOR operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_XOR = REGISTRY.register(new BinaryOperator("^", "xor", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a ^ b);
        }
    }));

    /**
     * Binary COMPLEMENT operator with one input integers and one output integers.
     */
    public static final BinaryOperator BINARY_COMPLEMENT = REGISTRY.register(new BinaryOperator("~", "complement", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(~a);
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Binary &lt;&lt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_LSHIFT = REGISTRY.register(new BinaryOperator("<<", "lshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a << b);
        }
    }));

    /**
     * Binary &gt;&gt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RSHIFT = REGISTRY.register(new BinaryOperator(">>", "rshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >> b);
        }
    }));

    /**
     * Binary &gt;&gt;&gt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RZSHIFT = REGISTRY.register(new BinaryOperator(">>>", "rzshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >>> b);
        }
    }));

    /**
     * ----------------------------------- STRING OPERATORS -----------------------------------
     */

    /**
     * String length operator with one input string and one output integer.
     */
    public static final StringOperator STRING_LENGTH = REGISTRY.register(new StringOperator("len", "length", new IValueType[]{ValueTypes.STRING}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            String a = ((ValueTypeString.ValueString) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a.length());
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * String concat operator with two input strings and one output string.
     */
    public static final StringOperator STRING_CONCAT = REGISTRY.register(new StringOperator("+", "concat", 2, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            String a = ((ValueTypeString.ValueString) variables[0].getValue()).getRawValue();
            String b = ((ValueTypeString.ValueString) variables[1].getValue()).getRawValue();
            return ValueTypeString.ValueString.of(a + b);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * Get a name value type name.
     */
    public static final StringOperator NAMED_NAME = REGISTRY.register(new StringOperator("name", "name", new IValueType[]{ValueTypes.CATEGORY_NAMED}, ValueTypes.STRING, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypeString.ValueString.of(ValueTypes.CATEGORY_NAMED.getName(variables[0]));
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * ----------------------------------- DOUBLE OPERATORS -----------------------------------
     */

    /**
     * Double round operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_ROUND = REGISTRY.register(new DoubleOperator("|| ||", "round", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.round(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Double ceil operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_CEIL = REGISTRY.register(new DoubleOperator("⌈ ⌉", "ceil", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.ceil(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Double floor operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_FLOOR = REGISTRY.register(new DoubleOperator("⌊ ⌋", "floor", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.floor(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * ----------------------------------- LIST OPERATORS -----------------------------------
     */

    /**
     * List operator with one input list and one output integer
     */
    public static final ListOperator LIST_LENGTH = REGISTRY.register(new ListOperator("| |", "length", new IValueType[]{ValueTypes.LIST}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            IValueTypeListProxy a = ((ValueTypeList.ValueList) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a.getLength());
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * List operator with one input list and one output integer
     */
    public static final ListOperator LIST_ELEMENT = REGISTRY.register(new ListOperator("get", "get", new IValueType[]{ValueTypes.LIST, ValueTypes.INTEGER}, ValueTypes.CATEGORY_ANY, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            IValueTypeListProxy a = ((ValueTypeList.ValueList) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            if(b < a.getLength()) {
                return a.get(b);
            } else {
                return a.getValueType().getDefault();
            }
        }
    }, IConfigRenderPattern.INFIX) {
        @Override
        public IValueType getConditionalOutputType(IVariable[] input) {
            try {
                IValueTypeListProxy a = ((ValueTypeList.ValueList) input[0].getValue()).getRawValue();
                return a.getValueType();
            } catch (EvaluationException e) {
                return super.getConditionalOutputType(input);
            }
        }
    });

    /**
     * ----------------------------------- BLOCK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Block isOpaque operator with one input block and one output boolean.
     */
    public static final ObjectBlockOperator OBJECT_BLOCK_OPAQUE = REGISTRY.register(new ObjectBlockOperator("opaque", new IValueType[]{ValueTypes.OBJECT_BLOCK}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<IBlockState> a = ((ValueObjectTypeBlock.ValueBlock) variables[0].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a.isPresent() && a.get().getBlock().isOpaqueCube());
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The itemstack representation of the block
     */
    public static final ObjectBlockOperator OBJECT_ITEMSTACK = REGISTRY.register(new ObjectBlockOperator("itemstack", new IValueType[]{ValueTypes.OBJECT_BLOCK}, ValueTypes.OBJECT_ITEMSTACK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<IBlockState> a = ((ValueObjectTypeBlock.ValueBlock) variables[0].getValue()).getRawValue();
            return ValueObjectTypeItemStack.ValueItemStack.of(a.isPresent() ? BlockHelpers.getItemStackFromBlockState(a.get()) : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The item representation of the block
     */
    public static final ObjectBlockOperator OBJECT_ITEM = REGISTRY.register(new ObjectBlockOperator("item", new IValueType[]{ValueTypes.OBJECT_BLOCK}, ValueTypes.OBJECT_ITEM, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<IBlockState> a = ((ValueObjectTypeBlock.ValueBlock) variables[0].getValue()).getRawValue();
            return ValueObjectTypeItem.ValueItem.of(a.isPresent() ? Item.getItemFromBlock(a.get().getBlock()) : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * ----------------------------------- ITEM STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Item Stack size operator with one input itemstack and one output integer.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_SIZE = REGISTRY.register(ObjectItemStackOperator.toInt("size", new ObjectItemStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.stackSize;
        }
    }));

    /**
     * Item Stack maxsize operator with one input itemstack and one output integer.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_MAXSIZE = REGISTRY.register(ObjectItemStackOperator.toInt("maxsize", new ObjectItemStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.getMaxStackSize();
        }
    }));

    /**
     * Item Stack isstackable operator with one input itemstack and one output boolean.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISSTACKABLE = REGISTRY.register(ObjectItemStackOperator.toBoolean("stackable", new ObjectItemStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.isStackable();
        }
    }));

    /**
     * Item Stack isdamageable operator with one input itemstack and one output boolean.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISDAMAGEABLE = REGISTRY.register(ObjectItemStackOperator.toBoolean("damageable", new ObjectItemStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.isItemStackDamageable();
        }
    }));

    /**
     * Item Stack damage operator with one input itemstack and one output integer.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_DAMAGE = REGISTRY.register(ObjectItemStackOperator.toInt("damage", new ObjectItemStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.getItemDamage();
        }
    }));

    /**
     * Item Stack maxdamage operator with one input itemstack and one output integer.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_MAXDAMAGE = REGISTRY.register(ObjectItemStackOperator.toInt("maxdamage", new ObjectItemStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.getMaxDamage();
        }
    }));

    /**
     * Item Stack isenchanted operator with one input itemstack and one output boolean.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISENCHANTED = REGISTRY.register(ObjectItemStackOperator.toBoolean("enchanted", new ObjectItemStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.isItemEnchanted();
        }
    }));

    /**
     * Item Stack isenchantable operator with one input itemstack and one output boolean.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISENCHANTABLE = REGISTRY.register(ObjectItemStackOperator.toBoolean("enchantable", new ObjectItemStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.isItemEnchantable();
        }
    }));

    /**
     * Item Stack repair cost with one input itemstack and one output integer.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_REPAIRCOST = REGISTRY.register(ObjectItemStackOperator.toInt("repaircost", new ObjectItemStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(ItemStack itemStack) throws EvaluationException {
            return itemStack.getRepairCost();
        }
    }));

    /**
     * Get the rarity of an itemstack.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_RARITY = REGISTRY.register(new ObjectItemStackOperator("rarity", "rarity", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.STRING, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            return ValueTypeString.ValueString.of(a.isPresent() ? a.get().getRarity().rarityName : "");
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * Get the strength of an itemstack against a block as a double.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_STRENGTH_VS_BLOCK = REGISTRY.register(new ObjectItemStackOperator("strength", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}, ValueTypes.DOUBLE, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            Optional<IBlockState> b = ((ValueObjectTypeBlock.ValueBlock) variables[1].getValue()).getRawValue();
            return ValueTypeDouble.ValueDouble.of(a.isPresent() && b.isPresent() ? a.get().getStrVsBlock(b.get().getBlock()) : 0);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * If the given itemstack can be used to harvest the given block.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK = REGISTRY.register(new ObjectItemStackOperator("canharvest", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            Optional<IBlockState> b = ((ValueObjectTypeBlock.ValueBlock) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a.isPresent() && b.isPresent() ? a.get().canHarvestBlock(b.get().getBlock()) : false);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * The item from the stack
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ITEM = REGISTRY.register(new ObjectItemStackOperator("item", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.OBJECT_ITEM, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            return ValueObjectTypeItem.ValueItem.of(a.isPresent() ? a.get().getItem() : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The block from the stack
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_BLOCK = REGISTRY.register(new ObjectItemStackOperator("block", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.OBJECT_BLOCK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            return ValueObjectTypeBlock.ValueBlock.of((a.isPresent() && a.get().getItem() instanceof ItemBlock) ? BlockHelpers.getBlockStateFromItemStack(a.get()) : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * ----------------------------------- ITEM OBJECT OPERATORS -----------------------------------
     */

    /**
     * If the item is a block item
     */
    public static final ObjectItemOperator OBJECT_ITEM_ISBLOCK = REGISTRY.register(new ObjectItemOperator("isblock", new IValueType[]{ValueTypes.OBJECT_ITEM}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<Item> a = ((ValueObjectTypeItem.ValueItem) variables[0].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a.isPresent() && a.get() instanceof ItemBlock);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The block from the item
     */
    public static final ObjectItemOperator OBJECT_ITEM_BLOCK = REGISTRY.register(new ObjectItemOperator("block", new IValueType[]{ValueTypes.OBJECT_ITEM}, ValueTypes.OBJECT_BLOCK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<Item> a = ((ValueObjectTypeItem.ValueItem) variables[0].getValue()).getRawValue();
            return ValueObjectTypeBlock.ValueBlock.of((a.isPresent() && a.get() instanceof ItemBlock) ? ((ItemBlock) a.get()).getBlock().getDefaultState() : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * ----------------------------------- GENERAL OPERATORS -----------------------------------
     */

    /**
     * Choice operator with one boolean input, two any inputs and one output any.
     */
    public static final GeneralOperator GENERAL_CHOICE = REGISTRY.register(new GeneralChoiceOperator("?", "choice"));

    /**
     * Identity operator with one any input and one any output
     */
    public static final GeneralOperator GENERAL_IDENTITY = REGISTRY.register(new GeneralIdentityOperator("id", "identity"));

}
