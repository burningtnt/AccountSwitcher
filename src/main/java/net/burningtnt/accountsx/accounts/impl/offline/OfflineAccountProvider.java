package net.burningtnt.accountsx.accounts.impl.offline;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.burningtnt.accountsx.accounts.AccountSession;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.accounts.api.Memory;
import net.burningtnt.accountsx.accounts.api.UIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class OfflineAccountProvider implements AccountProvider<OfflineAccount> {
    private static final String GUID_PLAYER_NAME = "guid:as.login.offline.widgets.player_name";
    private static final String GUID_PLAYER_UUID = "guid:as.login.offline.widgets.player_uuid";

    @Override
    @Nullable
    public <S extends UIScreen> S configure(Supplier<S> screenSupplier) {
        S screen = screenSupplier.get();

        screen.setTitle("as.general.login");
        screen.putTextInput(GUID_PLAYER_NAME, "as.account.objects.player_name");
        screen.putTextInput(GUID_PLAYER_UUID, "as.account.objects.player_uuid");

        return screen;
    }

    @Override
    public void validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
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
        account.setProfile(
                UUID.randomUUID().toString().replace("-", ""),
                account.getPlayerName(),
                account.getPlayerUUID()
        );
    }

    @Override
    public AccountSession createProfile(OfflineAccount account) {
        MinecraftSessionService sessionService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy()).createMinecraftSessionService();
        BaseAccount.AccountStorage s = account.getAccountStorage();

        return new AccountSession(
                new Session(s.playerName, s.playerUUID, s.accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG),
                sessionService, UserApiService.OFFLINE
        );
    }
}
