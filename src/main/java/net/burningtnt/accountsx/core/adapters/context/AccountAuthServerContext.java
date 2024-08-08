package net.burningtnt.accountsx.core.adapters.context;

public record AccountAuthServerContext(
        String authURL, String accountURL, String sessionURL, String serviceURL, String name,
        LoginMode mode
) {
}
