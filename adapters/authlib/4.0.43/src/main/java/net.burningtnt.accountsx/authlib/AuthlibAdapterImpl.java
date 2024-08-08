package net.burningtnt.accountsx.authlib;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.adapters.context.AccountAuthServerContext;
import net.burningtnt.accountsx.core.adapters.api.AuthlibAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AuthlibAdapterImpl implements AuthlibAdapter<AccountSessionImpl> {
    @Override
    public AccountSessionImpl createAccountProfile(BaseAccount.AccountStorage storage, AccountAuthServerContext context, Proxy proxy) throws IOException {
        if (context == null) {
            YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy);

            MinecraftSessionService sessionService = service.createMinecraftSessionService();

            return new AccountSessionImpl(storage, sessionService, UserApiService.OFFLINE, sessionService.fillProfileProperties(
                    new GameProfile(storage.getPlayerUUID(), storage.getPlayerName()), false
            ));
        } else {
            Environment env = Environment.create(context.authURL(), context.accountURL(), context.sessionURL(), context.serviceURL(), context.name());
            YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy, env);
            MinecraftSessionService sessionService = new YggdrasilMinecraftSessionService(service, env) {
                private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilMinecraftSessionService.class);

                private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

                @Override
                public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) throws InsecurePublicKeyException {
                    final Property textureProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);

                    if (textureProperty == null) {
                        return new HashMap<>();
                    }

                    final String value = requireSecure ? getSecurePropertyValue(textureProperty) : textureProperty.getValue();

                    final MinecraftTexturesPayload result;
                    try {
                        final String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                        result = this.gson.fromJson(json, MinecraftTexturesPayload.class);
                    } catch (final JsonParseException e) {
                        LOGGER.error("Could not decode textures payload", e);
                        return new HashMap<>();
                    }

                    if (result == null || result.getTextures() == null) {
                        return new HashMap<>();
                    }

                    return result.getTextures();
                }
            };

            return new AccountSessionImpl(storage, sessionService, switch (context.mode()) {
                case ONLINE -> {
                    try {
                        yield service.createUserApiService(storage.getAccessToken());
                    } catch (AuthenticationException e) {
                        throw new IOException("Cannot create the profile.", e);
                    }
                }
                case OFFLINE -> UserApiService.OFFLINE;
                case TRY -> {
                    try {
                        yield service.createUserApiService(storage.getAccessToken());
                    } catch (Exception e) {
                        yield UserApiService.OFFLINE;
                    }
                }
            }, sessionService.fillProfileProperties(
                    new GameProfile(storage.getPlayerUUID(), storage.getPlayerName()), false
            ));
        }
    }
}
