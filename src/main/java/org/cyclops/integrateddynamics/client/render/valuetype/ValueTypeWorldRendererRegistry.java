package org.cyclops.integrateddynamics.client.render.valuetype;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Registry for {@link IValueTypeWorldRenderer}.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public final class ValueTypeWorldRendererRegistry implements IValueTypeWorldRendererRegistry {

    private static ValueTypeWorldRendererRegistry INSTANCE = new ValueTypeWorldRendererRegistry();

    private final Map<IValueType<?>, IValueTypeWorldRenderer> renderers = new IdentityHashMap<>();

    private ValueTypeWorldRendererRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static ValueTypeWorldRendererRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <R extends IValueTypeWorldRenderer> R register(IValueType<?> valueType, R renderer) {
        renderers.put(valueType, renderer);
        return renderer;
    }

    @Override
    public IValueTypeWorldRenderer getRenderer(IValueType<?> valueType) {
        return renderers.get(valueType);
    }
}
