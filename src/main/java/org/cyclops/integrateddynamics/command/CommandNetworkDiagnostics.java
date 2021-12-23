package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Command for opening the network diagnostics gui.
 * @author rubensworks
 *
 */
public class CommandNetworkDiagnostics implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Style downloadStyle = Style.EMPTY;
        downloadStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to visit the relevant GitHub issue")));
        downloadStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/CyclopsMC/IntegratedDynamics/issues/863"));
        ITextComponent message = new StringTextComponent("Network diagnostics are not working (yet) in 1.15. Click here to help (re)making it!").setStyle(downloadStyle);
        context.getSource().getPlayerOrException().sendMessage(message, Util.NIL_UUID);
        // TODO: rewrite gui in LWJGL
        //IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new NetworkDiagnosticsOpenClient(), context.getSource().asPlayer());
        return 0;
    }

    public static LiteralArgumentBuilder<CommandSource> make() {
        return Commands.literal("networkdiagnostics")
                .requires((commandSource) -> commandSource.hasPermission(2))
                .executes(new CommandNetworkDiagnostics());
    }
}
