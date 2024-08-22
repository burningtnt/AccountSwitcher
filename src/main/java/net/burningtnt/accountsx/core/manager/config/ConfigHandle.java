package net.burningtnt.accountsx.core.manager.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.accountsx.core.AccountsX;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.accounts.model.AccountType;
import net.burningtnt.accountsx.core.manager.AccountManager;
import net.burningtnt.accountsx.core.utils.NetworkUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigHandle {
    private ConfigHandle() {
    }

    private static final String CONFIG_LOCATION = "accountsx/accounts.json";

    private static final class Config {
        public static final int CURRENT_VERSION = ConfigVersion.VALUES[ConfigVersion.VALUES.length - 1].getVersion();

        private final int version;

        private final List<BaseAccount> accounts;

        private Config(List<BaseAccount> accounts) {
            this.version = CURRENT_VERSION;
            this.accounts = accounts;
        }
    }

    public static List<? extends BaseAccount> load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_LOCATION);

        try {
            if (!Files.exists(configFile)) {
                Files.createDirectories(configFile.getParent());
                Files.writeString(configFile, NetworkUtils.GSON.toJson(new Config(List.of())));
                return List.of();
            }

            if (!Files.isRegularFile(configFile)) {
                Files.delete(configFile);
                Files.writeString(configFile, NetworkUtils.GSON.toJson(new Config(List.of())));
                return List.of();
            }

            JsonElement data;
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                data = NetworkUtils.GSON.fromJson(reader, JsonElement.class);
            }

            if (data instanceof JsonObject jo) {
                if (jo.get("version") instanceof JsonPrimitive versionJP && versionJP.isNumber()) {
                    int configVersion = versionJP.getAsNumber().intValue();

                    for (ConfigVersion value : ConfigVersion.VALUES) {
                        if (configVersion < value.getVersion()) {
                            value.upgrade(jo);
                        }
                    }

                    return NetworkUtils.GSON.fromJson(data, Config.class).accounts;
                }
            }

            throw new IllegalStateException("Illegal config.");
        } catch (Throwable t) {
            AccountsX.LOGGER.warn("Cannot load the config file.", t);
            return List.of();
        }
    }

    public static void write() throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_LOCATION);

        List<BaseAccount> accounts = new ArrayList<>();

        for (BaseAccount account : AccountManager.getAccountsView()) {
            if (account.getAccountType() != AccountType.ENV_DEFAULT) {
                accounts.add(account);
            }
        }

        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            NetworkUtils.GSON.toJson(new Config(accounts), writer);
        }
    }
}
