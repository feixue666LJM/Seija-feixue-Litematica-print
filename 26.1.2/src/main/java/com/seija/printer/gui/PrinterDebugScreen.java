package com.seija.printer.gui;

import com.seija.printer.print_main.InitClass;
import com.seija.printer.print_main.modules.ClientModule;
import com.seija.printer.print_main.modules.ItemSearcher;
import com.seija.printer.print_main.modules.Printer;
import com.seija.printer.settings.PrinterSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class PrinterDebugScreen extends Screen {
    private final Screen parent;
    private final InitClass runtime;

    private ModuleTabManager tabManager;
    private SettingsPanel settingsPanel;
    private DiagnosticsBar diagnosticsBar;
    private ActionBar actionBar;
    private ScreenLayout layout;

    private int selectedModule;
    private String status = "";

    public PrinterDebugScreen(Screen parent, InitClass runtime) {
        super(Component.translatable("screen.seija_printer.debug.title"));
        this.parent = parent;
        this.runtime = runtime;
        this.selectedModule = 0;
    }

    public static void open(Minecraft client, InitClass runtime) {
        if (client != null) client.setScreen(new PrinterDebugScreen(client.screen, runtime));
    }

    @Override
    protected void init() {
        clearWidgets();

        List<ClientModule> modules = modules();
        if (selectedModule >= modules.size()) selectedModule = Math.max(0, modules.size() - 1);

        ClientModule currentModule = currentModule();
        this.layout = new ScreenLayout(this, currentModule);

        this.tabManager = new ModuleTabManager(this, layout, modules);
        this.actionBar = new ActionBar(this, layout);
        this.settingsPanel = new SettingsPanel(this, layout);
        this.diagnosticsBar = new DiagnosticsBar(this, layout);

        tabManager.buildTabs(selectedModule);
        actionBar.buildButtons(currentModule);
        settingsPanel.buildSettings(currentModule);
    }

    public <T extends AbstractWidget> T addWidget(T widget) {
        return addRenderableWidget(widget);
    }

    public void refreshUI() {
        rebuildWidgets();
    }

    public void switchModule(int index) {
        selectedModule = index;
        settingsPanel.resetScroll();
        status = "";
        refreshUI();
    }

    public void saveSettings() {
        if (runtime != null) PrinterSettings.getINSTANCE().saveModules(runtime.modules());
        else PrinterSettings.getINSTANCE().save(Printer.getINSTANCE().settings);
        status = "Settings saved";
    }

    public void resetSettings() {
        ClientModule module = currentModule();
        if (module != null) {
            module.settings.reset();
            status = "Settings reset";
            refreshUI();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ClientModule module = currentModule();

        DebugScreenRenderer.renderPanel(graphics, layout);
        DebugScreenRenderer.renderTitle(graphics, this);
        diagnosticsBar.render(graphics, module, status);
        settingsPanel.renderLabels(graphics);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingsPanel != null && settingsPanel.handleScroll(mouseX, mouseY, verticalAmount)) {
            refreshUI();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        saveSettings();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Minecraft minecraft() {
        return this.minecraft;
    }

    public Font font() {
        return this.font;
    }

    List<ClientModule> modules() {
        if (runtime != null) return runtime.modules();
        return List.of(Printer.getINSTANCE());
    }

    ClientModule currentModule() {
        List<ClientModule> modules = modules();
        return modules.isEmpty() ? null : modules.get(Math.clamp(selectedModule, 0, modules.size() - 1));
    }

    ScreenLayout getLayout() {
        return layout;
    }
}