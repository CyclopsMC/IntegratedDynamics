package org.cyclops.integrateddynamics.api.evaluate.variable;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
    public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo, @Nullable V value);

    /**
     * @param value The value
     * @return A short string representation used in guis to show the value.
     */
    public IFormattableTextComponent toCompactString(V value);

    /**
     * @return The color that is used to identify this value type.
     */
    public int getDisplayColor();

    /**
     * @return The color that is used to identify this value type using MC formatting codes.
     */
    public TextFormatting getDisplayColorFormat();

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
    public INBT serialize(V value);

    /**
     * Check if the given value can be deserialized.
     * @param value The value to deserialize.
     * @return An error or null.
     */
    @Nullable
    public ITextComponent canDeserialize(INBT value);

    /**
     * Deserialize the given value.
     * @param value The value to deserialize.
     * @return The deserialized value.
     */
    public V deserialize(INBT value);

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
    public default IBakedModel getVariableItemOverrideModel(V value, IBakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity) {
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
    public default void renderISTER(V value, ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

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
