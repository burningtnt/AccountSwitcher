package net.burningtnt.accountsx.accounts.impl.offline;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.burningtnt.accountsx.accounts.AccountSession;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.accounts.gui.Memory;
import net.burningtnt.accountsx.accounts.gui.UIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

import java.util.Optional;
import java.util.UUID;

public class OfflineAccountProvider implements AccountProvider<OfflineAccount> {
    private static final String GUID_PLAYER_NAME = "guid:as.login.offline.widgets.player_name";
    private static final String GUID_PLAYER_UUID = "guid:as.login.offline.widgets.player_uuid";

    @Override
    public void configure(UIScreen screen) {
        screen.setTitle("as.general.login");
        screen.putTextInput(GUID_PLAYER_NAME, "as.account.objects.player_name");
        screen.putTextInput(GUID_PLAYER_UUID, "as.account.objects.player_uuid");
    }

    @Override
    public int validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        memory.set(GUID_PLAYER_NAME, screen.getTextInput(GUID_PLAYER_NAME));

        String playerUUIDString = screen.getTextInput(GUID_PLAYER_UUID);
        if (playerUUIDString.isEmpty()) {
            // TODO: Use specific way to generate UUID.
            memory.set(GUID_PLAYER_UUID, UUID.randomUUID().toString().replace("-", ""));
        } else {
            try {
                // TODO: Parse UUID
                memory.set(GUID_PLAYER_UUID, UUID.fromString(playerUUIDString).toString().replace("-", ""));
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
                memory.get(GUID_PLAYER_UUID, String.class)
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

    @Override
    public AccountSession createProfile(OfflineAccount account) {
        MinecraftSessionService sessionService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy()).createMinecraftSessionService();
        BaseAccount.AccountStorage s = account.getAccountStorage();

        return new AccountSession(
                new Session(s.getPlayerName(), s.getPlayerUUID(), s.getAccessToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG),
                sessionService, UserApiService.OFFLINE
        );
    }
}
