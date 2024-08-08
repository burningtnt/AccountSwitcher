package net.burningtnt.accountsx.core.accounts.impl.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.AccountUUID;
import net.burningtnt.accountsx.core.adapters.context.AccountAuthServerContext;
import net.burningtnt.accountsx.core.adapters.Adapters;
import net.burningtnt.accountsx.core.adapters.context.LoginMode;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;
import net.burningtnt.accountsx.core.utils.IOUtils;
import org.apache.http.client.methods.RequestBuilder;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CancellationException;

public class MicrosoftAccountProvider implements AccountProvider<MicrosoftAccount> {
    private static final String SCOPE = "XboxLive.signin offline_access";

    private static final String CLIENT_ID = "6a3728d6-27a3-4180-99bb-479895b8f88e";

    private static final String DEVICE_CODE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";

    private static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";

    @Override
    public AccountAuthServerContext createAccountContext(MicrosoftAccount account) {
        return new AccountAuthServerContext(
                MicrosoftConstants.AUTH,
                MicrosoftConstants.ACCOUNT,
                MicrosoftConstants.SESSION,
                MicrosoftConstants.SERVICES,
                "PROD",
                LoginMode.ONLINE
        );
    }

    public MicrosoftAccountProvider() {
        if (System.getProperty("swing.defaultlaf") == null) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void configure(UIScreen screen) {
        screen.setTitle("as.account.objects.external");
    }

    @Override
    public int validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        return STATE_HANDLE;
    }

    @Override
    public MicrosoftAccount login(Memory memory) throws IOException, CancellationException {
        if (memory.isScreenClosed()) {
            throw new CancellationException("Screen has been closed.");
        }

        Adapters.getMinecraftAdapter().showToast("as.account.objects.oauth2.code.generating", null);

        JsonObject device = IOUtils.postRequest(RequestBuilder.post(DEVICE_CODE_URL)
                .addParameter("client_id", CLIENT_ID)
                .addParameter("scope", SCOPE)
                .build());

        if (memory.isScreenClosed()) {
            throw new CancellationException("Screen has been closed.");
        }

        Adapters.getMinecraftAdapter().copyText(device.get("user_code").getAsString());

        String url = device.get("verification_uri").getAsString();
        Adapters.getMinecraftAdapter().openBrowser(url);

        String microsoftAccessToken, microsoftRefreshToken;

        for (int interval = device.get("interval").getAsInt(); ; ) {
            try {
                Thread.sleep(Math.max(interval, 1));
            } catch (InterruptedException e) {
                throw new IOException("Interrupted.", e);
            }

            if (memory.isScreenClosed()) {
                throw new CancellationException("Screen has been closed.");
            }
            Adapters.getMinecraftAdapter().showToast("as.account.objects.oauth2.code.title", "as.account.objects.oauth2.code.desc", device.get("user_code").getAsString());

            JsonObject token;
            token = IOUtils.postRequest(RequestBuilder.post(TOKEN_URL)
                    .addParameter("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                    .addParameter("code", device.get("device_code").getAsString())
                    .addParameter("client_id", CLIENT_ID)
                    .build(), true);

            JsonElement err = token.get("error");
            if (err == null) {
                microsoftAccessToken = token.get("access_token").getAsString();
                microsoftRefreshToken = token.get("refresh_token").getAsString();

                break;
            }

            String error = err.getAsString();

            if ("authorization_pending".equals(error)) {
                continue;
            }

            if ("expired_token".equals(error)) {
                throw new IOException("No character detected.");
            }

            if ("slow_down".equals(error)) {
                interval += 5;
                continue;
            }

            throw new IOException("Unknown error: " + error);
        }

        String xblToken, userHash;
        {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "d=" + microsoftAccessToken);

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

        return new MicrosoftAccount(accessToken, playerName, AccountUUID.parse(playerUUID), microsoftAccessToken, microsoftRefreshToken);
    }

    @Override
    public void refresh(MicrosoftAccount account) throws IOException {
        {
            JsonObject token = IOUtils.postRequest(RequestBuilder.post(TOKEN_URL)
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("refresh_token", account.getMicrosoftAccountRefreshToken())
                    .addParameter("grant_type", "refresh_token")
                    .build());

            account.setMicrosoftAccountToken(
                    token.get("access_token").getAsString(),
                    token.get("refresh_token").getAsString()
            );
        }

        String xblToken, userHash;
        {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "d=" + account.getMicrosoftAccountAccessToken());

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

        account.setProfile(accessToken, playerName, AccountUUID.parse(playerUUID));
    }
}
