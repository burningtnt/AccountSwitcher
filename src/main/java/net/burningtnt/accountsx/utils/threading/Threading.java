package net.burningtnt.accountsx.utils.threading;

import net.burningtnt.accountsx.config.AccountWorker;
import net.minecraft.client.MinecraftClient;

public final class Threading {
    private Threading() {
    }

    public static void checkMinecraftClientThread() {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("Should in Minecraft Client Thread.");
        }
    }

    public static void checkAccountWorkerThread() {
        if (AccountWorker.getWorkerThread() != Thread.currentThread()) {
            throw new IllegalStateException("Should in Account Worker Thread.");
        }
    }
}
