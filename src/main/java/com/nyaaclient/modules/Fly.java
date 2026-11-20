package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

public final class Fly extends Module {
    private static final float VANILLA_FLY_SPEED = 0.05f;

    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 0.10, 0.05, 1.00, 0.01));

    public Fly() {
        super("Fly", "Creative-style flight. Jump to rise, sneak to descend.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        PlayerAbilities abilities = player.getAbilities();
        abilities.allowFlying = true;
        abilities.flying = true;
        abilities.setFlySpeed(speed.getFloat());
    }

    @Override
    protected void onDisable() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        PlayerAbilities abilities = player.getAbilities();
        abilities.setFlySpeed(VANILLA_FLY_SPEED);
        if (!player.isCreative() && !player.isSpectator()) {
            abilities.flying = false;
            abilities.allowFlying = false;
        }
    }
}
