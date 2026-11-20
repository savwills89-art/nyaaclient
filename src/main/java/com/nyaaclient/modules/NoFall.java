package com.nyaaclient.modules;

import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;
import com.nyaaclient.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class NoFall extends Module {
    private final NumberSetting minFall = addSetting(new NumberSetting("Min fall distance", 2.0, 1.0, 3.0, 0.1));

    public NoFall() {
        super("NoFall", "Tells the server you are on the ground while falling.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.networkHandler == null) {
            return;
        }
        if (player.fallDistance > minFall.getFloat()
                && !player.isFallFlying()
                && !player.getAbilities().allowFlying) {
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
