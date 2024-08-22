package net.burningtnt.accountsx.core.manager.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public enum ConfigVersion {
    BASE(0) {
        @Override
        protected void upgrade(JsonObject config) {
            throw new IllegalStateException("There's no more legacy version.");
        }
    }, INJECTOR_SAFETY(1) {
        @Override
        protected void upgrade(JsonObject config) {
            if (config.get("accounts") instanceof JsonArray accounts) {
                for (int i = accounts.size() - 1; i >= 0; i--) {
                    JsonElement account = accounts.get(i);
                    if (account instanceof JsonObject jo && jo.get("type") instanceof JsonPrimitive jp && jp.isString()) {
                        if ("INJECTOR".equals(jp.getAsString())) {
                            accounts.remove(i);
                        }
                    }
                }
            }
        }
    }, RENAME_ACCOUNT_TYPE(2) {
        @Override
        protected void upgrade(JsonObject config) {
            if (config.get("accounts") instanceof JsonArray accounts) {
                for (JsonElement account : accounts) {
                    if (account instanceof JsonObject jo && jo.get("type") instanceof JsonPrimitive jp && jp.isString()) {
                        jo.addProperty("type", switch (jp.getAsString()) {
                            case "OFFLINE" -> "offline";
                            case "MICROSOFT" -> "microsoft";
                            case "INJECTOR" -> "injector.authlib-injector";
                            default -> throw new IllegalStateException("Unexpected account type: " + jp.getAsString());
                        });
                    }
                }
            }
        }
    };

    public static final ConfigVersion[] VALUES = values();

    private final int version;

    ConfigVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    protected abstract void upgrade(JsonObject config);
}
