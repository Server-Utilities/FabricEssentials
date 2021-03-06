package org.server_utilities.essentials.command.impl.teleportation.warp;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.server_utilities.essentials.command.Command;
import org.server_utilities.essentials.command.Properties;
import org.server_utilities.essentials.storage.EssentialsData;
import org.server_utilities.essentials.util.teleportation.Location;
import org.server_utilities.essentials.util.teleportation.Warp;

import java.util.List;
import java.util.Optional;

public class SetWarpCommand extends Command {

    private static final SimpleCommandExceptionType ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("text.fabric-essentials.command.setwarp.already_exists"));
    private static final String NAME = "name";

    public SetWarpCommand() {
        super(Properties.create("setwarp").permission("setwarp"));
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        RequiredArgumentBuilder<CommandSourceStack, String> name = Commands.argument(NAME, StringArgumentType.string());
        name.executes(ctx -> setWarp(ctx, StringArgumentType.getString(ctx, NAME), false));
        literal.then(name);
    }

    private int setWarp(CommandContext<CommandSourceStack> ctx, String name, boolean hasAlias) throws CommandSyntaxException {
        ServerPlayer serverPlayer = ctx.getSource().getPlayerOrException();
        EssentialsData essentialsData = getEssentialsDataStorage(ctx).getEssentialsData();
        Optional<Warp> optional = essentialsData.getWarp(name);
        if (optional.isEmpty()) {
            List<Warp> warps = essentialsData.getWarps();
            Warp newWarp = new Warp(name, new Location(serverPlayer), hasAlias);
            warps.add(newWarp);
            sendFeedback(ctx, "text.fabric-essentials.command.setwarp", name);
            return 1;
        } else {
            throw ALREADY_EXISTS.create();
        }
    }

}
