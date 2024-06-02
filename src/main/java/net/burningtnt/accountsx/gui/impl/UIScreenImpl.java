package net.burningtnt.accountsx.gui.impl;

import net.burningtnt.accountsx.accounts.api.UIScreen;
import net.burningtnt.accountsx.gui.AccountScreen;
import net.burningtnt.accountsx.gui.ButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class UIScreenImpl implements UIScreen {
    private static final class ValuedWidget<T extends Drawable> {
        private final String description;

        private T widget;

        public ValuedWidget(String description) {
            this.description = description;
        }
    }

    private boolean readonly = false;

    private String title;

    private final Map<String, ValuedWidget<TextFieldWidget>> inputs = new LinkedHashMap<>();

    @Override
    public void setTitle(String description) {
        if (readonly) {
            throw new IllegalStateException("UIScreen has been frozen.");
        }
        this.title = description;
    }

    @Override
    public void putTextInput(String guid, String description) {
        if (readonly) {
            throw new IllegalStateException("UIScreen has been frozen.");
        }
        this.inputs.put(guid, new ValuedWidget<>(description));
    }

    @Override
    public String getTextInput(String guid) {
        if (!readonly) {
            throw new IllegalStateException("UIScreen hasn't been frozen.");
        }
        return this.inputs.get(guid).widget.getText();
    }

    public Screen bind(AccountScreen parent, Consumer<Screen> callback) {
        readonly = true;

        return new Screen(Text.translatable(this.title)) {
            @Override
            public void close() {
                assert this.client != null;

                this.client.setScreen(parent);
            }

            @Override
            protected void init() {
                assert this.client != null;

                super.init();

                int widgetsTop = this.height / 2 - (UIScreenImpl.this.inputs.size() + 1) * 25 / 2;
                int widgetsLeft = this.width / 2 - 50;

                for (ValuedWidget<TextFieldWidget> widget : UIScreenImpl.this.inputs.values()) {
                    widget.widget = this.addField(
                            new TextFieldWidget(this.client.textRenderer, widgetsLeft, widgetsTop, 200, 20, Text.empty())
                    );

                    widgetsTop += 25;
                }

                Screen that = this;
                this.addField(new ButtonWidget(widgetsLeft, widgetsTop, 90, 20, Text.translatable("as.general.login"), widget -> callback.accept(that)));
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                assert this.client != null;

                super.renderBackground(context);
                super.render(context, mouseX, mouseY, delta);

                int textTop = this.height / 2 - (UIScreenImpl.this.inputs.size() + 1) * 25 / 2 + 5;
                int textLeft = this.width / 2 - 170;

                for (ValuedWidget<TextFieldWidget> widget : UIScreenImpl.this.inputs.values()) {
                    client.textRenderer.draw(
                            Text.translatable(widget.description), textLeft, textTop, 0xFFFFFF, true,
                            context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL,
                            0, 0xF000F0
                    );

                    textTop += 25;
                }
            }

            public <T extends ClickableWidget> T addField(T drawable) {
                this.addDrawable(drawable);
                this.addSelectableChild(drawable);
                return drawable;
            }
        };
    }
}
