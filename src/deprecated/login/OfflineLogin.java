package deprecated.login;

import deprecated.AccountL;

import java.util.UUID;

@Deprecated(forRemoval = true)
public class OfflineLogin implements ILogin {

    @Override
    public AccountL doAuth(AuthRequest request) throws IllegalMicrosoftAccountException {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return new AccountL(AccountL.AccountType.Offline, "", "", request.name, uuid);
    }

    @Override
    public void useAccount(AccountL account) {

    }

    @Override
    public void refreshAccessToken(AccountL account) {

    }

    @Override
    public String getProcess() {
        return null;
    }
}
