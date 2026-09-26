package org.cyclops.integrateddynamics.core.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Helpers for copying materialized values to, and pasting them from, the clipboard.
 *
 * Values are shared as a single SNBT compound, optionally gzipped and base64-encoded behind a prefix.
 * Only materialized values can be pasted, as other values refer to state that is meaningless,
 * or even abusable, in another world.
 *
 * @author rubensworks
 */
public class VariableClipboardHelpers {

    /**
     * The version of the clipboard format.
     */
    public static final int VERSION = 1;

    /**
     * The prefix that marks a gzipped and base64-encoded payload.
     */
    public static final String PREFIX_COMPRESSED = "idynvar" + VERSION + ":";

    /**
     * The maximum nesting depth of a payload, to bound the recursion over handcrafted payloads.
     */
    public static final int MAX_DEPTH = 512;

    public static final String KEY_VERSION = "version";
    public static final String KEY_PROXY_NAME = "proxyName";
    public static final String KEY_SERIALIZER = "serializer";

    /**
     * Serialize the given value to a versioned tag, as it is sent over the network.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value The value to serialize.
     * @return The serialized value.
     */
    public static CompoundTag serializeToTag(ValueDeseralizationContext valueDeseralizationContext, IValue value) {
        CompoundTag tag = ValueHelpers.serialize(valueDeseralizationContext, value);
        tag.putInt(KEY_VERSION, VERSION);
        return tag;
    }

    /**
     * Format the given serialized value as a clipboard string.
     * @param tag A serialized value.
     * @param compressed If the compressed form must be used instead of the human-readable one.
     * @return The clipboard string.
     * @throws VariableClipboardException If the value could not be compressed.
     */
    public static String format(CompoundTag tag, boolean compressed) throws VariableClipboardException {
        String snbt = tag.toString();
        return compressed ? compress(snbt) : snbt;
    }

    /**
     * Serialize the given value to a human-readable SNBT string.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value The value to serialize.
     * @return The serialized value.
     */
    public static String serialize(ValueDeseralizationContext valueDeseralizationContext, IValue value) {
        return serializeToTag(valueDeseralizationContext, value).toString();
    }

    /**
     * Serialize the given value to a compressed single-line string.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value The value to serialize.
     * @return The serialized value.
     * @throws VariableClipboardException If the value could not be compressed.
     */
    public static String serializeCompressed(ValueDeseralizationContext valueDeseralizationContext, IValue value)
            throws VariableClipboardException {
        return compress(serialize(valueDeseralizationContext, value));
    }

    protected static String compress(String snbt) throws VariableClipboardException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(snbt.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
        return PREFIX_COMPRESSED + Base64.getEncoder().encodeToString(output.toByteArray());
    }

    /**
     * Deserialize a value from the given clipboard string.
     * Both the human-readable and the compressed form are accepted.
     * @param valueDeseralizationContext A value deserialization context.
     * @param input A clipboard string.
     * @param maxLength The maximum length of the uncompressed payload.
     * @return The value.
     * @throws VariableClipboardException If the string does not hold a valid materialized value.
     */
    public static IValue deserialize(ValueDeseralizationContext valueDeseralizationContext, String input, int maxLength)
            throws VariableClipboardException {
        return deserialize(valueDeseralizationContext, parse(input, maxLength), maxLength);
    }

    /**
     * Parse the given clipboard string into a serialized value.
     * Both the human-readable and the compressed form are accepted.
     * @param input A clipboard string.
     * @param maxLength The maximum length of the uncompressed payload.
     * @return The serialized value.
     * @throws VariableClipboardException If the string is malformed or too large.
     */
    public static CompoundTag parse(String input, int maxLength) throws VariableClipboardException {
        try {
            return TagParser.parseTag(toSnbt(input, maxLength));
        } catch (CommandSyntaxException e) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
    }

