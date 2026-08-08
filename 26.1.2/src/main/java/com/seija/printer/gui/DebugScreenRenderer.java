package com.seija.printer.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class DebugScreenRenderer {

    private DebugScreenRenderer() {}

    public static void renderPanel(GuiGraphicsExtractor graphics, ScreenLayout layout) {
        graphics.fill(
                layout.panelLeft(), layout.panelTop(),
                layout.panelRight(), layout.panelBottom(),
                0x990B0F14
        );
        graphics.outline(
                layout.panelLeft(), layout.panelTop(),
                layout.panelRight() - layout.panelLeft(),
                layout.panelBottom() - layout.panelTop(),
                0xFF59616B
        );
    }

    public static void renderTitle(GuiGraphicsExtractor graphics, PrinterDebugScreen screen) {
        graphics.centeredText(
                screen.font(),
                screen.getTitle(),
                screen.width / 2,
                8,
                0xFFFFFFFF
        );
    }

    public static void renderGroupLabel(GuiGraphicsExtractor graphics,
                                        SettingsPanel.SettingRow row,
                                        ScreenLayout layout,
                                        Font font) {
        int left = layout.panelLeft();
        graphics.text(
                font,
                Component.literal(row.groupName()),
                left + 8,
                row.y() + 5,
                0xFF76B9FF
        );
        graphics.horizontalLine(
                left + 8,
                layout.panelRight() - 8,
                row.y() + 18,
                0x664A6176
        );
    }

    public static void renderSettingLabel(GuiGraphicsExtractor graphics,
                                          SettingsPanel.SettingRow row,
                                          ScreenLayout layout,
                                          Font font) {
        int left = layout.panelLeft();
        int labelWidth = layout.labelWidth();
        String label = font.plainSubstrByWidth(
                row.setting().name,
                Math.max(20, labelWidth - 12),
                true
        );
        graphics.text(
                font,
                Component.literal(label),
                left + 8,
                row.y() + 5,
                0xFFE8E8E8
        );
    }
}