package org.cyclops.integrateddynamics.api.client.render.part;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;

/**
 * Registry for {@link IPartOverlayRenderer}.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public interface IPartOverlayRendererRegistry extends IRegistry {

    /**
     * Register a new renderer for the given part type.
     * Multiple renderers can be added per part type.
     * @param partType The part type
     * @param renderer The renderer.
     * @param <R> The renderer type.
     * @return The registered renderer.
     */
    public <R extends IPartOverlayRenderer> R register(IPartType<?, ?> partType, R renderer);

    /**
     * Get all part overlay renderers for the given part type.
     * @param partType The part type
     * @return All registered renderers.
     */
    public Collection<IPartOverlayRenderer> getRenderers(IPartType<?, ?> partType);

}
