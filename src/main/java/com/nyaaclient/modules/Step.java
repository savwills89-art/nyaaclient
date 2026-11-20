package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public final class Step extends Module {
    private static final double VANILLA_STEP_HEIGHT = 0.6;

    private final NumberSetting height = addSetting(new NumberSetting("Height", 1.0, 0.6, 2.5, 0.1));

    public Step() {
        super("Step", "Raises the player's step height attribute so you walk up blocks.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.setBaseValue(height.get());
        }
    }

    @Override
    protected void onDisable() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.setBaseValue(VANILLA_STEP_HEIGHT);
        }
    }
}
