package org.cyclops.integrateddynamics.network.packet;

import com.mojang.text2speech.Narrator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.GeneralConfig;

import java.util.Date;

/**
 * Packet for speaking text.
 * @author rubensworks
 *
 */
public class SpeakTextPacket extends PacketCodec {

	public static long lastSay = 0;

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
		if (new Date().getTime() >= lastSay + GeneralConfig.speachMaxFrequency) {
			Narrator.getNarrator().clear();
			Narrator.getNarrator().say(this.text, false);
			lastSay = new Date().getTime();
		}
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}
	
}