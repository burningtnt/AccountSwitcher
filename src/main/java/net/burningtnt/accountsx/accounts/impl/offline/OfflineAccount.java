package net.burningtnt.accountsx.accounts.impl.offline;

import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;

public final class OfflineAccount extends BaseAccount {
    public OfflineAccount(String accessToken, String playerName, String playerUUID) {
        super(accessToken, playerName, playerUUID, AccountType.OFFLINE);
    }
}
