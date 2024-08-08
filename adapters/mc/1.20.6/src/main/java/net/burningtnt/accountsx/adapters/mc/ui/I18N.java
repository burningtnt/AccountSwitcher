package net.burningtnt.accountsx.adapters.mc.ui;

import net.burningtnt.accountsx.core.accounts.AccountState;
import net.burningtnt.accountsx.core.accounts.AccountType;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.minecraft.text.Text;

import java.util.Locale;

public final class I18N {
    private I18N() {
    }

    public static Text translate(AccountType type) {
        return Text.translatable("as.account.type." + type.name().toLowerCase(Locale.ROOT) + ".name");
    }

    public static Text translate(AccountState state) {
        return Text.translatable("as.account.state." + state.name().toLowerCase(Locale.ROOT) + ".name");
    }

    public static Text translate(BaseAccount account) {
        return Text.translatable(
                "as.account.type." + account.getAccountType().name().toLowerCase(Locale.ROOT) + ".using",
                account.getAccountStorage().getPlayerName()
        );
    }
}
