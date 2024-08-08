package net.burningtnt.accountsx.core.utils.threading;

import net.burningtnt.accountsx.core.adapters.Adapters;
import net.burningtnt.accountsx.core.config.AccountWorker;

public final class Threading {
    private Threading() {
    }

    public static void checkMinecraftClientThread() {
        if (Adapters.getMinecraftAdapter().getMinecraftClientThread() != Thread.currentThread()) {
            throw new IllegalStateException("Should in Minecraft Client Thread.");
        }
    }

    public static void checkAccountWorkerThread() {
        if (AccountWorker.getWorkerThread() != Thread.currentThread()) {
            throw new IllegalStateException("Should in Account Worker Thread.");
        }
    }
}
