package net.burningtnt.accountsx.accounts;

import com.mojang.authlib.Environment;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.burningtnt.accountsx.accounts.gui.Memory;
import net.burningtnt.accountsx.accounts.gui.UIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

import java.io.IOException;
import java.util.Optional;

public interface AccountProvider<T extends BaseAccount> {
    int STATE_IMMEDIATE_CLOSE = 0;

    int STATE_HANDLE = 1;

    void configure(UIScreen screen);

    int validate(UIScreen screen, Memory memory) throws IllegalArgumentException;

    T login(Memory memory) throws IOException;

    void refresh(T account) throws IOException;

    default AccountSession createProfile(T account) throws IOException {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy(), createDefaultEnvironment());
        MinecraftSessionService sessionService = service.createMinecraftSessionService();
        BaseAccount.AccountStorage s = account.storage;

        try {
            return new AccountSession(
                    createSession(s),
                    sessionService, service.createUserApiService(s.getAccessToken())
            );
        } catch (AuthenticationException e) {
            throw new IOException("Failed to create session service.", e);
        }
    }

    static Environment createDefaultEnvironment() {
        return Environment.create(
                "https://authserver.mojang.com",
                "https://api.mojang.com",
                "https://sessionserver.mojang.com",
                "https://api.minecraftservices.com",
                "PROD"
        );
    }

    static Session createSession(BaseAccount.AccountStorage s) {
        return new Session(s.getPlayerName(), AccountUUID.toMinecraftStyleString(s.getPlayerUUID()), s.getAccessToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
    }

    @SuppressWarnings("unchecked")
    static <T extends BaseAccount> AccountProvider<T> getProvider(T account) {
        return (AccountProvider<T>) account.getAccountType().getAccountProvider();
    }
}
