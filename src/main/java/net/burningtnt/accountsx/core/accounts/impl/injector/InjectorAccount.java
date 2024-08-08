package net.burningtnt.accountsx.core.accounts.impl.injector;

import net.burningtnt.accountsx.core.accounts.AccountType;
import net.burningtnt.accountsx.core.accounts.BaseAccount;

import java.util.UUID;

public class InjectorAccount extends BaseAccount {
    private final String server;

    private final String userName;

    private final String password;

    public InjectorAccount(String accessToken, String playerName, UUID playerUUID, String server, String userName, String password) {
        super(accessToken, playerName, playerUUID, AccountType.INJECTOR);
        this.server = server;
        this.userName = userName;
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
