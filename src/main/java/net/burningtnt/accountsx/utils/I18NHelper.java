package net.burningtnt.accountsx.utils;

import net.burningtnt.accountsx.accounts.AccountState;
import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.minecraft.text.Text;

import java.util.Locale;

public final class I18NHelper {
    private I18NHelper() {
    }

    public static Text translate(AccountType type) {
        return Text.translatable("as.account.type." + type.name().toLowerCase(Locale.ROOT) + ".name");
    }

    public static Text translateAccountState(AccountState state) {
        return Text.translatable("as.account.state." + state.name().toLowerCase(Locale.ROOT) + ".name");
    }

    public static Text translateUsingAccount(BaseAccount account) {
        return Text.translatable(
                "as.account.type." + account.getAccountType().name().toLowerCase(Locale.ROOT) + ".using",
                account.getPlayerName()
        );
    }
}
