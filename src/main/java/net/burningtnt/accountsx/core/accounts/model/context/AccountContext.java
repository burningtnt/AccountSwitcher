package net.burningtnt.accountsx.core.accounts.model.context;

public record AccountContext(
        AuthServerContext server, AuthSecurityContext security, AuthPolicy policy
) {
}
