package org.cyclops.integrateddynamics.api.client.render.valuetype;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;

/**
 * Registry for {@link IValueTypeWorldRenderer}.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public interface IValueTypeWorldRendererRegistry extends IRegistry {

    /**
     * Set the renderer for the given value type.
     * @param valueType The value type
     * @param renderer The renderer.
     * @param <R> The renderer type.
     * @return The registered renderer.
     */
    public <R extends IValueTypeWorldRenderer> R register(IValueType<?> valueType, R renderer);

    /**
     * Get the renderer for the value type.
     * @param valueType The value type
     * @return The registered renderer of null.
     */
    public @Nullable
    IValueTypeWorldRenderer getRenderer(IValueType<?> valueType);

}
