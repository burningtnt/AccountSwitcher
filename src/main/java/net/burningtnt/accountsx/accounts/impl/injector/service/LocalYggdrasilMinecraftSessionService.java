package net.burningtnt.accountsx.accounts.impl.injector.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import net.burningtnt.accountsx.AccountsX;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class LocalYggdrasilMinecraftSessionService extends YggdrasilMinecraftSessionService {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    public LocalYggdrasilMinecraftSessionService(YggdrasilAuthenticationService authenticationService, Environment environment) {
        super(authenticationService.getServicesKeySet(), authenticationService.getProxy(), environment);
    }

    private static final MethodHandle GET_PROPERTY_SIGNATURE_STATE_HANDLE;

    static {
        MethodHandle handle;
        try {
            handle = MethodHandles.privateLookupIn(YggdrasilMinecraftSessionService.class, MethodHandles.lookup())
                    .findVirtual(YggdrasilMinecraftSessionService.class, "getPropertySignatureState", MethodType.methodType(
                            SignatureState.class,
                            Property.class
                    ));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            AccountsX.LOGGER.error("Cannot get com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService::getPropertySignatureState handle.", e);
            handle = null;
        }

        GET_PROPERTY_SIGNATURE_STATE_HANDLE = handle;
    }

    @Override
    public MinecraftProfileTextures unpackTextures(Property packedTextures) {
        String value = packedTextures.value();
        SignatureState signatureState;
        try {
            signatureState = (SignatureState) GET_PROPERTY_SIGNATURE_STATE_HANDLE.invoke(this, packedTextures);
        } catch (Throwable e) {
            if (e instanceof RuntimeException er) {
                throw er;
            } else {
                throw new IllegalStateException("com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService::getPropertySignatureState encountered an checked exception.", e);
            }
        }

        MinecraftTexturesPayload result;
        try {
            result = gson.fromJson(new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8), MinecraftTexturesPayload.class);
        } catch (final JsonParseException | IllegalArgumentException e) {
            AccountsX.LOGGER.error("Could not decode textures payload", e);
            return MinecraftProfileTextures.EMPTY;
        }

        if (result == null || result.textures() == null || result.textures().isEmpty()) {
            return MinecraftProfileTextures.EMPTY;
        }

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = result.textures();

        return new MinecraftProfileTextures(
                textures.get(MinecraftProfileTexture.Type.SKIN),
                textures.get(MinecraftProfileTexture.Type.CAPE),
                textures.get(MinecraftProfileTexture.Type.ELYTRA),
                signatureState
        );
    }
}
