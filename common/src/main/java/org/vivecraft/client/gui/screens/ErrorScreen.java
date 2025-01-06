package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;

public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;
    private TextScrollWidget text;

    public ErrorScreen(Component title, Component error) {
        super(title);
        this.lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    @Override
    protected void init() {

        this.text = this.addRenderableWidget(
            new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, this.error));

        this.addRenderableWidget(
            new Button(this.width / 2 + 5, this.height - 32, 150, 20,
                Component.translatable("gui.back"),
                (p) -> Minecraft.getInstance().setScreen(this.lastScreen)));
        this.addRenderableWidget(
            new Button(this.width / 2 - 155, this.height - 32, 150, 20,
                Component.translatable("chat.copy.click"),
                (p) -> Minecraft.getInstance().keyboardHandler.setClipboard(
                    this.title.getString() + "\n" + this.error.getString())));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        Style style = this.text.getMouseoverStyle(mouseX, mouseY);
        if (style != null) {
            renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        }
    }
}
