package org.cyclops.integrateddynamics.network.packet;

import com.mojang.text2speech.Narrator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
	@OnlyIn(Dist.CLIENT)
	public void actionClient(Level world, Player player) {
		Narrator.getNarrator().say(this.text, false);
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {

	}
	
}