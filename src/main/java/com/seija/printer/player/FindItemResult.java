package com.seija.printer.player;

import net.minecraft.world.InteractionHand;
import net.minecraft.client.Minecraft;

/** Immutable result for an inventory search. Slots use player-inventory indices. */
public record FindItemResult(int slot, int count) {
    public boolean found() {
        return slot >= 0;
    }

    public InteractionHand getHand() {
        if (slot == 40) return InteractionHand.OFF_HAND;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && slot == minecraft.player.getInventory().getSelectedSlot()) {
            return InteractionHand.MAIN_HAND;
        }
        return null;
    }

    public boolean isMainHand() { return getHand() == InteractionHand.MAIN_HAND; }
    public boolean isOffhand() { return getHand() == InteractionHand.OFF_HAND; }
    public boolean isHotbar() { return SlotUtils.isHotbar(slot); }
    public boolean isMain() { return SlotUtils.isMain(slot); }
    public boolean isArmor() { return SlotUtils.isArmor(slot); }
}
