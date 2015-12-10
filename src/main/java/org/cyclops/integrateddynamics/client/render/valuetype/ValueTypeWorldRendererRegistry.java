package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.Map;

/**
 * Registry for {@link IValueTypeWorldRenderer}.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public final class ValueTypeWorldRendererRegistry implements IValueTypeWorldRendererRegistry {

    private static ValueTypeWorldRendererRegistry INSTANCE = new ValueTypeWorldRendererRegistry();

    private final Map<IValueType<?>, IValueTypeWorldRenderer> renderers = Maps.newHashMap();

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
