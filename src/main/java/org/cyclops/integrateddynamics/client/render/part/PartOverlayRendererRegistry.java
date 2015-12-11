package org.cyclops.integrateddynamics.client.render.part;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;

/**
 * Registry for {@link IPartOverlayRenderer}.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public final class PartOverlayRendererRegistry implements IPartOverlayRendererRegistry {

    private static PartOverlayRendererRegistry INSTANCE = new PartOverlayRendererRegistry();

    private final Multimap<IPartType<?, ?>, IPartOverlayRenderer> renderers = HashMultimap.create();

    private PartOverlayRendererRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static PartOverlayRendererRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <R extends IPartOverlayRenderer> R register(IPartType<?, ?> partType, R renderer) {
        renderers.put(partType, renderer);
        return renderer;
    }

    @Override
    public Collection<IPartOverlayRenderer> getRenderers(IPartType<?, ?> partType) {
        return renderers.get(partType);
    }
}
