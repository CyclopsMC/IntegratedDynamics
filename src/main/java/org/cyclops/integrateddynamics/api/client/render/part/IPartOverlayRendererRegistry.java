package org.cyclops.integrateddynamics.api.client.render.part;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;

/**
 * Registry for {@link IPartOverlayRenderer}.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
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
