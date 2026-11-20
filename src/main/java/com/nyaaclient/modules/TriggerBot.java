package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.util.TargetFilter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class TriggerBot extends Module {
    private final TargetFilter filter = new TargetFilter(this, true, true, false);

    public TriggerBot() {
        super("TriggerBot", "Attacks whatever is under the crosshair when the cooldown is ready.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }
        if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !filter.test(hit.getEntity())) {
            return;
        }
        if (player.getAttackCooldownProgress(0.5f) < 0.95f) {
            return;
        }
        mc.interactionManager.attackEntity(player, hit.getEntity());
        player.swingHand(Hand.MAIN_HAND);
    }
}
