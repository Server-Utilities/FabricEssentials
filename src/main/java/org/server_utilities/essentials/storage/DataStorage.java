package org.server_utilities.essentials.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.JsonDataStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.server_utilities.essentials.EssentialsMod;
import org.server_utilities.essentials.config.ConfigManager;
import org.server_utilities.essentials.storage.adapter.RecordTypeAdapterFactory;
import org.server_utilities.essentials.storage.adapter.ResourceLocationAdapter;
import org.server_utilities.essentials.storage.adapter.Vec3Adapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class DataStorage {

    public static final DataStorage STORAGE = new DataStorage();
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setLenient()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter())
            .registerTypeAdapter(Vec3.class, new Vec3Adapter())
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .create();
    private static final Path DATA_PATH = ConfigManager.CONFIG_DIR.resolve("data.json");

    public static final JsonDataStorage<PlayerData> USER_DATA_STORAGE = new JsonDataStorage<>("fabric-essentials", PlayerData.class, DataStorage.GSON);
    private ServerData serverData;

    private DataStorage() {
    }

    @ApiStatus.Internal
    public void init() {
        PlayerDataApi.register(USER_DATA_STORAGE);
        ServerLifecycleEvents.SERVER_STARTING.register(this::load);
    }

    @NotNull
    public ServerData getServerData() {
        return Objects.requireNonNull(serverData, "Server data was not yet loaded!");
    }

    private void load(MinecraftServer server) {
        if (!DATA_PATH.toFile().exists()) {
            serverData = new ServerData();
        } else {
            try {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(DATA_PATH.toFile()), StandardCharsets.UTF_8));
                ServerData essentialsData = GSON.fromJson(json, ServerData.class);
                this.serverData = Objects.requireNonNullElseGet(essentialsData, ServerData::new);
            } catch (IOException e) {
                EssentialsMod.LOGGER.error("Couldn't load server data", e);
                this.serverData = new ServerData();
            }
        }
    }

    @ApiStatus.Internal
    public void save() {
        ConfigManager.CONFIG_DIR.toFile().mkdirs();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_PATH.toFile()), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(serverData));
            writer.close();
        } catch (IOException e) {
            EssentialsMod.LOGGER.error("Couldn't save server data", e);
        }
    }

    @NotNull
    public PlayerData getPlayerData(ServerPlayer player) {
        PlayerData playerData = PlayerDataApi.getCustomDataFor(player, USER_DATA_STORAGE);
        if (playerData == null) playerData = new PlayerData();
        return playerData;
    }

    @NotNull
    public PlayerData getAndSavePlayerData(ServerPlayer player) {
        PlayerData playerData = PlayerDataApi.getCustomDataFor(player, USER_DATA_STORAGE);
        if (playerData == null) {
            playerData = new PlayerData();
            PlayerDataApi.setCustomDataFor(player, USER_DATA_STORAGE, playerData);
        }
        return playerData;
    }

    @NotNull
    public PlayerData getPlayerData(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return getOfflinePlayerData(ctx.getSource().getServer(), ctx.getSource().getPlayerOrException().getUUID());
    }

    @NotNull
    public PlayerData getOfflinePlayerData(CommandContext<CommandSourceStack> ctx, GameProfile target) {
        return getOfflinePlayerData(ctx.getSource().getServer(), target.getId());
    }

    @NotNull
    public PlayerData getOfflinePlayerData(MinecraftServer server, UUID uuid) {
        PlayerData playerData = PlayerDataApi.getCustomDataFor(server, uuid, USER_DATA_STORAGE);
        if (playerData == null) {
            playerData = new PlayerData();
        }
        return playerData;
    }

    public void saveOfflinePlayerData(CommandContext<CommandSourceStack> ctx, GameProfile target, PlayerData playerData) {
        saveOfflinePlayerData(ctx.getSource().getServer(), target.getId(), playerData);
    }

    public void saveOfflinePlayerData(MinecraftServer server, UUID uuid, PlayerData playerData) {
        PlayerDataApi.setCustomDataFor(server, uuid, USER_DATA_STORAGE, playerData);
    }

}
