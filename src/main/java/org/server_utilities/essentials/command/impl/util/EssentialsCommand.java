package org.server_utilities.essentials.command.impl.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.StopWatch;
import org.server_utilities.essentials.command.Command;
import org.server_utilities.essentials.command.Properties;
import org.server_utilities.essentials.command.impl.util.importer.DataImporter;
import org.server_utilities.essentials.command.impl.util.importer.KiloEssentialsImporter;
import org.server_utilities.essentials.config.ConfigManager;
import org.server_utilities.essentials.mixin.CommandSourceStackAccessor;
import org.spongepowered.configurate.ConfigurateException;

import java.util.Arrays;
import java.util.Optional;

public class EssentialsCommand extends Command {

    public static final SimpleCommandExceptionType UNKNOWN = new SimpleCommandExceptionType(Component.translatable("text.fabric-essentials.command.essentials.import.unknown"));
    private static final DataImporter[] DATA_IMPORTERS = new DataImporter[]{KiloEssentialsImporter.KILO_ESSENTIALS};

    public EssentialsCommand() {
        super(Properties.create("essentials").permission("essentials"));
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                Commands.literal("reload")
                        .executes(this::reload)

        ).then(
                Commands.literal("import")
                        .then(
                                Commands.argument("importer", StringArgumentType.string()).suggests(IMPORTER_PROVIDER)
                                        .requires(predicate("import"))
                                        .executes(this::importData)
                        )
        );
    }

    private int reload(CommandContext<CommandSourceStack> ctx) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ConfigManager.INSTANCE.load();
            stopWatch.stop();
            sendSuccess(ctx.getSource(), "reload", stopWatch.getTime());
            return SUCCESS;
        } catch (ConfigurateException e) {
            LOGGER.error("An error occurred while loading the config, keeping old values", e);
            sendFailure(ctx.getSource(), join("reload", "error"), e.getMessage());
            return FAILURE;
        }
    }

    // TODO: Only console allowed
    private int importData(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!((CommandSourceStackAccessor)ctx.getSource()).getSource().equals(ctx.getSource().getServer())) {
            sendFailure(ctx.getSource(), join("import", "console"));
            return FAILURE;
        }
        String importerId = StringArgumentType.getString(ctx, "importer");
        Optional<DataImporter> optional = Arrays.stream(DATA_IMPORTERS).filter(importer -> importer.getImporterId().equals(importerId)).findFirst();
        DataImporter dataImporter = optional.orElseThrow(UNKNOWN::create);
        dataImporter.importData(ctx.getSource().getServer());
        return SUCCESS;
    }

    public static final SuggestionProvider<CommandSourceStack> IMPORTER_PROVIDER = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(DATA_IMPORTERS).map(DataImporter::getImporterId), builder);


}
