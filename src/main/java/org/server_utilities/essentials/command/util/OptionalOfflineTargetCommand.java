package org.server_utilities.essentials.command.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import org.server_utilities.essentials.command.Properties;

import java.util.Collection;

public abstract class OptionalOfflineTargetCommand extends OptionalTargetCommand<GameProfileArgument.Result, GameProfile> {

    private static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.toomany"));
    private static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.player"));

    public OptionalOfflineTargetCommand(Properties properties) {
        super(properties);
    }

    public OptionalOfflineTargetCommand(Properties properties, String targetArgumentId) {
        super(properties, targetArgumentId);
    }

    @Override
    protected ArgumentType<GameProfileArgument.Result> getArgumentType() {
        return GameProfileArgument.gameProfile();
    }

    @Override
    protected GameProfile getArgument(CommandContext<CommandSourceStack> ctx, String string) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, string);
        if (profiles.isEmpty()) {
            throw NO_PLAYERS_FOUND.create();
        } else {
            if (profiles.size() != 1) {
                throw ERROR_NOT_SINGLE_PLAYER.create();
            } else {
                return profiles.iterator().next();
            }
        }
    }
}
