package com.kijinseija.seija_printer.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

/** Immutable result for an inventory search. Slots use player-inventory indices. */
public record FindItemResult(int slot, int count) {
    public boolean found() {
        return slot >= 0;
    }

    public Hand getHand() {
        if (slot == 40) return Hand.OFF_HAND;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player != null && slot == minecraft.player.getInventory().selectedSlot) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public boolean isMainHand() { return getHand() == Hand.MAIN_HAND; }
    public boolean isOffhand() { return getHand() == Hand.OFF_HAND; }
    public boolean isHotbar() { return SlotUtils.isHotbar(slot); }
    public boolean isMain() { return SlotUtils.isMain(slot); }
    public boolean isArmor() { return SlotUtils.isArmor(slot); }
}
