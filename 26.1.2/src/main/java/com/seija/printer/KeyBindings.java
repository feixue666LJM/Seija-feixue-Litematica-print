package com.seija.printer;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * @Description: Kiss my ass
 * @Author: Eremin12
 * @Date: 2026/8/8 15:55
 */

public class KeyBindings {
    private KeyMapping enablePrinter;
    private KeyMapping disablePrinter;
    private KeyMapping openDebugScreen;
    private KeyMapping togglePrinter;

    // register all clean shit codes
    public void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(Main.MOD_ID, "controls")
        );

        enablePrinter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.seija_printer.enable",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                category
        ));

        disablePrinter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.seija_printer.disable",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                category
        ));

        openDebugScreen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.seija_printer.open_debug",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                category
        ));

        togglePrinter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.seija_printer.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category
        ));
    }

    public KeyMapping getEnablePrinter() {
        return enablePrinter;
    }

    public KeyMapping getDisablePrinter() {
        return disablePrinter;
    }

    public KeyMapping getOpenDebugScreen() {
        return openDebugScreen;
    }

    public KeyMapping getTogglePrinter() {
        return togglePrinter;
    }
}