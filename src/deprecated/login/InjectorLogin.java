package deprecated.login;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deprecated.AccountL;
import iafenvoy.accountswitcher.utils.IOUtils;

@Deprecated(forRemoval = true)
public class InjectorLogin implements ILogin {
    private String stats = "";

    public String getProcess() {
        return stats;
    }

    public boolean doLogin(AccountL account, String server, String name, String password) {
        try {
            stats = "Login...";
            String url = "https://" + server + "/api/yggdrasil/authserver/authenticate";
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);

            JsonObject root = new JsonObject();
            root.add("agent", agent);
            root.addProperty("username", name);
            root.addProperty("password", password);

            String data = IOUtils.jsonFetch(url, root);
            JsonObject json = new JsonParser().parse(data).getAsJsonObject();
            if (json.has("error")) {
                stats = json.get("errorMessage").getAsString();
                return false;
            }
            stats = "";
            String mcToken = json.get("accessToken").getAsString();
            String uuid = json.get("selectedProfile").getAsJsonObject().get("id").getAsString();
            String username = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();
            account.setAccessToken(password);
            account.setUsername(username);
            account.setUuid(uuid);
            account.setMcToken(mcToken);
            account.setInjectorServer(server);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AccountL doAuth(AuthRequest request) throws IllegalMicrosoftAccountException {
        return null;
    }

    @Override
    public void useAccount(AccountL account) {

    }

    @Override
    public void refreshAccessToken(AccountL account) {

    }
}
