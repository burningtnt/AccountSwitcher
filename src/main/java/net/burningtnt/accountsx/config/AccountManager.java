package net.burningtnt.accountsx.config;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import net.burningtnt.accountsx.accounts.*;
import net.burningtnt.accountsx.accounts.impl.env.EnvironmentAccount;
import net.burningtnt.accountsx.mixins.MinecraftClientAccessor;
import net.burningtnt.accountsx.mixins.PlayerSkinProviderAccessor;
import net.burningtnt.accountsx.utils.I18NHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AccountManager {
    private static final List<BaseAccount> accounts = new CopyOnWriteArrayList<>();
    private static final List<BaseAccount> readonlyAccounts = Collections.unmodifiableList(accounts);
    private static volatile BaseAccount current = null;

    private AccountManager() {
    }

    public static List<BaseAccount> getAccounts() {
        return readonlyAccounts;
    }

    public static BaseAccount getCurrentAccount() {
        return current;
    }

    public static void initialize() {
        BaseAccount defaultAccount = EnvironmentAccount.fromSession(MinecraftClient.getInstance().getSession());
        accounts.add(defaultAccount);
        current = defaultAccount;

        accounts.addAll(ConfigProcessor.load());

        for (BaseAccount account : accounts) {
            if (account.getAccountState() != AccountState.AUTHORIZED) {
                AccountTaskExecutor.submit(() -> {
                    account.setProfileState(AccountState.AUTHORIZING);

                    try {
                        AccountProvider.getProvider(account).refresh(account);
                    } catch (Throwable t) {
                        account.setProfileState(AccountState.UNAUTHORIZED);
                    }
                });
            }
        }
    }

    public static void dropAccount(BaseAccount account) {
        checkThread();

        if (account.getAccountType() == AccountType.ENV_DEFAULT) {
            return;
        }

        accounts.remove(account);
    }

    public static void addAccount(BaseAccount account) {
        checkThread();

        accounts.add(account);
        AccountTaskExecutor.submit(ConfigProcessor::write);
    }

    public static void moveAccount(BaseAccount account, int index) {
        checkThread();

        accounts.remove(account);
        accounts.add(index, account);
    }

    public static <T extends BaseAccount> void switchAccount(T account, AccountSession service) {
        checkThread();

        if (account.getAccountState() != AccountState.AUTHORIZED) {
            throw new IllegalStateException("Account is not authorized.");
        }

        MinecraftClient client = MinecraftClient.getInstance();
        current = account;

        MinecraftSessionService sessionService = service.getSessionService();
        UserApiService userAPIService = service.getUserAPIService();

        ((MinecraftClientAccessor) client).setSession(service.getSession());
        ((MinecraftClientAccessor) client).setSessionService(sessionService);
        ((MinecraftClientAccessor) client).setUserAPIService(userAPIService);
        ((MinecraftClientAccessor) client).setSocialInteractionManager(new SocialInteractionsManager(client, userAPIService));
        ((MinecraftClientAccessor) client).setSkinProvider(new PlayerSkinProvider(
                client.getTextureManager(),
                ((PlayerSkinProviderAccessor) client.getSkinProvider()).getSkinCacheDir(),
                sessionService
        ));
    }

    public static Text getCurrentAccountInfoText() {
        return I18NHelper.translateUsingAccount(current);
    }

    private static void checkThread() {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("Should in Minecraft Client Thread.");
        }
    }
}
