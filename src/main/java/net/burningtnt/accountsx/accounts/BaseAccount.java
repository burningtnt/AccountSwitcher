package net.burningtnt.accountsx.accounts;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonAdapter(BaseAccount.Adapter.class)
public abstract class BaseAccount {
    public static final class Adapter implements TypeAdapterFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() != BaseAccount.class) {
                return null;
            }

            Map<AccountType, TypeAdapter<BaseAccount>> cache = new ConcurrentHashMap<>();

            return (TypeAdapter<T>) new TypeAdapter<BaseAccount>() {
                private TypeAdapter<BaseAccount> compute(AccountType type) {
                    return cache.computeIfAbsent(type, t -> (TypeAdapter<BaseAccount>) gson.getDelegateAdapter(
                            Adapter.this, TypeToken.get(t.getAccountClass())
                    ));
                }

                @Override
                public void write(JsonWriter out, BaseAccount account) throws IOException {
                    Streams.write(compute(account.getAccountType()).toJsonTree(account).getAsJsonObject(), out);
                }

                @Override
                public BaseAccount read(JsonReader in) {
                    JsonObject jo = Streams.parse(in).getAsJsonObject();
                    return compute(AccountType.valueOf(jo.get("type").getAsJsonPrimitive().getAsString())).fromJsonTree(jo);
                }
            };
        }
    }

    public static final class AccountStorage {
        public final String accessToken;

        public final String playerName;

        public final String playerUUID;

        public final transient AccountState state;

        private AccountStorage() {
            this.accessToken = null;
            this.playerName = null;
            this.playerUUID = null;
            this.state = AccountState.UNAUTHORIZED;
        }

        private AccountStorage(String accessToken, String playerName, String playerUUID, AccountState state) {
            this.accessToken = accessToken;
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.state = state;
        }
    }

    protected volatile AccountStorage storage;

    private final AccountType type;

    protected BaseAccount(String accessToken, String playerName, String playerUUID, AccountType type) {
        this.storage = new AccountStorage(accessToken, playerName, playerUUID, AccountState.AUTHORIZED);
        this.type = type;
    }

    public AccountStorage getAccountStorage() {
        return storage;
    }

    public final String getAccessToken() {
        return this.storage.accessToken;
    }

    public final String getPlayerName() {
        return this.storage.playerName;
    }

    public final String getPlayerUUID() {
        return this.storage.playerUUID;
    }

    public final AccountState getAccountState() {
        return this.storage.state;
    }

    public final AccountType getAccountType() {
        return type;
    }

    public final void setProfile(String accessToken, String playerName, String playerUUID) {
        this.storage = new AccountStorage(accessToken, playerName, playerUUID, AccountState.AUTHORIZED);
    }

    public final void setProfileState(AccountState state) {
        AccountStorage s = this.storage;
        this.storage = new AccountStorage(s.accessToken, s.playerName, s.playerUUID, state);
    }
}
