package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;

public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Sprints automatically while moving forward.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        boolean canSprint = player.input.movementForward > 0.0f
                && !player.isSneaking()
                && !player.isUsingItem()
                && !player.horizontalCollision
                && player.getHungerManager().getFoodLevel() > 6;
        if (canSprint) {
            player.setSprinting(true);
        }
    }
}
