package com.kijinseija.seija_printer.utils.player;

import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Inventory helpers implemented against vanilla's container interaction API.
 */
public final class InvUtils {
    public static int previousSlot = -1;

    private InvUtils() {
    }

    public static FindItemResult findEmpty() {
        return find(stack -> stack.isEmpty(), 0, 35);
    }

    public static FindItemResult find(Item item) {
        return find(stack -> stack.isOf(item));
    }

    public static FindItemResult find(Predicate<ItemStack> predicate) {
        return find(predicate, 0, 40);
    }

    public static FindItemResult find(Predicate<ItemStack> predicate, int start, int end) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return new FindItemResult(-1, 0);
        PlayerInventory inventory = minecraft.player.getInventory();
        int first = Math.max(0, start);
        int last = Math.min(40, end);
        for (int slot = first; slot <= last; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (predicate.test(stack)) return new FindItemResult(slot, stack.getCount());
        }
        return new FindItemResult(-1, 0);
    }

    public static FindItemResult findInHotbar(Predicate<ItemStack> predicate) {
        return find(predicate, SlotUtils.HOTBAR_START, SlotUtils.HOTBAR_END);
    }

    public static FindItemResult findInHotbar(Item item) {
        return findInHotbar(stack -> stack.isOf(item));
    }

    public static boolean swap(int slot, boolean swapBack) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null || !SlotUtils.isHotbar(slot)) return false;
        int current = minecraft.player.getInventory().selectedSlot;
        if (current == slot) return true;
        if (swapBack && previousSlot < 0) previousSlot = current;
        minecraft.player.getInventory().selectedSlot = slot;
        if (minecraft.getNetworkHandler() != null) {
            minecraft.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
        return true;
    }

    public static boolean swapBack() {
        if (previousSlot < 0) return false;
        int slot = previousSlot;
        previousSlot = -1;
        return swap(slot, false);
    }

    public static Action move() { return new Action(SlotActionType.PICKUP); }
    public static Action click() { return new Action(SlotActionType.PICKUP); }
    public static Action quickSwap() { return new Action(SlotActionType.SWAP); }
    public static Action shiftClick() { return new Action(SlotActionType.QUICK_MOVE); }

    public static final class Action {
        private final SlotActionType input;
        private int from = -1;
        private int fromButton = 0;

        private Action(SlotActionType input) { this.input = input; }

        public Action from(int slot) {
            this.from = slot;
            return this;
        }

        public Action fromId(int slot) {
            this.fromButton = slot;
            return this;
        }

        public Action to(int slot) {
            if (input == SlotActionType.SWAP) {
                perform(SlotUtils.indexToId(slot), fromButton);
            } else {
                int sourceId = SlotUtils.indexToId(from);
                int targetId = SlotUtils.indexToId(slot);
                perform(sourceId, 0);
                perform(targetId, 0);
            }
            return this;
        }

        public Action slotId(int slotId) {
            perform(slotId, 0);
            return this;
        }

        private void perform(int slotId, int button) {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            PlayerEntity player = minecraft.player;
            ClientPlayerInteractionManager gameMode = minecraft.interactionManager;
            if (player == null || gameMode == null || slotId < 0) return;
            ScreenHandler menu = player.currentScreenHandler;
            gameMode.clickSlot(menu.syncId, slotId, button, input, player);
        }
    }
}
