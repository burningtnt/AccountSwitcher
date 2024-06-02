package deprecated.login;

import deprecated.AccountL;

@Deprecated(forRemoval = true)
public interface ILogin {
    AccountL doAuth(AuthRequest request) throws IllegalMicrosoftAccountException;

    void useAccount(AccountL account);

    void refreshAccessToken(AccountL account);

    String getProcess();
}
