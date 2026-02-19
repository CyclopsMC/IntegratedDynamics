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
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsTriggerClient;

/**
 * Command for opening the network diagnostics gui.
 * @author rubensworks
 *
 */
public class CommandNetworkDiagnostics implements Command<CommandSourceStack> {

    private final boolean operationArg;
    private final boolean portArg;

    public CommandNetworkDiagnostics(boolean operationArg, boolean portArg) {
        this.operationArg = operationArg;
        this.portArg = portArg;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StartStop operation = this.operationArg ? ArgumentTypeEnum.getValue(context, "operation", StartStop.class) : StartStop.START;
        int port = this.portArg ? IntegerArgumentType.getInteger(context, "port") : GeneralConfig.diagnosticsWebServerPort;
        IntegratedDynamics._instance.getPacketHandler().sendToPlayer
                (new NetworkDiagnosticsTriggerClient(operation == StartStop.START, port),
                context.getSource().getPlayerOrException()
        );
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("networkdiagnostics")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(new CommandNetworkDiagnostics(false, false));

        // Add the operation/port subcommand chain
        builder.then(Commands.argument("operation", new ArgumentTypeEnum(StartStop.class))
                .executes(new CommandNetworkDiagnostics(true, false))
                .then(Commands.argument("port", IntegerArgumentType.integer())
                    .executes(new CommandNetworkDiagnostics(true, true))));

        // Add the measure subcommand
        builder.then(Commands.literal("measure")
                .executes(new CommandMeasure(false))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 600))
                        .executes(new CommandMeasure(true))));

        return builder;
    }

    public static enum StartStop {
        START,
        STOP;
    }

    /**
     * Subcommand for measuring network tick times.
     */
    public static class CommandMeasure implements Command<CommandSourceStack> {
        private final boolean hasSecondsArg;

        public CommandMeasure(boolean hasSecondsArg) {
            this.hasSecondsArg = hasSecondsArg;
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            int seconds = hasSecondsArg ? IntegerArgumentType.getInteger(context, "seconds") : 10;
            NetworkDiagnostics.getInstance().startMeasurement(context.getSource().getPlayerOrException(), seconds);
            return 0;
        }
    }
}
