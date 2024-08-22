package net.burningtnt.accountsx.core.accounts.impl.injector.impl;

import net.burningtnt.accountsx.core.accounts.impl.injector.AbstractInjectorAccount;
import net.burningtnt.accountsx.core.accounts.impl.injector.AbstractInjectorAccountProvider;
import net.burningtnt.accountsx.core.accounts.model.AccountType;

import java.util.BitSet;
import java.util.UUID;

public final class UnitedInjectorAccountProvider extends AbstractInjectorAccountProvider<UnitedInjectorAccountProvider.UnitedInjectorAccount> {
    public UnitedInjectorAccountProvider() {
        super("as.account.objects.server_id", "United-Injector");
    }

    private static final BitSet SAFE_SERVER_ID = new BitSet(128);

    static {
        SAFE_SERVER_ID.set('a', 'z' + 1);
        SAFE_SERVER_ID.set('A', 'Z' + 1);
        SAFE_SERVER_ID.set('0', '9' + 1);
    }

    @Override
    protected void validateServerBaseURL(String server) throws IllegalArgumentException {
        int length = server.length();
        if (length != 32) {
            throw new IllegalArgumentException("Server ID must be 32 characters long: " + server);
        }
        for (int i = 0; i < length; i++) {
            char c = server.charAt(i);
            if (c >= 128 || !SAFE_SERVER_ID.get(c)) {
                throw new IllegalArgumentException("Server ID must only contains a-z, A-Z, 0-9.");
            }
        }
    }

    @Override
    protected String transformServerBaseURL(String server) {
        return "https://auth.mc-user.com:233/" + server + '/';
    }

    @Override
    protected UnitedInjectorAccount createAccount(String accessToken, String playerName, UUID playerUUID, String server, String preferredPlayerUUID) {
        return new UnitedInjectorAccount(accessToken, playerName, playerUUID, server, preferredPlayerUUID);
    }

    public static class UnitedInjectorAccount extends AbstractInjectorAccount {
        public UnitedInjectorAccount(String accessToken, String playerName, UUID playerUUID, String server, String preferredPlayerUUID) {
            super(accessToken, playerName, playerUUID, server, preferredPlayerUUID, AccountType.UNITED_INJECTOR);
        }
    }
}
