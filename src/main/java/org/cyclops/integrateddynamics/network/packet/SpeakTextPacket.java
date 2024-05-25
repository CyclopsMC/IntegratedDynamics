package org.cyclops.integrateddynamics.network.packet;

import com.mojang.text2speech.Narrator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;

import java.util.Date;

/**
 * Packet for speaking text.
 *
 * @author rubensworks
 */
public class SpeakTextPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "speak_text");

    public static long lastSay = 0;

    @CodecField
    private String text;

    public SpeakTextPacket() {
        super(ID);
    }

    public SpeakTextPacket(String text) {
        super(ID);
        this.text = text;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if (new Date().getTime() >= lastSay + GeneralConfig.speachMaxFrequency) {
            Narrator.getNarrator().clear();
            Narrator.getNarrator().say(this.text, false);
            lastSay = new Date().getTime();
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
