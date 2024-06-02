package deprecated.login;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.impl.Pair;
import deprecated.AccountL;
import iafenvoy.accountswitcher.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
public class MicrosoftLogin implements ILogin {
    private final LoginProfiler profiler = new LoginProfiler();
    private String code;
    private String accessToken, refreshToken, xblToken, xstsToken, mcToken;
    private String userHash;
    private String username, uuid;

    public MicrosoftLogin() {
    }

    @Nullable
    public AccountL doAuth(AuthRequest request) throws IllegalMicrosoftAccountException {
        try {
            profiler.push("Initialize");
            this.init();
            profiler.swap("1/6 - Oauth");
            this.openOauth();
            profiler.swap("2/6 - To token");
            this.toToken();
            profiler.swap("3/6 - XBox Live Auth");
            this.xBoxLiveAuth();
            profiler.swap("4/6 - XSTS Auth");
            this.authXSTS();
            profiler.swap("5/6 - To Minecraft Token");
            this.getMinecraftToken();
            profiler.swap("6/6 - Getting UUID");
            this.getUuid();
            profiler.swap("Done");
            AccountL account = new AccountL(AccountL.AccountType.Microsoft, accessToken, refreshToken, username, uuid);
            account.setMcToken(mcToken);
            profiler.pop();
            return account;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return AccountL.EMPTY;
        }
    }

    public void useAccount(AccountL account) {
        try {
            profiler.push("Initialize");
            this.init();
            this.accessToken = account.getAccessToken();
            profiler.swap("1/4 - XBox Live Auth");
            this.xBoxLiveAuth();
            profiler.swap("2/4 - XSTS Auth");
            this.authXSTS();
            profiler.swap("3/4 - To Minecraft Token");
            this.getMinecraftToken();
            profiler.swap("4/4 - Getting UUID");
            this.getUuid();
            profiler.swap("Done");
            account.setMcToken(this.mcToken);
            account.setUsername(this.username);
            account.setUuid(this.uuid);
            profiler.pop();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalMicrosoftAccountException ignored) {

        }
    }

