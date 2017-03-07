package org.cyclops.integrateddynamics;

import com.google.common.base.Predicate;
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
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;
import org.cyclops.integrateddynamics.item.*;

/**
 * Obtainable achievements in this mod.
 * @author rubensworks
 *
 */
public class Achievements {

	private static final Achievements _INSTANCE = new Achievements();

	public static final Achievement MENEGLIN_DISCOVERY = new ExtendedAchievement("meneglinDiscovery", 0, 0, new ItemStack(ConfigHandler.isEnabled(BlockMenrilLogFilledConfig.class) ? BlockMenrilLogFilledConfig._instance.getBlockInstance() : Blocks.LOG), null);
	public static final Achievement SQUEEZING = new ExtendedAchievement("squeezing", -1, 1, new ItemStack(ConfigHandler.isEnabled(BlockSqueezerConfig.class) ? BlockSqueezer.getInstance() : Blocks.LOG), MENEGLIN_DISCOVERY);
	public static final Achievement DRYING = new ExtendedAchievement("drying", -1, 2, new ItemStack(ConfigHandler.isEnabled(BlockDryingBasinConfig.class) ? BlockDryingBasin.getInstance() : Blocks.LOG), SQUEEZING);
	public static final Achievement MENRIL_PRODUCTION = new ExtendedAchievement("menrilProduction", 0, 3, new ItemStack(ConfigHandler.isEnabled(BlockCrystalizedMenrilBlockConfig.class) ? BlockCrystalizedMenrilBlockConfig._instance.getBlockInstance() : Blocks.LOG), DRYING);

	public static final Achievement CABLES = new ExtendedAchievement("cablesLogic", 0, 4, new ItemStack(ConfigHandler.isEnabled(CableConfig.class) ? BlockCable.getInstance() : Blocks.LOG), MENRIL_PRODUCTION);
	public static final Achievement NETWORKS = new ExtendedAchievement("networksLogic", 1, 2, new ItemStack(ConfigHandler.isEnabled(CableConfig.class) ? BlockCable.getInstance() : Blocks.LOG), CABLES);
	public static final Achievement WRENCHING = new ExtendedAchievement("menrilWrenching", 2, 3, new ItemStack(ConfigHandler.isEnabled(ItemWrenchConfig.class) ? ItemWrench.getInstance() : Items.APPLE), MENRIL_PRODUCTION);
	public static final Achievement VARIABLES = new ExtendedAchievement("variables", 2, 2, new ItemStack(ConfigHandler.isEnabled(ItemVariableConfig.class) ? ItemVariable.getInstance() : Items.APPLE), MENRIL_PRODUCTION);
	public static final Achievement VARIABLEINPUT = new ExtendedAchievement("variableInput", 2, 1, new ItemStack(ConfigHandler.isEnabled(ItemVariableTransformerConfig.class) ? ItemVariableTransformerConfig._instance.getItemInstance() : Items.APPLE, 1, 1), MENRIL_PRODUCTION);
	public static final Achievement VARIABLEOUTPUT = new ExtendedAchievement("variableOutput", 2, 0, new ItemStack(ConfigHandler.isEnabled(ItemVariableTransformerConfig.class) ? ItemVariableTransformerConfig._instance.getItemInstance() : Items.APPLE, 1, 0), MENRIL_PRODUCTION);

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
			VARIABLEOUTPUT
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
	}

	@SubscribeEvent
	public void onCrafted(NetworkInitializedEvent event) {
		if (event.getPlacer() != null && event.getPlacer() instanceof EntityPlayer && event.getNetwork().getCablesCount() >= 10) {
			((EntityPlayer) event.getPlacer()).addStat(NETWORKS);
		}
	}
	
	static class ExtendedAchievement extends Achievement {

		public ExtendedAchievement(String id, int column, int row, ItemStack item, Achievement parent) {
			super(Reference.MOD_ID + "." + id, Reference.MOD_ID + "." + id, column, row, item, parent);
			registerStat();
		}
		
	}
	
}
