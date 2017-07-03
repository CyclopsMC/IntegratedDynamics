package org.cyclops.integrateddynamics.core.helper.obfuscation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
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
     * Call the the {@link EntityLivingBase#getHurtSound(DamageSource)} ()}.
     * @param entity The entity.
     * @param damageSource The damage source.
     * @return The hurt sound.
     */
    public static SoundEvent getEntityLivingBaseHurtSound(EntityLivingBase entity, DamageSource damageSource) {
        Method method = ReflectionHelper.findMethod(EntityLivingBase.class, ObfuscationData.ENTITYLIVINGBASE_HURTSOUND[0], ObfuscationData.ENTITYLIVINGBASE_HURTSOUND[1], DamageSource.class);
        try {
            return (SoundEvent) method.invoke(entity, damageSource);
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
        Method method = ReflectionHelper.findMethod(EntityLivingBase.class, ObfuscationData.ENTITYLIVINGBASE_DEATHSOUND[0], ObfuscationData.ENTITYLIVINGBASE_DEATHSOUND[1]);
        try {
            return (SoundEvent) method.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
