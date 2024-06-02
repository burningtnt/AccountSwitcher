package net.burningtnt.accountsx.config;

import net.burningtnt.accountsx.AccountsX;
import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.utils.IOUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigProcessor {
    private ConfigProcessor() {
    }

    private static final String CONFIG_LOCATION = "accountsx/accounts.json";

    private static final class Config {
        private final int version;

        private final List<BaseAccount> accounts;

        private Config(List<BaseAccount> accounts) {
            this.version = 0;
            this.accounts = accounts;
        }
    }

    public static List<? extends BaseAccount> load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_LOCATION);

        try {
            if (!Files.exists(configFile)) {
                Files.createDirectories(configFile.getParent());
                Files.writeString(configFile, IOUtils.GSON.toJson(new Config(List.of())));
                return List.of();
            }

            if (!Files.isRegularFile(configFile)) {
                Files.delete(configFile);
                Files.writeString(configFile, IOUtils.GSON.toJson(new Config(List.of())));
                return List.of();
            }

            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                Config config = IOUtils.GSON.fromJson(reader, Config.class);

                return config.accounts;
            }
        } catch (Exception e) {
            AccountsX.LOGGER.warn("Cannot load the config file.", e);
            return List.of();
        }
    }

    public static void write() throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_LOCATION);

        List<BaseAccount> accounts = new ArrayList<>();

        for (BaseAccount account : AccountManager.getAccounts()) {
            if (account.getAccountType() != AccountType.ENV_DEFAULT) {
                accounts.add(account);
            }
        }

        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            IOUtils.GSON.toJson(new Config(accounts), writer);
        }
    }
}
