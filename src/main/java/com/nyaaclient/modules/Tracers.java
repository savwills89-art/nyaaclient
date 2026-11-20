package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.NumberSetting;
import com.nyaaclient.util.LineBatch;
import com.nyaaclient.util.RenderUtil;
import com.nyaaclient.util.TargetFilter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class Tracers extends Module {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 96.0, 16.0, 256.0, 8.0));
    private final TargetFilter filter = new TargetFilter(this, true, false, false);

    public Tracers() {
        super("Tracers", "Draws lines from your crosshair to entities.", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || context.matrixStack() == null) {
            return;
        }
        Camera camera = context.camera();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        double maxSquared = range.get() * range.get();

        Vec3d origin = camera.getPos().add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()));
        LineBatch batch = new LineBatch(context.matrixStack(), camera.getPos());
        for (Entity entity : mc.world.getEntities()) {
            if (!filter.test(entity) || player.squaredDistanceTo(entity) > maxSquared) {
                continue;
            }
            Vec3d target = RenderUtil.center(RenderUtil.interpolatedBox(entity, tickDelta));
            batch.line(origin, target, Esp.colorFor(entity));
        }
        batch.draw();
    }
}
