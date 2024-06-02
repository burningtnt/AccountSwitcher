package net.burningtnt.accountsx.gui;

import net.burningtnt.accountsx.AccountsX;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.accounts.api.Memory;
import net.burningtnt.accountsx.config.AccountManager;
import net.burningtnt.accountsx.config.AccountTaskExecutor;
import net.burningtnt.accountsx.gui.impl.DefaultMemory;
import net.burningtnt.accountsx.gui.impl.UIScreenImpl;
import net.burningtnt.accountsx.utils.I18NHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class AccountScreen extends Screen {
    private static final int LAYOUT_HORIZONTAL_SPACING = 16;
    private static final int LAYOUT_VERTICAL_SPACING = 32;

    private static final int LAYOUT_BUTTON_H = 20;

    private static final int LAYOUT_TOOL_BAR_W = 150;
    private static final int LAYOUT_TOOL_BAR_SPACING = 20;
    private static final int LAYOUT_TOOL_BAR_TEXT_CENTER_X = LAYOUT_HORIZONTAL_SPACING + LAYOUT_TOOL_BAR_W / 2;
    private static final int LAYOUT_TOOL_BAR_ADD_ACCOUNT_Y = LAYOUT_VERTICAL_SPACING + LAYOUT_BUTTON_H + LAYOUT_BUTTON_H;

    private static final int LAYOUT_ENTRY_X = LAYOUT_HORIZONTAL_SPACING + LAYOUT_TOOL_BAR_W + LAYOUT_TOOL_BAR_SPACING / 2 + 10;
    private static final int LAYOUT_ENTRY_H = 36;

    private final Text WORKING = Text.translatable("as.account.action.operating");

    private final Screen parent;
    private AccountListWidget accountListWidget;

    public AccountScreen(Screen parent) {
        super(Text.translatable("as.account.action.add_account"));
        this.parent = parent;
    }

    public void close() {
        assert this.client != null;

        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        if (this.accountListWidget != null) {
            this.accountListWidget.updateSize(
                    LAYOUT_ENTRY_X, this.width - LAYOUT_HORIZONTAL_SPACING,
                    LAYOUT_VERTICAL_SPACING + 20, this.height - LAYOUT_VERTICAL_SPACING - 20
            );
        } else {
            this.accountListWidget = new AccountListWidget(this.client,
                    LAYOUT_ENTRY_X, this.width - LAYOUT_HORIZONTAL_SPACING,
                    LAYOUT_VERTICAL_SPACING + 20, this.height - LAYOUT_VERTICAL_SPACING - 20,
                    LAYOUT_ENTRY_H
            );
        }

        this.addSelectableChild(this.accountListWidget);

        this.addField(new ButtonWidget(
                LAYOUT_HORIZONTAL_SPACING, LAYOUT_VERTICAL_SPACING,
                LAYOUT_TOOL_BAR_W, LAYOUT_BUTTON_H,
                Text.translatable("as.general.action.close"),
                button -> this.close())
        );

        int y = LAYOUT_TOOL_BAR_ADD_ACCOUNT_Y + 10;
        for (AccountType type : AccountType.CONFIGURABLE_VALUES) {
            this.addField(new ButtonWidget(
                    LAYOUT_HORIZONTAL_SPACING, y,
                    LAYOUT_TOOL_BAR_W, LAYOUT_BUTTON_H,
                    I18NHelper.translate(type),
                    button -> {
                        assert this.client != null;

                        AccountProvider<?> provider = type.getAccountProvider();

                        UIScreenImpl screen = provider.configure(UIScreenImpl::new);
                        if (screen != null) {
                            this.client.setScreen(screen.bind(this, loginScreen -> {
                                Memory memory = new DefaultMemory();

                                try {
                                    provider.validate(screen, memory);
                                } catch (IllegalArgumentException e) {
                                    AccountsX.LOGGER.warn("Invalid account argument.", e);
                                    return;
                                }

                                AccountTaskExecutor.submit(() -> {
                                    BaseAccount account = provider.login(memory);

                                    this.client.send(() -> {
                                        AccountManager.addAccount(account);

                                        this.accountListWidget.syncAccounts();
                                    });
                                });

                                loginScreen.close();
                            }));
                        } else {
                            AccountTaskExecutor.submit(() -> {
                                BaseAccount account = provider.login(null);

                                this.client.send(() -> {
                                    AccountManager.addAccount(account);

                                    this.accountListWidget.syncAccounts();
                                });
                            });
                        }
                    }
            ));

            y += LAYOUT_BUTTON_H;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        this.accountListWidget.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, AccountTaskExecutor.isRunning() ? WORKING : this.title, this.width / 2 + LAYOUT_ENTRY_X / 2, LAYOUT_VERTICAL_SPACING, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, AccountManager.getCurrentAccountInfoText(), this.width / 2 + LAYOUT_ENTRY_X / 2, this.height - LAYOUT_VERTICAL_SPACING, 0xFFFFFF);

        context.drawCenteredTextWithShadow(
                this.textRenderer, Text.translatable("as.account.action.add_account"),
                LAYOUT_TOOL_BAR_TEXT_CENTER_X, LAYOUT_TOOL_BAR_ADD_ACCOUNT_Y,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    public void addField(ClickableWidget drawable) {
        this.addDrawable(drawable);
        this.addSelectableChild(drawable);
    }
}
