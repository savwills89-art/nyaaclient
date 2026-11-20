package com.nyaaclient.modules;

import com.nyaaclient.gui.ClickGuiScreen;
import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import org.lwjgl.glfw.GLFW;

/** Not shown in the GUI. Its key opens the ClickGUI; the default is Right Shift. */
public final class ClickGuiModule extends Module {
    public ClickGuiModule() {
        super("ClickGUI", "Opens the module menu.", Category.MISC, true);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onKeyPress() {
        mc.setScreen(new ClickGuiScreen());
    }
}
