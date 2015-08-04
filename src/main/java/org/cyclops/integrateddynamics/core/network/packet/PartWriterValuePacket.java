package org.cyclops.integrateddynamics.core.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;

/**
 * Packet for sending current write values to clients inside a {@link org.cyclops.integrateddynamics.client.gui.GuiPartWriter}.
 * @author rubensworks
 *
 */
public class PartWriterValuePacket extends PacketCodec {

    @CodecField
    private String value;
	@CodecField
	private int color;

    public PartWriterValuePacket() {

    }

    public PartWriterValuePacket(String value, int color) {
        this.value = value;
        this.color = color;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
        if(player.openContainer instanceof ContainerPartWriter) {
            ((ContainerPartWriter) player.openContainer).setWriteValue(value);
            ((ContainerPartWriter) player.openContainer).setWriteValueColor(color);
        }
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}