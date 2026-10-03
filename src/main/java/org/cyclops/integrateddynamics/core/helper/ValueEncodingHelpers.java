package org.cyclops.integrateddynamics.core.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
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
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Helpers for encoding materialized values into strings, and decoding them again.
 *
 * Values are encoded as a single SNBT compound, optionally gzipped and base64-encoded behind a prefix.
 * Only materialized values can be encoded and decoded, as other values refer to state that is meaningless,
 * or even abusable, in another world.
 *
 * @author rubensworks
 */
public class ValueEncodingHelpers {

    /**
     * The prefix that marks a gzipped and base64-encoded value, standing for Integrated Dynamics Compressed Encoding.
     */
    public static final String PREFIX_COMPRESSED = "idce:";

    /**
     * The maximum nesting depth of an encoded value, to bound the recursion over handcrafted ones.
     */
    public static final int MAX_DEPTH = 512;

    /**
     * The versions of Minecraft and this mod that a value was encoded in.
     * These are only informative, so values are never rejected based on them.
     */
    public static final String KEY_VERSION = "version";
    public static final String KEY_VERSION_MINECRAFT = "minecraft";
    public static final String KEY_VERSION_MOD = Reference.MOD_ID;
    public static final String KEY_VALUE_TYPE = "valueType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_PROXY_NAME = "proxyName";
    public static final String KEY_SERIALIZER = "serializer";

    /**
     * Encode the given value into a versioned tag, as it is sent over the network.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value A materialized value.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The encoded value.
     * @throws ValueEncodingException If the value is not materialized or too large.
     */
    public static CompoundTag encodeToTag(ValueDeseralizationContext valueDeseralizationContext, IValue value, int maxLength)
            throws ValueEncodingException {
        CompoundTag tag = ValueHelpers.serialize(valueDeseralizationContext, value);
        tag.put(KEY_VERSION, createVersionTag());
        // Fail when encoding, instead of producing something that can never be decoded
        validateMaterialized(tag);
        checkLength(tag.toString().length(), maxLength);
        return tag;
    }

