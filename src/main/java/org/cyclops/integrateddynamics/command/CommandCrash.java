package org.cyclops.integrateddynamics.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.TickHandler;

import java.util.List;

/**
 * A command to let the server crash.
 * @author rubensworks
 *
 */
public class CommandCrash extends CommandMod {

    public static final String NAME = "crash";

    public CommandCrash(ModBase mod) {
        super(mod, NAME);
    }

    @Override
    public List getTabCompletions(MinecraftServer server, ICommandSender sender, String[] parts, BlockPos blockPos) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] parts) {
        IntegratedDynamics.clog(Level.WARN, sender.getName() + " initialized a server crash.");
        TickHandler.getInstance().setShouldCrash();
    }

}
