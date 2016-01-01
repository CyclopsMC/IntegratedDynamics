package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * Base implementation of a value type.
 * @author rubensworks
 */
public abstract class ValueTypeBase<V extends IValue> implements IValueType<V> {

    private final String typeName;
    private final int color;
    private final String colorFormat;

    public ValueTypeBase(String typeName, int color, String colorFormat) {
        this.typeName = typeName;
        this.color = color;
        this.colorFormat = colorFormat;
        if(MinecraftHelpers.isModdedEnvironment() && MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    protected String getUnlocalizedPrefix() {
        return "valuetype.valuetypes." + getModId() + getTypeNamespace() + getTypeName();
    }

    protected String getTypeNamespace() {
        return ".";
    }

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedPrefix() + ".name";
    }

    protected String getTypeName() {
        return this.typeName;
    }

    @Override
    public int getDisplayColor() {
        return this.color;
    }

    @Override
    public String getDisplayColorFormat() {
        return this.colorFormat;
    }

    @Override
    public boolean correspondsTo(IValueType valueType) {
        return this == valueType;
    }

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        ValueTypes.REGISTRY.registerValueTypeModel(this,
                new ResourceLocation(getModId() + ":valuetype" + getTypeNamespace().replace('.', '/') + getTypeName()));
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        String typeName = L10NHelpers.localize(getUnlocalizedName());
        lines.add(L10NHelpers.localize(L10NValues.VALUETYPE_TOOLTIP_TYPENAME, getDisplayColorFormat() + typeName));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
    }

    @Override
    public L10NHelpers.UnlocalizedString canDeserialize(String value) {
        try {
            deserialize(value);
            return null;
        } catch (IllegalArgumentException e) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, value);
        }
    }

    @Override
    public V materialize(V value) {
        return value;
    }

    @Override
    public String toString() {
        return L10NHelpers.localize(getUnlocalizedName());
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
