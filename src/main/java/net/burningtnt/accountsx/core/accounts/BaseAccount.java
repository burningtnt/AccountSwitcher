package net.burningtnt.accountsx.core.accounts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.burningtnt.accountsx.core.utils.threading.ThreadState;
import net.burningtnt.accountsx.core.utils.threading.Threading;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@JsonAdapter(BaseAccount.Adapter.class)
public abstract class BaseAccount {
    static final class Adapter implements TypeAdapterFactory {
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
        private final String accessToken;

        private final String playerName;

        private final UUID playerUUID;

        private final transient AccountState state;

        private AccountStorage() {
            this.accessToken = null;
            this.playerName = null;
            this.playerUUID = null;
            this.state = AccountState.UNAUTHORIZED;
        }

        private AccountStorage(String accessToken, String playerName, UUID playerUUID, AccountState state) {
            this.accessToken = accessToken;
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.state = state;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getPlayerName() {
            return playerName;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public AccountState getState() {
            return state;
        }
    }

    protected volatile AccountStorage storage;

    private final AccountType type;

    protected BaseAccount(String accessToken, String playerName, UUID playerUUID, AccountType type) {
        this.storage = new AccountStorage(accessToken, playerName, playerUUID, AccountState.AUTHORIZED);
        this.type = type;
    }

    public AccountStorage getAccountStorage() {
        return storage;
    }

    public final AccountState getAccountState() {
        return this.storage.state;
    }

    public final AccountType getAccountType() {
        return type;
    }

    @ThreadState(ThreadState.ACCOUNT_WORKER)
    public final void setProfile(String accessToken, String playerName, UUID playerUUID) {
        Threading.checkAccountWorkerThread();

        this.storage = new AccountStorage(accessToken, playerName, playerUUID, AccountState.AUTHORIZED);
    }

    @ThreadState(ThreadState.ACCOUNT_WORKER)
    public final void setProfileState(AccountState state) {
        Threading.checkAccountWorkerThread();

        AccountStorage s = this.storage;
        this.storage = new AccountStorage(s.accessToken, s.playerName, s.playerUUID, state);
    }
}
