package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BlockedServerScreen extends Screen {

    private final Screen lastScreen;
    private final String server;
    private final Runnable onContinue;
    private MultiLineLabel message;

    public BlockedServerScreen(Screen lastScreen, String server, Runnable onContinue) {
        super(Component.translatable("vivecraft.messages.blocklist.title"));
        this.lastScreen = lastScreen;
        this.server = server;
        this.onContinue = onContinue;
    }

    protected void init() {

        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 32, 150, 20,
            Component.translatable("gui.back"), (p) ->
            Minecraft.getInstance().setScreen(this.lastScreen)));
        this.addRenderableWidget(
            new Button(this.width / 2 - 155, this.height - 32, 150, 20,
                Component.translatable("vivecraft.gui.continueWithout"), p -> this.onContinue.run()));
        this.message = MultiLineLabel.create(this.font,
            Component.translatable("vivecraft.messages.blocklist", this.server), 360);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        this.message.renderCentered(poseStack, this.width / 2, 120);
    }
}
