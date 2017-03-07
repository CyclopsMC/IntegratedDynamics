package org.cyclops.integrateddynamics;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.player.ItemCraftedAchievements;
import org.cyclops.integrateddynamics.block.*;

/**
 * Obtainable achievements in this mod.
 * @author rubensworks
 *
 */
public class Achievements {

	public static final Achievement MENEGLIN_DISCOVERY = new ExtendedAchievement("meneglinDiscovery", 0, 0, new ItemStack(ConfigHandler.isEnabled(BlockMenrilLogFilledConfig.class) ? BlockMenrilLogFilledConfig._instance.getBlockInstance() : Blocks.LOG), null);
	public static final Achievement SQUEEZING = new ExtendedAchievement("squeezing", 0, 1, new ItemStack(ConfigHandler.isEnabled(BlockSqueezerConfig.class) ? BlockSqueezer.getInstance() : Blocks.LOG), MENEGLIN_DISCOVERY);
	public static final Achievement DRYING = new ExtendedAchievement("drying", 0, 2, new ItemStack(ConfigHandler.isEnabled(BlockDryingBasinConfig.class) ? BlockDryingBasin.getInstance() : Blocks.LOG), SQUEEZING);
	public static final Achievement MENRIL_PRODUCTION = new ExtendedAchievement("menrilProduction", 0, 3, new ItemStack(ConfigHandler.isEnabled(BlockCrystalizedMenrilBlockConfig.class) ? BlockCrystalizedMenrilBlockConfig._instance.getBlockInstance() : Blocks.LOG), DRYING);

    private static final Achievement[] ACHIEVEMENTS = {
			MENEGLIN_DISCOVERY,
			SQUEEZING,
			DRYING,
			MENRIL_PRODUCTION
	};
	
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
	}
	
	static class ExtendedAchievement extends Achievement {

		public ExtendedAchievement(String id, int column, int row, ItemStack item, Achievement parent) {
			super(Reference.MOD_ID + "." + id, Reference.MOD_ID + "." + id, column, row, item, parent);
			registerStat();
		}
		
	}
	
}