    /**
     * @return The versions of Minecraft and this mod that are running.
     */
    public static CompoundTag createVersionTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_VERSION_MINECRAFT, MinecraftHelpers.getMinecraftVersion());
        tag.putString(KEY_VERSION_MOD, getModVersion());
        return tag;
    }

    protected static String getModVersion() {
        // The mod is not loaded outside a running game, such as in unit tests
        return Optional.ofNullable(IntegratedDynamics._instance)
                .map(ModBase::getContainer)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    /**
     * Format the given encoded value as a string.
     * @param tag An encoded value.
     * @param compressed If the compressed form must be used instead of the human-readable one.
     * @return The encoded string.
     * @throws ValueEncodingException If the value could not be compressed.
     */
    public static String format(CompoundTag tag, boolean compressed) throws ValueEncodingException {
        String snbt = tag.toString();
        return compressed ? compress(snbt) : snbt;
    }

    /**
     * Encode the given value into a human-readable SNBT string.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value A materialized value.
     * @param maxLength The maximum length of the encoded value.
     * @return The encoded value.
     * @throws ValueEncodingException If the value is not materialized or too large.
     */
    public static String encode(ValueDeseralizationContext valueDeseralizationContext, IValue value, int maxLength)
            throws ValueEncodingException {
        return format(encodeToTag(valueDeseralizationContext, value, maxLength), false);
    }

    /**
     * Encode the given value into a compressed single-line string.
     * @param valueDeseralizationContext A value deserialization context.
     * @param value A materialized value.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The encoded value.
     * @throws ValueEncodingException If the value is not materialized, too large, or could not be compressed.
     */
    public static String encodeCompressed(ValueDeseralizationContext valueDeseralizationContext, IValue value, int maxLength)
            throws ValueEncodingException {
        return format(encodeToTag(valueDeseralizationContext, value, maxLength), true);
    }

    protected static String compress(String snbt) throws ValueEncodingException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(snbt.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        return PREFIX_COMPRESSED + Base64.getEncoder().encodeToString(output.toByteArray());
    }

    /**
     * Decode a value from the given encoded string.
     * Both the human-readable and the compressed form are accepted.
     * @param valueDeseralizationContext A value deserialization context.
     * @param input An encoded string.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The value.
     * @throws ValueEncodingException If the string does not hold a valid materialized value.
     */
    public static IValue decode(ValueDeseralizationContext valueDeseralizationContext, String input, int maxLength)
            throws ValueEncodingException {
        return decode(valueDeseralizationContext, parse(input, maxLength), maxLength);
    }

    /**
     * Parse the given encoded string into an encoded value.
     * Both the human-readable and the compressed form are accepted.
     * @param input An encoded string.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The encoded value.
     * @throws ValueEncodingException If the string is malformed or too large.
     */
    public static CompoundTag parse(String input, int maxLength) throws ValueEncodingException {
        try {
            return TagParser.parseTag(toSnbt(input, maxLength));
        } catch (CommandSyntaxException e) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
    }

    /**
     * Decode a value from the given encoded value.
     * @param valueDeseralizationContext A value deserialization context.
     * @param tag An encoded value.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The value.
     * @throws ValueEncodingException If the tag does not hold a valid materialized value.
     */
    public static IValue decode(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, int maxLength)
            throws ValueEncodingException {
        checkLength(tag.toString().length(), maxLength);
        if (!tag.contains(KEY_VALUE_TYPE, Tag.TAG_STRING) || !tag.contains(KEY_VALUE)) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        validateMaterialized(tag);

        IValue value;
        try {
            value = ValueHelpers.deserialize(valueDeseralizationContext, tag);
        } catch (RuntimeException e) {
            // Encoded values can be handcrafted, so a value type can fail on them in any way
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        if (value == null) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_VALUETYPE,
                    tag.getString(KEY_VALUE_TYPE)));
        }
        return value;
    }

    /**
     * @param input An encoded string.
     * @param maxLength The maximum length of the uncompressed encoded value.
     * @return The uncompressed SNBT of the given encoded string.
     * @throws ValueEncodingException If the string is malformed or too large.
     */
    protected static String toSnbt(String input, int maxLength) throws ValueEncodingException {
        String trimmed = input.trim();
        if (trimmed.startsWith(PREFIX_COMPRESSED)) {
            return decompress(trimmed.substring(PREFIX_COMPRESSED.length()), maxLength);
        }
        if (!trimmed.startsWith("{")) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        checkLength(trimmed.length(), maxLength);
        return trimmed;
    }

    protected static String decompress(String base64, int maxLength) throws ValueEncodingException {
        byte[] compressed;
        try {
            compressed = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }

        // Stop reading beyond the maximum, so that a small string can not expand into a huge one
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            int read;
            while ((read = gzip.read(buffer)) > 0) {
                output.write(buffer, 0, read);
                checkLength(output.size(), maxLength);
            }
        } catch (IOException e) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    protected static void checkLength(int length, int maxLength) throws ValueEncodingException {
        if (length > maxLength) {
            throw new ValueEncodingException(Component.translatable(
                    L10NValues.VALUE_ENCODING_ERROR_TOOLARGE, length, maxLength));
        }
    }

    /**
     * Check that the given tag only holds materialized state.
     *
     * Non-materialized values refer to a position in a world, which would allow a handcrafted value
     * to read blocks that the player has no access to.
     *
     * @param tag An encoded value.
     * @throws ValueEncodingException If the tag holds anything that is not materialized.
     */
    public static void validateMaterialized(Tag tag) throws ValueEncodingException {
        validateMaterialized(tag, 0);
    }

    protected static void validateMaterialized(Tag tag, int depth) throws ValueEncodingException {
        if (depth > MAX_DEPTH) {
            throw new ValueEncodingException(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_PARSE));
        }
        if (tag instanceof CompoundTag compoundTag) {
            if (compoundTag.contains(KEY_PROXY_NAME, Tag.TAG_STRING)) {
                String proxyName = compoundTag.getString(KEY_PROXY_NAME);
                IValueTypeListProxyFactoryTypeRegistry.IProxyFactory factory = ValueTypeListProxyFactories.REGISTRY
                        .getFactory(parseName(proxyName));
                if (factory == null || !factory.isMaterialized()) {
                    throw new ValueEncodingException(Component.translatable(
                            L10NValues.VALUE_ENCODING_ERROR_UNMATERIALIZED, proxyName));
                }
            }
            if (compoundTag.contains(KEY_SERIALIZER, Tag.TAG_STRING)) {
                String serializerName = compoundTag.getString(KEY_SERIALIZER);
                IOperatorSerializer serializer = Operators.REGISTRY
                        .getSerializer(parseName(serializerName));
                if (serializer == null || !serializer.isMaterialized()) {
                    throw new ValueEncodingException(Component.translatable(
                            L10NValues.VALUE_ENCODING_ERROR_UNMATERIALIZED, serializerName));
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
     * @param name A name from an encoded value, which can be anything.
     * @return The parsed name, which never matches a registered one if the name is malformed.
     */
    protected static ResourceLocation parseName(String name) throws ValueEncodingException {
        ResourceLocation parsed = ResourceLocation.tryParse(name);
        if (parsed == null) {
            throw new ValueEncodingException(Component.translatable(
                    L10NValues.VALUE_ENCODING_ERROR_UNMATERIALIZED, name));
        }
        return parsed;
    }

    /**
     * An error while encoding or decoding a value.
     */
    public static class ValueEncodingException extends Exception {

        private final MutableComponent errorMessage;

        public ValueEncodingException(MutableComponent errorMessage) {
            this.errorMessage = errorMessage;
        }

        public MutableComponent getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String getMessage() {
            return errorMessage.getString();
        }

    }

}
