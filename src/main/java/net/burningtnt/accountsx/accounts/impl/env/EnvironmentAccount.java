package net.burningtnt.accountsx.accounts.impl.env;

import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.minecraft.client.util.Session;

public final class EnvironmentAccount extends BaseAccount {
    public EnvironmentAccount(String accessToken, String playerName, String playerUUID) {
        super(accessToken, playerName, playerUUID, AccountType.ENV_DEFAULT);
    }

    public static BaseAccount fromSession(Session session) {
        return new EnvironmentAccount(session.getAccessToken(), session.getUsername(), session.getUuid());
    }
}
