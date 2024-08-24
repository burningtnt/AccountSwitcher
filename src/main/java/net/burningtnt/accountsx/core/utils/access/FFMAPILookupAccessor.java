package net.burningtnt.accountsx.core.utils.access;

import java.lang.invoke.MethodHandles;

public final class FFMAPILookupAccessor {
    private FFMAPILookupAccessor() {
    }

    public static MethodHandles.Lookup get() throws Throwable {
        // UnsafeLookupAccessor might NOT work in future versions of JDK due to JEP 471, where it had been settled to remove the memory access method in JDK 24.
        // But there's no existed plans for restrict the usage of FFM API.
        // We have a workaround for future JDK releases here: https://gist.github.com/burningtnt/c188e65f048c2cf096db095e5858b5af
        throw new IllegalStateException("FFMAPI is still a preview feature in Java 21.");
    }
}
