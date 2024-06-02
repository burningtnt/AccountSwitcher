package net.burningtnt.accountsx.accounts;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.util.Session;

public final class AccountSession {
    private final Session session;

    private final MinecraftSessionService sessionService;

    private final UserApiService userAPIService;

    public AccountSession(Session session, MinecraftSessionService sessionService, UserApiService authenticationService) {
        this.session = session;
        this.sessionService = sessionService;
        this.userAPIService = authenticationService;
    }

    public Session getSession() {
        return session;
    }

    public MinecraftSessionService getSessionService() {
        return sessionService;
    }

    public UserApiService getUserAPIService() {
        return userAPIService;
    }
}
