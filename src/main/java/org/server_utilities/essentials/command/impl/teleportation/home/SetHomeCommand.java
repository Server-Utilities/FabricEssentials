package org.server_utilities.essentials.command.impl.teleportation.home;

import com.mojang.authlib.GameProfile;
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
import org.server_utilities.essentials.command.Properties;
import org.server_utilities.essentials.command.util.OptionalOfflineTargetCommand;
import org.server_utilities.essentials.config.homes.HomesConfig;
import org.server_utilities.essentials.config.homes.HomesLimit;
import org.server_utilities.essentials.storage.DataStorage;
import org.server_utilities.essentials.storage.PlayerData;
import org.server_utilities.essentials.util.teleportation.Home;
import org.server_utilities.essentials.util.teleportation.Location;

import java.util.List;
import java.util.Optional;

public class SetHomeCommand extends OptionalOfflineTargetCommand {

    private static final SimpleCommandExceptionType ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("text.fabric-essentials.command.sethome.already_exists"));
    private static final String NAME = "name";
    private static final String PERMISSION_LIMIT = "limit";
    private static final String PERMISSION_LIMIT_BYPASS = "bypass";

    public SetHomeCommand() {
        super(Properties.create("sethome").permission("sethome"));
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        RequiredArgumentBuilder<CommandSourceStack, String> name = Commands.argument(NAME, StringArgumentType.string());
        registerOptionalArgument(name);
        literal.then(name);
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> ctx, GameProfile target, boolean self) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, NAME);
        ServerPlayer serverPlayer = ctx.getSource().getPlayerOrException();
        DataStorage dataStorage = DataStorage.STORAGE;
        PlayerData playerData = dataStorage.getPlayerData(ctx.getSource().getServer(), target.getId());
        Optional<Home> optional = playerData.getHome(name);
        if (optional.isEmpty()) {
            List<Home> homes = playerData.getHomes();
            int limit = getHomesLimit(ctx.getSource());
            if (homes.size() >= limit) {
                ctx.getSource().sendFailure(Component.translatable("text.fabric-essentials.command.sethome.limit"));
                return FAILURE;
            } else {
                Home newHome = new Home(name, new Location(serverPlayer));
                homes.add(newHome);
                dataStorage.savePlayerData(ctx.getSource().getServer(), target.getId(), playerData);
                sendQueryFeedbackWithOptionalTarget(ctx, self, new Object[]{name}, new Object[]{name, target.getName()});

                return SUCCESS;
            }
        } else {
            throw ALREADY_EXISTS.create();
        }
    }

    private int getHomesLimit(CommandSourceStack source) {
        if (predicate(PERMISSION_LIMIT, PERMISSION_LIMIT_BYPASS).test(source)) {
            return Integer.MAX_VALUE;
        } else {
            HomesConfig homesConfig = config().homes;
            int limit = homesConfig.defaultLimit;
            int added = 0;
            for (HomesLimit homesLimit : homesConfig.homesLimits) {
                if (predicate(PERMISSION_LIMIT, homesLimit.permission).test(source)) {
                    if (homesLimit.stacks) {
                        added += homesLimit.limit;
                    } else {
                        limit = Math.max(limit, homesLimit.limit);
                    }
                }
            }
            limit += added;
            return limit;
        }
    }

}
