package net.burningtnt.accountsx.core.accounts.impl.injector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.accountsx.core.accounts.AccountProvider;
import net.burningtnt.accountsx.core.accounts.AccountUUID;
import net.burningtnt.accountsx.core.accounts.model.PlayerNoLongerExistedException;
import net.burningtnt.accountsx.core.accounts.model.context.*;
import net.burningtnt.accountsx.core.ui.Memory;
import net.burningtnt.accountsx.core.ui.UIScreen;
import net.burningtnt.accountsx.core.utils.NetworkUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public abstract class AbstractInjectorAccountProvider<T extends AbstractInjectorAccount> implements AccountProvider<T> {
    private static final String GUID_SERVER_BASE = "guid:as.login.injector.widgets.server_url";
    private static final String GUID_USER_NAME = "guid:as.login.injector.widgets.user_name";
    private static final String GUID_PASSWORD = "guid:as.login.injector.widgets.user_password";
    private static final String GUID_PLAYER_NAME = "guid:as.login.injector.widgets.player_name";

    private final String serverBaseTranslationKey;

    private final String accountContextName;

    protected AbstractInjectorAccountProvider(String serverBaseTranslationKey, String accountContextName) {
        this.serverBaseTranslationKey = serverBaseTranslationKey;
        this.accountContextName = accountContextName;
    }

    protected void validateServerBaseURL(String server) throws IllegalArgumentException {
    }

    protected abstract String transformServerBaseURL(String server);

    protected abstract T createAccount(String accessToken, String playerName, UUID playerUUID, String server, String preferredPlayerUUID);

    @Override
    public final AccountContext createAccountContext(T account) throws IOException {
        String url = transformServerBaseURL(account.getServer());

        List<PublicKey> publicKeys;
        List<String> skinDomains = new ArrayList<>();

        JsonObject response = NetworkUtils.postRequest(new HttpGet(url));
        if (response.get("signaturePublickey") instanceof JsonPrimitive jp && jp.isString()) {
            try {
                publicKeys = List.of(parseSignaturePublicKey(jp.getAsString()));
            } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new IOException("Invalid yggdrasil public key!", e);
            }
        } else {
            throw new IOException("Invalid yggdrasil public key!");
        }

        if (response.get("skinDomains") instanceof JsonArray ja) {
            for (JsonElement je : ja) {
                if (je instanceof JsonPrimitive domain && domain.isString()) {
                    skinDomains.add(domain.getAsString());
                } else {
                    throw new IOException("Invalid yggdrasil public key!");
                }
            }
        } else {
            throw new IOException("Invalid yggdrasil public key!");
        }

        return new AccountContext(new AuthServerContext(
                url + "authserver", url + "api",
                url + "sessionserver", url + "minecraftservices",
                accountContextName
        ), new AuthSecurityContext(
                publicKeys, publicKeys,
                SkinURLVerifier.ofOperationOR(SkinURLVerifier.ofDomainVerifier(skinDomains, List.of()), SkinURLVerifier.MOJANG_DEFAULT)
        ), AuthPolicy.TRY);
    }

    private static PublicKey parseSignaturePublicKey(String pem) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        pem = pem.replace("\n", "").replace("\r", "");

        String header = "-----BEGIN PUBLIC KEY-----", end = "-----END PUBLIC KEY-----";
        if (!pem.startsWith(header) || !pem.endsWith(end)) {
            throw new IOException("Bad key format");
        }

        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(pem.substring(header.length(), pem.length() - end.length()))
        ));
    }

    @Override
    public final void configure(UIScreen screen) {
        screen.setTitle("as.account.general.login");
        screen.putTextInput(GUID_SERVER_BASE, serverBaseTranslationKey);
        screen.putTextInput(GUID_USER_NAME, "as.account.objects.user_name");
        screen.putTextInput(GUID_PASSWORD, "as.account.objects.user_password");
        screen.putTextInput(GUID_PLAYER_NAME, "as.account.objects.player_name");
    }

    @Override
    public final int validate(UIScreen screen, Memory memory) throws IllegalArgumentException {
        String serverBase = screen.getTextInput(GUID_SERVER_BASE);
        validateServerBaseURL(serverBase);
        memory.set(GUID_SERVER_BASE, serverBase);
        memory.set(GUID_USER_NAME, screen.getTextInput(GUID_USER_NAME));
        memory.set(GUID_PASSWORD, screen.getTextInput(GUID_PASSWORD));
        memory.set(GUID_PLAYER_NAME, screen.getTextInput(GUID_PLAYER_NAME));

        return STATE_IMMEDIATE_CLOSE;
    }

    @Override
    public final T login(Memory memory) throws IOException {
        String url = transformServerBaseURL(memory.get(GUID_SERVER_BASE, String.class)) + "authserver/authenticate";

        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject root = new JsonObject();
        root.add("agent", agent);
        root.addProperty("username", memory.get(GUID_USER_NAME, String.class));
        root.addProperty("password", memory.get(GUID_PASSWORD, String.class));

        JsonObject json = NetworkUtils.postRequest(url, root);
        if (json.has("error")) {
            throw new IOException("Cannot auth this injector: " + json.get("errorMessage").getAsString());
        }

        String accessToken = json.get("accessToken").getAsString();

        String playerName = memory.get(GUID_PLAYER_NAME, String.class);
        List<Profile> profiles = readProfiles(json);
        if (profiles.size() == 1) {
            Profile profile = profiles.get(0);

            if (!playerName.isEmpty()) {
                if (!playerName.equals(profile.playerName)) {
                    throw new IOException("Player not found.");
                }
            }

            return createAccount(
                    accessToken, profile.playerName, AccountUUID.parse(profile.playerUUID),
                    memory.get(GUID_SERVER_BASE, String.class), profile.playerUUID
            );
        } else {
            for (Profile profile : profiles) {
                if (playerName.equals(profile.playerName)) {
                    return createAccount(
                            accessToken, profile.playerName, AccountUUID.parse(profile.playerUUID),
                            memory.get(GUID_SERVER_BASE, String.class), profile.playerUUID
                    );
                }
            }

            throw new PlayerNoLongerExistedException("Cannot find player which match " + playerName);
        }
    }

    @Override
    public final void refresh(T account) throws IOException {
        String url = transformServerBaseURL(account.getServer()) + "authserver/refresh";

        JsonObject root = new JsonObject();
        root.addProperty("accessToken", account.getLoginToken());

        JsonObject json = NetworkUtils.postRequest(url, root);
        if (json.has("error")) {
            throw new IOException("Cannot auth this injector: " + json.get("errorMessage").getAsString());
        }

        String accessToken = json.get("accessToken").getAsString();

        List<Profile> profiles = readProfiles(json);
        if (profiles.size() == 1) {
            Profile profile = profiles.get(0);
            account.setLoginProfile(accessToken, profile.playerUUID);
            account.setProfile(accessToken, profile.playerName, AccountUUID.parse(profile.playerUUID));
        } else {
            String preferredPlayerUUID = account.getPreferredPlayerUUID();

            for (Profile profile : profiles) {
                if (profile.playerUUID.equals(preferredPlayerUUID)) {
                    account.setLoginProfile(accessToken, profile.playerUUID);
                    account.setProfile(accessToken, profile.playerName, AccountUUID.parse(profile.playerUUID));
                    return;
                }
            }

            throw new PlayerNoLongerExistedException("Cannot find player which match " + preferredPlayerUUID);
        }
    }

    private record Profile(String playerName, String playerUUID) {
    }

    private static List<Profile> readProfiles(JsonObject json) {
        JsonElement selectedProfile = json.get("selectedProfile");
        if (selectedProfile != null) {
            JsonObject jo = selectedProfile.getAsJsonObject();
            String playerName = jo.get("name").getAsString();
            String playerUUID = jo.get("id").getAsString();

            return List.of(new Profile(playerName, playerUUID));
        } else {
            JsonArray availableProfiles = json.get("availableProfiles").getAsJsonArray();
            List<Profile> results = new ArrayList<>(availableProfiles.size());

            for (JsonElement availableProfile : availableProfiles) {
                JsonObject jo = availableProfile.getAsJsonObject();
                String playerName = jo.get("name").getAsString();
                String playerUUID = jo.get("id").getAsString();
                results.add(new Profile(playerName, playerUUID));
            }

            return results;
        }
    }
}
