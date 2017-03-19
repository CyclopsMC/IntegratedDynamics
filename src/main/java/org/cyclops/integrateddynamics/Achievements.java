package org.cyclops.integrateddynamics;

import com.google.common.base.Predicate;
import net.minecraft.block.BlockSponge;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.player.ItemCraftedAchievements;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.logicprogrammer.event.LogicProgrammerVariableFacadeCreatedEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;
import org.cyclops.integrateddynamics.item.*;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Obtainable achievements in this mod.
 * @author rubensworks
 *
 */
public class Achievements {

	private static final Achievements _INSTANCE = new Achievements();

	public static final Achievement MENEGLIN_DISCOVERY = new ExtendedAchievement("meneglinDiscovery", -1, 0, new ItemStack(ConfigHandler.isEnabled(BlockMenrilLogFilledConfig.class) ? BlockMenrilLogFilledConfig._instance.getBlockInstance() : Blocks.LOG), null);
	public static final Achievement SQUEEZING = new ExtendedAchievement("squeezing", -1, 1, new ItemStack(ConfigHandler.isEnabled(BlockSqueezerConfig.class) ? BlockSqueezer.getInstance() : Blocks.LOG), MENEGLIN_DISCOVERY);
	public static final Achievement DRYING = new ExtendedAchievement("drying", -1, 2, new ItemStack(ConfigHandler.isEnabled(BlockDryingBasinConfig.class) ? BlockDryingBasin.getInstance() : Blocks.LOG), SQUEEZING);
	public static final Achievement MENRIL_PRODUCTION = new ExtendedAchievement("menrilProduction", 0, 3, new ItemStack(ConfigHandler.isEnabled(BlockCrystalizedMenrilBlockConfig.class) ? BlockCrystalizedMenrilBlockConfig._instance.getBlockInstance() : Blocks.LOG), DRYING);

