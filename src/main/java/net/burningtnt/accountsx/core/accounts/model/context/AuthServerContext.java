package net.burningtnt.accountsx.core.accounts.model.context;

public record AuthServerContext(
        String authURL, String accountURL, String sessionURL, String serviceURL,
        String name
) {
}
