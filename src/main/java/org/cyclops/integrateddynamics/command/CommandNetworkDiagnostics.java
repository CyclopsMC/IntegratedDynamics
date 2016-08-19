package org.cyclops.integrateddynamics.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.diagnostics.GuiNetworkDiagnostics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsSubscribePacket;

import java.util.List;

/**
 * Command for opening the network diagnostics gui.
 * @author rubensworks
 *
 */
public class CommandNetworkDiagnostics extends CommandMod {

    public static final String NAME = "networkdiagnostics";

    public CommandNetworkDiagnostics(ModBase mod) {
        super(mod, NAME);
    }

    @Override
    public List getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] parts, BlockPos blockPos) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] parts) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GuiNetworkDiagnostics.clearNetworkData();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.subscribe());
                GuiNetworkDiagnostics.start();
            }
        }).start();
    }

}
