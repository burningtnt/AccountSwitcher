package net.burningtnt.accountsx.core.utils.threading;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface ThreadState {
    String ACCOUNT_WORKER = "Account Worker";

    String value();
}
