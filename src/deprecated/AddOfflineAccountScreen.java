package deprecated;

import iafenvoy.accountswitcher.gui.AccountScreen;
import iafenvoy.accountswitcher.gui.ButtonWidget;
import deprecated.login.OfflineLogin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Deprecated(forRemoval = true)
public class AddOfflineAccountScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final AccountScreen parent;
    private TextFieldWidget usernameField;

    public AddOfflineAccountScreen(AccountScreen parent) {
        super(Text.translatable("as.gui.offline.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.usernameField = (TextFieldWidget) this.addField(new TextFieldWidget(client.textRenderer, this.width / 2 - 100, this.height / 2 - 30, 200, 20, Text.empty()));
        this.addField(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 10, 100, 20, Text.translatable("as.gui.Accept"), button -> {
            if (this.usernameField.getText().equals("")) return;
            AuthRequest request = new AuthRequest();
            request.name = this.usernameField.getText();
            AccountL account = null;
            try {
                account = new OfflineLogin().doAuth(request);
            } catch (IllegalMicrosoftAccountException e) {
                throw new RuntimeException(e);
            }
            parent.addAccount(account);
            this.openParent();
        }));
        this.addField(new ButtonWidget(this.width / 2, this.height / 2 + 10, 100, 20, Text.translatable("as.gui.Cancel"), button -> this.openParent()));
    }

    public void openParent() {
        client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);

        client.textRenderer.draw(Text.translatable("as.gui.injector.label1"), this.width / 2.0F - 175, this.height / 2.0F - 45, 0xFFFFFF, true,
                context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.injector.label2"), this.width / 2.0F - 175, this.height / 2.0F - 20, 0xFFFFFF, true,
                context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.injector.label3"), this.width / 2.0F - 175, this.height / 2.0F + 5, 0xFFFFFF, true,
                context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.injector.label4"), this.width / 2.0F - 175, this.height / 2.0F + 30, 0xFFFFFF, true,
                context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    public ClickableWidget addField(ClickableWidget drawable) {
        this.addDrawable(drawable);
        this.addSelectableChild(drawable);
        return drawable;
    }
}
