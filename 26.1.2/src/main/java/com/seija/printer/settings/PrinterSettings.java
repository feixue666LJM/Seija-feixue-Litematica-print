package com.seija.printer.settings;

import com.seija.printer.print_main.modules.ClientModule;
import com.seija.printer.print_main.modules.Printer;
import com.seija.printer.settings.core.Settings;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public final class PrinterSettings {
    private static final PrinterSettings INSTANCE = new PrinterSettings();
    private static final String FILE_NAME = "seija-printer.json";
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern LEADING_TRAILING_HYPHEN = Pattern.compile("(^-|-$)");

    private PrinterSettings() {}

    public static PrinterSettings getINSTANCE() {
        return INSTANCE;
    }

    public void load(Settings settings) {
        loadSettings(settings, FILE_NAME);
    }

    public void loadModule(ClientModule module) {
        Optional.ofNullable(module)
                .map(PrinterSettings::fileName)
                .ifPresent(fileName -> loadSettings(module.settings, fileName));
    }

    public void save(Settings settings) {
        saveSettings(settings, FILE_NAME);
    }

    public void saveModule(ClientModule module) {
        Optional.ofNullable(module)
                .ifPresent(m -> saveSettings(m.settings, fileName(m)));
    }

    public void saveModules(Iterable<? extends ClientModule> modules) {
        Optional.ofNullable(modules)
                .stream()
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .forEach(this::saveModule);
    }

    private void loadSettings(Settings settings, String fileName) {
        Optional.of(CONFIG_PATH.resolve(fileName))
                .filter(Files::isRegularFile)
                .ifPresent(path -> {
                    try {
                        settings.fromJsonString(Files.readString(path, StandardCharsets.UTF_8));
                        settings.onModuleActivated();
                    } catch (IOException ignored) {}
                });
    }

    private void saveSettings(Settings settings, String fileName) {
        try {
            Path path = CONFIG_PATH.resolve(fileName);
            Files.createDirectories(path.getParent());
            Files.writeString(path, settings.toJsonString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    private static String fileName(ClientModule module) {
        if (module instanceof Printer) return FILE_NAME;
        String slug = NON_ALPHANUMERIC.matcher(
                LEADING_TRAILING_HYPHEN.matcher(
                        module.name.toLowerCase(Locale.ROOT)
                ).replaceAll("")
        ).replaceAll("-");
        return slug.isBlank() ? "seija-printer-module.json" : "seija-printer-" + slug + ".json";
    }
}