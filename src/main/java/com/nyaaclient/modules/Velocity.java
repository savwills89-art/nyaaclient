package com.nyaaclient.modules;

import com.nyaaclient.NyaaClient;
import com.nyaaclient.module.Category;
import com.nyaaclient.module.Module;

/** Logic lives in ClientPlayNetworkHandlerMixin, which cancels velocity packets aimed at the local player. */
public final class Velocity extends Module {
    public Velocity() {
        super("Velocity", "Ignores knockback and other server velocity updates for your player.", Category.COMBAT);
    }

    public static boolean shouldCancel() {
        NyaaClient client = NyaaClient.instance();
        return client != null && client.modules().get(Velocity.class).isEnabled();
    }
}
