package com.nyaaclient.util;

import com.nyaaclient.NyaaClient;
import net.minecraft.util.Identifier;

public final class Theme {
    public static final Identifier CAT = Identifier.of(NyaaClient.MOD_ID, "textures/gui/cat_32.png");
    public static final int CAT_SIZE = 32;

    public static final int ACCENT = 0xFFF2C6DE;
    public static final int ACCENT_DARK = 0xFF8E5C75;
    public static final int HEADER = 0xF0181820;
    public static final int ROW_OFF = 0xE00E0E14;
    public static final int ROW_ON = 0xE0402C38;
    public static final int ROW_HOVER = 0xE01E1E28;
    public static final int SETTING_BG = 0xE0141419;
    public static final int TEXT = 0xFFEDEDED;
    public static final int TEXT_DIM = 0xFF9A9AA6;
    public static final int HUD_BG = 0x90000000;

    private Theme() {
    }
}