    public void refreshAccessToken(AccountL account) {
        try {
            profiler.push("Initialize");
            this.init();
            profiler.push("Refreshing...");
            this.refreshToken = account.getRefreshToken();
            this.refreshToken();
            account.setAccessToken(this.accessToken);
            account.setRefreshToken(this.refreshToken);
            profiler.pop();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public String getProcess() {
        return profiler.getState();
    }

    private void init() {
        this.code = null;
        this.accessToken = this.refreshToken = this.xblToken = this.xstsToken = this.mcToken = null;
        this.userHash = null;
        this.username = this.uuid = null;
    }

    //教程：https://minecraft.fandom.com/zh/wiki/%E6%95%99%E7%A8%8B/%E7%BC%96%E5%86%99%E5%90%AF%E5%8A%A8%E5%99%A8
    //第一步：微软Oauth流程，这个操作只能浏览器执行
    private void openOauth() {
        IOUtils.openBrowser("Login your Microsoft account", "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&prompt=login", url -> {
            String search = url.split("\\?")[1];
            String[] data = search.split("&");
            for (String s : data) {
                String[] d = s.split("=");
                if (d[0].equals("code")) {
                    code = d[1];
                    return true;
                }
            }
            return false;
        });
    }

    //第二步：授权码->授权令牌  POST https://login.live.com/oauth20_token.srf
    private void toToken() throws IllegalArgumentException {
        if (this.code == null)
            throw new IllegalArgumentException("Fail to get code");

        List<Pair<String, String>> map = new ArrayList<>();
        map.add(Pair.of("client_id", "00000000402b5328"));
        map.add(Pair.of("code", this.code));
        map.add(Pair.of("grant_type", "authorization_code"));
        map.add(Pair.of("redirect_uri", "https://login.live.com/oauth20_desktop.srf"));
        map.add(Pair.of("scope", "service::user.auth.xboxlive.com::MBI_SSL"));

        String data = IOUtils.formFetch("https://login.live.com/oauth20_token.srf", map);
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        this.accessToken = json.get("access_token").getAsString();
        this.refreshToken = json.get("refresh_token").getAsString();
    }

    //第三步：XBox Live身份验证  POST https://user.auth.xboxlive.com/user/authenticate
    private void xBoxLiveAuth() throws IllegalArgumentException {
        if (this.accessToken == null)
            throw new IllegalArgumentException("Fail to get access token");

        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", this.accessToken);

        JsonObject root = new JsonObject();
        root.add("Properties", properties);
        root.addProperty("RelyingParty", "http://auth.xboxlive.com");
        root.addProperty("TokenType", "JWT");

        String data = IOUtils.jsonFetch("https://user.auth.xboxlive.com/user/authenticate", root);
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        this.xblToken = json.get("Token").getAsString();
        this.userHash = json.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
    }

    //第四步：XSTS身份验证  POST https://xsts.auth.xboxlive.com/xsts/authorize
    public void authXSTS() throws IllegalArgumentException {
        if (this.xblToken == null)
            throw new IllegalArgumentException("Fail to get xbl token");
        JsonArray tokens = new JsonArray();
        tokens.add(this.xblToken);

        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        properties.add("UserTokens", tokens);

        JsonObject root = new JsonObject();
        root.add("Properties", properties);
        root.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        root.addProperty("TokenType", "JWT");

        String data = IOUtils.jsonFetch("https://xsts.auth.xboxlive.com/xsts/authorize", root);
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        this.xstsToken = json.get("Token").getAsString();
    }

    //第五步：获得Minecraft访问令牌  POST https://api.minecraftservices.com/authentication/login_with_xbox
    //注意：此操作必须每次使用此账户前进行（有效期86400）
    private void getMinecraftToken() throws IllegalArgumentException {
        if (this.xstsToken == null)
            throw new IllegalArgumentException("Fail to get xsts token");
        if (this.userHash == null)
            throw new IllegalArgumentException("Fail to get user hash");

        JsonObject root = new JsonObject();
        root.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", this.userHash, this.xstsToken));

        String data = IOUtils.jsonFetch("https://api.minecraftservices.com/authentication/login_with_xbox", root);
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        this.mcToken = json.get("access_token").getAsString();
    }

    //第六步：获取玩家UUID  GET https://api.minecraftservices.com/minecraft/profile
    public void getUuid() throws IllegalArgumentException, IllegalMicrosoftAccountException {
        if (this.mcToken == null)
            throw new IllegalArgumentException("Fail to get Minecraft token");

        String data = IOUtils.getDataWithHeader("https://api.minecraftservices.com/minecraft/profile", Lists.newArrayList(Pair.of("Authorization", "Bearer " + this.mcToken)));
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        if (json.has("error"))
            throw new IllegalMicrosoftAccountException();
        this.username = json.get("name").getAsString();
        this.uuid = json.get("id").getAsString();
    }

    //刷新token
    private void refreshToken() throws IllegalArgumentException {
        if (this.refreshToken == null)
            throw new IllegalArgumentException("Fail to get refresh token");

        List<Pair<String, String>> map = new ArrayList<>();
        map.add(Pair.of("client_id", "00000000402b5328"));
        map.add(Pair.of("code", this.refreshToken));
        map.add(Pair.of("grant_type", "authorization_code"));
        map.add(Pair.of("redirect_uri", "https://login.live.com/oauth20_desktop.srf"));
        map.add(Pair.of("scope", "service::user.auth.xboxlive.com::MBI_SSL"));

        String data = IOUtils.formFetch("https://login.live.com/oauth20_token.srf", map);
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        this.accessToken = json.get("access_token").getAsString();
        this.refreshToken = json.get("refresh_token").getAsString();
    }

}
