package net.burningtnt.accountsx.accounts.impl.env;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.burningtnt.accountsx.AccountsX;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.AccountSession;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.accounts.api.Memory;
import net.burningtnt.accountsx.accounts.api.UIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public final class EnvironmentAccountProvider implements AccountProvider<EnvironmentAccount> {
    @Override
    public <S extends UIScreen> @Nullable S configure(Supplier<S> screenSupplier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(UIScreen screen, Memory memory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnvironmentAccount login(Memory memory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(EnvironmentAccount account) {
    }

    @Override
    public AccountSession createProfile(EnvironmentAccount account) throws IOException {
        try {
            return AccountProvider.super.createProfile(account);
        } catch (IOException e) {
            AccountsX.LOGGER.warn("Cannot authorize the environment account. Fallback to Offline Mode.");

            MinecraftSessionService sessionService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy()).createMinecraftSessionService();
            BaseAccount.AccountStorage s = account.getAccountStorage();

            return new AccountSession(
                    new Session(s.playerName, s.playerUUID, s.accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG),
                    sessionService, UserApiService.OFFLINE
            );
        }
    }
}
