package org.cyclops.integrateddynamics.network.packet;

import com.mojang.text2speech.Narrator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for speaking text.
 * @author rubensworks
 *
 */
public class SpeakTextPacket extends PacketCodec {

    @CodecField
    private String text;

    public SpeakTextPacket() {

    }

    public SpeakTextPacket(String text) {
        this.text = text;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		Narrator.getNarrator().say(this.text);
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}