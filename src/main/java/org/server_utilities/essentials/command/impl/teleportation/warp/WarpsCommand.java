package org.server_utilities.essentials.command.impl.teleportation.warp;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import org.server_utilities.essentials.command.Command;
import org.server_utilities.essentials.command.Properties;
import org.server_utilities.essentials.storage.EssentialsData;
import org.server_utilities.essentials.util.teleportation.Warp;

import java.util.List;

public class WarpsCommand extends Command {

    public WarpsCommand() {
        super(Properties.create("warps").permission("warps"));
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        EssentialsData essentialsData = getEssentialsDataStorage(ctx).getEssentialsData();
        List<Warp> warps = essentialsData.getWarps();
        if (warps.isEmpty()) {
            sendFeedback(ctx, "text.fabric-essentials.command.warps.no_warp");
        } else {
            sendFeedback(ctx, "text.fabric-essentials.command.warps");
            Component warpsComponent = ComponentUtils.formatList(warps.stream().map(Warp::getName).toList(), name -> Component.literal(name).withStyle(
                    Style.EMPTY
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("text.fabric-essentials.command.warps.hover")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s", WarpCommand.WARP_COMMAND, name)))
            ));
            ctx.getSource().sendSuccess(warpsComponent, false);
        }
        return warps.size();
    }
}
