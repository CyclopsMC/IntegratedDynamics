package org.cyclops.integrateddynamics.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A facade for retrieving a variable.
 * @author rubensworks
 */
public interface IVariableFacade {

    /**
     * @return The unique id for this facade.
     */
    public int getId();

    /**
     * @return The optional label for this facade.
     */
    public @Nullable String getLabel();

    /**
     * Get the variable.
     * @param <V> The value type.
     * @param network The object used to look for the variable.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network);

    /**
     * @return If this is a valid reference.
     */
    public boolean isValid();

    /**
     * Check if this facade is valid, otherwise notify the validator of any errors.
     * @param network The object used to look for the variable.
     * @param validator The object to notify errors to.
     * @param containingValueType The value type in which this variable facade is being used.
     */
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType);

    /**
     * @return The output type of this variable facade.
     */
    public IValueType getOutputType();

    /**
     * Add information about this variable facade to the list.
     * @param list The list to add lines to.
     * @param world The world.
     */
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(List<Component> list, Level world);

    /**
     * Handle the quads for the given baked model.
     * @param variableModelBaked The baked model.
     * @param quads The quads that can be added to.
     * @param random A random instance.
     * @param modelData Model data.
     */
    @OnlyIn(Dist.CLIENT)
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, IModelData modelData);

    /**
     * An optional baked model to override when rendering the variable as item.
     * @param model The original baked model.
     * @param stack The variable stack.
     * @param world The client world.
     * @param livingEntity The entity holding the stack.
     * @return The overridden model. Will fallback to default if null.
     */
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public default BakedModel getVariableItemOverrideModel(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity) {
        return null;
    }

    /**
     * Called during ISTER rendering of an variable item.
     * @param stack The variable stack.
     * @param transformType Transform type.
     * @param matrixStack Matrix stack.
     * @param buffer Render buffer.
     * @param combinedLight Lighting.
     * @param combinedOverlay Overlay.
     */
    @OnlyIn(Dist.CLIENT)
    public default void renderISTER(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

    }

    public static interface IValidator {

        /**
         * Set the current error for the given aspect.
         * @param error The error to set, or null to clear.
         */
        public void addError(MutableComponent error);

    }

}
