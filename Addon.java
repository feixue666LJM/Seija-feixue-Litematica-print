/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer;

import com.kijinseija.seija_printer.events.render.Render3DEvent;
import com.kijinseija.seija_printer.gui.PrinterDebugScreen;
import com.kijinseija.seija_printer.print_main.InitClass;
import com.kijinseija.seija_printer.print_main.modules.Printer;
import com.kijinseija.seija_printer.settings.PrinterSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fabric client entrypoint for the standalone printer. */
public final class Addon implements ClientModInitializer {
    public static final String MOD_ID = "seija-printer";
    public static final String CATEGORY = "printer";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private static Addon instance;
    private InitClass runtime;
    private KeyBinding enablePrinter;
    private KeyBinding disablePrinter;
    private KeyBinding openDebugScreen;
    /** Legacy invert binding kept for existing profiles; use enable/disable for deterministic control. */
    private KeyBinding togglePrinter;

    /** Returns the initialized Fabric entrypoint for screens and integrations. */
    public static Addon getInstance() {
        return instance;
    }

    /** Returns the standalone module registry, or {@code null} before startup. */
    public static InitClass getRuntime() {
        return instance == null ? null : instance.runtime;
    }

    public KeyBinding enablePrinterKey() {
        return enablePrinter;
    }

    public KeyBinding disablePrinterKey() {
        return disablePrinter;
    }

    public KeyBinding openDebugScreenKey() {
        return openDebugScreen;
    }

    public KeyBinding togglePrinterKey() {
        return togglePrinter;
    }

    @Override
    public void onInitializeClient() {
        LOG.info("Initializing standalone Seija litematica printer");

        instance = this;
        runtime = new InitClass();
        registerKeyMappings();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        WorldRenderEvents.LAST.register(this::onEndLevelRender);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runtime.onDisconnect());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            runtime.onDisconnect();
            PrinterSettings.getINSTANCE().saveModules(runtime.modules());
        });
    }

    private void registerKeyMappings() {
        String category = "key.category." + MOD_ID + ".controls";
        // F6/F7 are deliberately separate so an accidental press cannot invert state.
        enablePrinter = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.seija_printer.enable",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            category
        ));
        disablePrinter = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.seija_printer.disable",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            category
        ));
        openDebugScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.seija_printer.open_debug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            category
        ));
        // Preserve the original toggle key for existing keybinding profiles.
        togglePrinter = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.seija_printer.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            category
        ));
    }

    private void onEndClientTick(MinecraftClient client) {
        while (openDebugScreen.wasPressed()) {
            // Do not create screens at title/menu: there is no module context there.
            if (client.player == null || client.world == null) continue;
            if (client.currentScreen instanceof PrinterDebugScreen) {
                client.currentScreen.close();
            } else {
                client.setScreen(new PrinterDebugScreen(client.currentScreen, runtime));
            }
        }

        while (enablePrinter.wasPressed()) {
            setPrinterActive(client, true);
        }
        while (disablePrinter.wasPressed()) {
            setPrinterActive(client, false);
        }
        while (togglePrinter.wasPressed()) {
            setPrinterActive(client, !Printer.getINSTANCE().isActive());
        }

        if (runtime != null) runtime.onClientTick();
    }

    private void setPrinterActive(MinecraftClient client, boolean active) {
        if (active && (client.player == null || client.world == null)) return;
        Printer printer = Printer.getINSTANCE();
        printer.setActive(active);
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(
                active ? "message.seija_printer.enabled" : "message.seija_printer.disabled"
            ), false);
        }
    }

    private void onEndLevelRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Render3DEvent event = new Render3DEvent(context);
        runtime.onRender3d(event);
        event.flush();
    }
}
