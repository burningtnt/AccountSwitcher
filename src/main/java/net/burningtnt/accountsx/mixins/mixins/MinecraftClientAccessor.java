package net.burningtnt.accountsx.mixins.mixins;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.Session;
import net.minecraft.client.texture.PlayerSkinProvider;
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
}
