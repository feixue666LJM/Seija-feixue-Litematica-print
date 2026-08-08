package com.seija.printer.gui;



import com.seija.printer.print_main.modules.ClientModule;
import net.minecraft.client.gui.screens.Screen;

public class ScreenLayout {
    private static final int PANEL_MARGIN = 12;
    private static final int BOTTOM_BAR_HEIGHT = 36;

    private final Screen screen;
    private final int panelLeft;
    private final int panelRight;
    private final int panelWidth;
    private final int panelTop;
    private final int panelBottom;
    private final int labelWidth;
    private final int controlWidth;
    private final int resetButtonX;
    private final int controlX;

    public ScreenLayout(Screen screen, ClientModule currentModule) {
        this.screen = screen;
        this.panelWidth = Math.clamp(screen.width - PANEL_MARGIN * 2, 260, 640);
        this.panelLeft = (screen.width - panelWidth) / 2;
        this.panelRight = (screen.width + panelWidth) / 2;
        this.panelTop = 82;
        this.panelBottom = Math.max(panelTop + 40, screen.height - BOTTOM_BAR_HEIGHT);

        this.labelWidth = Math.max(90, Math.min(260, (panelRight - panelLeft) * 40 / 100));
        this.controlX = panelLeft + labelWidth;
        this.resetButtonX = panelRight - 56;
        this.controlWidth = Math.max(72, resetButtonX - controlX - 8);
    }

    public int panelLeft() { return panelLeft; }
    public int panelRight() { return panelRight; }
    public int panelWidth() { return panelWidth; }
    public int panelTop() { return panelTop; }
    public int panelBottom() { return panelBottom; }
    public int labelWidth() { return labelWidth; }
    public int controlX() { return controlX; }
    public int controlWidth() { return controlWidth; }
    public int resetButtonX() { return resetButtonX; }
    public int contentHeight() { return panelBottom - panelTop; }
    public int availableWidth() { return panelRight - panelLeft; }
}