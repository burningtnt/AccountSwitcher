package net.burningtnt.accountsx.core.accounts.impl.microsoft;

import com.google.gson.annotations.SerializedName;
import net.burningtnt.accountsx.core.AccountsX;
import net.burningtnt.accountsx.core.accounts.model.context.AuthSecurityContext;
import net.burningtnt.accountsx.core.accounts.model.context.AuthServerContext;
import net.burningtnt.accountsx.core.utils.NetworkUtils;
import org.apache.http.client.methods.RequestBuilder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class MicrosoftConstants {
    // https://authserver.mojang.com
    public static final String AUTH = decode("aHR0cHM6Ly9hdXRoc2VydmVyLm1vamFuZy5jb20=");

    // https://api.mojang.com
    public static final String ACCOUNT = decode("aHR0cHM6Ly9hcGkubW9qYW5nLmNvbQ==");

    // https://sessionserver.mojang.com
    public static final String SESSION = decode("aHR0cHM6Ly9zZXNzaW9uc2VydmVyLm1vamFuZy5jb20=");

    // https://api.minecraftservices.com
    public static final String SERVICES = decode("aHR0cHM6Ly9hcGkubWluZWNyYWZ0c2VydmljZXMuY29t");

    public static final AuthServerContext SERVER_CONTEXT = new AuthServerContext(
            AUTH, ACCOUNT, SESSION, SERVICES, "PROD"
    );

    // https://api.minecraftservices.com/authentication/login_with_xbox
    public static final String MS_LOGIN_XBOX = decode("aHR0cHM6Ly9hcGkubWluZWNyYWZ0c2VydmljZXMuY29tL2F1dGhlbnRpY2F0aW9uL2xvZ2luX3dpdGhfeGJveA==");

    // https://api.minecraftservices.com/minecraft/profile
    public static final String MS_GAME_PROFILE = decode("aHR0cHM6Ly9hcGkubWluZWNyYWZ0c2VydmljZXMuY29tL21pbmVjcmFmdC9wcm9maWxl");

    public static void initialize() {
        if (!AUTH.equals("https://authserver.mojang.com")) {
            AccountsX.LOGGER.warn("authlib-injector is detected! The compatibility between AccountsX and authlib-injector is an experimental feature.");
        }
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.ISO_8859_1);
    }

    public static AuthSecurityContext computeMicrosoftPublicKeys() throws IOException {
        KeySetResponse response = NetworkUtils.GSON.fromJson(
                NetworkUtils.postRequest(RequestBuilder.get(SERVICES + "/publickeys").build(), false),
                KeySetResponse.class
        );

        if (response == null) {
            throw new IOException("Received malformed yggdrasil public key data: null.");
        }

        try {
            return new AuthSecurityContext(parsePublicKeys(response.profilePropertyKeys), parsePublicKeys(response.profilePropertyKeys));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IOException("Received malformed yggdrasil public key data.", e);
        }
    }

    private static List<PublicKey> parsePublicKeys(List<KeyData> data) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (data == null || data.isEmpty()) {
            return List.of();
        }

        List<PublicKey> r = new ArrayList<>(data.size());
        for (KeyData kd : data) {
            r.add(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(kd.publicKey))));
        }

        return r;
    }

    private record KeySetResponse(
            @SerializedName("profilePropertyKeys")
            @Nullable List<KeyData> profilePropertyKeys,
            @SerializedName("playerCertificateKeys")
            @Nullable List<KeyData> playerCertificateKeys
    ) {
    }

    private record KeyData(
            @SerializedName("publicKey")
            String publicKey
    ) {
    }
}
