package net.burningtnt.accountsx.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.burningtnt.accountsx.accounts.AccountProvider;
import net.burningtnt.accountsx.accounts.AccountSession;
import net.burningtnt.accountsx.accounts.AccountType;
import net.burningtnt.accountsx.accounts.BaseAccount;
import net.burningtnt.accountsx.config.AccountManager;
import net.burningtnt.accountsx.config.AccountWorker;
import net.burningtnt.accountsx.utils.I18NHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AccountListWidget extends AlwaysSelectedEntryListWidget<AccountListWidget.AccountEntry> {
    public AccountListWidget(MinecraftClient client, int left, int right, int top, int bottom, int entryHeight) {
        super(client, right - left, bottom - top, top, bottom, entryHeight);
        this.updateSize(left, right, top, bottom);

        syncAccounts();
    }

    @Override
    public void updateSize(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public void syncAccounts() {
        this.clearEntries();
        for (BaseAccount account : AccountManager.getAccounts()) {
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
            if (AccountManager.getCurrentAccount() != entry.account) {
                AccountWorker.submit(() -> {
                    if (AccountManager.getCurrentAccount() == entry.account) {
                        return;
                    }

                    AccountSession session = AccountProvider.getProvider(entry.account).createProfile(entry.account);

                    client.send(() -> {
                        AccountManager.switchAccount(entry.account, session);

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
    protected int getScrollbarPositionX() {
        return this.right;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        AccountEntry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public class AccountEntry extends AlwaysSelectedEntryListWidget.Entry<AccountEntry> {
        private static final String ACTION_UP = "\u2191";

        private static final String ACTION_DELETE = "x";

        private static final String ACTION_DOWN = "\u2193";

        private final BaseAccount account;

        public AccountEntry(BaseAccount account) {
            this.account = account;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            VertexConsumerProvider vertexConsumers = context.getVertexConsumers();

            double scale = client.getWindow().getScaleFactor();
            RenderSystem.enableScissor(
                    (int) (scale * x),
                    (int) (scale * Math.max(y, AccountListWidget.this.top)),
                    (int) (scale * entryWidth),
                    (int) (scale * Math.min(entryHeight, AccountListWidget.this.bottom - y))
            );

            client.textRenderer.draw(this.account.getAccountStorage().getPlayerName(), (float) (x + 32 + 3), (float) (y + 1), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            client.textRenderer.draw(I18NHelper.translate(this.account.getAccountType()), (float) (x + 32 + 3), (float) (y + 1 + 9), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            client.textRenderer.draw(I18NHelper.translate(this.account.getAccountState()), (float) (x + 32 + 3), (float) (y + 1 + 18), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            RenderSystem.disableScissor();

            if (this.account.getAccountType() != AccountType.ENV_DEFAULT) {
                if (index > 1) {
                    client.textRenderer.draw(ACTION_UP, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_UP)), (float) (y + 1 + 5 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                }

                client.textRenderer.draw(ACTION_DELETE, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_DELETE)), (float) (y + 1 + 15 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

                if (index < getEntryCount() - 1) {
                    client.textRenderer.draw(ACTION_DOWN, (float) (x + entryWidth - 1.5 * client.textRenderer.getWidth(ACTION_DOWN)), (float) (y + 1 + 25 - client.textRenderer.fontHeight / 2), 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
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
