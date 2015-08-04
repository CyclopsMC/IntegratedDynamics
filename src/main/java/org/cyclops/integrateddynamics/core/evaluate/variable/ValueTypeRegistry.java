package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

import java.util.Collection;
import java.util.Map;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.evaluate.variable.IValueType}.
 * @author rubensworks
 */
public final class ValueTypeRegistry implements IValueTypeRegistry {

    private static ValueTypeRegistry INSTANCE = new ValueTypeRegistry();

    private Map<String, IValueType> valueTypes = Maps.newHashMap();
    @SideOnly(Side.CLIENT)
    private Map<IValueType, ModelResourceLocation> valueTypeModels;

    private ValueTypeRegistry() {
        if(MinecraftHelpers.isClientSide()) {
            valueTypeModels = Maps.newHashMap();
        }
    }

    /**
     * @return The unique instance.
     */
    public static ValueTypeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> T register(T valueType) {
        valueTypes.put(valueType.getUnlocalizedName(), valueType);
        return valueType;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ModelResourceLocation modelLocation) {
        valueTypeModels.put(valueType, modelLocation);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> ModelResourceLocation getValueTypeModel(T valueType) {
        return valueTypeModels.get(valueType);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Collection<ModelResourceLocation> getValueTypeModels() {
        return valueTypeModels.values();
    }

    @Override
    public Collection<IValueType> getValueTypes() {
        return valueTypes.values();
    }
}
