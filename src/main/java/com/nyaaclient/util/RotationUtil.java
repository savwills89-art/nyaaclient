package com.nyaaclient.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Pure math helpers. Minecraft convention: yaw 0 looks toward +Z, yaw 90 toward -X, degrees;
 * pitch -90 is straight up, +90 straight down; Y is up.
 */
public final class RotationUtil {
    private RotationUtil() {
    }

    /** Returns {yaw, pitch} in degrees needed to look from {@code from} toward {@code to}. */
    public static float[] anglesTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[] {MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f)};
    }

    /** Moves {@code current} toward {@code target} by at most {@code maxStep} degrees along the shortest arc. */
    public static float stepToward(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        return current + MathHelper.clamp(delta, -maxStep, maxStep);
    }

    /** Smallest absolute angle in degrees between two yaw values. */
    public static float yawDistance(float a, float b) {
        return Math.abs(MathHelper.wrapDegrees(a - b));
    }

    /** Distance from a point to the closest point of a box; 0 if the point is inside. */
    public static double distanceToBox(Vec3d point, Box box) {
        double cx = MathHelper.clamp(point.x, box.minX, box.maxX);
        double cy = MathHelper.clamp(point.y, box.minY, box.maxY);
        double cz = MathHelper.clamp(point.z, box.minZ, box.maxZ);
        double dx = point.x - cx;
        double dy = point.y - cy;
        double dz = point.z - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
