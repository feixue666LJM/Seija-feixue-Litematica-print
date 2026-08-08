package com.seija.printer.gui;

import com.seija.printer.print_main.modules.ClientModule;
import com.seija.printer.print_main.modules.ItemSearcher;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ActionBar {
    private final PrinterDebugScreen screen;
    private final ScreenLayout layout;

    public ActionBar(PrinterDebugScreen screen, ScreenLayout layout) {
        this.screen = screen;
        this.layout = layout;
    }

    public void buildButtons(ClientModule module) {
        int left = layout.panelLeft();
        int right = layout.panelRight();
        int actionY = 54;

        int actionWidth = Math.min(112, Math.max(84, (right - left - 18) / 5));

        buildToggleButton(module, left, actionY, actionWidth);

        if (module instanceof ItemSearcher searcher) {
            buildSearchButtons(searcher, left, right);
        }

        buildSaveResetButtons(right, actionY);
    }

    private void buildToggleButton(ClientModule module, int x, int y, int width) {
        Component label;
        if (module == null) {
            label = Component.translatable("screen.seija_printer.debug.no_module");
        } else {
            label = Component.translatable(module.isActive()
                    ? "screen.seija_printer.debug.disable"
                    : "screen.seija_printer.debug.enable");
        }

        Button button = Button.builder(label, btn -> {
            if (module != null) {
                module.toggle();
                screen.setStatus(module.name + (module.isActive() ? " enabled" : " disabled"));
                screen.refreshUI();
            }
        }).bounds(x, y, width, 20).build();

        screen.addWidget(button);
    }

    private void buildSearchButtons(ItemSearcher searcher, int left, int right) {
        int y = 78;
        int width = (right - left - 6) / 2;

        Button startButton = Button.builder(
                Component.translatable("screen.seija_printer.debug.start_analysis"),
                btn -> {
                    if (!searcher.isActive()) searcher.activate();
                    searcher.startAnalysis();
                    screen.setStatus("Analysis started");
                }
        ).bounds(left, y, width, 20).build();

        Button printButton = Button.builder(
                Component.translatable("screen.seija_printer.debug.print_list"),
                btn -> {
                    searcher.printItemList();
                    screen.setStatus("Material list sent to chat");
                }
        ).bounds(left + width + 6, y, width, 20).build();

        screen.addWidget(startButton);
        screen.addWidget(printButton);
    }

    private void buildSaveResetButtons(int right, int y) {
        int saveX = right - 2 * 76 - 6;

        Button saveButton = Button.builder(
                Component.translatable("screen.seija_printer.debug.save"),
                btn -> screen.saveSettings()
        ).bounds(saveX, y, 76, 20).build();

        Button resetButton = Button.builder(
                Component.translatable("screen.seija_printer.debug.reset_all"),
                btn -> screen.resetSettings()
        ).bounds(right - 76, y, 76, 20).build();

        screen.addWidget(saveButton);
        screen.addWidget(resetButton);
    }
}