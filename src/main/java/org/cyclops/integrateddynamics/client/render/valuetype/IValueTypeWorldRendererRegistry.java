package org.cyclops.integrateddynamics.client.render.valuetype;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

import javax.annotation.Nullable;

/**
 * Registry for {@link IValueTypeWorldRenderer}.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public interface IValueTypeWorldRendererRegistry extends IRegistry {

    /**
     * Set the renderer for the given value type.
     * @param valueType The value type
     * @param renderer The renderer.
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
