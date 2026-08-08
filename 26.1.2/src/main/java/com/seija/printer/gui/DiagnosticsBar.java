package com.seija.printer.gui;

import com.seija.printer.print_main.modules.ClientModule;
import com.seija.printer.print_main.modules.ItemSearcher;
import com.seija.printer.print_main.printer.task_manager.RotationManager;
import com.seija.printer.print_main.printer.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class DiagnosticsBar {
    private final PrinterDebugScreen screen;
    private final Font font;
    private final ScreenLayout layout;

    public DiagnosticsBar(PrinterDebugScreen screen, ScreenLayout layout) {
        this.screen = screen;
        this.font = screen.getFont();
        this.layout = layout;
    }

    public void render(GuiGraphicsExtractor graphics, ClientModule module, String status) {
        int bottom = layout.panelBottom();
        int left = layout.panelLeft();

        if (!status.isBlank()) {
            graphics.centeredText(font, Component.literal(status),
                    left + 8, bottom + 9, 0xFFE0C46C);
        }

        String diagnostics = buildDiagnostics(module);
        graphics.centeredText(font, Component.literal(diagnostics),
                left + 8, bottom + 21, 0xFF9FAAB5);
    }

    private String buildDiagnostics(ClientModule module) {
        Minecraft mc = screen.minecraft();
        int maxWidth = layout.availableWidth() - 16;

        StringBuilder sb = new StringBuilder();

        String moduleState = module == null ? "module: none"
                : "module: " + (module.isActive() ? "active" : "inactive");
        sb.append(moduleState);

        sb.append(" | ");
        sb.append(mc.level == null ? "world: none" : "world: loaded");

        sb.append(" | ");
        sb.append(mc.player == null ? "player: none"
                : "player: " + mc.player.getGameProfile().name());

        sb.append(" | ");
        sb.append("rotation: ").append(RotationManager.INSTANCE.taskSize());

        sb.append(" | ");
        sb.append("render: ").append(RenderUtil.renderList.size());

        if (module instanceof ItemSearcher searcher) {
            sb.append(" | ");
            sb.append("materials: ").append(searcher.materialCounts().size());
        }

        return trimText(sb.toString(), maxWidth);
    }

    private String trimText(String text, int pixelWidth) {
        int maxChars = Math.max(20, pixelWidth / 6);
        if (text.length() <= maxChars) return text;
        return text.substring(0, Math.max(1, maxChars - 3)) + "...";
    }
}