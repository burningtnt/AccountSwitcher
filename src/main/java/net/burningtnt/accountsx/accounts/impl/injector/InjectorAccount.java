package net.burningtnt.accountsx.accounts.impl.injector;

import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;

public class InjectorAccount extends BaseAccount {
    private final String server;

    private final String userName;

    private final String password;

    public InjectorAccount(String accessToken, String playerName, String playerUUID, String server, String userName, String password) {
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
