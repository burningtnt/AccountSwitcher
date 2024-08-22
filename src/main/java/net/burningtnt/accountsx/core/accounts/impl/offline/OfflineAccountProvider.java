package net.burningtnt.accountsx.core.accounts.impl.offline;

import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.AccountUUID;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.accounts.model.context.AccountContext;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;

import java.util.UUID;

public class OfflineAccountProvider implements AccountProvider<OfflineAccount> {
    private static final String GUID_PLAYER_NAME = "guid:as.login.offline.widgets.player_name";
    private static final String GUID_PLAYER_UUID = "guid:as.login.offline.widgets.player_uuid";

    @Override
    public AccountContext createAccountContext(OfflineAccount account) {
        return null;
    }

    @Override
    public void configure(UIScreen screen) {
        screen.setTitle("as.account.general.login");
        screen.putTextInput(GUID_PLAYER_NAME, "as.account.objects.player_name");
        screen.putTextInput(GUID_PLAYER_UUID, "as.account.objects.player_uuid");
    }

    @Override
    public int validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        String playerName = screen.getTextInput(GUID_PLAYER_NAME);
        memory.set(GUID_PLAYER_NAME, playerName);

        String playerUUIDString = screen.getTextInput(GUID_PLAYER_UUID);
        if (playerUUIDString.isEmpty()) {
            memory.set(GUID_PLAYER_UUID, AccountUUID.ofPlayerName(playerName));
        } else {
            try {
                memory.set(GUID_PLAYER_UUID, AccountUUID.parse(playerUUIDString));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot parse current UUID: " + playerUUIDString);
            }
        }

        return STATE_IMMEDIATE_CLOSE;
    }

    @Override
    public OfflineAccount login(Memory memory) {
        return new OfflineAccount(
                UUID.randomUUID().toString().replace("-", ""),
                memory.get(GUID_PLAYER_NAME, String.class),
                memory.get(GUID_PLAYER_UUID, UUID.class)
        );
    }

    @Override
    public void refresh(OfflineAccount account) {
        BaseAccount.AccountStorage s = account.getAccountStorage();

        account.setProfile(
                UUID.randomUUID().toString().replace("-", ""),
                s.getPlayerName(),
                s.getPlayerUUID()
        );
    }
}
