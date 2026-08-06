package com.seija.printer;

import com.mojang.blaze3d.platform.InputConstants;
import com.seija.printer.events.Render3DEvent;
import com.seija.printer.gui.PrinterDebugScreen;
import com.seija.printer.print_main.InitClass;
import com.seija.printer.print_main.modules.Printer;
import com.seija.printer.settings.PrinterSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fabric client entrypoint for the standalone printer. */
public final class Main implements ClientModInitializer {
    public static final String MOD_ID = "seija-printer";
    public static final String CATEGORY = "printer";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private static Main instance;
    private InitClass runtime;
    private KeyMapping enablePrinter;
    private KeyMapping disablePrinter;
    private KeyMapping openDebugScreen;
    /** Legacy invert binding kept for existing profiles; use enable/disable for deterministic control. */
    private KeyMapping togglePrinter;

    /** Returns the initialized Fabric entrypoint for screens and integrations. */
    public static Main getInstance() {
        return instance;
    }

    /** Returns the standalone module registry, or {@code null} before startup. */
    public static InitClass getRuntime() {
        return instance == null ? null : instance.runtime;
    }

    public KeyMapping enablePrinterKey() {
        return enablePrinter;
    }

    public KeyMapping disablePrinterKey() {
        return disablePrinter;
    }

    public KeyMapping openDebugScreenKey() {
        return openDebugScreen;
    }

    public KeyMapping togglePrinterKey() {
        return togglePrinter;
    }

    @Override
    public void onInitializeClient() {
        LOG.info("Initializing standalone Seija litematica printer");

        instance = this;
        runtime = new InitClass();
        registerKeyMappings();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        LevelRenderEvents.END_MAIN.register(this::onEndLevelRender);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runtime.onDisconnect());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            runtime.onDisconnect();
            PrinterSettings.getINSTANCE().saveModules(runtime.modules());
        });
    }

    private void registerKeyMappings() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "controls")
        );
        // F6/F7 are deliberately separate so an accidental press cannot invert state.
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
        // Preserve the original toggle key for existing keybinding profiles.
        togglePrinter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.seija_printer.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            category
        ));
    }

    private void onEndClientTick(Minecraft client) {
        while (openDebugScreen.consumeClick()) {
            // Do not create screens at title/menu: there is no module context there.
            if (client.player == null || client.level == null) continue;
            if (client.screen instanceof PrinterDebugScreen) {
                client.screen.onClose();
            } else {
                client.setScreen(new PrinterDebugScreen(client.screen, runtime));
            }
        }

        while (enablePrinter.consumeClick()) {
            setPrinterActive(client, true);
        }
        while (disablePrinter.consumeClick()) {
            setPrinterActive(client, false);
        }
        while (togglePrinter.consumeClick()) {
            setPrinterActive(client, !Printer.getINSTANCE().isActive());
        }

        if (runtime != null) runtime.onClientTick();
    }

    private void setPrinterActive(Minecraft client, boolean active) {
        if (active && (client.player == null || client.level == null)) return;
        Printer printer = Printer.getINSTANCE();
        printer.setActive(active);
        if (client.player != null) {
            client.player.sendSystemMessage(Component.translatable(
                active ? "message.seija_printer.enabled" : "message.seija_printer.disabled"
            ));
        }
    }

    private void onEndLevelRender(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        Render3DEvent event = new Render3DEvent(context);
        runtime.onRender3d(event);
        event.flush();
    }
}
