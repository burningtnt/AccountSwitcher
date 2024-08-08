package net.burningtnt.accountsx.authlib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public final class AuthlibAdapterImpl implements AuthlibAdapter<AccountSessionImpl> {
    @Override
    public AccountSessionImpl createAccountProfile(BaseAccount.AccountStorage storage, AccountAuthServerContext context, Proxy proxy) {
        if (context == null) {
            YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy);

            MinecraftSessionService sessionService = service.createMinecraftSessionService();

            return new AccountSessionImpl(storage, sessionService, UserApiService.OFFLINE, sessionService.fetchProfile(storage.getPlayerUUID(), true));
        } else {
            Environment env = new Environment(context.sessionURL(), context.serviceURL(), context.name());
            YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy, env);
            MinecraftSessionService sessionService = new YggdrasilMinecraftSessionService(service.getServicesKeySet(), service.getProxy(), env) {
                private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilMinecraftSessionService.class);

                private static final BiFunction<YggdrasilMinecraftSessionService, Property, SignatureState> GET_PROPERTY_SIGNATURE_STATE = new BiFunction<YggdrasilMinecraftSessionService, Property, SignatureState>() {
                    private static final MethodHandle MH;

                    static {
                        try {
                            MH = MethodHandles.privateLookupIn(
                                    YggdrasilMinecraftSessionService.class,
                                    MethodHandles.lookup()
                            ).findVirtual(
                                    YggdrasilMinecraftSessionService.class,
                                    "getPropertySignatureState",
                                    MethodType.methodType(SignatureState.class, Property.class)
                            );
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException(e);
                        }
                    }

                    @Override
                    public SignatureState apply(YggdrasilMinecraftSessionService instance, Property property) {
                        try {
                            return (SignatureState) MH.invoke(instance, property);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

                @Override
                public MinecraftProfileTextures unpackTextures(Property packedTextures) {
                    final String value = packedTextures.value();
                    final SignatureState signatureState = GET_PROPERTY_SIGNATURE_STATE.apply(this, packedTextures);

                    final MinecraftTexturesPayload result;
                    try {
                        final String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                        result = this.gson.fromJson(json, MinecraftTexturesPayload.class);
                    } catch (final JsonParseException | IllegalArgumentException e) {
                        LOGGER.error("Could not decode textures payload", e);
                        return MinecraftProfileTextures.EMPTY;
                    }

                    if (result == null || result.textures() == null || result.textures().isEmpty()) {
                        return MinecraftProfileTextures.EMPTY;
                    }

                    final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = result.textures();

                    return new MinecraftProfileTextures(
                        textures.get(MinecraftProfileTexture.Type.SKIN),
                        textures.get(MinecraftProfileTexture.Type.CAPE),
                        textures.get(MinecraftProfileTexture.Type.ELYTRA),
                        signatureState
                    );
                }
            };

            return new AccountSessionImpl(storage, sessionService, switch (context.mode()) {
                case ONLINE -> service.createUserApiService(storage.getAccessToken());
                case OFFLINE -> UserApiService.OFFLINE;
                case TRY -> {
                    try {
                        yield service.createUserApiService(storage.getAccessToken());
                    } catch (Exception e) {
                        yield UserApiService.OFFLINE;
                    }
                }
            }, sessionService.fetchProfile(storage.getPlayerUUID(), true));
        }
    }
}
