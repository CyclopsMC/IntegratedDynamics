package org.cyclops.integrateddynamics.core.helper.obfuscation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper for getting private fields or methods.
 * @author rubensworks
 *
 */
public class ObfuscationHelpers {

    /**
     * Call the the {@link EntityLivingBase#getHurtSound()}.
     * @param entity The entity.
     * @return The hurt sound.
     */
    public static SoundEvent getEntityLivingBaseHurtSound(EntityLivingBase entity) {
        Method method = ReflectionHelper.findMethod(EntityLivingBase.class, entity, ObfuscationData.ENTITYLIVINGBASE_HURTSOUND);
        try {
            return (SoundEvent) method.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Call the the {@link EntityLivingBase#getDeathSound()} ()}.
     * @param entity The entity.
     * @return The death sound.
     */
    public static SoundEvent getEntityLivingBaseDeathSound(EntityLivingBase entity) {
        Method method = ReflectionHelper.findMethod(EntityLivingBase.class, entity, ObfuscationData.ENTITYLIVINGBASE_DEATHSOUND);
        try {
            return (SoundEvent) method.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
