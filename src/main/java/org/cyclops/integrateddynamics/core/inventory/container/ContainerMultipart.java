package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionServer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;

import java.util.Map;

/**
 * Container for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ExtendedInventoryContainer implements IDirtyMarkListener {

    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final World world;
    private final BlockPos pos;
    private final Map<IAspect, Integer> aspectPropertyButtons = Maps.newHashMap();

    protected final EntityPlayer player;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerMultipart(EntityPlayer player, PartTarget target, IPartContainer partContainer, P partType) {
        super(player.inventory, partType);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();

        this.player = player;

        putButtonAction(BUTTON_SETTINGS, new IButtonActionServer<InventoryContainer>() {
            @Override
            public void onAction(int buttonId, InventoryContainer container) {
                IGuiContainerProvider gui = ((PartTypeConfigurable) getPartType()).getSettingsGuiProvider();
                IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide()); // Pass the side as extra data to the gui
                if(!MinecraftHelpers.isClientSide()) {
                    BlockPos cPos = getTarget().getCenter().getPos().getBlockPos();
                    ContainerMultipart.this.player.openGui(gui.getMod(), gui.getGuiID(),
                            world, cPos.getX(), cPos.getY(), cPos.getZ());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public S getPartState() {
        return (S) partContainer.getPartState(getTarget().getCenter().getSide());
    }

}
