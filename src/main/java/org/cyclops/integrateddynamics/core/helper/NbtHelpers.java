package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
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
    public static boolean nbtMatchesSubset(CompoundNBT a, CompoundNBT b, boolean recursive) {
        for (String key : a.getAllKeys()) {
            INBT valueA = a.get(key);
            if (recursive && (valueA instanceof CompoundNBT || valueA instanceof ListNBT)) {
                INBT valueB = b.get(key);
                if (valueA instanceof CompoundNBT) {
                    if (!(valueB instanceof CompoundNBT)) {
                        return false;
                    }
                    CompoundNBT tagA = (CompoundNBT) valueA;
                    CompoundNBT tagB = (CompoundNBT) valueB;
                    if (!nbtMatchesSubset(tagA, tagB, recursive)) {
                        return false;
                    }
                } else if (valueA instanceof ListNBT) {
                    if (!(valueB instanceof ListNBT)) {
                        return false;
                    }
                    ListNBT tagA = (ListNBT) valueA;
                    ListNBT tagB = (ListNBT) valueB;
                    for (int i = 0; i < tagA.size(); i++) {
                        CompoundNBT subTagA = tagA.getCompound(i);
                        boolean foundA = false;
                        for (int j = 0; j < tagB.size(); j++) {
                            CompoundNBT subTagB = tagB.getCompound(j);
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
    public static CompoundNBT union(CompoundNBT... tags) {
        CompoundNBT tag = new CompoundNBT();
        for (CompoundNBT inputTag : tags) {
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
    public static CompoundNBT intersection(CompoundNBT... tags) {
        if (tags.length == 0) {
            return new CompoundNBT();
        }
        CompoundNBT tag = null;
        for (CompoundNBT inputTag : tags) {
            if (tag == null) {
                tag = inputTag.copy();
            } else {
                Set<String> keys = Sets.newHashSet(tag.getAllKeys());
                for (String key : keys) {
                    int type = tag.get(key).getId();
                    if (!inputTag.contains(key, type)) {
                        tag.remove(key);
                    } else if (type == Constants.NBT.TAG_COMPOUND) {
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
    public static CompoundNBT minus(CompoundNBT a, CompoundNBT b) {
        CompoundNBT tag = a.copy();
        for (String key : b.getAllKeys()) {
            int type = b.get(key).getId();
            if (tag.contains(key, type)) {
                if (type == Constants.NBT.TAG_COMPOUND) {
                    CompoundNBT difference = minus(tag.getCompound(key), b.getCompound(key));
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
    public static ListNBT getListNbtTag(ValueTypeList.ValueList<?, ?> value, ITextComponent operatorName) {
        ListNBT list = new ListNBT();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.NBT) {
                IFormattableTextComponent error = new TranslationTextComponent(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        new TranslationTextComponent(value.getType().getTranslationKey()),
                        1,
                        new TranslationTextComponent(ValueTypes.NBT.getTranslationKey()));
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
    public static ByteArrayNBT getListNbtByte(ValueTypeList.ValueList<?, ?> value, ITextComponent operatorName) {
        List<Byte> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                IFormattableTextComponent error = new TranslationTextComponent(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        new TranslationTextComponent(value.getType().getTranslationKey()),
                        1,
                        new TranslationTextComponent(ValueTypes.INTEGER.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add((byte) ((ValueTypeInteger.ValueInteger) valueNbt).getRawValue());
        }
        return new ByteArrayNBT(list);
    }

    /**
     * Create an NBT int array from the given integer value list.
     * @param value An integer value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT int array.
     */
    public static IntArrayNBT getListNbtInt(ValueTypeList.ValueList<?, ?> value, ITextComponent operatorName) {
        List<Integer> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                IFormattableTextComponent error = new TranslationTextComponent(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        new TranslationTextComponent(value.getType().getTranslationKey()),
                        1,
                        new TranslationTextComponent(ValueTypes.INTEGER.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add(((ValueTypeInteger.ValueInteger) valueNbt).getRawValue());
        }
        return new IntArrayNBT(list);
    }

    /**
     * Create an NBT long array from the given long value list.
     * @param value A long value list.
     * @param operatorName An operator name for error reporting.
     * @return An NBT long list.
     */
    public static LongArrayNBT getListNbtLong(ValueTypeList.ValueList<?, ?> value, ITextComponent operatorName) {
        List<Long> list = Lists.newLinkedList();
        for (IValue valueNbt : value.getRawValue()) {
            if (value.getRawValue().getValueType() != ValueTypes.LONG) {
                IFormattableTextComponent error = new TranslationTextComponent(
                        L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        operatorName,
                        new TranslationTextComponent(value.getType().getTranslationKey()),
                        1,
                        new TranslationTextComponent(ValueTypes.LONG.getTranslationKey()));
                Helpers.sneakyThrow(new EvaluationException(error));
            }
            list.add(((ValueTypeLong.ValueLong) valueNbt).getRawValue());
        }
        return new LongArrayNBT(list);
    }

}
