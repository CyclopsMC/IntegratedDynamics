package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
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
import org.cyclops.integrateddynamics.core.helper.Helpers;

import java.util.Collections;

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
                throw new EvaluationException(String.format("Index %s out of bounds for list of length %s.", b, a.getLength()));
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
    public static final ObjectBlockOperator OBJECT_BLOCK_ITEMSTACK = REGISTRY.register(new ObjectBlockOperator("itemstack", new IValueType[]{ValueTypes.OBJECT_BLOCK}, ValueTypes.OBJECT_ITEMSTACK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<IBlockState> a = ((ValueObjectTypeBlock.ValueBlock) variables[0].getValue()).getRawValue();
            return ValueObjectTypeItemStack.ValueItemStack.of(a.isPresent() ? BlockHelpers.getItemStackFromBlockState(a.get()) : null);
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
     * If the given stack has a fluid.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISFLUIDSTACK = REGISTRY.register(ObjectItemStackOperator.toBoolean("isfluidstack", new ObjectItemStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(ItemStack itemStack) throws EvaluationException {
            return Helpers.getFluidStack(itemStack) != null;
        }
    }));

    /**
     * The fluidstack from the stack
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_FLUIDSTACK = REGISTRY.register(new ObjectItemStackOperator("fluidstack", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.OBJECT_FLUIDSTACK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            return ValueObjectTypeFluidStack.ValueFluidStack.of(a.isPresent() ? Helpers.getFluidStack(a.get()) : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The capacity of the fluidstack from the stack.
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_FLUIDSTACKCAPACITY = REGISTRY.register(new ObjectItemStackOperator("fluidstackcapacity", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a.isPresent() ? Helpers.getFluidStackCapacity(a.get()) : 0);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * If the NBT tags of the given stacks are equal
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISNBTEQUAL = REGISTRY.register(new ObjectItemStackOperator("=NBT=", "isnbtequal", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            Optional<ItemStack> b = ((ValueObjectTypeItemStack.ValueItemStack) variables[1].getValue()).getRawValue();
            boolean equal = false;
            if(a.isPresent() && b.isPresent()) {
                equal = a.get().isItemEqual(b.get()) && ItemStack.areItemStackTagsEqual(a.get(), b.get());
            } else if(!a.isPresent() && !b.isPresent()) {
                equal = true;
            }
            return ValueTypeBoolean.ValueBoolean.of(equal);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * If the raw items of the given stacks are equal
     */
    public static final ObjectItemStackOperator OBJECT_ITEMSTACK_ISRAWITEMEQUAL = REGISTRY.register(new ObjectItemStackOperator("=Raw=", "israwitemequal", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
            Optional<ItemStack> b = ((ValueObjectTypeItemStack.ValueItemStack) variables[1].getValue()).getRawValue();
            boolean equal = false;
            if(a.isPresent() && b.isPresent()) {
                equal = ItemStack.areItemsEqual(a.get(), b.get());
            } else if(!a.isPresent() && !b.isPresent()) {
                equal = true;
            }
            return ValueTypeBoolean.ValueBoolean.of(equal);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * ----------------------------------- ENTITY OBJECT OPERATORS -----------------------------------
     */

    /**
     * If the entity is a mob
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISMOB = REGISTRY.register(ObjectEntityOperator.toBoolean("ismob", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity instanceof IMob;
        }
    }));

    /**
     * If the entity is an animal
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISANIMAL = REGISTRY.register(ObjectEntityOperator.toBoolean("isanimal", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity instanceof IAnimals;
        }
    }));

    /**
     * If the entity is an item
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISITEM = REGISTRY.register(ObjectEntityOperator.toBoolean("isitem", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity instanceof EntityItem;
        }
    }));

    /**
     * If the entity is a player
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISPLAYER = REGISTRY.register(ObjectEntityOperator.toBoolean("isplayer", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity instanceof EntityPlayer;
        }
    }));

    /**
     * The itemstack from the entity
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ITEMSTACK = REGISTRY.register(new ObjectEntityOperator("item", new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.OBJECT_ITEMSTACK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
            return ValueObjectTypeItemStack.ValueItemStack.of((a.isPresent() && a.get() instanceof EntityItem) ? ((EntityItem) a.get()).getEntityItem() : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The entity health
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_HEALTH = REGISTRY.register(ObjectEntityOperator.toDouble("health", new ObjectEntityOperator.IDoubleFunction() {
        @Override
        public double evaluate(Entity entity) throws EvaluationException {
            return (entity instanceof EntityLivingBase) ? ((EntityLivingBase) entity).getHealth() : 0;
        }
    }));

    /**
     * The entity width
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_WIDTH = REGISTRY.register(ObjectEntityOperator.toDouble("width", new ObjectEntityOperator.IDoubleFunction() {
        @Override
        public double evaluate(Entity entity) throws EvaluationException {
            return entity.width;
        }
    }));

    /**
     * The entity width
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_HEIGHT = REGISTRY.register(ObjectEntityOperator.toDouble("height", new ObjectEntityOperator.IDoubleFunction() {
        @Override
        public double evaluate(Entity entity) throws EvaluationException {
            return entity.height;
        }
    }));

    /**
     * If the entity is burning
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISBURNING = REGISTRY.register(ObjectEntityOperator.toBoolean("isburning", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity.isBurning();
        }
    }));

    /**
     * If the entity is wet
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISWET = REGISTRY.register(ObjectEntityOperator.toBoolean("iswet", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity.isWet();
        }
    }));

    /**
     * If the entity is sneaking
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISSNEAKING = REGISTRY.register(ObjectEntityOperator.toBoolean("issneaking", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity.isSneaking();
        }
    }));

    /**
     * If the entity is eating
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ISEATING = REGISTRY.register(ObjectEntityOperator.toBoolean("iseating", new ObjectEntityOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(Entity entity) throws EvaluationException {
            return entity.isEating();
        }
    }));

    /**
     * The list of armor itemstacks from an entity
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_ARMORINVENTORY = REGISTRY.register(new ObjectEntityOperator("armorinventory", new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.LIST, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
            if(a.isPresent()) {
                Entity entity = a.get();
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityArmorInventory(entity.worldObj, entity));
            } else {
                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Collections.EMPTY_LIST);
            }
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The list of itemstacks from an entity
     */
    public static final ObjectEntityOperator OBJECT_ENTITY_INVENTORY = REGISTRY.register(new ObjectEntityOperator("inventory", new IValueType[]{ValueTypes.OBJECT_ENTITY}, ValueTypes.LIST, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables[0].getValue()).getRawValue();
            if(a.isPresent()) {
                Entity entity = a.get();
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityInventory(entity.worldObj, entity));
            } else {
                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Collections.EMPTY_LIST);
            }
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * ----------------------------------- FLUID STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * The amount of fluid in the fluidstack
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_AMOUNT = REGISTRY.register(ObjectFluidStackOperator.toInt("amount", new ObjectFluidStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(FluidStack fluidStack) throws EvaluationException {
            return fluidStack.amount;
        }
    }));

    /**
     * The block from the fluidstack
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_BLOCK = REGISTRY.register(new ObjectFluidStackOperator("block", new IValueType[]{ValueTypes.OBJECT_FLUIDSTACK}, ValueTypes.OBJECT_BLOCK, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables[0].getValue()).getRawValue();
            return ValueObjectTypeBlock.ValueBlock.of(a.isPresent() ? a.get().getFluid().getBlock().getDefaultState() : null);
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * The fluidstack luminosity
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_LUMINOSITY = REGISTRY.register(ObjectFluidStackOperator.toInt("luminosity", new ObjectFluidStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(FluidStack fluidStack) throws EvaluationException {
            return fluidStack.getFluid().getLuminosity(fluidStack);
        }
    }));

    /**
     * The fluidstack density
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_DENSITY = REGISTRY.register(ObjectFluidStackOperator.toInt("density", new ObjectFluidStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(FluidStack fluidStack) throws EvaluationException {
            return fluidStack.getFluid().getDensity(fluidStack);
        }
    }));

    /**
     * The fluidstack viscosity
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_VISCOSITY = REGISTRY.register(ObjectFluidStackOperator.toInt("viscosity", new ObjectFluidStackOperator.IIntegerFunction() {
        @Override
        public int evaluate(FluidStack fluidStack) throws EvaluationException {
            return fluidStack.getFluid().getViscosity(fluidStack);
        }
    }));

    /**
     * If the fluidstack is gaseous
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_ISGASEOUS = REGISTRY.register(ObjectFluidStackOperator.toBoolean("isgaseous", new ObjectFluidStackOperator.IBooleanFunction() {
        @Override
        public boolean evaluate(FluidStack fluidStack) throws EvaluationException {
            return fluidStack.getFluid().isGaseous(fluidStack);
        }
    }));

    /**
     * The rarity of the fluidstack
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_RARITY = REGISTRY.register(new ObjectFluidStackOperator("rarity", new IValueType[]{ValueTypes.OBJECT_FLUIDSTACK}, ValueTypes.STRING, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables[0].getValue()).getRawValue();
            return ValueTypeString.ValueString.of(a.isPresent() ? a.get().getFluid().getRarity(a.get()).rarityName : "");
        }
    }, IConfigRenderPattern.SUFFIX_1_LONG));

    /**
     * If the fluid types of the two given fluidstacks are equal
     */
    public static final ObjectFluidStackOperator OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL = REGISTRY.register(new ObjectFluidStackOperator("=Raw=", "israwfluidequal", new IValueType[]{ValueTypes.OBJECT_FLUIDSTACK, ValueTypes.OBJECT_FLUIDSTACK}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables[0].getValue()).getRawValue();
            Optional<FluidStack> b = ((ValueObjectTypeFluidStack.ValueFluidStack) variables[0].getValue()).getRawValue();
            boolean equal = false;
            if(a.isPresent() && b.isPresent()) {
                equal = a.get().isFluidEqual(b.get());
            } else if(!a.isPresent() && !b.isPresent()) {
                equal = true;
            }
            return ValueTypeBoolean.ValueBoolean.of(equal);
        }
    }, IConfigRenderPattern.INFIX));

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
