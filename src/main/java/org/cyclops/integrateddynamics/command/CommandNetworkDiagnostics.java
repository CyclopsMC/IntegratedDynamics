package org.cyclops.integrateddynamics.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsOpenClient;

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
    public void execute(MinecraftServer server, final ICommandSender sender, String[] parts) {
        if (sender instanceof EntityPlayerMP) {
            IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new NetworkDiagnosticsOpenClient(), (EntityPlayerMP) sender);
        }
    }

}
