package net.burningtnt.accountsx.core.accounts.impl.microsoft;

import java.util.Base64;

public final class MicrosoftConstants {
    public static final String AUTH = wrap("aHR0cHM6Ly9hdXRoc2VydmVyLm1vamFuZy5jb20=");

    public static final String ACCOUNT = wrap("aHR0cHM6Ly9hcGkubW9qYW5nLmNvbQ==");

    public static final String SESSION = wrap("aHR0cHM6Ly9zZXNzaW9uc2VydmVyLm1vamFuZy5jb20=");

    public static final String SERVICES = wrap("aHR0cHM6Ly9hcGkubWluZWNyYWZ0c2VydmljZXMuY29t");

    private static String wrap(String value) {
        return new String(Base64.getDecoder().decode(value));
    }
}
