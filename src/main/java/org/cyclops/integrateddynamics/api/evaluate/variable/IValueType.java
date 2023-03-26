package org.cyclops.integrateddynamics.api.evaluate.variable;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Type of value
 * @author rubensworks
 */
public interface IValueType<V extends IValue> {

    /**
     * @return If this type is a category and should be handled accordingly.
     */
    public boolean isCategory();

    /**
     * @return If this type is an object type, otherwise it is a raw type.
     */
    public boolean isObject();

    /**
     * Create an immutable default value.
     * @return The default value of this type.
     */
    public V getDefault();

    /**
     * @return The name of this type without any prefixes.
     */
    public String getTypeName();

    /**
     * @return The unique name for this value type, only used for internal storage.
     */
    public ResourceLocation getUniqueName();

    /**
     * @return The unique name of this type that will also be used for display.
     */
    public String getTranslationKey();

    /**
     * Add tooltip lines for this aspect when hovered in a gui.
     * @param lines The list to add lines to.
     * @param appendOptionalInfo If shift-to-show info should be added.
     * @param value The value to show the tooltip for.
     */
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo, @Nullable V value);

    /**
     * @param value The value
     * @return A short string representation used in guis to show the value.
     */
    public MutableComponent toCompactString(V value);

    /**
     * @return The color that is used to identify this value type.
     */
    public int getDisplayColor();

    /**
     * @return The color that is used to identify this value type using MC formatting codes.
     */
    public ChatFormatting getDisplayColorFormat();

    /**
     * Check if the given type corresponds with this type.
     * To check bidirectional, use {@link org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers#correspondsTo(IValueType, IValueType)}.
     * @param valueType The value type to check correspondence with.
     * @return If the given value type can be used with this value type.
     */
    public boolean correspondsTo(IValueType<?> valueType);

    /**
     * Serialize the given value.
     * @param value The value to serialize.
     * @return The serialized value.
     */
    public Tag serialize(V value);

    /**
     * Check if the given value can be deserialized.
     * @param valueDeseralizationContext Getter for blocks.
     * @param value The value to deserialize.
     * @return An error or null.
     */
    @Nullable
    public Component canDeserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value);

    /**
     * Deserialize the given value.
     * @param valueDeseralizationContext Deserialization context.
     * @param value The value to deserialize.
     * @return The deserialized value.
     */
    public V deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value);

    /**
     * Materialize the given value so that it can exist without any external references.
     * @param value The value to materialize.
     * @return The materialized value.
     * @throws EvaluationException if materialization fails because of a variable evaluation.
     */
    public V materialize(V value) throws EvaluationException;

    /**
     * Get the string representation of the given value.
     * This is useful for cases when the value needs to be edited in a GUI.
     *
     * This corresponds to {@link #parseString(String)}.
     *
     * @param value A value.
     * @return A string representation of the given value.
     */
    public String toString(V value);

    /**
     * Parse the given string representation of a value.
     *
     * This corresponds to {@link #toString(IValue)}.
     *
     * @param value A string representation of a value.
     * @return A value.
     * @throws EvaluationException If parsing failed.
     */
    public V parseString(String value) throws EvaluationException;

    /**
     * @return A new logic programmer element for this value type.
     */
    public IValueTypeLogicProgrammerElement createLogicProgrammerElement();

    /**
     * Deserialize the given JSON element to a value predicate.
     * @param element The JSON element.
     * @param value The value.
     * @return The value predicate.
     */
    default public ValuePredicate<V> deserializeValuePredicate(JsonObject element, @Nullable IValue value) {
        return new ValuePredicate<>(this, value);
    }

    /**
     * Attempt to cast the given value to a value of this value type.
     * @param value A value of unknown type.
     * @return The casted value.
     * @throws EvaluationException If the incorrect value type was found.
     */
    public V cast(IValue value) throws EvaluationException;

    /**
     * An optional baked model to override when rendering the variable as item.
     * @param value The value to value to get the model for.
     * @param model The original baked model.
     * @param stack The variable stack.
     * @param world The client world.
     * @param livingEntity The entity holding the stack.
     * @return The overridden model. Will fallback to default if null.
     */
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public default BakedModel getVariableItemOverrideModel(V value, BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity) {
        return null;
    }

    /**
     * Called during ISTER rendering of an variable item.
     * @param value The value to value to render.
     * @param stack The variable stack.
     * @param transformType Transform type.
     * @param matrixStack Matrix stack.
     * @param buffer Render buffer.
     * @param combinedLight Lighting.
     * @param combinedOverlay Overlay.
     */
    @OnlyIn(Dist.CLIENT)
    public default void renderISTER(V value, ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

    }

    /**
     * Use this comparator for any comparisons with value types.
     */
    public static class ValueTypeComparator implements Comparator<IValueType<?>> {

        private static ValueTypeComparator INSTANCE = null;

        private ValueTypeComparator() {

        }

        public static ValueTypeComparator getInstance() {
            if(INSTANCE == null) INSTANCE = new ValueTypeComparator();
            return INSTANCE;
        }

        @Override
        public int compare(IValueType<?> o1, IValueType<?> o2) {
            return o1.getUniqueName().compareTo(o2.getUniqueName());
        }
    }

}
