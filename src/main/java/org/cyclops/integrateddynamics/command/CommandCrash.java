package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.TickHandler;

/**
 * A command to let the server crash.
 * @author rubensworks
 *
 */
public class CommandCrash implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        IntegratedDynamics.clog(Level.WARN, context.getSource().getName() + " initialized a server crash.");
        TickHandler.getInstance().setShouldCrash();
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSource> make() {
        return Commands.literal("crash")
                .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                        .executes(new CommandCrash());
    }

}
