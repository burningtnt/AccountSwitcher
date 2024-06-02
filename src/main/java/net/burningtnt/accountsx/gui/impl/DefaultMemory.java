package net.burningtnt.accountsx.gui.impl;

import net.burningtnt.accountsx.accounts.api.Memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultMemory implements Memory {
    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String guid, T value) {
        objects.put(guid, value);
    }

    @Override
    public <T> T get(String guid, Class<T> type) {
        return type.cast(objects.get(guid));
    }
}
