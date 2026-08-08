package com.seija.printer.print_main.modules;

import com.seija.printer.events.Render3DEvent;
import com.seija.printer.settings.core.Settings;
import net.minecraft.client.Minecraft;

public abstract class ClientModule {
    protected static final Minecraft mc = Minecraft.getInstance();

    public final String name;
    public final String description;
    public final Settings settings = new Settings();

    private boolean active;

    protected ClientModule(String name, String description) {
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
    }

    protected ClientModule(String ignoredCategory, String name, String description) {
        this(name, description);
    }

    public final boolean isActive() {
        return active;
    }

    public final void toggle() {
        setActive(!active);
    }

    public final void activate() {
        setActive(true);
    }

    public final void deactivate() {
        setActive(false);
    }

    public final void setActive(boolean active) {
        if (this.active == active) return;

        this.active = active;
        if (active) {
            onActivate();
            settings.onModuleActivated();
        } else {
            onDeactivate();
        }
    }

    public void onActivate() {}

    public void onDeactivate() {}

    public void onClientTick() {}

    public void onRender3d(Render3DEvent event) {}

    public void onDisconnect() {
        deactivate();
    }

    public void sendToggledMsg() {}
}