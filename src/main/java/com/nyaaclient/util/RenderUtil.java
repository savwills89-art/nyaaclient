package com.nyaaclient.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RenderUtil {
    private RenderUtil() {
    }

    /** Bounding box shifted to the entity's position interpolated between ticks. */
    public static Box interpolatedBox(Entity entity, float tickDelta) {
        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
        return entity.getBoundingBox().offset(x - entity.getX(), y - entity.getY(), z - entity.getZ());
    }

    public static Vec3d center(Box box) {
        return new Vec3d((box.minX + box.maxX) * 0.5, (box.minY + box.maxY) * 0.5, (box.minZ + box.maxZ) * 0.5);
    }
}
