package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.NumberSetting;
import com.nyaaclient.util.LineBatch;
import com.nyaaclient.util.RenderUtil;
import com.nyaaclient.util.TargetFilter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

public final class Esp extends Module {
    private static final int COLOR_PLAYER = 0xFFFFFFFF;
    private static final int COLOR_HOSTILE = 0xFFFF5A5A;
    private static final int COLOR_PASSIVE = 0xFF7CE08A;

    private final NumberSetting range = addSetting(new NumberSetting("Range", 96.0, 16.0, 256.0, 8.0));
    private final TargetFilter filter = new TargetFilter(this, true, true, false);

    public Esp() {
        super("ESP", "Draws boxes around entities through walls.", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || context.matrixStack() == null) {
            return;
        }
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        double maxSquared = range.get() * range.get();

        LineBatch batch = new LineBatch(context.matrixStack(), context.camera().getPos());
        for (Entity entity : mc.world.getEntities()) {
            if (!filter.test(entity) || player.squaredDistanceTo(entity) > maxSquared) {
                continue;
            }
            batch.box(RenderUtil.interpolatedBox(entity, tickDelta), colorFor(entity));
        }
        batch.draw();
    }

    static int colorFor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return COLOR_PLAYER;
        }
        return entity instanceof Monster ? COLOR_HOSTILE : COLOR_PASSIVE;
    }
}
