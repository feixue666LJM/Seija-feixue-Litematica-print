package com.seija.printer.player;

import com.seija.printer.player.FindItemResult;
import com.seija.printer.player.SlotUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

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
        return find(stack -> stack.is(item));
    }

    public static FindItemResult find(Predicate<ItemStack> predicate) {
        return find(predicate, 0, 40);
    }

    public static FindItemResult find(Predicate<ItemStack> predicate, int start, int end) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return new FindItemResult(-1, 0);
        Inventory inventory = minecraft.player.getInventory();
        int first = Math.max(0, start);
        int last = Math.min(40, end);
        for (int slot = first; slot <= last; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (predicate.test(stack)) return new FindItemResult(slot, stack.getCount());
        }
        return new FindItemResult(-1, 0);
    }

    public static FindItemResult findInHotbar(Predicate<ItemStack> predicate) {
        return find(predicate, SlotUtils.HOTBAR_START, SlotUtils.HOTBAR_END);
    }

    public static FindItemResult findInHotbar(Item item) {
        return findInHotbar(stack -> stack.is(item));
    }

    public static boolean swap(int slot, boolean swapBack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !SlotUtils.isHotbar(slot)) return false;
        int current = minecraft.player.getInventory().getSelectedSlot();
        if (current == slot) return true;
        if (swapBack && previousSlot < 0) previousSlot = current;
        minecraft.player.getInventory().setSelectedSlot(slot);
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
        return true;
    }

    public static boolean swapBack() {
        if (previousSlot < 0) return false;
        int slot = previousSlot;
        previousSlot = -1;
        return swap(slot, false);
    }

    public static Action move() { return new Action(ContainerInput.PICKUP); }
    public static Action click() { return new Action(ContainerInput.PICKUP); }
    public static Action quickSwap() { return new Action(ContainerInput.SWAP); }
    public static Action shiftClick() { return new Action(ContainerInput.QUICK_MOVE); }

    public static final class Action {
        private final ContainerInput input;
        private int from = -1;
        private int fromButton = 0;

        private Action(ContainerInput input) { this.input = input; }

        public Action from(int slot) {
            this.from = slot;
            return this;
        }

        public Action fromId(int slot) {
            this.fromButton = slot;
            return this;
        }

        public Action to(int slot) {
            if (input == ContainerInput.SWAP) {
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
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            MultiPlayerGameMode gameMode = minecraft.gameMode;
            if (player == null || gameMode == null || slotId < 0) return;
            AbstractContainerMenu menu = player.containerMenu;
            gameMode.handleContainerInput(menu.containerId, slotId, button, input, player);
        }
    }
}
