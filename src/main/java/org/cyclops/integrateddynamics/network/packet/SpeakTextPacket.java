package org.cyclops.integrateddynamics.network.packet;

import com.mojang.text2speech.Narrator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
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
	public void actionClient(World world, PlayerEntity player) {
		Narrator.getNarrator().say(this.text, false);
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}
	
}