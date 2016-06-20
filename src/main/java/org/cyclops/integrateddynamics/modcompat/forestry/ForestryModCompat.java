package org.cyclops.integrateddynamics.modcompat.forestry;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockMenrilLogConfig;
import org.cyclops.integrateddynamics.block.BlockMenrilSaplingConfig;

/**
 * Compatibility plugin for Forestry.
 * @author rubensworks
 *
 */
public class ForestryModCompat implements IModCompat {

    @Override
    public String getModID() {
       return Reference.MOD_FORESTRY;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.INIT) {
	        // Register the Undead Sapling.
	        if(ConfigHandler.isEnabled(BlockMenrilSaplingConfig.class)) {
	            FMLInterModComms.sendMessage(getModID(), "add-farmable-sapling",
						"farmArboreal@" + Block.REGISTRY.getNameForObject(BlockMenrilSaplingConfig._instance.getBlockInstance()).toString() + ".0");
	        }
	        
	        // Add undead clog to forester backpack.
	        if(ConfigHandler.isEnabled(BlockMenrilLogConfig.class)) {
	            FMLInterModComms.sendMessage(getModID(), "add-backpack-items",
						"forester@" + Block.REGISTRY.getNameForObject(BlockMenrilLogConfig._instance.getBlockInstance()).toString() + ":*");
	        }
    	} else if(step == Step.POSTINIT) {
			ForestryRecipeManager.register();
		}
    }
    
    @Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Squeezer and backpack support.";
	}

}
