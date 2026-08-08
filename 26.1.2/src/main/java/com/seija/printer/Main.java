package com.seija.printer;

import com.seija.printer.events.Render3DEvent;
import com.seija.printer.gui.PrinterDebugScreen;
import com.seija.printer.print_main.InitClass;
import com.seija.printer.print_main.modules.Printer;
import com.seija.printer.settings.PrinterSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: Kiss my ass
 * @Author: Eremin12
 * @Date: 2026/8/8 15:55
 */

public final class Main implements ClientModInitializer {
    public static final String MOD_ID = "seija-printer";
    public static final String CATEGORY = "printer";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static final String Author = "chunfeng663";
    public static final String About = "Temporarily maintained by WindyTeam";
    private static Main instance;
    private final KeyBindings keyBindings;
    private InitClass runtime;

    public static Main getInstance() {
        return instance;
    }

    public static InitClass getRuntime() {
        return instance == null ? null : instance.runtime;
    }

    public Main() {
        this.keyBindings = new KeyBindings();
    }

    public KeyMapping enablePrinterKey() {
        return keyBindings.getEnablePrinter();
    }

    public KeyMapping disablePrinterKey() {
        return keyBindings.getDisablePrinter();
    }

    public KeyMapping openDebugScreenKey() {
        return keyBindings.getOpenDebugScreen();
    }

    public KeyMapping togglePrinterKey() {
        return keyBindings.getTogglePrinter();
    }

    @Override
    public void onInitializeClient() {
        LOG.info("Initializing " + CATEGORY + " by " + Author + "About of" + About);
        instance = this;
        runtime = new InitClass();
        keyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        LevelRenderEvents.END_MAIN.register(this::onEndLevelRender);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runtime.onDisconnect());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            runtime.onDisconnect();
            PrinterSettings.getINSTANCE().saveModules(runtime.modules());
        });
    }

    private void onEndClientTick(Minecraft client) {
        while (keyBindings.getOpenDebugScreen().consumeClick()) {
            if (client.player == null || client.level == null) continue;
            if (client.screen instanceof PrinterDebugScreen) {
                client.screen.onClose();
            } else {
                client.setScreen(new PrinterDebugScreen(client.screen, runtime));
            }
        }

        while (keyBindings.getEnablePrinter().consumeClick()) {
            setPrinterActive(client, true);
        }
        while (keyBindings.getDisablePrinter().consumeClick()) {
            setPrinterActive(client, false);
        }
        while (keyBindings.getTogglePrinter().consumeClick()) {
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