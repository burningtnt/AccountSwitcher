package net.burningtnt.accountsx.adapters.mc;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.PropertyMap;
import net.burningtnt.accountsx.adapters.mc.mixins.mixins.PlayerSkinProviderAccessor;
import net.burningtnt.accountsx.adapters.mc.mixins.mixins.MinecraftClientAccessor;
import net.burningtnt.accountsx.authlib.AccountSessionImpl;
import net.burningtnt.accountsx.core.accounts.AccountUUID;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.accounts.impl.env.EnvironmentAccount;
import net.burningtnt.accountsx.core.adapters.api.MinecraftAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.Proxy;
import java.util.Optional;

public class MinecraftAdapaterImpl implements MinecraftAdapter<AccountSessionImpl> {
    @Override
    public EnvironmentAccount fromCurrentClient() {
        Session session = MinecraftClient.getInstance().getSession();
        return new EnvironmentAccount(session.getAccessToken(), session.getUsername(), session.getUuidOrNull());
    }

    @Override
    public <T extends BaseAccount> void switchAccount(AccountSessionImpl session) {
        MinecraftSessionService sessionService = session.sessionService();
        UserApiService userAPIService = session.userAPIService();
        BaseAccount.AccountStorage storage = session.storage();
        Session s = new Session(storage.getPlayerName(), AccountUUID.toMinecraftStyleString(storage.getPlayerUUID()), storage.getAccessToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);

        MinecraftClient client = MinecraftClient.getInstance();
        ((MinecraftClientAccessor) client).setSession(s);
        ((MinecraftClientAccessor) client).setSessionService(sessionService);
        ((MinecraftClientAccessor) client).setUserAPIService(userAPIService);
        ((MinecraftClientAccessor) client).setSocialInteractionManager(new SocialInteractionsManager(client, userAPIService));
        ((MinecraftClientAccessor) client).setSkinProvider(new PlayerSkinProvider(
                client.getTextureManager(),
                ((PlayerSkinProviderAccessor) client.getSkinProvider()).getSkinCacheDir(),
                sessionService
        ));

        PropertyMap propertyMap = ((MinecraftClientAccessor) client).getSessionPropertyMap();
        propertyMap.clear();
        propertyMap.putAll(session.gameProfile().getProperties());
    }

    @Override
    public Proxy getGameProxy() {
        return MinecraftClient.getInstance().getNetworkProxy();
    }

    @Override
    public void openBrowser(String url) {
        Util.getOperatingSystem().open(url);
    }

    @Override
    public Thread getMinecraftClientThread() {
        return ((MinecraftClientAccessor) MinecraftClient.getInstance()).getThread();
    }

    @Override
    public void crash(RuntimeException e) {
        MinecraftClient.getInstance().send(() -> {
            throw e;
        });
    }

    @Override
    public void copyText(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            new Clipboard().setClipboard(client.getWindow().getHandle(), text);
        } else {
            client.send(() -> copyText(text));
        }
    }

    @Override
    public void showToast(String title, String description, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            SystemToast.show(
                    MinecraftClient.getInstance().getToastManager(),
                    SystemToast.Type.NARRATOR_TOGGLE,
                    Text.translatable(title),
                    description == null ? null : Text.translatable(description, args)
            );
        } else {
            client.send(() -> showToast(title, description, args));
        }
    }
}
