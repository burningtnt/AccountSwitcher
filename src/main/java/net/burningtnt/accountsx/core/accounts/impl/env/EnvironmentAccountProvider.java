package net.burningtnt.accountsx.core.accounts.impl.env;

import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.model.context.AccountContext;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;

public final class EnvironmentAccountProvider implements AccountProvider<EnvironmentAccount> {
    @Override
    public AccountContext createAccountContext(EnvironmentAccount account) {
        return null;
    }

    @Override
    public void configure(UIScreen screen) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int validate(UIScreen screen, Memory memory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnvironmentAccount login(Memory memory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(EnvironmentAccount account) {
    }
}
