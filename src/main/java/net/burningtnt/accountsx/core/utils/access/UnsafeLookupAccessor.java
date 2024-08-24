package net.burningtnt.accountsx.core.utils.access;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class UnsafeLookupAccessor {
    private UnsafeLookupAccessor() {
    }

    public static MethodHandles.Lookup get() throws Throwable {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe U = (Unsafe) theUnsafe.get(null);
        Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        Object base = U.staticFieldBase(implLookup);
        long l = U.staticFieldOffset(implLookup);
        return (MethodHandles.Lookup) U.getObject(base, l);
    }
}