    /**
     * Deserialize a value from the given serialized tag.
     * @param valueDeseralizationContext A value deserialization context.
     * @param tag A serialized value.
     * @param maxLength The maximum length of the uncompressed payload.
     * @return The value.
     * @throws VariableClipboardException If the tag does not hold a valid materialized value.
     */
    public static IValue deserialize(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, int maxLength)
            throws VariableClipboardException {
        checkLength(tag.toString().length(), maxLength);

        int version = tag.getInt(KEY_VERSION);
        if (version > VERSION) {
            throw new VariableClipboardException(Component.translatable(
                    L10NValues.VARIABLE_CLIPBOARD_ERROR_VERSION, version, VERSION));
        }

        validateMaterialized(tag);

        IValue value;
        try {
            value = ValueHelpers.deserialize(valueDeseralizationContext, tag);
        } catch (IllegalArgumentException e) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
        if (value == null) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_VALUETYPE,
                    tag.getString("valueType")));
        }
        return value;
    }

    /**
     * @param input A clipboard string.
     * @param maxLength The maximum length of the uncompressed payload.
     * @return The uncompressed SNBT payload of the given clipboard string.
     * @throws VariableClipboardException If the string is malformed or too large.
     */
    protected static String toSnbt(String input, int maxLength) throws VariableClipboardException {
        String trimmed = input.trim();
        if (trimmed.startsWith(PREFIX_COMPRESSED)) {
            return decompress(trimmed.substring(PREFIX_COMPRESSED.length()), maxLength);
        }
        if (!trimmed.startsWith("{")) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
        checkLength(trimmed.length(), maxLength);
        return trimmed;
    }

    protected static String decompress(String base64, int maxLength) throws VariableClipboardException {
        byte[] compressed;
        try {
            compressed = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }

        // Read at most one byte beyond the maximum, so that a small payload can not expand into a huge one.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            int read;
            while ((read = gzip.read(buffer)) > 0) {
                output.write(buffer, 0, read);
                checkLength(output.size(), maxLength);
            }
        } catch (IOException e) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    protected static void checkLength(int length, int maxLength) throws VariableClipboardException {
        if (length > maxLength) {
            throw new VariableClipboardException(Component.translatable(
                    L10NValues.VARIABLE_CLIPBOARD_ERROR_TOOLARGE, length, maxLength));
        }
    }

    /**
     * Check that the given tag only holds materialized state.
     *
     * Non-materialized values refer to a position in a world, which would allow a handcrafted payload
     * to read blocks that the player has no access to.
     *
     * @param tag A serialized value.
     * @throws VariableClipboardException If the tag holds anything that is not materialized.
     */
    public static void validateMaterialized(Tag tag) throws VariableClipboardException {
        validateMaterialized(tag, 0);
    }

    protected static void validateMaterialized(Tag tag, int depth) throws VariableClipboardException {
        if (depth > MAX_DEPTH) {
            throw new VariableClipboardException(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_PARSE));
        }
        if (tag instanceof CompoundTag compoundTag) {
            if (compoundTag.contains(KEY_PROXY_NAME, Tag.TAG_STRING)) {
                String proxyName = compoundTag.getString(KEY_PROXY_NAME);
                IValueTypeListProxyFactoryTypeRegistry.IProxyFactory factory = ValueTypeListProxyFactories.REGISTRY
                        .getFactory(parseName(proxyName));
                if (factory == null || !factory.isMaterialized()) {
                    throw new VariableClipboardException(Component.translatable(
                            L10NValues.VARIABLE_CLIPBOARD_ERROR_UNMATERIALIZED, proxyName));
                }
            }
            if (compoundTag.contains(KEY_SERIALIZER, Tag.TAG_STRING)) {
                String serializerName = compoundTag.getString(KEY_SERIALIZER);
                IOperatorSerializer serializer = Operators.REGISTRY
                        .getSerializer(parseName(serializerName));
                if (serializer == null || !serializer.isMaterialized()) {
                    throw new VariableClipboardException(Component.translatable(
                            L10NValues.VARIABLE_CLIPBOARD_ERROR_UNMATERIALIZED, serializerName));
                }
            }
            for (String key : compoundTag.getAllKeys()) {
                validateMaterialized(compoundTag.get(key), depth + 1);
            }
        } else if (tag instanceof ListTag listTag) {
            for (Tag subTag : listTag) {
                validateMaterialized(subTag, depth + 1);
            }
        }
    }

    /**
     * @param name A name from a payload, which can be anything.
     * @return The parsed name, which never matches a registered one if the name is malformed.
     */
    protected static ResourceLocation parseName(String name) throws VariableClipboardException {
        ResourceLocation parsed = ResourceLocation.tryParse(name);
        if (parsed == null) {
            throw new VariableClipboardException(Component.translatable(
                    L10NValues.VARIABLE_CLIPBOARD_ERROR_UNMATERIALIZED, name));
        }
        return parsed;
    }

    /**
     * An error while copying or pasting a value.
     */
    public static class VariableClipboardException extends Exception {

        private final Component errorMessage;

        public VariableClipboardException(Component errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Component getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String getMessage() {
            return errorMessage.getString();
        }

    }

}
