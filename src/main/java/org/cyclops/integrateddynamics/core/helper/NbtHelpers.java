package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

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

    /**
     * Create a new NBT tag that contains all entries from the given tags.
     * If multiple tags contain the same entry, the entry from the latest tag will be given preference.
     * If nested tags are present, these will be combined recursively.
     * @param tags NBT tags.
     * @return A new tag containing the combined entries from the given tags.
     */
    public static NBTTagCompound union(NBTTagCompound... tags) {
        NBTTagCompound tag = new NBTTagCompound();
        for (NBTTagCompound inputTag : tags) {
            tag.merge(inputTag);
        }
        return tag;
    }

    /**
     * Create a new NBT tag that contains the entries that are present in all given tags.
     * If nested tags are present, these will be intersected recursively.
     * @param tags NBT tags.
     * @return A new tag containing the intersected entries from the given tags.
     */
    public static NBTTagCompound intersection(NBTTagCompound... tags) {
        if (tags.length == 0) {
            return new NBTTagCompound();
        }
        NBTTagCompound tag = null;
        for (NBTTagCompound inputTag : tags) {
            if (tag == null) {
                tag = inputTag.copy();
            } else {
                Set<String> keys = Sets.newHashSet(tag.getKeySet());
                for (String key : keys) {
                    int type = tag.getTag(key).getId();
                    if (!inputTag.hasKey(key, type)) {
                        tag.removeTag(key);
                    } else if (type == Constants.NBT.TAG_COMPOUND) {
                        tag.setTag(key, intersection(tag.getCompoundTag(key), inputTag.getCompoundTag(key)));
                    }
                }
            }
        }
        return tag;
    }

    /**
     * Create a new NBT tag that contains all entries of the first tag minus the entries of the second tag.
     * If nested tags are present, these will be operated recursively.
     * @param a an NBT tag.
     * @param b an NBT tag.
     * @return A new tag containing the entries of a minus b.
     */
    public static NBTTagCompound minus(NBTTagCompound a, NBTTagCompound b) {
        NBTTagCompound tag = a.copy();
        for (String key : b.getKeySet()) {
            int type = b.getTag(key).getId();
            if (tag.hasKey(key, type)) {
                if (type == Constants.NBT.TAG_COMPOUND) {
                    NBTTagCompound difference = minus(tag.getCompoundTag(key), b.getCompoundTag(key));
                    if (difference.hasNoTags()) {
                        tag.removeTag(key);
                    } else {
                        tag.setTag(key, difference);
                    }
                } else {
                    tag.removeTag(key);
                }
            }
        }
        return tag;
    }

}
