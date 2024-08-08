package net.burningtnt.accountsx.core.accounts.impl.injector;

import com.google.gson.JsonObject;
import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.AccountUUID;
import net.burningtnt.accountsx.core.adapters.context.AccountAuthServerContext;
import net.burningtnt.accountsx.core.adapters.context.LoginMode;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;
import net.burningtnt.accountsx.core.utils.IOUtils;

import java.io.IOException;

public class InjectorAccountProvider implements AccountProvider<InjectorAccount> {
    private static final String GUID_SERVER_DOMAIN = "guid:as.login.injector.widgets.server_url";
    private static final String GUID_USER_NAME = "guid:as.login.injector.widgets.user_name";
    private static final String GUID_USER_UUID = "guid:as.login.injector.widgets.user_password";

    @Override
    public AccountAuthServerContext createAccountContext(InjectorAccount account) {
        String url = account.getServer();
        return new AccountAuthServerContext(
                "https://" + url + "/api/yggdrasil/authserver",
                "https://" + url + "/api/yggdrasil/api",
                "https://" + url + "/api/yggdrasil/sessionserver",
                "https://" + url + "/api/yggdrasil/minecraftservices",
                "Authlib-Injector",
                LoginMode.TRY
        );
    }

    @Override
    public void configure(UIScreen screen) {
        screen.setTitle("as.general.login");
        screen.putTextInput(GUID_SERVER_DOMAIN, "as.account.objects.server_domain");
        screen.putTextInput(GUID_USER_NAME, "as.account.objects.user_name");
        screen.putTextInput(GUID_USER_UUID, "as.account.objects.user_password");
    }

    @Override
    public int validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        memory.set(GUID_SERVER_DOMAIN, screen.getTextInput(GUID_SERVER_DOMAIN));
        memory.set(GUID_USER_NAME, screen.getTextInput(GUID_USER_NAME));
        memory.set(GUID_USER_UUID, screen.getTextInput(GUID_USER_UUID));

        return STATE_IMMEDIATE_CLOSE;
    }

    @Override
    public InjectorAccount login(Memory memory) throws IOException {
        String url = "https://" + memory.get(GUID_SERVER_DOMAIN, String.class) + "/api/yggdrasil/authserver/authenticate";

        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject root = new JsonObject();
        root.add("agent", agent);
        root.addProperty("username", memory.get(GUID_USER_NAME, String.class));
        root.addProperty("password", memory.get(GUID_USER_UUID, String.class));

        JsonObject json = IOUtils.postRequest(url, root);
        if (json.has("error")) {
            throw new IOException("Cannot auth this injector: " + json.get("errorMessage").getAsString());
        }

        String accessToken = json.get("accessToken").getAsString();
        String playerUUID = json.get("selectedProfile").getAsJsonObject().get("id").getAsString();
        String playerName = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();


        return new InjectorAccount(
                accessToken, playerName, AccountUUID.parse(playerUUID),
                memory.get(GUID_SERVER_DOMAIN, String.class), memory.get(GUID_USER_NAME, String.class), memory.get(GUID_USER_UUID, String.class)
        );
    }

    @Override
    public void refresh(InjectorAccount account) throws IOException {
        String url = "https://" + account.getServer() + "/api/yggdrasil/authserver/authenticate";

        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject root = new JsonObject();
        root.add("agent", agent);
        root.addProperty("username", account.getUserName());
        root.addProperty("password", account.getPassword());

        JsonObject json = IOUtils.postRequest(url, root);
        if (json.has("error")) {
            throw new IOException("Cannot auth this injector: " + json.get("errorMessage").getAsString());
        }

        String accessToken = json.get("accessToken").getAsString();
        String playerUUID = json.get("selectedProfile").getAsJsonObject().get("id").getAsString();
        String playerName = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();

        account.setProfile(accessToken, playerName, AccountUUID.parse(playerUUID));
    }
}
