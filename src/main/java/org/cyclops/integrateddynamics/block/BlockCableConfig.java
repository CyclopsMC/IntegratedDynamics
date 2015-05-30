package org.cyclops.integrateddynamics.block;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderCable;
import org.cyclops.integrateddynamics.core.block.BlockMultipartTicking;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * Config for {@link BlockMultipartTicking}.
 * @author rubensworks
 */
public class BlockCableConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockCableConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCableConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "cable",
            null,
            BlockCable.class
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if(MinecraftHelpers.isClientSide()) {
            registerClientSide();
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerClientSide() {
        IntegratedDynamics._instance.proxy.registerRenderer(TileMultipartTicking.class, new RenderCable());
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockCable.class;
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
