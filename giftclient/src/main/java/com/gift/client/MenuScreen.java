package com.gift.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MenuScreen extends Screen {
    public MenuScreen() {
        super(Text.literal("GiftClient"));
    }

    @Override
    protected void init() {
        int w = 130, h = 20, gap = 4;
        int x = width / 2 - w / 2;
        int y = height / 2 - (Modules.ALL.size() * (h + gap)) / 2;
        for (Module m : Modules.ALL) {
            ButtonWidget button = ButtonWidget.builder(label(m), btn -> {
                m.toggle();
                btn.setMessage(label(m));
            }).dimensions(x, y, w, h).build();
            addDrawableChild(button);
            y += h + gap;
        }
    }

    private static Text label(Module m) {
        return Text.literal(m.name + ": " + (m.isEnabled() ? "ON" : "OFF"));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
