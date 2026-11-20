package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;

/** Flag module: when enabled, the vanilla title screen is replaced by NyaaMainMenuScreen (see NyaaClient). */
public final class MainMenu extends Module {
    public MainMenu() {
        super("Main menu", "Replaces the vanilla title screen with the Nyaa Client menu.", Category.MISC);
        restoreEnabled(true);
    }
}
