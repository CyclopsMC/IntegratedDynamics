package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author rubensworks
 */
public class NbtHelpers {

    /**
     * Check if the first tag is a subset of the second tag.
     * If nested tags are present, this will be checked recursively.
     * @param a An NBT tag.
     * @param b An NBT tag.
     * @return If tag a is a subset (or equal) of tag b.
     */
    public static boolean nbtMatchesSubset(NBTTagCompound a, NBTTagCompound b) {
        for (String key : a.getKeySet()) {
            NBTBase valueA = a.getTag(key);
            if (valueA instanceof NBTTagCompound) {
                NBTBase valueB = b.getTag(key);
                if (!(valueB instanceof NBTTagCompound)) {
                    return false;
                }
                NBTTagCompound tagA = (NBTTagCompound) valueA;
                NBTTagCompound tagB = (NBTTagCompound) valueB;
                return nbtMatchesSubset(tagA, tagB);
            } else {
                if (!valueA.equals(b.getTag(key))) {
                    return false;
                }
            }
        }
        return true;
    }

}
