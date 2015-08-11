package org.cyclops.integrateddynamics.core.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;

/**
 * Packet for sending current write values to clients inside a {@link org.cyclops.integrateddynamics.client.gui.GuiPartWriter}.
 * @author rubensworks
 *
 */
public class PartReaderValuePacket extends PacketCodec {

	@CodecField
	private String aspectName;
    @CodecField
    private String value;
	@CodecField
	private int color;

    public PartReaderValuePacket() {

    }

    public PartReaderValuePacket(String aspectName, String value, int color) {
		this.aspectName = aspectName;
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
        if(player.openContainer instanceof ContainerPartReader) {
            ((ContainerPartReader) player.openContainer).setReadValue(aspectName, Pair.of(value, color));
        }
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}