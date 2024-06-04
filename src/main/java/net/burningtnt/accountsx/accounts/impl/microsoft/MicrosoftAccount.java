package net.burningtnt.accountsx.accounts.impl.microsoft;

import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;

public final class  MicrosoftAccount extends BaseAccount {
    private String microsoftAccountAccessToken;

    private String microsoftAccountRefreshToken;

    public MicrosoftAccount(String accessToken, String playerName, String playerUUID, String microsoftAccountAccessToken, String microsoftAccountRefreshToken) {
        super(accessToken, playerName, playerUUID, AccountType.MICROSOFT);
        this.microsoftAccountAccessToken = microsoftAccountAccessToken;
        this.microsoftAccountRefreshToken = microsoftAccountRefreshToken;
    }

    public String getMicrosoftAccountAccessToken() {
        return microsoftAccountAccessToken;
    }

    public String getMicrosoftAccountRefreshToken() {
        return microsoftAccountRefreshToken;
    }

    public void setMicrosoftAccountToken(String microsoftAccountAccessToken, String microsoftAccountRefreshToken) {
        this.microsoftAccountAccessToken = microsoftAccountAccessToken;
        this.microsoftAccountRefreshToken = microsoftAccountRefreshToken;
    }
}
