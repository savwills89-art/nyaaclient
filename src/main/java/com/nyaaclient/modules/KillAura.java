package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.BoolSetting;
import com.nyaaclient.setting.ModeSetting;
import com.nyaaclient.setting.NumberSetting;
import com.nyaaclient.util.RotationUtil;
import com.nyaaclient.util.TargetFilter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public final class KillAura extends Module {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 3.5, 2.0, 6.0, 0.1));
    private final NumberSetting fov = addSetting(new NumberSetting("FOV", 180.0, 30.0, 360.0, 5.0));
    private final ModeSetting sort = addSetting(new ModeSetting("Sort", "Distance", "Distance", "Health"));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    // Degrees per tick. Uncalibrated: tune in game.
    private final NumberSetting turnSpeed = addSetting(new NumberSetting("Turn speed", 60.0, 5.0, 180.0, 5.0));
    private final TargetFilter filter = new TargetFilter(this, true, false, false);

    public KillAura() {
        super("KillAura", "Attacks the best target in range once the attack cooldown is ready.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if (player == null || world == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        LivingEntity target = findTarget(player, world);
        if (target == null) {
            return;
        }

        if (rotate.get()) {
            float[] needed = RotationUtil.anglesTo(player.getEyePos(), aimPoint(target));
            player.setYaw(RotationUtil.stepToward(player.getYaw(), needed[0], turnSpeed.getFloat()));
            player.setPitch(RotationUtil.stepToward(player.getPitch(), needed[1], turnSpeed.getFloat()));
        }

        if (player.getAttackCooldownProgress(0.5f) < 0.95f) {
            return;
        }
        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
    }

    private LivingEntity findTarget(ClientPlayerEntity player, ClientWorld world) {
        Vec3d eye = player.getEyePos();
        double maxRange = range.get();
        double halfFov = fov.get() / 2.0;
        boolean byHealth = sort.is("Health");

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : world.getEntities()) {
            if (!filter.test(entity)) {
                continue;
            }
            LivingEntity living = (LivingEntity) entity;
            double distance = RotationUtil.distanceToBox(eye, living.getBoundingBox());
            if (distance > maxRange) {
                continue;
            }
            float neededYaw = RotationUtil.anglesTo(eye, aimPoint(living))[0];
            if (RotationUtil.yawDistance(neededYaw, player.getYaw()) > halfFov) {
                continue;
            }
            double score = byHealth ? living.getHealth() : distance;
            if (score < bestScore) {
                bestScore = score;
                best = living;
            }
        }
        return best;
    }

    private static Vec3d aimPoint(LivingEntity target) {
        return target.getPos().add(0.0, target.getHeight() * 0.85, 0.0);
    }
}
