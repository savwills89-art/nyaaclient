package com.nyaaclient.util;

import com.nyaaclient.module.Module;
import com.nyaaclient.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

/** Shared Players / Hostile / Passive toggles so every module filters entities the same way. */
public final class TargetFilter {
    public final BoolSetting players;
    public final BoolSetting hostile;
    public final BoolSetting passive;

    public TargetFilter(Module owner, boolean players, boolean hostile, boolean passive) {
        this.players = owner.addSetting(new BoolSetting("Players", players));
        this.hostile = owner.addSetting(new BoolSetting("Hostile", hostile));
        this.passive = owner.addSetting(new BoolSetting("Passive", passive));
    }

    public boolean test(Entity entity) {
        if (!(entity instanceof LivingEntity living)
                || entity == MinecraftClient.getInstance().player
                || !living.isAlive()
                || living.isSpectator()) {
            return false;
        }
        if (living instanceof PlayerEntity) {
            return players.get();
        }
        if (living instanceof Monster) {
            return hostile.get();
        }
        return living instanceof MobEntity && passive.get();
    }
}
