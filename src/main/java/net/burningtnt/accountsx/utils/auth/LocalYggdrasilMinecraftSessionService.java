package net.burningtnt.accountsx.utils.auth;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import net.burningtnt.accountsx.AccountsX;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalYggdrasilMinecraftSessionService extends YggdrasilMinecraftSessionService {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    public LocalYggdrasilMinecraftSessionService(YggdrasilAuthenticationService authenticationService, Environment environment) {
        super(authenticationService, environment);
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(final GameProfile profile, final boolean requireSecure) throws InsecurePublicKeyException {
        Property textureProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);

        if (textureProperty == null) {
            return new HashMap<>();
        }

        String value = requireSecure ? getSecurePropertyValue(textureProperty) : textureProperty.getValue();

        MinecraftTexturesPayload result;
        try {
            result = gson.fromJson(new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8), MinecraftTexturesPayload.class);
        } catch (final JsonParseException e) {
            AccountsX.LOGGER.error("Could not decode textures payload", e);
            return new HashMap<>();
        }

        if (result == null || result.getTextures() == null) {
            return new HashMap<>();
        }

        return result.getTextures();
    }
}
