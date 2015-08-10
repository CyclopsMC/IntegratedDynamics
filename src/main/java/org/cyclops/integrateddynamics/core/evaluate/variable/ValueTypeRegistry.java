package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;

import java.util.Collection;
import java.util.Map;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.evaluate.variable.IValueType}.
 * @author rubensworks
 */
public final class ValueTypeRegistry implements IValueTypeRegistry {

    private static ValueTypeRegistry INSTANCE = new ValueTypeRegistry();
    private static final ValueTypeVariableFacade INVALID_FACADE = new ValueTypeVariableFacade(false, null, (IValue) null);

    private Map<String, IValueType> valueTypes = Maps.newHashMap();
    @SideOnly(Side.CLIENT)
    private Map<IValueType, ModelResourceLocation> valueTypeModels;

    private ValueTypeRegistry() {
        if(MinecraftHelpers.isClientSide()) {
            valueTypeModels = Maps.newHashMap();
        }
        if(MinecraftHelpers.isModdedEnvironment()) {
            IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(this);
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

    @Override
    public IValueType getValueType(String name) {
        return valueTypes.get(name);
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

    @Override
    public String getTypeId() {
        return "valuetype";
    }

    @Override
    public ValueTypeVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
        if(!tag.hasKey("typeName", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                || !tag.hasKey("value", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
            return INVALID_FACADE;
        }
        IValueType type = getValueType(tag.getString("typeName"));
        if(type == null) {
            return INVALID_FACADE;
        }
        IValue value = type.deserialize(tag.getString("value"));
        return new ValueTypeVariableFacade(id, type, value);
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, ValueTypeVariableFacade variableFacade) {
        tag.setString("typeName", variableFacade.getValueType().getUnlocalizedName());
        tag.setString("value", variableFacade.getValue().getType().serialize(variableFacade.getValue()));
    }
}
