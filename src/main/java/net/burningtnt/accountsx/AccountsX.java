package net.burningtnt.accountsx;

import net.burningtnt.accountsx.config.AccountManager;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountsX implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "accountsx";
    public static final String MOD_NAME = "Accounts X";

    @Override
    public void onInitializeClient() {
        LOGGER.info("[" + MOD_NAME + "] Initializing...");

        AccountManager.initialize();
    }
}
