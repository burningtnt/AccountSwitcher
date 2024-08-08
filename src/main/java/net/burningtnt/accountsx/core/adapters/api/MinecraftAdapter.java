package net.burningtnt.accountsx.core.adapters.api;

import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.accounts.impl.env.EnvironmentAccount;

import java.net.Proxy;

public interface MinecraftAdapter<S extends AccountSession> {
    EnvironmentAccount fromCurrentClient();

    <T extends BaseAccount> void switchAccount(S session);

    Proxy getGameProxy();

    Thread getMinecraftClientThread();

    void openBrowser(String url);

    void crash(RuntimeException e);

    void copyText(String text);

    void showToast(String title, String description, Object... args);
}
