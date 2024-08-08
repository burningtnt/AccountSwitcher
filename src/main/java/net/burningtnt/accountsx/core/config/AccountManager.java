package net.burningtnt.accountsx.core.config;

import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.AccountState;
import net.burningtnt.accountsx.core.accounts.AccountType;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.adapters.api.AccountSession;
import net.burningtnt.accountsx.core.adapters.Adapters;
import net.burningtnt.accountsx.core.utils.threading.ThreadState;
import net.burningtnt.accountsx.core.utils.threading.Threading;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AccountManager {
    private static final List<BaseAccount> accounts = new CopyOnWriteArrayList<>();
    private static final List<BaseAccount> readonlyAccounts = Collections.unmodifiableList(accounts);
    private static volatile BaseAccount current = null;

    private AccountManager() {
    }

    public static List<BaseAccount> getAccountsView() {
        return readonlyAccounts;
    }

    public static BaseAccount getCurrentAccount() {
        return current;
    }

    public static void initialize() {
        BaseAccount defaultAccount = Adapters.getMinecraftAdapter().fromCurrentClient();
        accounts.add(defaultAccount);
        current = defaultAccount;

        accounts.addAll(ConfigHandle.load());

        for (BaseAccount account : accounts) {
            if (account.getAccountState() != AccountState.AUTHORIZED) {
                AccountWorker.submit(() -> {
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

    @ThreadState("Minecraft Client Thread")
    public static void dropAccount(BaseAccount account) {
        Threading.checkMinecraftClientThread();

        if (account.getAccountType() == AccountType.ENV_DEFAULT) {
            return;
        }

        accounts.remove(account);
        AccountWorker.submit(ConfigHandle::write);
    }

    @ThreadState("Minecraft Client Thread")
    public static void addAccount(BaseAccount account) {
        Threading.checkMinecraftClientThread();

        accounts.add(account);
        AccountWorker.submit(ConfigHandle::write);
    }

    @ThreadState("Minecraft Client Thread")
    public static void moveAccount(BaseAccount account, int index) {
        Threading.checkMinecraftClientThread();

        accounts.remove(account);
        accounts.add(index, account);
        AccountWorker.submit(ConfigHandle::write);
    }

    public static AccountSession loginAccount(BaseAccount account) throws IOException {
        return Adapters.getAuthlibAdpater().createAccountProfile(
                account.getAccountStorage(),
                AccountProvider.getProvider(account).createAccountContext(account),
                Adapters.getMinecraftAdapter().getGameProxy()
        );
    }

    @ThreadState("Minecraft Client Thread")
    public static void switchAccount(BaseAccount account, AccountSession session) {
        Threading.checkMinecraftClientThread();

        current = account;
        Adapters.getMinecraftAdapter().switchAccount(session);
    }
}
