package net.burningtnt.accountsx.accounts;

import net.burningtnt.accountsx.accounts.impl.env.EnvironmentAccount;
import net.burningtnt.accountsx.accounts.impl.env.EnvironmentAccountProvider;
import net.burningtnt.accountsx.accounts.impl.injector.InjectorAccount;
import net.burningtnt.accountsx.accounts.impl.injector.InjectorAccountProvider;
import net.burningtnt.accountsx.accounts.impl.microsoft.MicrosoftAccount;
import net.burningtnt.accountsx.accounts.impl.microsoft.MicrosoftAccountProvider;
import net.burningtnt.accountsx.accounts.impl.offline.OfflineAccount;
import net.burningtnt.accountsx.accounts.impl.offline.OfflineAccountProvider;

import java.util.Arrays;

public enum AccountType {
    ENV_DEFAULT(EnvironmentAccount.class, new EnvironmentAccountProvider()),
    OFFLINE(OfflineAccount.class, new OfflineAccountProvider()),
    MICROSOFT(MicrosoftAccount.class, new MicrosoftAccountProvider()),
    INJECTOR(InjectorAccount.class, new InjectorAccountProvider());

    public static final AccountType[] VALUES = values();

    public static final AccountType[] CONFIGURABLE_VALUES = Arrays.stream(VALUES).filter(i -> i != ENV_DEFAULT).toArray(AccountType[]::new);

    private final Class<? extends BaseAccount> accountClass;

    private final AccountProvider<?> accountProvider;

    AccountType(Class<? extends BaseAccount> accountClass, AccountProvider<?> accountProvider) {
        this.accountClass = accountClass;
        this.accountProvider = accountProvider;
    }

    public Class<? extends BaseAccount> getAccountClass() {
        return accountClass;
    }

    public AccountProvider<?> getAccountProvider() {
        return accountProvider;
    }
}