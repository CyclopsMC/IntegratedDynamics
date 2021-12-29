package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.TickHandler;

/**
 * A command to let the server crash.
 * @author rubensworks
 *
 */
public class CommandCrash implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, context.getSource().getTextName() + " initialized a server crash.");
        TickHandler.getInstance().setShouldCrash();
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("crash")
                .requires((commandSource) -> commandSource.hasPermission(2))
                        .executes(new CommandCrash());
    }

}
