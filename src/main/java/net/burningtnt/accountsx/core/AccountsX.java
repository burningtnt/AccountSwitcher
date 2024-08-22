package net.burningtnt.accountsx.core;

import net.burningtnt.accountsx.core.accounts.impl.microsoft.MicrosoftConstants;
import net.burningtnt.accountsx.core.manager.AccountManager;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountsX implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MC_ADAPTER_ID = "accountsx-adapter-mc";
    public static final String AUTHLIB_ADAPTER_ID = "accountsx-adapter-authlib";
    public static final String MOD_ID = "accountsx";
    public static final String MOD_NAME = "Accounts X";

    @Override
    public void onInitializeClient() {
        AccountManager.initialize();
        MicrosoftConstants.initialize();
    }
}
