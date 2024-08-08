package net.burningtnt.accountsx.adapters.mc.ui;

import net.burningtnt.accountsx.core.accounts.AccountType;
import net.burningtnt.accountsx.core.accounts.BaseAccount;
import net.burningtnt.accountsx.core.adapters.api.AccountSession;
import net.burningtnt.accountsx.core.config.AccountManager;
import net.burningtnt.accountsx.core.config.AccountWorker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class AccountListWidget extends AlwaysSelectedEntryListWidget<AccountListWidget.AccountEntry> {
    public AccountListWidget(MinecraftClient client, int left, int right, int top, int bottom, int entryHeight) {
        super(client, right - left, bottom - top, top, entryHeight);
        this.updateSize(left, right, top, bottom);

        syncAccounts();
    }

    public void updateSize(int left, int right, int top, int bottom) {
        this.setX(left);
        this.setY(top);
        this.setWidth(right - left);
        this.setHeight(bottom - top);
    }

    public void syncAccounts() {
        this.clearEntries();
        for (BaseAccount account : AccountManager.getAccountsView()) {
            AccountEntry entry = new AccountEntry(account);
            this.addEntry(entry);

            if (account == AccountManager.getCurrentAccount()) {
                super.setSelected(entry);
            }
        }
    }

    @Override
    public void setSelected(@Nullable AccountListWidget.AccountEntry entry) {
        if (entry != null) {
            BaseAccount account = entry.account;
            if (AccountManager.getCurrentAccount() != account) {
                AccountWorker.submit(() -> {
                    if (AccountManager.getCurrentAccount() == account) {
                        return;
                    }

                    AccountSession session =  AccountManager.loginAccount(account);

                    client.send(() -> {
                        AccountManager.switchAccount(account, session);

                        super.setSelected(entry);
                        client.getNarratorManager().narrate((Text.translatable("narrator.select", entry.account.getAccountStorage().getPlayerName())).getString());
                    });
                });
            }
        } else {
            super.setSelected(null);
        }
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        AccountEntry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public class AccountEntry extends Entry<AccountEntry> {
        private static final String ACTION_UP = "\u2191";

        private static final String ACTION_DELETE = "x";

        private static final String ACTION_DOWN = "\u2193";

        private final BaseAccount account;

        public AccountEntry(BaseAccount account) {
            this.account = account;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(client.textRenderer, this.account.getAccountStorage().getPlayerName(), x + 32 + 3, y + 1, 0xFFFFFF, false);
            context.drawText(client.textRenderer, I18N.translate(this.account.getAccountType()), x + 32 + 3, y + 1 + 9, 0xFFFFFF, false);
            context.drawText(client.textRenderer, I18N.translate(this.account.getAccountState()), x + 32 + 3, y + 1 + 18, 0xFFFFFF, false);

            if (this.account.getAccountType() != AccountType.ENV_DEFAULT) {
                if (index > 1) {
                    client.textRenderer.draw(ACTION_UP, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_UP)), (float) (y + 1 + 5 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                }

                client.textRenderer.draw(ACTION_DELETE, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_DELETE)), (float) (y + 1 + 15 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

                if (index < getEntryCount() - 1) {
                    client.textRenderer.draw(ACTION_DOWN, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_DOWN)), (float) (y + 1 + 25 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int right = getRowRight();
            int buttonW = client.textRenderer.getWidth("x");
            if (mouseX >= right - buttonW * 1.5 && mouseX <= right - buttonW * 0.5) {
                int index = children().indexOf(this);
                int top = getRowTop(index);

                if (this.account.getAccountType() != AccountType.ENV_DEFAULT) {
                    if (index > 1) {
                        int btnTop = top + 1 + 5 - client.textRenderer.fontHeight / 2;
                        if (mouseY >= btnTop && mouseY <= btnTop + client.textRenderer.fontHeight) {
                            AccountManager.moveAccount(this.account, index - 1);
                            AccountListWidget.this.syncAccounts();
                            return false;
                        }
                    }

                    int btnTop = top + 1 + 15 - client.textRenderer.fontHeight / 2;
                    if (mouseY >= btnTop && mouseY <= btnTop + client.textRenderer.fontHeight) {
                        AccountManager.dropAccount(this.account);
                        AccountListWidget.this.syncAccounts();
                        return false;
                    }

                    if (index < getEntryCount() - 1) {
                        int btnTop2 = top + 1 + 25 - client.textRenderer.fontHeight / 2;
                        if (mouseY >= btnTop2 && mouseY <= btnTop2 + client.textRenderer.fontHeight) {
                            AccountManager.moveAccount(this.account, index + 1);
                            AccountListWidget.this.syncAccounts();
                            return false;
                        }
                    }
                }
            }

            setSelected(this);
            return false;
        }

        @Override
        public Text getNarration() {
            return Text.of("");
        }
    }
}
