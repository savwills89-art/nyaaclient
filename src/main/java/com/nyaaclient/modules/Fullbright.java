package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class Fullbright extends Module {
    private static final int DURATION_TICKS = 16000;
    private static final int REFRESH_BELOW_TICKS = 400;

    private boolean addedByUs;

    public Fullbright() {
        super("Fullbright", "Client-side night vision so dark areas are fully lit.", Category.RENDER);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (current == null) {
            addNightVision(player);
            addedByUs = true;
        } else if (addedByUs && current.getDuration() < REFRESH_BELOW_TICKS) {
            addNightVision(player);
        }
    }

    @Override
    protected void onDisable() {
        ClientPlayerEntity player = mc.player;
        if (player != null && addedByUs) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        addedByUs = false;
    }

    private static void addNightVision(ClientPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, DURATION_TICKS, 0, false, false, false));
    }
}
