package org.server_utilities.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.server_utilities.essentials.EssentialsMod;
import org.server_utilities.essentials.config.MainConfig;
import org.server_utilities.essentials.storage.EssentialsDataStorage;
import org.server_utilities.essentials.util.data.IMinecraftServer;

import java.util.function.Predicate;

public abstract class Command {

    protected final Properties properties;
    public static final String PERMISSION_PREFIX = "fabric_essentials";
    protected static final String PERMISSION_DELIMITER = ".";
    protected static final String SELF_PERMISSION_SUFFIX = "self";
    protected static final String OTHER_PERMISSION_SUFFIX = "other";

    public Command(@NotNull Properties properties) {
        this.properties = properties;
    }

    public void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : this.properties.getLiterals()) {
            LiteralArgumentBuilder<CommandSourceStack> literalArgument = Commands.literal(literal);
            literalArgument.requires(this.properties.getPredicate());
            register(literalArgument);
            dispatcher.register(literalArgument);
        }
    }

    protected abstract void register(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder);

    protected static Predicate<CommandSourceStack> permission(String... permission) {
        return commandSourceStack -> Permissions.check(commandSourceStack, PERMISSION_PREFIX + PERMISSION_DELIMITER + String.join(PERMISSION_DELIMITER, permission));
    }

    public static EssentialsDataStorage getEssentialsDataStorage(CommandContext<CommandSourceStack> ctx) {
        return ((IMinecraftServer)ctx.getSource().getServer()).getEssentialsStorage();
    }

    public static MainConfig getConfig() {
        return EssentialsMod.getConfig().main();
    }

    public static void sendFeedback(CommandContext<CommandSourceStack> ctx, String translation, Object... args) {
        ctx.getSource().sendSuccess(Component.translatable(translation, args), false);
    }

    public static void sendFeedback(ServerPlayer serverPlayer, String translation, Object... args) {
        serverPlayer.sendSystemMessage(Component.translatable(translation, args));
    }

    public static String toName(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        try {
            Entity entity = source.getEntityOrException();
            return entity.getScoreboardName();
        } catch (CommandSyntaxException e) {
            return "Console";
        }
    }

}
