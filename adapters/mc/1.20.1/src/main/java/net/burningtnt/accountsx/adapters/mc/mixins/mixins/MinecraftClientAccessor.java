package net.burningtnt.accountsx.adapters.mc.mixins.mixins;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Mutable
    @Accessor("session")
    void setSession(Session session);

    @Mutable
    @Accessor("sessionService")
    void setSessionService(MinecraftSessionService service);

    @Mutable
    @Accessor("socialInteractionsManager")
    void setSocialInteractionManager(SocialInteractionsManager manager);

    @Mutable
    @Accessor("userApiService")
    void setUserAPIService(UserApiService service);

    @Mutable
    @Accessor("skinProvider")
    void setSkinProvider(PlayerSkinProvider skinProvider);

    @Accessor("thread")
    Thread getThread();

    @Accessor("sessionPropertyMap")
    PropertyMap getSessionPropertyMap();
}
