package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerDelay;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class ContainerScreenMaterializer extends ContainerScreenActiveVariableBase<ContainerMaterializer> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    public ContainerScreenMaterializer(ContainerMaterializer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/materializer.png");
    }

    @Override
    protected int getBaseYSize() {
        return 189;
    }

    @Override
    protected int getErrorX() {
        return ERROR_X;
    }

    @Override
    protected int getErrorY() {
        return ERROR_Y;
    }
}
