package net.burningtnt.accountsx.core.adapters.api;

import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.accounts.model.context.AccountContext;

import java.io.IOException;
import java.net.Proxy;

public interface AuthlibAdapter<S extends AccountSession> {
    S createAccountProfile(BaseAccount.AccountStorage storage, AccountContext context, Proxy proxy) throws IOException;
}
