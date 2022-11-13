package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.cyclops.cyclopscore.command.argument.ArgumentTypeEnum;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsTriggerClient;

/**
 * Command for opening the network diagnostics gui.
 * @author rubensworks
 *
 */
public class CommandNetworkDiagnostics implements Command<CommandSourceStack> {

    private final boolean portArg;

    public CommandNetworkDiagnostics(boolean portArg) {
        this.portArg = portArg;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StartStop operation = ArgumentTypeEnum.getValue(context, "operation", StartStop.class);
        int port = this.portArg ? IntegerArgumentType.getInteger(context, "port") : GeneralConfig.diagnosticsWebServerPort;
        IntegratedDynamics._instance.getPacketHandler().sendToPlayer
                (new NetworkDiagnosticsTriggerClient(operation == StartStop.START, port),
                context.getSource().getPlayerOrException()
        );
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("networkdiagnostics")
                .requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.argument("operation", new ArgumentTypeEnum(StartStop.class))
                        .executes(new CommandNetworkDiagnostics(false))
                        .then(Commands.argument("port", IntegerArgumentType.integer())
                            .executes(new CommandNetworkDiagnostics(true))));
    }

    public static enum StartStop {
        START,
        STOP;
    }
}
