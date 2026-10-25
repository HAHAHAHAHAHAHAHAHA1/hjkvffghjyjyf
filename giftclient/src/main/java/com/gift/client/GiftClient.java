package com.gift.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class GiftClient implements ClientModInitializer {
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        ServerGate.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.giftclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.giftclient"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.world == null) return;
            boolean allowed = ServerGate.isAllowed(mc);

            while (menuKey.wasPressed()) {
                if (allowed) {
                    mc.setScreen(new MenuScreen());
                } else {
                    mc.player.sendMessage(Text.literal("GiftClient: server not listed in giftclient-servers.txt"), true);
                }
            }

            for (Module m : Modules.ALL) {
                if (!allowed) {
                    m.setEnabled(false);
                } else if (m.isEnabled()) {
                    m.onTick(mc);
                }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden) return;
            ctx.drawTextWithShadow(mc.textRenderer, "GiftClient", 4, 4, 0xFF55FFFF);
            int y = 4;
            int w = ctx.getScaledWindowWidth();
            for (Module m : Modules.ALL) {
                if (!m.isEnabled()) continue;
                ctx.drawTextWithShadow(mc.textRenderer, m.name, w - mc.textRenderer.getWidth(m.name) - 4, y, 0xFFFFFFFF);
                y += 10;
            }
        });
    }
}
