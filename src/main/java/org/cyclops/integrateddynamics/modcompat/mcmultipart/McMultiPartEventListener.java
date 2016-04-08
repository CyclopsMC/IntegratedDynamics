package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author rubensworks
 */
public class McMultiPartEventListener {

    /**
     * Called for baking the model of this cable depending on its state.
     * @param event The bake event.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event){
        IBakedModel cableModel = new PartCableModel();
        event.modelRegistry.putObject(McMultiPartHelpers.CABLE_MODEL_LOCATION, cableModel);
    }

}
