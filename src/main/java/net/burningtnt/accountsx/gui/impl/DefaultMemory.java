package net.burningtnt.accountsx.gui.impl;

import net.burningtnt.accountsx.accounts.gui.Memory;
import net.burningtnt.accountsx.utils.threading.ThreadState;
import net.burningtnt.accountsx.utils.threading.Threading;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultMemory implements Memory {
    private final Screen loginScreen;

    public DefaultMemory(Screen loginScreen) {
        this.loginScreen = loginScreen;
    }

    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String guid, T value) {
        objects.put(guid, value);
    }

    @Override
    public <T> T get(String guid, Class<T> type) {
        return type.cast(objects.get(guid));
    }

    @Override
    @ThreadState(ThreadState.ACCOUNT_WORKER)
    public boolean isScreenClosed() {
        Threading.checkAccountWorkerThread();

        // TODO: currentScreen is not volatile.
        return MinecraftClient.getInstance().currentScreen != loginScreen;
    }

    @Override
    @ThreadState(ThreadState.ACCOUNT_WORKER)
    public void parkUntilScreenClosed() throws InterruptedException {
        Threading.checkAccountWorkerThread();

        while (true) {
            if (isScreenClosed()) {
                return;
            }

            Thread.sleep(100);
        }
    }
}
