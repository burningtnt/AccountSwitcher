package net.burningtnt.accountsx.core.accounts;

import net.burningtnt.accountsx.core.accounts.model.context.AccountContext;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;

import java.io.IOException;

public interface AccountProvider<T extends BaseAccount> {
    int STATE_IMMEDIATE_CLOSE = 0;

    int STATE_HANDLE = 1;

    void configure(UIScreen screen);

    int validate(UIScreen screen, Memory memory) throws IllegalArgumentException;

    AccountContext createAccountContext(T account) throws IOException;

    T login(Memory memory) throws IOException;

    void refresh(T account) throws IOException;

    @SuppressWarnings("unchecked")
    static <T extends BaseAccount> AccountProvider<T> getProvider(T account) {
        return (AccountProvider<T>) account.getAccountType().getAccountProvider();
    }
}
