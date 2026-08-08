package com.kijinseija.seija_printer.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/** Small client-side chat adapter used by the printer code. */
public final class ChatUtils {
    private ChatUtils() {
    }

    public static void sendMsg(Text message) {
        sendMsg(null, message);
    }

    public static void sendMsg(String prefix, Text message) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null || message == null) return;

        if (prefix == null || prefix.isBlank()) {
            minecraft.player.sendMessage(message, false);
        } else {
            minecraft.player.sendMessage(Text.literal(prefix + " ").append(message), false);
        }
    }

    public static void sendMsg(String message) {
        sendMsg(Text.literal(message == null ? "" : message));
    }
}
