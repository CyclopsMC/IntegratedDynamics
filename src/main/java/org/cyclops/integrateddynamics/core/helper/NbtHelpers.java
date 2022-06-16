package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;
import java.util.Set;

/**
 * @author rubensworks
 */
public class NbtHelpers {

    /**
     * Check if the first tag is a subset of the second tag.
     * @param a An NBT tag.
     * @param b An NBT tag.
     * @param recursive If tags and list should be checked recursively. (list must be in the same order)
     * @return If tag a is a subset (or equal) of tag b.
     */
    public static boolean nbtMatchesSubset(CompoundTag a, CompoundTag b, boolean recursive) {
        for (String key : a.getAllKeys()) {
            Tag valueA = a.get(key);
            if (recursive && (valueA instanceof CompoundTag || valueA instanceof ListTag)) {
                Tag valueB = b.get(key);
                if (valueA instanceof CompoundTag) {
                    if (!(valueB instanceof CompoundTag)) {
                        return false;
                    }
                    CompoundTag tagA = (CompoundTag) valueA;
                    CompoundTag tagB = (CompoundTag) valueB;
                    if (!nbtMatchesSubset(tagA, tagB, recursive)) {
                        return false;
                    }
                } else if (valueA instanceof ListTag) {
                    if (!(valueB instanceof ListTag)) {
                        return false;
                    }
                    ListTag tagA = (ListTag) valueA;
                    ListTag tagB = (ListTag) valueB;
                    for (int i = 0; i < tagA.size(); i++) {
                        CompoundTag subTagA = tagA.getCompound(i);
                        boolean foundA = false;
                        for (int j = 0; j < tagB.size(); j++) {
                            CompoundTag subTagB = tagB.getCompound(j);
                            if (nbtMatchesSubset(subTagA, subTagB, recursive)) {
                                foundA = true;
                                break;
                            }
                        }
                        if (!foundA) {
                            return false;
                        }
                    }
                }
            } else {
                if (!valueA.equals(b.get(key))) {
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
    public static CompoundTag union(CompoundTag... tags) {
        CompoundTag tag = new CompoundTag();
        for (CompoundTag inputTag : tags) {
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
    public static CompoundTag intersection(CompoundTag... tags) {
        if (tags.length == 0) {
            return new CompoundTag();
        }
        CompoundTag tag = null;
        for (CompoundTag inputTag : tags) {
            if (tag == null) {
                tag = inputTag.copy();
            } else {
                Set<String> keys = Sets.newHashSet(tag.getAllKeys());
                for (String key : keys) {
                    int type = tag.get(key).getId();
                    if (!inputTag.contains(key, type)) {
                        tag.remove(key);
                    } else if (type == Tag.TAG_COMPOUND) {
                        tag.put(key, intersection(tag.getCompound(key), inputTag.getCompound(key)));
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
    public static CompoundTag minus(CompoundTag a, CompoundTag b) {
        CompoundTag tag = a.copy();
        for (String key : b.getAllKeys()) {
            int type = b.get(key).getId();
            if (tag.contains(key, type)) {
                if (type == Tag.TAG_COMPOUND) {
                    CompoundTag difference = minus(tag.getCompound(key), b.getCompound(key));
                    if (difference.isEmpty()) {
                        tag.remove(key);
                    } else {
                        tag.put(key, difference);
                    }
                } else {
                    tag.remove(key);
                }
            }
        }
        return tag;
    }

    /**
     * Create an NBT List from the given NBT value list.
     * @param value An NBT value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT list.
     */
    public static ListTag getListNbtTag(ValueTypeList.ValueList<?, ?> value, Component operatorName) {
        ListTag list = new ListTag();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.NBT) {
                MutableComponent error = Component.translatable(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        Component.translatable(value.getType().getTranslationKey()),
                        1,
                        Component.translatable(ValueTypes.NBT.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            ((ValueTypeNbt.ValueNbt) valueNbt).getRawValue().ifPresent(list::add);
        }
        return list;
    }

    /**
     * Create an NBT byte array from the given integer value list.
     * @param value An integer value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT byte array.
     */
    public static ByteArrayTag getListNbtByte(ValueTypeList.ValueList<?, ?> value, Component operatorName) {
        List<Byte> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                MutableComponent error = Component.translatable(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        Component.translatable(value.getType().getTranslationKey()),
                        1,
                        Component.translatable(ValueTypes.INTEGER.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add((byte) ((ValueTypeInteger.ValueInteger) valueNbt).getRawValue());
        }
        return new ByteArrayTag(list);
    }

    /**
     * Create an NBT int array from the given integer value list.
     * @param value An integer value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT int array.
     */
    public static IntArrayTag getListNbtInt(ValueTypeList.ValueList<?, ?> value, Component operatorName) {
        List<Integer> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                MutableComponent error = Component.translatable(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        Component.translatable(value.getType().getTranslationKey()),
                        1,
                        Component.translatable(ValueTypes.INTEGER.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add(((ValueTypeInteger.ValueInteger) valueNbt).getRawValue());
        }
        return new IntArrayTag(list);
    }

    /**
     * Create an NBT long array from the given long value list.
     * @param value A long value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT long list.
     */
    public static LongArrayTag getListNbtLong(ValueTypeList.ValueList<?, ?> value, Component operatorName) {
        List<Long> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.LONG) {
                MutableComponent error = Component.translatable(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        Component.translatable(value.getType().getTranslationKey()),
                        1,
                        Component.translatable(ValueTypes.LONG.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add(((ValueTypeLong.ValueLong) valueNbt).getRawValue());
        }
        return new LongArrayTag(list);
    }

}
