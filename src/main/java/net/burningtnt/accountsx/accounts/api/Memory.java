package net.burningtnt.accountsx.accounts.api;

public interface Memory {
    <T> void set(String guid, T value);

    <T> T get(String guid, Class<T> type);
}
