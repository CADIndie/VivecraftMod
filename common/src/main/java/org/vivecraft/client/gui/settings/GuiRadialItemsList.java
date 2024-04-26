package org.vivecraft.client.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class GuiRadialItemsList extends ObjectSelectionList<GuiRadialItemsList.BaseEntry> {
    private final GuiRadialConfiguration parent;
    private final static int maxListLabelWidth = 90;

    public GuiRadialItemsList(GuiRadialConfiguration parent, Minecraft mc) {
        super(mc, parent.width, parent.height, 49, parent.height - 28, 20);
        this.parent = parent;
        this.buildList();
    }

    public void buildList() {
        KeyMapping[] mappings = ArrayUtils.clone(this.minecraft.options.keyMappings);
        Arrays.sort(mappings);
        String currentCategory = null;

        for (KeyMapping keymapping : mappings) {
            String category = keymapping != null ? keymapping.getCategory() : null;

            if (category != null) {
                if (!category.equals(currentCategory)) {
                    currentCategory = category;
                    this.addEntry(new CategoryEntry(category));
                }

                this.addEntry(new MappingEntry(keymapping, this.parent));
            }
        }
    }

    public class CategoryEntry extends BaseEntry {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.get(name);
            this.labelWidth = minecraft.font.width(this.labelText);
        }

        public void render(
            PoseStack poseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY,
            boolean pIsMouseOver, float pPartialTicks)
        {
            minecraft.font.draw(poseStack, this.labelText, (minecraft.screen.width / 2 - this.labelWidth / 2),
                (pTop + pHeight - 9 - 1), 6777215);
        }
    }

    public class MappingEntry extends BaseEntry {
        private final KeyMapping myKey;
        private final GuiRadialConfiguration parentScreen;

        private MappingEntry(KeyMapping key, GuiRadialConfiguration parent) {
            this.myKey = key;
            this.parentScreen = parent;
        }

        public void render(
            PoseStack poseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY,
            boolean pIsMouseOver, float pPartialTicks)
        {
            ChatFormatting chatformatting = ChatFormatting.WHITE;

            if (pIsMouseOver) {
                chatformatting = ChatFormatting.GREEN;
            }

            minecraft.font.draw(poseStack, chatformatting + I18n.get(this.myKey.getName()),
                (minecraft.screen.width / 2 - maxListLabelWidth / 2), (pTop + pHeight / 2 - 9 / 2), 16777215);
        }

        public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY) {
            this.parentScreen.setKey(this.myKey);
            return true;
        }
    }

    public static abstract class BaseEntry extends Entry<BaseEntry> {

        public BaseEntry() {}

        @Override
        public Component getNarration() {
            return null;
        }
    }
}
