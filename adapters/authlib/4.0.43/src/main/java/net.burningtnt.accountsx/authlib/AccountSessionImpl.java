package net.burningtnt.accountsx.authlib;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.adapters.api.AccountSession;

public record AccountSessionImpl(
        BaseAccount.AccountStorage storage,
        MinecraftSessionService sessionService,
        UserApiService userAPIService,
        GameProfile gameProfile
) implements AccountSession {
}
