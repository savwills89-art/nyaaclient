package com.nyaaclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Collects line segments in world coordinates and draws them in one call.
 * Vertices are made camera-relative because the world matrix stack carries rotation only.
 * Depth and blend state are set by the caller (see NyaaClient world render hook).
 */
public final class LineBatch {
    private final BufferBuilder buffer;
    private final Matrix4f matrix;
    private final Vec3d camera;

    public LineBatch(MatrixStack matrices, Vec3d cameraPos) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        this.buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        this.matrix = matrices.peek().getPositionMatrix();
        this.camera = cameraPos;
    }

    public void line(Vec3d from, Vec3d to, int argb) {
        vertex(from, argb);
        vertex(to, argb);
    }

    public void box(Box box, int argb) {
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        line(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), argb);
        line(new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), argb);
        line(new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), argb);
        line(new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), argb);

        line(new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), argb);
        line(new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), argb);
        line(new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), argb);
        line(new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), argb);

        line(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), argb);
        line(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), argb);
        line(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), argb);
        line(new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), argb);
    }

    public void draw() {
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void vertex(Vec3d world, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        buffer.vertex(matrix, (float) (world.x - camera.x), (float) (world.y - camera.y), (float) (world.z - camera.z))
                .color(r, g, b, a);
    }
}
