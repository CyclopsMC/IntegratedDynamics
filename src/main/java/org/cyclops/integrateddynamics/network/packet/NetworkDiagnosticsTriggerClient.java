package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDataClient;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnosticsPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.network.diagnostics.http.DiagnosticsWebServer;
import org.cyclops.integrateddynamics.proxy.ClientProxy;

/**
 * Packet for opening or closing network diagnostics at a client.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsTriggerClient extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "network_diagnostics_trigger");

    @CodecField
    private boolean start;
    @CodecField
    private int port;

    public NetworkDiagnosticsTriggerClient(boolean start, int port) {
        super(ID);
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
                    player.sendSystemMessage(
                            Component.literal("Diagnostics server has been started on ")
                                    .append(Component.literal(ClientProxy.DIAGNOSTICS_SERVER.getUrl())
                                            .setStyle(Style.EMPTY
                                                    .withUnderlined(true)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ClientProxy.DIAGNOSTICS_SERVER.getUrl()))))
                    );
                }).start();
            } else {
                player.sendSystemMessage(
                        Component.literal("Diagnostics server is already running on ")
                                .append(Component.literal(ClientProxy.DIAGNOSTICS_SERVER.getUrl())
                                        .setStyle(Style.EMPTY
                                                .withUnderlined(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ClientProxy.DIAGNOSTICS_SERVER.getUrl()))))
                );
            }
        } else {
            if (ClientProxy.DIAGNOSTICS_SERVER != null) {
                IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe());
                new Thread(() -> {
                    NetworkDiagnosticsPartOverlayRenderer.getInstance().clearPositions();
                    NetworkDataClient.clearNetworkData();
                    ClientProxy.DIAGNOSTICS_SERVER.deinitialize();
                    ClientProxy.DIAGNOSTICS_SERVER = null;
                    player.sendSystemMessage(Component.literal("Stopped diagnostics server"));
                }).start();
            } else {
                player.sendSystemMessage(Component.literal("No diagnostics server is running"));
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
