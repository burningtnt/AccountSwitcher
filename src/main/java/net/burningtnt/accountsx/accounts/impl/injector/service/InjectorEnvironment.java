package net.burningtnt.accountsx.accounts.impl.injector.service;

import com.mojang.authlib.Environment;

import java.util.StringJoiner;

public class InjectorEnvironment implements Environment {
    private final String url;

    public InjectorEnvironment(String serverUrl) {
        this.url = serverUrl;
    }

    @Override
    public String getAuthHost() {
        return "https://" + url + "/api/yggdrasil/authserver";
    }

    @Override
    public String getAccountsHost() {
        return "https://" + url + "/api/yggdrasil/api";
    }

    @Override
    public String getSessionHost() {
        return "https://" + url + "/api/yggdrasil/sessionserver";
    }

    @Override
    public String getServicesHost() {
        return "https://" + url + "/api/yggdrasil/minecraftservices";
    }

    @Override
    public String getName() {
        return "Authlib-Injector";
    }

    @Override
    public String asString() {
        return new StringJoiner(", ", "", "")
                .add("authHost='" + getAuthHost() + "'")
                .add("accountsHost='" + getAccountsHost() + "'")
                .add("sessionHost='" + getSessionHost() + "'")
                .add("servicesHost='" + getServicesHost() + "'")
                .add("name='" + getName() + "'")
                .toString();
    }
}
