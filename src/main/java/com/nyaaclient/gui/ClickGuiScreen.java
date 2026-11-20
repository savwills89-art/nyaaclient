package com.nyaaclient.gui;

import com.nyaaclient.NyaaClient;
import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.modules.ClickGuiModule;
import com.nyaaclient.setting.BoolSetting;
import com.nyaaclient.setting.ModeSetting;
import com.nyaaclient.setting.NumberSetting;
import com.nyaaclient.setting.Setting;
import com.nyaaclient.util.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Left click toggles a module or a setting, right click on a module expands its settings,
 * middle click on a module binds a key (Esc cancels, Backspace/Delete clears).
 * Drag a panel header to move it, drag a slider to change a number.
 */
public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 112;
    private static final int HEADER_HEIGHT = 18;
    private static final int MODULE_HEIGHT = 15;
    private static final int SETTING_HEIGHT = 13;
    private static final long CLOSE_KEY_GUARD_MS = 250L;

    private static final List<Panel> PANELS = new ArrayList<>();

    private final long openedAt = System.currentTimeMillis();
    private Module binding;
    private Panel draggingPanel;
    private int dragOffsetX;
    private int dragOffsetY;
    private NumberSetting draggingSlider;
    private Panel sliderPanel;

    public ClickGuiScreen() {
        super(Text.literal("Nyaa ClickGUI"));
        if (PANELS.isEmpty()) {
            int x = 8;
            for (Category category : Category.values()) {
                PANELS.add(new Panel(category, x, 44));
                x += PANEL_WIDTH + 8;
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        NyaaClient.instance().config().save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x80000000);
        TextRenderer font = textRenderer;

        context.drawTexture(Theme.CAT, 8, 6, 32, 32, 0.0f, 0.0f,
                Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE);
        context.drawTextWithShadow(font, "Nyaa Client", 46, 12, Theme.ACCENT);
        context.drawTextWithShadow(font, "RMB: settings   MMB: bind key", 46, 24, Theme.TEXT_DIM);

        Module hovered = null;
        for (Panel panel : PANELS) {
            for (Row row : rows(panel)) {
                boolean over = inside(mouseX, mouseY, panel.x, row.y, PANEL_WIDTH, row.height);
                drawRow(context, font, panel, row, over);
                if (over && row.kind == RowKind.MODULE) {
                    hovered = row.module;
                }
            }
        }

        if (hovered != null) {
            String tip = hovered.getDescription();
            int tipWidth = font.getWidth(tip);
            int tipX = Math.min(mouseX + 8, width - tipWidth - 6);
            int tipY = mouseY + 10;
            context.fill(tipX - 2, tipY - 2, tipX + tipWidth + 2, tipY + font.fontHeight + 2, 0xF0101014);
            context.drawTextWithShadow(font, tip, tipX, tipY, Theme.TEXT);
        }
    }

    private void drawRow(DrawContext context, TextRenderer font, Panel panel, Row row, boolean hovered) {
        int x = panel.x;
        int y = row.y;
        switch (row.kind) {
            case HEADER -> {
                context.fill(x, y, x + PANEL_WIDTH, y + row.height, Theme.HEADER);
                context.fill(x, y + row.height - 1, x + PANEL_WIDTH, y + row.height, Theme.ACCENT_DARK);
                context.drawTextWithShadow(font, panel.category.displayName(), x + 6, y + 5, Theme.ACCENT);
                context.drawTextWithShadow(font, panel.open ? "-" : "+", x + PANEL_WIDTH - 10, y + 5, Theme.TEXT_DIM);
            }
            case MODULE -> {
                Module module = row.module;
                int background = module.isEnabled() ? Theme.ROW_ON : (hovered ? Theme.ROW_HOVER : Theme.ROW_OFF);
                context.fill(x, y, x + PANEL_WIDTH, y + row.height, background);
                if (module.isEnabled()) {
                    context.fill(x, y, x + 2, y + row.height, Theme.ACCENT);
                }
                String label = binding == module ? "Press a key..." : module.getName();
                context.drawTextWithShadow(font, label, x + 6, y + 4, module.isEnabled() ? Theme.TEXT : Theme.TEXT_DIM);
                if (binding != module && module.getKey() >= 0) {
                    String keyName = "[" + keyName(module.getKey()) + "]";
                    context.drawTextWithShadow(font, keyName, x + PANEL_WIDTH - font.getWidth(keyName) - 4, y + 4,
                            Theme.TEXT_DIM);
                }
            }
            case SETTING -> drawSetting(context, font, x, y, row.setting);
        }
    }

    private void drawSetting(DrawContext context, TextRenderer font, int x, int y, Setting<?> setting) {
        context.fill(x, y, x + PANEL_WIDTH, y + SETTING_HEIGHT, Theme.SETTING_BG);
        if (setting instanceof BoolSetting bool) {
            context.drawTextWithShadow(font, bool.getName(), x + 8, y + 3, Theme.TEXT_DIM);
            int box = x + PANEL_WIDTH - 12;
            context.fill(box, y + 3, box + 7, y + 10, 0xFF2A2A34);
            if (bool.get()) {
                context.fill(box + 1, y + 4, box + 6, y + 9, Theme.ACCENT);
            }
        } else if (setting instanceof ModeSetting mode) {
            String text = mode.getName() + ": " + mode.get();
            context.drawTextWithShadow(font, text, x + 8, y + 3, Theme.TEXT_DIM);
        } else if (setting instanceof NumberSetting number) {
            double t = (number.get() - number.getMin()) / (number.getMax() - number.getMin());
            int trackLeft = x + 4;
            int trackRight = x + PANEL_WIDTH - 4;
            int fill = trackLeft + (int) Math.round(MathHelper.clamp(t, 0.0, 1.0) * (trackRight - trackLeft));
            context.fill(trackLeft, y, fill, y + SETTING_HEIGHT, Theme.ACCENT_DARK);
            String text = number.getName() + ": " + formatNumber(number);
            context.drawTextWithShadow(font, text, x + 8, y + 3, Theme.TEXT);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        binding = null;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        for (Panel panel : PANELS) {
            for (Row row : rows(panel)) {
                if (!inside(mx, my, panel.x, row.y, PANEL_WIDTH, row.height)) {
                    continue;
                }
                handleClick(panel, row, mx, my, button);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleClick(Panel panel, Row row, int mouseX, int mouseY, int button) {
        switch (row.kind) {
            case HEADER -> {
                if (button == 0) {
                    draggingPanel = panel;
                    dragOffsetX = mouseX - panel.x;
                    dragOffsetY = mouseY - panel.y;
                } else if (button == 1) {
                    panel.open = !panel.open;
                }
            }
            case MODULE -> {
                Module module = row.module;
                if (button == 0) {
                    module.toggle();
                } else if (button == 1 && !module.getSettings().isEmpty()) {
                    if (!panel.expanded.remove(module)) {
                        panel.expanded.add(module);
                    }
                } else if (button == 2) {
                    binding = module;
                }
            }
            case SETTING -> {
                Setting<?> setting = row.setting;
                if (setting instanceof BoolSetting bool && button == 0) {
                    bool.toggle();
                } else if (setting instanceof ModeSetting mode && button == 0) {
                    mode.cycle();
                } else if (setting instanceof NumberSetting number && button == 0) {
                    draggingSlider = number;
                    sliderPanel = panel;
                    updateSlider(mouseX);
                }
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingPanel != null && button == 0) {
            draggingPanel.x = MathHelper.clamp((int) mouseX - dragOffsetX, 0, Math.max(0, width - PANEL_WIDTH));
            draggingPanel.y = MathHelper.clamp((int) mouseY - dragOffsetY, 0, Math.max(0, height - HEADER_HEIGHT));
            return true;
        }
        if (draggingSlider != null && button == 0) {
            updateSlider((int) mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingPanel = null;
            draggingSlider = null;
            sliderPanel = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSlider(int mouseX) {
        if (draggingSlider == null || sliderPanel == null) {
            return;
        }
        double t = MathHelper.clamp((mouseX - (sliderPanel.x + 4)) / (double) (PANEL_WIDTH - 8), 0.0, 1.0);
        draggingSlider.set(draggingSlider.getMin() + t * (draggingSlider.getMax() - draggingSlider.getMin()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding != null) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                binding.setKey(-1);
            } else if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                binding.setKey(keyCode);
            }
            binding = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        int openKey = NyaaClient.instance().modules().get(ClickGuiModule.class).getKey();
        if (keyCode == openKey && System.currentTimeMillis() - openedAt > CLOSE_KEY_GUARD_MS) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private List<Row> rows(Panel panel) {
        List<Row> rows = new ArrayList<>();
        int y = panel.y;
        rows.add(new Row(RowKind.HEADER, y, HEADER_HEIGHT, null, null));
        y += HEADER_HEIGHT;
        if (!panel.open) {
            return rows;
        }
        for (Module module : NyaaClient.instance().modules().byCategory(panel.category)) {
            rows.add(new Row(RowKind.MODULE, y, MODULE_HEIGHT, module, null));
            y += MODULE_HEIGHT;
            if (panel.expanded.contains(module)) {
                for (Setting<?> setting : module.getSettings()) {
                    rows.add(new Row(RowKind.SETTING, y, SETTING_HEIGHT, module, setting));
                    y += SETTING_HEIGHT;
                }
            }
        }
        return rows;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static String keyName(int keyCode) {
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }

    private static String formatNumber(NumberSetting number) {
        return String.format(Locale.ROOT, "%." + number.decimals() + "f", number.get());
    }

    private enum RowKind {HEADER, MODULE, SETTING}

    private record Row(RowKind kind, int y, int height, Module module, Setting<?> setting) {
    }

    private static final class Panel {
        final Category category;
        final Set<Module> expanded = new HashSet<>();
        int x;
        int y;
        boolean open = true;

        Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }
}
