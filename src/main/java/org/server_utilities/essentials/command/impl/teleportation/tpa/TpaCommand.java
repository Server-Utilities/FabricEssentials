package org.server_utilities.essentials.command.impl.teleportation.tpa;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.api.Message;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.server_utilities.essentials.command.Command;
import org.server_utilities.essentials.util.TpaManager;

public class TpaCommand extends Command {

    private static final String TARGET_ARGUMENT_ID = "target";
    private final TpaManager.Direction direction;

    public static final TpaCommand TPA = new TpaCommand(TpaManager.Direction.THERE);
    public static final TpaCommand TPA_HERE = new TpaCommand(TpaManager.Direction.HERE);

    private TpaCommand(TpaManager.Direction direction) {
        super(direction.getProperties());
        this.direction = direction;
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> target = Commands.argument(TARGET_ARGUMENT_ID, EntityArgument.player());
        target.executes(this::execute);
        literal.then(target);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, TARGET_ARGUMENT_ID);
        TpaManager.Participants participants = new TpaManager.Participants(ctx.getSource().getPlayerOrException().getUUID(), target.getUUID());
        TpaManager.Direction direction = TpaManager.INSTANCE.getRequest(participants);
        if (direction == this.direction) {
            ctx.getSource().sendFailure(Message.message("fabric-essentials.commands.tpa.pending", PlaceholderContext.of(target)));
            return FAILURE;
        }
        TpaManager.INSTANCE.addRequest(participants, this.direction);
        ctx.getSource().sendSuccess(Message.message("fabric-essentials.commands." + this.direction.getTranslationKey() + ".self", PlaceholderContext.of(target)), false);
        target.sendSystemMessage(Message.message("fabric-essentials.commands." + this.direction.getTranslationKey() + ".victim", PlaceholderContext.of(ctx.getSource())));
        return SUCCESS;
    }

}
