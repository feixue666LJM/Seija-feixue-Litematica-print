package com.seija.printer.gui;

import com.seija.printer.print_main.modules.ClientModule;
import com.seija.printer.settings.core.Setting;
import com.seija.printer.settings.core.SettingGroup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SettingsPanel {
    private static final int ROW_HEIGHT = 28;
    private static final int GROUP_HEIGHT = 22;

    private final PrinterDebugScreen screen;
    private final ScreenLayout layout;
    private final SettingWidgetFactory widgetFactory;

    private final List<SettingRow> rows = new ArrayList<>();
    private int scrollOffset = 0;
    private int contentHeight = 0;

    public SettingsPanel(PrinterDebugScreen screen, ScreenLayout layout) {
        this.screen = screen;
        this.layout = layout;
        this.widgetFactory = new SettingWidgetFactory(screen);
    }

    public void buildSettings(ClientModule module) {
        rows.clear();
        if (module == null) return;

        int y = layout.panelTop() + 6 - scrollOffset;

        for (SettingGroup group : module.settings.values()) {
            if (group.settings.stream().noneMatch(Setting::isVisible)) continue;

            rows.add(SettingRow.group(group.name, y));
            y += GROUP_HEIGHT;

            for (Setting<?> setting : group.settings) {
                if (!setting.isVisible()) continue;

                rows.add(SettingRow.setting(setting, y));

                if (isWithinPanel(y)) {
                    addSettingWidgets(setting, y);
                }
                y += ROW_HEIGHT;
            }
        }

        contentHeight = Math.max(0, y + scrollOffset - layout.panelTop());
    }

    private void addSettingWidgets(Setting<?> setting, int y) {
        widgetFactory.createWidget(setting, layout.controlX(), y, layout.controlWidth());

        Button resetButton = Button.builder(
                Component.translatable("screen.seija_printer.debug.reset"),
                btn -> {
                    setting.reset();
                    screen.setStatus(setting.name + " reset");
                    screen.refreshUI();
                }
        ).bounds(layout.resetButtonX(), y - 1, 54, 20).build();
        screen.addWidget(resetButton);
    }

    public void renderLabels(GuiGraphicsExtractor graphics) {
        for (SettingRow row : rows) {
            if (!isWithinPanel(row.y())) continue;

            if (row.isGroup()) {
                DebugScreenRenderer.renderGroupLabel(graphics, row, layout, screen.getFont());
            } else {
                DebugScreenRenderer.renderSettingLabel(graphics, row, layout, screen.getFont());
            }
        }
    }

    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        if (mouseY >= layout.panelTop() && mouseY <= layout.panelBottom()
                && contentHeight > layout.contentHeight()) {
            int step = verticalAmount > 0 ? -ROW_HEIGHT : ROW_HEIGHT;
            int next = Math.max(0, Math.min(maxScroll(), scrollOffset + step));
            if (next != scrollOffset) {
                scrollOffset = next;
                return true;
            }
        }
        return false;
    }

    public void resetScroll() {
        scrollOffset = 0;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - layout.contentHeight());
    }

    private boolean isWithinPanel(int y) {
        return y + ROW_HEIGHT >= layout.panelTop() && y <= layout.panelBottom() - 2;
    }

    record SettingRow(String groupName, Setting<?> setting, int y, boolean isGroup) {
        static SettingRow group(String name, int y) {
            return new SettingRow(name, null, y, true);
        }

        static SettingRow setting(Setting<?> setting, int y) {
            return new SettingRow("", setting, y, false);
        }
    }
}