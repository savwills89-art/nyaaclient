package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.BoolSetting;
import com.nyaaclient.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class Speed extends Module {
    // Blocks per tick before friction. Uncalibrated: vanilla sprinting is roughly 0.28.
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 0.35, 0.10, 1.50, 0.01));
    private final BoolSetting onlyOnGround = addSetting(new BoolSetting("Only on ground", false));

    public Speed() {
        super("Speed", "Sets horizontal velocity directly along your input direction.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        if (player.isTouchingWater() || player.isInLava() || player.isClimbing() || player.isFallFlying()
                || player.getAbilities().flying) {
            return;
        }
        if (onlyOnGround.get() && !player.isOnGround()) {
            return;
        }

        double forward = player.input.movementForward;
        double sideways = player.input.movementSideways;
        if (forward == 0.0 && sideways == 0.0) {
            return;
        }
        double length = Math.sqrt(forward * forward + sideways * sideways);
        if (length > 1.0) {
            forward /= length;
            sideways /= length;
        }

        double yaw = Math.toRadians(player.getYaw());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        // Forward is (-sin, cos); positive sideways input is the player's left, (cos, sin).
        double motionX = (-sin * forward + cos * sideways) * speed.get();
        double motionZ = (cos * forward + sin * sideways) * speed.get();

        Vec3d current = player.getVelocity();
        player.setVelocity(motionX, current.y, motionZ);
    }
}
