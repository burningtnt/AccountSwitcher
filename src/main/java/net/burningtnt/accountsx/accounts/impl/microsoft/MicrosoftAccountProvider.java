package net.burningtnt.accountsx.accounts.impl.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.api.Memory;
import net.burningtnt.accountsx.accounts.api.UIScreen;
import net.burningtnt.accountsx.utils.IOUtils;
import org.apache.http.client.methods.RequestBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Supplier;

public class MicrosoftAccountProvider implements AccountProvider<MicrosoftAccount> {
    @Override
    @Nullable
    public <S extends UIScreen> S configure(Supplier<S> screenSupplier) {
        return null;
    }

    @Override
    public void validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        throw new AssertionError("Should NOT be here.");
    }

    @Override
    public MicrosoftAccount login(Memory memory) throws IOException {
        String[] code = new String[1];
        IOUtils.openBrowser("Login your Microsoft Account", "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&prompt=login", url -> {
            for (String pair : url.split("\\?", 3)[1].split("&")) {
                int index = pair.indexOf("=");
                if (index < 0) {
                    continue;
                }

                if (pair.regionMatches(0, "code", 0, 4)) {
                    code[0] = pair.substring(5);
                    return true;
                }
            }
            return false;
        });
        if (code[0] == null) {
            throw new IllegalArgumentException("Fail to get code");
        }

        String microsoftAccessToken, microsoftRefreshToken;

        {
            JsonObject jo = IOUtils.postRequest(RequestBuilder.post("https://login.live.com/oauth20_token.srf")
                    .addParameter("client_id", "00000000402b5328")
                    .addParameter("code", code[0])
                    .addParameter("grant_type", "authorization_code")
                    .addParameter("redirect_uri", "https://login.live.com/oauth20_desktop.srf")
                    .addParameter("scope", "service::user.auth.xboxlive.com::MBI_SSL")
                    .build()
            );

            microsoftAccessToken = jo.get("access_token").getAsString();
            microsoftRefreshToken = jo.get("refresh_token").getAsString();
        }

        String xblToken, userHash;
        {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", microsoftAccessToken);

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            root.addProperty("RelyingParty", "http://auth.xboxlive.com");
            root.addProperty("TokenType", "JWT");

            JsonObject json = IOUtils.postRequest("https://user.auth.xboxlive.com/user/authenticate", root);
            xblToken = json.get("Token").getAsString();
            userHash = json.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
        }

        String xstsToken;
        {
            JsonArray tokens = new JsonArray();
            tokens.add(xblToken);

            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            properties.add("UserTokens", tokens);

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            root.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            root.addProperty("TokenType", "JWT");

            xstsToken = IOUtils.postRequest("https://xsts.auth.xboxlive.com/xsts/authorize", root).get("Token").getAsString();
        }

        String accessToken;
        {
            JsonObject root = new JsonObject();
            root.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", userHash, xstsToken));

            JsonObject json = IOUtils.postRequest("https://api.minecraftservices.com/authentication/login_with_xbox", root);
            accessToken = json.get("access_token").getAsString();
        }

        String playerName, playerUUID;
        {
            JsonObject json = IOUtils.postRequest(RequestBuilder.get("https://api.minecraftservices.com/minecraft/profile")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build());

            if (json.has("error"))
                throw new IOException("Failed to get UUID");

            playerName = json.get("name").getAsString();
            playerUUID = json.get("id").getAsString();
        }

        return new MicrosoftAccount(accessToken, playerName, playerUUID, microsoftAccessToken, microsoftRefreshToken);
    }

    @Override
    public void refresh(MicrosoftAccount account) throws IOException {
        String xblToken, userHash;
        {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", account.getMicrosoftAccountAccessToken());

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            root.addProperty("RelyingParty", "http://auth.xboxlive.com");
            root.addProperty("TokenType", "JWT");

            JsonObject json = IOUtils.postRequest("https://user.auth.xboxlive.com/user/authenticate", root);
            xblToken = json.get("Token").getAsString();
            userHash = json.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
        }

        String xstsToken;
        {
            JsonArray tokens = new JsonArray();
            tokens.add(xblToken);

            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            properties.add("UserTokens", tokens);

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            root.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            root.addProperty("TokenType", "JWT");

            xstsToken = IOUtils.postRequest("https://xsts.auth.xboxlive.com/xsts/authorize", root).get("Token").getAsString();
        }

        String accessToken;
        {
            JsonObject root = new JsonObject();
            root.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", userHash, xstsToken));

            JsonObject json = IOUtils.postRequest("https://api.minecraftservices.com/authentication/login_with_xbox", root);
            accessToken = json.get("access_token").getAsString();
        }

        String playerName, playerUUID;
        {
            JsonObject json = IOUtils.postRequest(RequestBuilder.get("https://api.minecraftservices.com/minecraft/profile")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build());

            if (json.has("error"))
                throw new IOException("Failed to get UUID");

            playerName = json.get("name").getAsString();
            playerUUID = json.get("id").getAsString();
        }

        account.setProfile(accessToken, playerName, playerUUID);
    }
}
