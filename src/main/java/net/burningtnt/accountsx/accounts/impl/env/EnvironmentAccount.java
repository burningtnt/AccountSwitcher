package net.burningtnt.accountsx.accounts.impl.env;

import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;

import java.util.UUID;

public final class EnvironmentAccount extends BaseAccount {
    public EnvironmentAccount(String accessToken, String playerName, UUID playerUUID) {
        super(accessToken, playerName, playerUUID, AccountType.ENV_DEFAULT);
    }
}
