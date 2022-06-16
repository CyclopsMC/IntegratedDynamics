package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

/**
 * Command for opening the network diagnostics gui.
 * @author rubensworks
 *
 */
public class CommandNetworkDiagnostics implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Style downloadStyle = Style.EMPTY;
        downloadStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to visit the relevant GitHub issue")));
        downloadStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/CyclopsMC/IntegratedDynamics/issues/863"));
        Component message = Component.literal("Network diagnostics are not working (yet) in 1.15. Click here to help (re)making it!").setStyle(downloadStyle);
        context.getSource().getPlayerOrException().sendSystemMessage(message);
        // TODO: rewrite gui in LWJGL
        //IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new NetworkDiagnosticsOpenClient(), context.getSource().asPlayer());
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("networkdiagnostics")
                .requires((commandSource) -> commandSource.hasPermission(2))
                .executes(new CommandNetworkDiagnostics());
    }
}
