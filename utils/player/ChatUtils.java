package com.kijinseija.seija_printer.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Small client-side chat adapter used by the printer code. */
public final class ChatUtils {
    private ChatUtils() {
    }

    public static void sendMsg(Component message) {
        sendMsg(null, message);
    }

    public static void sendMsg(String prefix, Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || message == null) return;

        if (prefix == null || prefix.isBlank()) {
            minecraft.player.sendSystemMessage(message);
        } else {
            minecraft.player.sendSystemMessage(Component.literal(prefix + " ").append(message));
        }
    }

    public static void sendMsg(String message) {
        sendMsg(Component.literal(message == null ? "" : message));
    }
}
