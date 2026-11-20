package com.nyaaclient.gui;

import com.nyaaclient.util.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main menu with borderless bold text entries on a dark gradient, wavy ribbons and twinkling stars on the right.
 * Layout is modelled on the screenshot and video you sent; exact positions and timings there are estimates.
 */
public final class NyaaMainMenuScreen extends Screen {
    private static final float ENTRY_SCALE = 1.5f;
    private static final float TITLE_SCALE = 2.0f;
    private static final int ENTRY_HEIGHT = 22;
    private static final int GROUP_GAP = 10;
    private static final int STAR_COUNT = 16;
    private static final int RIBBON_COUNT = 5;

    private final float[][] stars = new float[STAR_COUNT][3];

    private record Entry(String label, Runnable action, boolean groupBreakBefore) {
    }

    private record Slot(Entry entry, int x, int y, int w, int h) {
    }

    private final List<Entry> entries = new ArrayList<>();

    public NyaaMainMenuScreen() {
        super(Text.literal("Nyaa Client"));
        Random random = new Random(0x4E79AAL);
        for (float[] star : stars) {
            star[0] = 0.62f + random.nextFloat() * 0.34f;
            star[1] = 0.12f + random.nextFloat() * 0.76f;
            star[2] = random.nextFloat() * 6.28f;
        }

        entries.add(new Entry("Singleplayer", () -> client.setScreen(new SelectWorldScreen(this)), false));
        entries.add(new Entry("Multiplayer", this::openMultiplayer, false));
        entries.add(new Entry("Minecraft Realms", () -> client.setScreen(new RealmsMainScreen(this)), false));
        entries.add(new Entry("Minecraft Settings", () -> client.setScreen(new OptionsScreen(this, client.options)), true));
        entries.add(new Entry("Client", () -> client.setScreen(new ClickGuiScreen()), false));
        entries.add(new Entry("Quit Game", () -> client.scheduleStop(), false));
    }

    private void openMultiplayer() {
        Screen next = client.options.skipMultiplayerWarning
                ? new MultiplayerScreen(this)
                : new MultiplayerWarningScreen(this);
        client.setScreen(next);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer font = textRenderer;
        double time = (System.currentTimeMillis() % 3_600_000L) / 1000.0;

        context.fillGradient(0, 0, width, height, 0xFF111116, 0xFF2B2B31);
        drawRibbons(context, time);
        drawStars(context, time);

        int centerX = (int) (width * 0.36f);
        List<Slot> slots = layout(font, centerX);

        int titleY = slots.get(0).y - 52;
        Text title = Text.literal("Nyaa Client").formatted(Formatting.BOLD);
        int titleWidth = (int) (font.getWidth(title) * TITLE_SCALE);
        int logoSize = Theme.CAT_SIZE;
        int blockWidth = logoSize + 8 + titleWidth;
        int blockX = centerX - blockWidth / 2;
        context.drawTexture(Theme.CAT, blockX, titleY, logoSize, logoSize, 0.0f, 0.0f,
                Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE, Theme.CAT_SIZE);
        drawScaledText(context, font, title, blockX + logoSize + 8, titleY + (logoSize - (int) (font.fontHeight * TITLE_SCALE)) / 2,
                TITLE_SCALE, 0xFFFFFFFF, false);

        for (Slot slot : slots) {
            boolean hovered = mouseX >= slot.x && mouseX < slot.x + slot.w && mouseY >= slot.y && mouseY < slot.y + slot.h;
            Text label = Text.literal(slot.entry.label).formatted(Formatting.BOLD);
            int textWidth = (int) (font.getWidth(label) * ENTRY_SCALE);
            int textX = centerX - textWidth / 2;
            int textY = slot.y + (slot.h - (int) (font.fontHeight * ENTRY_SCALE)) / 2;
            drawScaledText(context, font, label, textX, textY, ENTRY_SCALE, hovered ? 0xFFFFFFFF : 0xFFC9C9D1, hovered);
        }

        context.drawTextWithShadow(font, "1.21.1", 4, height - 12, Theme.TEXT_DIM);
    }

    private List<Slot> layout(TextRenderer font, int centerX) {
        int totalHeight = 0;
        for (Entry entry : entries) {
            totalHeight += ENTRY_HEIGHT + (entry.groupBreakBefore ? GROUP_GAP : 0);
        }
        int y = Math.max(70, (height - totalHeight) / 2 + 10);
        List<Slot> slots = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.groupBreakBefore) {
                y += GROUP_GAP;
            }
            int w = (int) (font.getWidth(Text.literal(entry.label).formatted(Formatting.BOLD)) * ENTRY_SCALE) + 24;
            slots.add(new Slot(entry, centerX - w / 2, y, w, ENTRY_HEIGHT));
            y += ENTRY_HEIGHT;
        }
        return slots;
    }

    private static void drawScaledText(DrawContext context, TextRenderer font, Text text, int x, int y,
                                       float scale, int color, boolean glow) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        if (glow) {
            int halo = (Theme.ACCENT & 0x00FFFFFF) | 0x40000000;
            context.drawText(font, text, -1, 0, halo, false);
            context.drawText(font, text, 1, 0, halo, false);
            context.drawText(font, text, 0, -1, halo, false);
            context.drawText(font, text, 0, 1, halo, false);
        }
        context.drawTextWithShadow(font, text, 0, 0, color);
        matrices.pop();
    }

    private void drawRibbons(DrawContext context, double time) {
        for (int ribbon = 0; ribbon < RIBBON_COUNT; ribbon++) {
            float baseX = width * 0.80f + ribbon * 12.0f;
            int alpha = 0x16 + ribbon * 4;
            int color = (alpha << 24) | 0x00FFFFFF;
            for (int y = 0; y < height; y += 2) {
                double phase = y * 0.011 + time * 0.55 + ribbon * 0.7;
                double sway = Math.sin(phase) * 34.0 + Math.sin(phase * 0.5 + ribbon) * 18.0;
                int x = (int) (baseX + sway);
                context.fill(x, y, x + 2, y + 2, color);
            }
        }
    }

    private void drawStars(DrawContext context, double time) {
        for (float[] star : stars) {
            int x = (int) (star[0] * width);
            int y = (int) (star[1] * height);
            double twinkle = 0.55 + 0.45 * Math.sin(time * 1.6 + star[2]);
            int alpha = MathHelper.clamp((int) (twinkle * 255.0), 40, 255);
            int color = (alpha << 24) | 0x00FFFFFF;
            context.fill(x - 2, y, x + 3, y + 1, color);
            context.fill(x, y - 2, x + 1, y + 3, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = (int) (width * 0.36f);
            for (Slot slot : layout(textRenderer, centerX)) {
                if (mouseX >= slot.x && mouseX < slot.x + slot.w && mouseY >= slot.y && mouseY < slot.y + slot.h) {
                    slot.entry.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
