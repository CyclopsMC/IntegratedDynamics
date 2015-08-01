package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;

/**
 * Base implementation of a value type.
 * @author rubensworks
 */
public abstract class ValueTypeBase<V extends IValue> implements IValueType<V> {

    private final String typeName;
    private final int color;

    public ValueTypeBase(String typeName, int color) {
        this.typeName = typeName;
        this.color = color;
        if(MinecraftHelpers.isModdedEnvironment() && MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @Override
    public String getUnlocalizedName() {
        return "valuetype.valuetypes." + getModId() + "." + getTypeName() + ".name";
    }

    protected String getTypeName() {
        return this.typeName;
    }

    @Override
    public int getDisplayColor() {
        return this.color;
    }

    @Override
    public boolean correspondsTo(IValueType valueType) {
        return this == valueType;
    }

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        ValueTypes.REGISTRY.registerValueTypeModel(this,
                new ModelResourceLocation(getModId() + ":valuetype/" + getTypeName()));
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
