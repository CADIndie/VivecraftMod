package org.vivecraft.client.gui.framework.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import java.util.List;

public abstract class GuiDualListScreen extends Screen {

    protected final Screen lastScreen;

    protected SettingsList leftList;
    protected SettingsList rightList;

    protected EditBox searchBox;
    protected boolean searchable;

    protected int lastSelectedLeft = -1;
    protected int lastSelectedRight = -1;
    protected boolean reinit = false;

    public GuiDualListScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
        this.searchable = true;
    }

    @Override
    protected void rebuildWidgets() {
        // Save selection indices before widgets are cleared
        this.lastSelectedLeft = this.leftList != null && this.leftList.getSelected() != null
            ? this.leftList.children().indexOf(this.leftList.getSelected()) : -1;

        this.lastSelectedRight = this.rightList != null && this.rightList.getSelected() != null
            ? this.rightList.children().indexOf(this.rightList.getSelected()) : -1;

        super.rebuildWidgets();
    }

    @Override
    protected void init() {
        clearWidgets();
        double leftScrollAmount = this.leftList != null ? this.leftList.scrollAmount() : 0.0D;
        double rightScrollAmount = this.rightList != null ? this.rightList.scrollAmount() : 0.0D;
        String filter = this.leftList != null ? this.leftList.getActiveFilter() : ""; // TODO: this is a bit hacky

        int gap = 50;
        int sideMargin = 10;

        int top = this.searchable ? 48 : 32;
        int bottom = this.height - 28;
        int height = bottom - top;

        int totalWidth = this.width - sideMargin * 2;
        int listWidth = (totalWidth - gap) / 2;

        int rightX = sideMargin + listWidth + gap;

        this.leftList = new SettingsList(this, this.minecraft, listWidth, height, top, getLeftEntries());
        this.rightList = new SettingsList(this, this.minecraft, listWidth, height, top, getRightEntries());

        this.leftList.setX(sideMargin);
        this.rightList.setX(rightX);

        if (this.searchable) {
            this.leftList.filter(filter);
            this.rightList.filter(filter);

            this.searchBox = new EditBox(this.minecraft.font, this.width / 2 - 150, 20, 300, 20,
                Component.translatable("vivecraft.options.screen.search"));
            this.searchBox.setHint(Component.translatable("vivecraft.options.screen.search")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC));
            this.searchBox.setValue(filter);
            this.searchBox.setResponder(search -> {
                this.leftList.filter(search);
                this.rightList.filter(search);
            });
            this.addRenderableWidget(this.searchBox);
        } else {
            this.searchBox = null;
        }

        List<SettingsList.BaseEntry> leftChildren = this.leftList.children();
        List<SettingsList.BaseEntry> rightChildren = this.rightList.children();

        this.leftList.setSelected(
            this.lastSelectedLeft == -1 || leftChildren.isEmpty()
                ? null : leftChildren.get(Math.min(leftChildren.size() - 1, this.lastSelectedLeft))
        );
        this.leftList.setFocused(this.leftList.getSelected());
        this.leftList.setScrollAmount(leftScrollAmount);
        this.addRenderableWidget(this.leftList);

        this.rightList.setSelected(
            this.lastSelectedRight == -1 || rightChildren.isEmpty()
                ? null : rightChildren.get(Math.min(rightChildren.size() - 1, this.lastSelectedRight))
        );
        this.rightList.setFocused(this.rightList.getSelected());
        this.rightList.setScrollAmount(rightScrollAmount);
        this.addRenderableWidget(this.rightList);

        this.addLowerButtons(this.height - 26);
    }

    /**
     * method to add buttons below the list
     *
     * @param top Y position of the buttons
     */
    protected void addLowerButtons(int top) {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, top, 200, 20).build());
    }

    protected abstract List<SettingsList.BaseEntry> getLeftEntries();
    protected abstract List<SettingsList.BaseEntry> getRightEntries();

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.reinit) {
            init();
            this.reinit = false;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

//        // render custom tooltip
//        SettingsList.BaseEntry entry = null;
//        if ((this.minecraft.getLastInputType().isKeyboard()) && this.list1.getSelected() != null || this.list2.getSelected() != null) {
//            // render custom tooltip
//            entry = this.list1.getSelected() != null ? this.list1.getSelected() : this.list2.getSelected();
//        } else if (this.list1.getHovered() != null || this.list2.getHovered() != null) {
//            entry = this.list1.getHovered() != null ? this.list1.getHovered() : this.list2.getHovered();
//        }
//        if (entry != null && (this.list1.isEntryVisible(entry) || this.list2.isEntryVisible(entry)) &&
//            ((GuiGraphicsAccessor) guiGraphics).getDeferredTooltip() == null)
//        {//TODO Fix later
//            TooltipRenderer.renderTooltip(guiGraphics, entry.getTooltip(),
//                this.width / 2, this.list1.getRowTop(this.list1.children().indexOf(entry)) + 2,
//                this.list1.getItemHeight());
//        }
    }
}
