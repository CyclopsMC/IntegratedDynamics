package org.cyclops.integrateddynamics.core.helper.obfuscation;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Entries used for getting private fields and methods by using it in
 * {@link ReflectionHelper#getPrivateValue(Class, Object, String...)}.
 * These MCP mappings should be updated with every MC update!
 * @author rubensworks
 */
public class ObfuscationData {

    /**
     * Field from {@link net.minecraft.entity.EntityLivingBase}.
     */
    public static final String[] ENTITYLIVINGBASE_HURTSOUND = new String[] { "getHurtSound", "func_70621_aR", "aR" };

    /**
     * Field from {@link net.minecraft.entity.EntityLivingBase}.
     */
    public static final String[] ENTITYLIVINGBASE_DEATHSOUND = new String[] { "getDeathSound", "func_70673_aS", "aS" };

    /**
     * Field from {@link net.minecraft.util.SoundEvent}.
     */
    public static final String[] SOUNDEVENT_SOUNDNAME = new String[] { "soundName", "field_187506_b" };
	
}
