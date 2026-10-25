package com.gift.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.List;

public final class Modules {
    private Modules() {}

    public static final Module FLY = new Module("Fly") {
        @Override protected void onEnable() {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p == null) return;
            p.getAbilities().allowFlying = true;
            p.getAbilities().flying = true;
        }

        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            p.getAbilities().allowFlying = true;
            p.getAbilities().setFlySpeed(0.08f);
        }

        @Override protected void onDisable() {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p == null || p.isCreative() || p.isSpectator()) return;
            p.getAbilities().allowFlying = false;
            p.getAbilities().flying = false;
            p.getAbilities().setFlySpeed(0.05f);
        }
    };

    public static final Module SPEED = new Module("Speed") {
        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            float f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
            float s = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
            if (f == 0 && s == 0) return;
            double yaw = Math.toRadians(p.getYaw());
            double sin = Math.sin(yaw), cos = Math.cos(yaw);
            double len = Math.sqrt(f * f + s * s);
            double speed = 0.35;
            double x = (-sin * f + cos * s) / len * speed;
            double z = (cos * f + sin * s) / len * speed;
            p.setVelocity(x, p.getVelocity().y, z);
        }
    };

    public static final Module SPRINT = new Module("Sprint") {
        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                    && p.getHungerManager().getFoodLevel() > 6) {
                p.setSprinting(true);
            }
        }
    };

    public static final Module NO_FALL = new Module("NoFall") {
        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null || p.networkHandler == null) return;
            if (p.fallDistance > 2.0f) {
                p.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
            }
        }
    };

    public static final Module FULLBRIGHT = new Module("Fullbright") {
        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            if (!p.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
            }
        }

        @Override protected void onDisable() {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p != null) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    };

    public static final Module STEP = new Module("Step") {
        @Override protected void onEnable() {
            setStep(1.0);
        }

        @Override protected void onDisable() {
            setStep(0.6);
        }

        private void setStep(double value) {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p == null) return;
            EntityAttributeInstance inst = p.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
            if (inst != null) inst.setBaseValue(value);
        }
    };

    // Hostile mobs only, within 4 blocks.
    public static final Module KILL_AURA = new Module("KillAura") {
        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null || mc.world == null || mc.interactionManager == null) return;
            if (p.getAttackCooldownProgress(0.5f) < 1.0f) return;
            Entity best = null;
            double bestDist = 16.0;
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof HostileEntity) || !e.isAlive()) continue;
                double d = p.squaredDistanceTo(e);
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
            if (best != null) {
                mc.interactionManager.attackEntity(p, best);
                p.swingHand(Hand.MAIN_HAND);
            }
        }
    };

    public static final List<Module> ALL = List.of(FLY, SPEED, SPRINT, NO_FALL, FULLBRIGHT, STEP, KILL_AURA);
}
