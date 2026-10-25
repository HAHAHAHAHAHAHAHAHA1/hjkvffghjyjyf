package com.gift.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Modules only run in singleplayer or on servers listed in config/giftclient-servers.txt.
 */
public final class ServerGate {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("giftclient-servers.txt");
    private static List<String> allowed = List.of();

    private ServerGate() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                Files.writeString(FILE, "# One server address per line. Singleplayer is always allowed.\nlocalhost\n");
            }
            allowed = Files.readAllLines(FILE).stream()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(ServerGate::normalize)
                    .toList();
        } catch (IOException e) {
            allowed = List.of();
        }
    }

    public static boolean isAllowed(MinecraftClient mc) {
        if (mc.isInSingleplayer()) return true;
        ServerInfo info = mc.getCurrentServerEntry();
        return info != null && allowed.contains(normalize(info.address));
    }

    private static String normalize(String address) {
        String a = address.toLowerCase();
        return a.endsWith(":25565") ? a.substring(0, a.length() - 6) : a;
    }
}