	public static final Achievement CABLES = new ExtendedAchievement("cablesLogic", 0, 4, new ItemStack(ConfigHandler.isEnabled(CableConfig.class) ? BlockCable.getInstance() : Blocks.LOG), MENRIL_PRODUCTION);
	public static final Achievement NETWORKS = new ExtendedAchievement("networksLogic", 1, 2, new ItemStack(ConfigHandler.isEnabled(CableConfig.class) ? BlockCable.getInstance() : Blocks.LOG), CABLES);
	public static final Achievement WRENCHING = new ExtendedAchievement("menrilWrenching", 2, 3, new ItemStack(ConfigHandler.isEnabled(ItemWrenchConfig.class) ? ItemWrench.getInstance() : Items.APPLE), MENRIL_PRODUCTION);
	public static final Achievement VARIABLES = new ExtendedAchievement("variables", 2, 2, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), MENRIL_PRODUCTION);
	public static final Achievement VARIABLEINPUT = new ExtendedAchievement("variableInput", 2, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableTransformerConfig.class) ? ItemVariableTransformerConfig._instance.getItemInstance() : Items.APPLE, 1, 1), MENRIL_PRODUCTION);
	public static final Achievement VARIABLEOUTPUT = new ExtendedAchievement("variableOutput", 3, 0, new ItemStack(ConfigHandler.isEnabled(ItemVariableTransformerConfig.class) ? ItemVariableTransformerConfig._instance.getItemInstance() : Items.APPLE, 1, 0), MENRIL_PRODUCTION);

	public static final Achievement REDSTONE_READING = new ExtendedAchievement("redstoneReading", 1, -1, new ItemStack(PartTypes.REDSTONE_READER.getItem()), VARIABLEINPUT);
	public static final Achievement BLOCK_READING = new ExtendedAchievement("blockReading", 1, -2, new ItemStack(PartTypes.BLOCK_READER.getItem()), VARIABLEINPUT);
	public static final Achievement INVENTORY_READING = new ExtendedAchievement("inventoryReading", 1, -3, new ItemStack(PartTypes.INVENTORY_READER.getItem()), VARIABLEINPUT);
	public static final Achievement VALUE_DISPLAYING = new ExtendedAchievement("valueDisplaying", 4, -1, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()), VARIABLEOUTPUT);

	public static final Achievement REDSTONE_CAPTURING = new ExtendedAchievement("redstoneCapturing", 0, -2, new ItemStack(PartTypes.REDSTONE_READER.getItem()), REDSTONE_READING);
	public static final Achievement REDSTONE_OBSERVEMENT= new ExtendedAchievement("redstoneObservement", -1, -2, new ItemStack(PartTypes.REDSTONE_READER.getItem()), REDSTONE_READING);
	public static final Achievement REDSTONE_TRANSMISSION = new ExtendedAchievement("redstoneTransmission", -2, -2, new ItemStack(PartTypes.REDSTONE_READER.getItem()), REDSTONE_READING);

	public static final Achievement LOGIC_PROGRAMMING = new ExtendedAchievement("logicProgramming", 4, 2, new ItemStack(ConfigHandler.isEnabled(BlockLogicProgrammerConfig.class) ? BlockLogicProgrammer.getInstance() : Blocks.CRAFTING_TABLE), VARIABLES);
	public static final Achievement CONSTANT_DEFINITION = new ExtendedAchievement("constantDefinition", 4, 3, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), VARIABLES);
	public static final Achievement ARITHMETIC_ADDITION = new ExtendedAchievement("arithmeticAddition", 4, 4, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), VARIABLES);

	public static final Achievement VARIABLE_MATERIALIZATION = new ExtendedAchievement("variableMaterialization", 4, 5, new ItemStack(ConfigHandler.isEnabled(BlockMaterializerConfig.class) ? BlockMaterializer.getInstance() : Blocks.CRAFTING_TABLE), VARIABLES);
	public static final Achievement VARIABLE_PROXYING = new ExtendedAchievement("variableProxying", 4, 6, new ItemStack(ConfigHandler.isEnabled(BlockProxyConfig.class) ? BlockProxy.getInstance() : Blocks.CRAFTING_TABLE), VARIABLES);

	public static final Achievement LOGICAL_LIST_BUILDING = new ExtendedAchievement("logicalListBuilding", 5, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), LOGIC_PROGRAMMING);
	public static final Achievement ITEM_ORIGIN_IDENTIFICATION = new ExtendedAchievement("itemOriginIdentification", 6, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), LOGIC_PROGRAMMING);
	public static final Achievement WHAT_WOULD_I_BE_LOOKING_AT = new ExtendedAchievement("whatWouldIBeLookingAt", 7, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), LOGIC_PROGRAMMING);

	public static final Achievement DYNAMIC_ADDITIONS = new ExtendedAchievement("dynamicAdditions", 8, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), LOGIC_PROGRAMMING);
	public static final Achievement DYNAMIC_LIST_FILTERING = new ExtendedAchievement("dynamicListFiltering", 9, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), LOGIC_PROGRAMMING);

	public static final Achievement CREEPER_TAMING = new ExtendedAchievement("creeperTaming", 8, 2, new ItemStack(Items.SKULL, 1, 4), WHAT_WOULD_I_BE_LOOKING_AT).setSpecial();
	public static final Achievement SPONGE_STEP_SOUND = new ExtendedAchievement("spongeStepSound", 8, 3, new ItemStack(Blocks.SPONGE, 1, 1), WHAT_WOULD_I_BE_LOOKING_AT).setSpecial();
	public static final Achievement RECURSIVE_RECURSION = new ExtendedAchievement("recursiveRecursion", 6, 2, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), WHAT_WOULD_I_BE_LOOKING_AT).setSpecial();
	public static final Achievement TOOL_FOR_OBSIDIAN = new ExtendedAchievement("toolForObsidian", 6, 3, new ItemStack(Blocks.OBSIDIAN), WHAT_WOULD_I_BE_LOOKING_AT).setSpecial();
	public static final Achievement SMART_PRESSURE_PLATE = new ExtendedAchievement("smartPressurePlate", 7, 4, new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), WHAT_WOULD_I_BE_LOOKING_AT).setSpecial();

    private static final Achievement[] ACHIEVEMENTS = {
			MENEGLIN_DISCOVERY,
			SQUEEZING,
			DRYING,
			MENRIL_PRODUCTION,

			CABLES,
			NETWORKS,
			WRENCHING,
			VARIABLES,
			VARIABLEINPUT,
			VARIABLEOUTPUT,

			REDSTONE_READING,
			BLOCK_READING,
			INVENTORY_READING,
			VALUE_DISPLAYING,

			REDSTONE_CAPTURING,
			REDSTONE_OBSERVEMENT,
			REDSTONE_TRANSMISSION,

			LOGIC_PROGRAMMING,
			CONSTANT_DEFINITION,
			ARITHMETIC_ADDITION,

			VARIABLE_MATERIALIZATION,
			VARIABLE_PROXYING,

			LOGICAL_LIST_BUILDING,
			ITEM_ORIGIN_IDENTIFICATION,
			WHAT_WOULD_I_BE_LOOKING_AT,

			DYNAMIC_ADDITIONS,
			DYNAMIC_LIST_FILTERING,

			CREEPER_TAMING,
			SPONGE_STEP_SOUND,
			RECURSIVE_RECURSION,
			TOOL_FOR_OBSIDIAN,
			SMART_PRESSURE_PLATE
	};

	private Achievements() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/**
	 * Register the achievements.
	 */
	public static void registerAchievements() {
		AchievementPage.registerAchievementPage(new AchievementPage(Reference.MOD_NAME, ACHIEVEMENTS));

		if (ConfigHandler.isEnabled(BlockSqueezerConfig.class)) {
			ItemCraftedAchievements.register(Item.getItemFromBlock(BlockSqueezer.getInstance()), SQUEEZING);
		}
		if (ConfigHandler.isEnabled(BlockDryingBasinConfig.class)) {
			ItemCraftedAchievements.register(Item.getItemFromBlock(BlockDryingBasin.getInstance()), DRYING);
		}

		if (ConfigHandler.isEnabled(CableConfig.class)) {
			ItemCraftedAchievements.register(Item.getItemFromBlock(BlockCable.getInstance()), CABLES);
		}
		if (ConfigHandler.isEnabled(ItemWrenchConfig.class)) {
			ItemCraftedAchievements.register(ItemWrench.getInstance(), WRENCHING);
		}
		if (ConfigHandler.isEnabled(ItemVariableConfig.class)) {
			ItemCraftedAchievements.register(ItemVariable.getInstance(), VARIABLES);
		}
		if (ConfigHandler.isEnabled(ItemVariableTransformerConfig.class)) {
			ItemCraftedAchievements.register(ItemVariableTransformerConfig._instance.getItemInstance(), VARIABLEINPUT, new Predicate<ItemStack>() {
				@Override
				public boolean apply(ItemStack input) {
					return input.getMetadata() == 1;
				}
			});
		}
		if (ConfigHandler.isEnabled(ItemVariableTransformerConfig.class)) {
			ItemCraftedAchievements.register(ItemVariableTransformerConfig._instance.getItemInstance(), VARIABLEOUTPUT, new Predicate<ItemStack>() {
				@Override
				public boolean apply(ItemStack input) {
					return input.getMetadata() == 0;
				}
			});
		}

		ItemCraftedAchievements.register(PartTypes.REDSTONE_READER.getItem(), REDSTONE_READING);
		ItemCraftedAchievements.register(PartTypes.BLOCK_READER.getItem(), BLOCK_READING);
		ItemCraftedAchievements.register(PartTypes.INVENTORY_READER.getItem(), INVENTORY_READING);
		ItemCraftedAchievements.register(PartTypes.DISPLAY_PANEL.getItem(), VALUE_DISPLAYING);
	}

	@SubscribeEvent
	public void onCrafted(NetworkInitializedEvent event) {
		if (event.getPlacer() != null && event.getPlacer() instanceof EntityPlayer && event.getNetwork().getCablesCount() >= 10) {
			((EntityPlayer) event.getPlacer()).addStat(NETWORKS);
		}
	}

	@SubscribeEvent
	public void onPartReaderAspect(PartReaderAspectEvent event) {
		if (event.getPartType() == PartTypes.REDSTONE_READER
				&& event.getAspect() == Aspects.Read.Redstone.INTEGER_VALUE
				&& event.getEntityPlayer() != null) {
			event.getEntityPlayer().addStat(REDSTONE_CAPTURING);
		}
	}

	@SubscribeEvent
	public void onPartVariableDrivenUpdateEvent(PartVariableDrivenVariableContentsUpdatedEvent event) {
		if (event.getPartType() == PartTypes.DISPLAY_PANEL && event.getEntityPlayer() != null && event.getValue() != null) {
			if (event.getVariable() instanceof IAspectVariable
					&& ((IAspectVariable) event.getVariable()).getAspect() == Aspects.Read.Redstone.INTEGER_VALUE) {
				event.getEntityPlayer().addStat(REDSTONE_OBSERVEMENT);
			} else if (event.getVariable() instanceof LazyExpression) {
				if (((LazyExpression) event.getVariable()).getOperator() == Operators.ARITHMETIC_ADDITION) {
					event.getEntityPlayer().addStat(ARITHMETIC_ADDITION);
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OBJECT_ITEMSTACK_MODNAME) {
					event.getEntityPlayer().addStat(ITEM_ORIGIN_IDENTIFICATION);
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OBJECT_PLAYER_TARGETBLOCK) {
					event.getEntityPlayer().addStat(WHAT_WOULD_I_BE_LOOKING_AT);
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OPERATOR_APPLY) {
					if (((LazyExpression) event.getVariable()).getInput().length == 2) {
						IVariable variable = ((LazyExpression) event.getVariable()).getInput()[0];
						if (variable instanceof LazyExpression
								&& ((LazyExpression) variable).getOperator() == Operators.OPERATOR_APPLY) {
							try {
								IValue value = event.getVariable().getValue();
								if (value.getType() == ValueTypes.INTEGER) {
									event.getEntityPlayer().addStat(DYNAMIC_ADDITIONS);
								}
							} catch (EvaluationException e) {}
						}
					}
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OPERATOR_FILTER) {
					try {
						IValue value = event.getVariable().getValue();
						if (value.getType() == ValueTypes.LIST
								&& ((ValueTypeList.ValueList) value).getRawValue().getLength() == 1
								&& ((ValueTypeList.ValueList) value).getRawValue().get(0).getType() == ValueTypes.INTEGER
								&& ((ValueTypeInteger.ValueInteger) ((ValueTypeList.ValueList) value).getRawValue().get(0)).getRawValue() == 10) {
							event.getEntityPlayer().addStat(DYNAMIC_LIST_FILTERING);
                        }
					} catch (EvaluationException e) {}
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OBJECT_ENTITY_HEALTH) {
					try {
						IValue value0 = ((LazyExpression) event.getVariable()).getInput()[0].getValue();
						if (value0.getType() == ValueTypes.OBJECT_ENTITY
								&& ((ValueObjectTypeEntity.ValueEntity) value0).getRawValue().isPresent()
								&& ((ValueObjectTypeEntity.ValueEntity) value0).getRawValue().get() instanceof EntityCreeper) {
							event.getEntityPlayer().addStat(CREEPER_TAMING);
						}
					} catch (EvaluationException e) {}
				} else if (((LazyExpression) event.getVariable()).getOperator() == Operators.OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK) {
					try {
						IValue value0 = ((LazyExpression) event.getVariable()).getInput()[0].getValue();
						IValue value1 = ((LazyExpression) event.getVariable()).getInput()[1].getValue();
						if (value0.getType() == ValueTypes.OBJECT_ITEMSTACK
								&& ((ValueObjectTypeItemStack.ValueItemStack) value0).getRawValue().isPresent()
								&& ((ValueObjectTypeItemStack.ValueItemStack) value0).getRawValue().get().getItem() == Items.STONE_PICKAXE
								&& value1.getType() == ValueTypes.OBJECT_BLOCK
								&& ((ValueObjectTypeBlock.ValueBlock) value1).getRawValue().isPresent()
								&& ((ValueObjectTypeBlock.ValueBlock) value1).getRawValue().get().getBlock() == Blocks.OBSIDIAN) {
							event.getEntityPlayer().addStat(TOOL_FOR_OBSIDIAN);
						}
					} catch (EvaluationException e) {}
				} else if (event.getVariable().getType() == ValueTypes.LIST) {
					try {
						IValue value0 = event.getVariable().getValue();
						if (((ValueTypeList.ValueList) value0).getRawValue().isInfinite()) {
							event.getEntityPlayer().addStat(RECURSIVE_RECURSION);
						}
					} catch (EvaluationException e) {
						e.printStackTrace();
					}
				}
			} else if (event.getVariable() instanceof Variable
					&& event.getVariable().getType() == ValueTypes.LIST) {
				IValueTypeListProxy list = ((ValueTypeList.ValueList) event.getValue()).getRawValue();
				try {
					if (list.getLength() == 3
							&& list.getValueType() == ValueTypes.INTEGER
							&& ((ValueTypeInteger.ValueInteger) list.get(0)).getRawValue() == 1
							&& ((ValueTypeInteger.ValueInteger) list.get(1)).getRawValue() == 10
							&& ((ValueTypeInteger.ValueInteger) list.get(2)).getRawValue() == 100) {
						event.getEntityPlayer().addStat(LOGICAL_LIST_BUILDING);
					} else if (list.isInfinite()) {
						event.getEntityPlayer().addStat(RECURSIVE_RECURSION);
					}
				} catch (EvaluationException e) {}
			}
		}
	}

	@SubscribeEvent
	public void onPartWriterAspect(PartWriterAspectEvent event) {
		try {
			IVariable variable = ((IPartStateWriter) event.getPartState()).getVariable(event.getPartNetwork());
			if (event.getPartType() == PartTypes.REDSTONE_WRITER
					&& event.getEntityPlayer() != null) {
				if (((event.getAspect() == Aspects.Write.Redstone.INTEGER
						&& variable.getValue() instanceof ValueTypeInteger.ValueInteger
						&& ((ValueTypeInteger.ValueInteger) variable.getValue()).getRawValue() >= 15)
						|| (event.getAspect() == Aspects.Write.Redstone.INTEGER)
						&& variable.getValue() instanceof ValueTypeBoolean.ValueBoolean
						&& ((ValueTypeBoolean.ValueBoolean) variable.getValue()).getRawValue())) {
					event.getEntityPlayer().addStat(REDSTONE_TRANSMISSION);
				} else if (event.getAspect() == Aspects.Write.Redstone.BOOLEAN
						&& variable.getValue() instanceof ValueTypeBoolean.ValueBoolean
						&& variable instanceof LazyExpression
						&& ((LazyExpression) variable).getOperator() == Operators.RELATIONAL_EQUALS) {
					IVariable var0 = ((LazyExpression) variable).getInput()[0];
					IVariable var1 = ((LazyExpression) variable).getInput()[1];
					IVariable varName, varCheck;
					// Check which of the variables in the == operator is used for the static and dynamic name
					if (var0 instanceof Variable) {
						varName = var0;
						varCheck = var1;
					} else {
						varName = var1;
						varCheck = var0;
					}
					// The player name must equal the one of the current player,
					// and the other variable must be read using an entity reader.
					if (varName.getType() == ValueTypes.STRING
							&& event.getEntityPlayer().getName()
								.equals(((ValueTypeString.ValueString) varName.getValue()).getRawValue())
							&& varCheck instanceof LazyExpression
							&& ((LazyExpression) varCheck).getInput()[0] instanceof IAspectVariable
							&& ((IAspectVariable) ((LazyExpression) varCheck).getInput()[0]).getAspect() == Aspects.Read.Entity.ENTITY) {
						event.getEntityPlayer().addStat(SMART_PRESSURE_PLATE);
					}
				}
			} else if (event.getPartType() == PartTypes.AUDIO_WRITER
					&& event.getAspect() == Aspects.Write.Audio.STRING_SOUND
					&& event.getEntityPlayer() != null
					&& ((LazyExpression) variable).getOperator() == Operators.OBJECT_BLOCK_STEPSOUND) {
				IValue value0 = ((LazyExpression) variable).getInput()[0].getValue();
				if (value0.getType() == ValueTypes.OBJECT_BLOCK
						&& ((ValueObjectTypeBlock.ValueBlock) value0).getRawValue().isPresent()
						&& ((ValueObjectTypeBlock.ValueBlock) value0).getRawValue().get().getValue(BlockSponge.WET)) {
					event.getEntityPlayer().addStat(SPONGE_STEP_SOUND);
				}
			}
		} catch (EvaluationException e) {

		}
	}

	@SubscribeEvent
	public void onVariableFacadeCreated(LogicProgrammerVariableFacadeCreatedEvent event) {
		if (event.getBlock() == BlockLogicProgrammer.getInstance()
				&& event.getVariableFacade() instanceof IValueTypeVariableFacade
				&& ((IValueTypeVariableFacade) event.getVariableFacade()).getValueType() == ValueTypes.INTEGER) {
			event.getPlayer().addStat(CONSTANT_DEFINITION);
		} else if (event.getBlock() == BlockMaterializer.getInstance()) {
			event.getPlayer().addStat(VARIABLE_MATERIALIZATION);
		} else if (event.getBlock() == BlockProxy.getInstance()
				&& event.getVariableFacade() instanceof IProxyVariableFacade) {
			event.getPlayer().addStat(VARIABLE_PROXYING);
		}
	}
	
	static class ExtendedAchievement extends Achievement {

		public ExtendedAchievement(String id, int column, int row, ItemStack item, Achievement parent) {
			super(Reference.MOD_ID + "." + id, Reference.MOD_ID + "." + id, column, row, item, parent);
			registerStat();
		}
		
	}
	
}
