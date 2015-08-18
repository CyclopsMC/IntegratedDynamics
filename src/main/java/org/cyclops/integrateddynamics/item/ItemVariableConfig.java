package org.cyclops.integrateddynamics.item;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.model.VariableModel;

/**
 * Config for a variable item.
 * @author rubensworks
 */
public class ItemVariableConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemVariableConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemVariableConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "variable",
                null,
                ItemVariable.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected void validateModels() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        VariableModel.addAdditionalModels(builder);
        ImmutableSet<ResourceLocation> models = builder.build();
        for(ResourceLocation model : models) {
            if(!ModelLoaderRegistry.loaded(model)) {
                //IntegratedDynamics.clog(Level.ERROR, String.format("Model file %s not found, it is required by the variable item model.", model));
                throw new RuntimeException(String.format("Model file %s not found, it is required by the variable item model.", model));
            }
        }
    }

    @Override
    public void onInit(Step step) {
        super.onInit(step);
        if(step == Step.POSTINIT && MinecraftHelpers.isClientSide()) {
            validateModels();
        }
    }
}
