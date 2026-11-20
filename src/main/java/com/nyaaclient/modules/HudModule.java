package com.nyaaclient.modules;

import com.nyaaclient.NyaaClient;
import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.BoolSetting;
import com.nyaaclient.util.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public final class HudModule extends Module {
    private static final int LIST_ROW_HEIGHT = 11;
    private static final int ICON_DRAW_SIZE = 16;

    private final BoolSetting watermark = addSetting(new BoolSetting("Watermark", true));
    private final BoolSetting moduleList = addSetting(new BoolSetting("Module list", true));

    public HudModule() {
        super("HUD", "Watermark with the cat icon and the list of enabled modules.", Category.MISC);
        restoreEnabled(true);
    }

    @Override
    public void onHud(DrawContext context) {
        if (mc.options.hudHidden || mc.textRenderer == null) {
            return;
        }
        TextRenderer font = mc.textRenderer;

        if (watermark.get()) {
            String title = "Nyaa Client";
            int width = font.getWidth(title);
            context.fill(2, 2, 2 + ICON_DRAW_SIZE + 6 + width + 4, 2 + ICON_DRAW_SIZE, Theme.HUD_BG);
            context.drawTexture(Theme.CAT, 2, 2, ICON_DRAW_SIZE, ICON_DRAW_SIZE, 0.0f, 0.0f,
                    Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE);
            context.drawTextWithShadow(font, title, 2 + ICON_DRAW_SIZE + 4, 2 + (ICON_DRAW_SIZE - font.fontHeight) / 2,
                    Theme.ACCENT);
        }

        if (moduleList.get()) {
            List<Module> active = NyaaClient.instance().modules().all().stream()
                    .filter(m -> m.isEnabled() && !m.isHidden() && m != this)
                    .sorted(Comparator.comparingInt((Module m) -> -font.getWidth(m.getName())))
                    .toList();
            int screenWidth = context.getScaledWindowWidth();
            int y = 2;
            for (Module module : active) {
                int textWidth = font.getWidth(module.getName());
                context.fill(screenWidth - textWidth - 6, y, screenWidth, y + LIST_ROW_HEIGHT, Theme.HUD_BG);
                context.fill(screenWidth - 2, y, screenWidth, y + LIST_ROW_HEIGHT, Theme.ACCENT);
                context.drawTextWithShadow(font, module.getName(), screenWidth - textWidth - 4, y + 1, Theme.ACCENT);
                y += LIST_ROW_HEIGHT;
            }
        }
    }
}
