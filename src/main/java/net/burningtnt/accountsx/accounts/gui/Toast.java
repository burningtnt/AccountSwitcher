package net.burningtnt.accountsx.accounts.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Clipboard;
import net.minecraft.text.Text;

public interface Toast {
    static void copy(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            new Clipboard().setClipboard(client.getWindow().getHandle(), text);
        } else {
            client.send(() -> new Clipboard().setClipboard(client.getWindow().getHandle(), text));
        }
    }

    static void show(Type type, String title, String description, Object... args) {
        if (MinecraftClient.getInstance().isOnThread()) {
            SystemToast.show(
                    MinecraftClient.getInstance().getToastManager(),
                    SystemToast.Type.valueOf(type.name()),
                    Text.translatable(title),
                    description == null ? null : Text.translatable(description, args)
            );
        } else {
            MinecraftClient.getInstance().send(() -> SystemToast.show(
                    MinecraftClient.getInstance().getToastManager(),
                    SystemToast.Type.valueOf(type.name()),
                    Text.translatable(title),
                    description == null ? null : Text.translatable(description, args)
            ));
        }
    }

    enum Type {
        TUTORIAL_HINT,
        NARRATOR_TOGGLE,
        WORLD_BACKUP,
        PACK_LOAD_FAILURE,
        WORLD_ACCESS_FAILURE,
        PACK_COPY_FAILURE,
        PERIODIC_NOTIFICATION,
        UNSECURE_SERVER_WARNING
    }
}
