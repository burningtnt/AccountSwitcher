package deprecated;

import iafenvoy.accountswitcher.gui.AccountScreen;
import iafenvoy.accountswitcher.gui.ButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Deprecated(forRemoval = true)
public class AddCustomAccountScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final AccountScreen parent;
    private TextFieldWidget username, uuid, token;

    public AddCustomAccountScreen(AccountScreen parent) {
        super(Text.translatable("as.gui.custom.title"));
        this.parent = parent;
    }

    public void openParent() {
        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        this.username = (TextFieldWidget) this.addField(new TextFieldWidget(client.textRenderer, this.width / 2 - 100, this.height / 2 - 50, 200, 20, Text.translatable("")));
        this.uuid = (TextFieldWidget) this.addField(new TextFieldWidget(client.textRenderer, this.width / 2 - 100, this.height / 2 - 25, 200, 20, Text.translatable("")));
        this.token = (TextFieldWidget) this.addField(new TextFieldWidget(client.textRenderer, this.width / 2 - 100, this.height / 2, 200, 20, Text.translatable("")));
        this.token.setMaxLength(1000);
        this.addField(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 25, 100, 20, Text.translatable("as.gui.Accept"), button -> {
            new Thread(() -> {
                AccountL account = new AccountL(AccountL.AccountType.Custom);
                account.setUsername(this.username.getText());
                account.setUuid(this.uuid.getText());
                account.setMcToken(this.token.getText());
                this.parent.addAccount(account);
            }).start();
            this.openParent();
        }));
        this.addField(new ButtonWidget(this.width / 2, this.height / 2 + 25, 100, 20, Text.translatable("as.gui.Cancel"), button -> this.openParent()));

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        client.textRenderer.draw(Text.translatable("as.gui.custom.label1"), this.width / 2.0F - 175, this.height / 2.0F - 45, 0xFFFFFF,
                true, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.custom.label2"), this.width / 2.0F - 175, this.height / 2.0F - 20, 0xFFFFFF,
                true, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.custom.label3"), this.width / 2.0F - 175, this.height / 2.0F + 5, 0xFFFFFF,
                true, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);
        client.textRenderer.draw(Text.translatable("as.gui.custom.label4"), this.width / 2.0F - 175, this.height / 2.0F + 30, 0xFFFFFF,
                true, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
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
