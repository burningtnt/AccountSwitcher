package net.burningtnt.accountsx.accounts.gui;

public interface Memory {
    <T> void set(String guid, T value);

    <T> T get(String guid, Class<T> type);

    boolean isScreenClosed();

    void parkUntilScreenClosed() throws InterruptedException;
}
