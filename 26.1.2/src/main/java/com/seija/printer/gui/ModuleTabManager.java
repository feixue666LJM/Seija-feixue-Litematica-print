package com.seija.printer.gui;

import com.seija.printer.print_main.modules.ClientModule;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModuleTabManager {
    private final PrinterDebugScreen screen;
    private final ScreenLayout layout;
    private final List<ClientModule> modules;

    public ModuleTabManager(PrinterDebugScreen screen, ScreenLayout layout, List<ClientModule> modules) {
        this.screen = screen;
        this.layout = layout;
        this.modules = modules;
    }

    public void buildTabs(int selectedIndex) {
        int x = layout.panelLeft();
        int y = 28;
        int available = Math.max(100, layout.availableWidth());
        int tabWidth = Math.clamp(available / Math.max(1, modules.size()), 96, 170);

        for (int i = 0; i < modules.size(); i++) {
            ClientModule module = modules.get(i);
            final int index = i;

            Button tab = Button.builder(
                    Component.literal(module.name),
                    button -> screen.switchModule(index)
            ).bounds(x, y, tabWidth - 4, 20).build();

            if (!module.description.isBlank()) {
                tab.setTooltip(Tooltip.create(Component.literal(module.description)));
            }
            tab.active = selectedIndex != i;

            screen.addWidget(tab);
            x += tabWidth;
        }
    }
}