package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnosticsPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.network.diagnostics.http.DiagnosticsWebServer;
import org.cyclops.integrateddynamics.proxy.ClientProxy;

/**
 * Packet for opening or closing network diagnostics at a client.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsTriggerClient extends PacketCodec {

    @CodecField
    private boolean start;
    @CodecField
    private int port;

    public NetworkDiagnosticsTriggerClient(boolean start, int port) {
        this.start = start;
        this.port = port;
    }

    public NetworkDiagnosticsTriggerClient() {
        this(true, 0);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if (start) {
            if (ClientProxy.DIAGNOSTICS_SERVER == null) {
                IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.subscribe());
                new Thread(() -> {
                    ClientProxy.DIAGNOSTICS_SERVER = new DiagnosticsWebServer(port);
                    ClientProxy.DIAGNOSTICS_SERVER.initialize();
                    player.sendMessage(
                            new TextComponent("Diagnostics server has been started on ")
                                    .append(new TextComponent(ClientProxy.DIAGNOSTICS_SERVER.getUrl())
                                            .setStyle(Style.EMPTY
                                                    .setUnderlined(true)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ClientProxy.DIAGNOSTICS_SERVER.getUrl())))),
                            Util.NIL_UUID
                    );
                }).start();
            } else {
                player.sendMessage(
                        new TextComponent("Diagnostics server is already running on ")
                                .append(new TextComponent(ClientProxy.DIAGNOSTICS_SERVER.getUrl())
                                        .setStyle(Style.EMPTY
                                                .setUnderlined(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ClientProxy.DIAGNOSTICS_SERVER.getUrl())))),
                        Util.NIL_UUID
                );
            }
        } else {
            if (ClientProxy.DIAGNOSTICS_SERVER != null) {
                IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe());
                new Thread(() -> {
                    NetworkDiagnosticsPartOverlayRenderer.getInstance().clearPositions();
                    ClientProxy.DIAGNOSTICS_SERVER.deinitialize();
                    ClientProxy.DIAGNOSTICS_SERVER = null;
                    player.sendMessage(new TextComponent("Stopped diagnostics server"), Util.NIL_UUID);
                }).start();
            } else {
                player.sendMessage(new TextComponent("No diagnostics server is running"), Util.NIL_UUID);
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
