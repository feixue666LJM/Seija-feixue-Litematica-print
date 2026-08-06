package com.kijinseija.seija_printer.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;

/** Maps player inventory indices to the active container's slot ids. */
public final class SlotUtils {
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;
    public static final int OFFHAND = 40;

    private SlotUtils() {
    }

    public static boolean isHotbar(int index) { return index >= HOTBAR_START && index <= HOTBAR_END; }
    public static boolean isMain(int index) { return index >= MAIN_START && index <= MAIN_END; }
    public static boolean isArmor(int index) { return index >= ARMOR_START && index <= ARMOR_END; }

    public static int indexToId(int index) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return -1;
        ScreenHandler menu = minecraft.player.currentScreenHandler;
        if (menu == null) return -1;

        if (menu instanceof PlayerScreenHandler) {
            if (isHotbar(index)) return 36 + index;
            if (isMain(index)) return index;
            if (isArmor(index)) return 8 - (index - ARMOR_START);
            if (index == OFFHAND) return 45;
            return -1;
        }

        // Other vanilla menus append the 36 player slots after their own slots.
        int base = findPlayerInventoryBase(menu);
        if (isHotbar(index)) return base + 27 + index;
        if (isMain(index)) return base + (index - MAIN_START);
        return -1;
    }

    private static int findPlayerInventoryBase(ScreenHandler menu) {
        // Vanilla menus place the 36 player slots at the end. This also works
        // for custom menus that follow the same convention.
        int size = menu.slots.size();
        return Math.max(0, size - 36);
    }
}
