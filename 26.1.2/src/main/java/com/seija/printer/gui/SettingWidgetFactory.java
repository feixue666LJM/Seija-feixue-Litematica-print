package com.seija.printer.gui;

import com.seija.printer.settings.core.*;
import com.seija.printer.settings.impl.DirectionListSetting;
import com.seija.printer.settings.impl.DoubleRangeSetting;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SettingWidgetFactory {
    private final PrinterDebugScreen screen;
    private final net.minecraft.client.gui.Font font;

    public SettingWidgetFactory(PrinterDebugScreen screen) {
        this.screen = screen;
        this.font = screen.getFont();
    }

    public void createWidget(Setting<?> setting, int x, int y, int width) {
        WidgetCreationContext context = new WidgetCreationContext(setting, x, y, width);

        if (setting instanceof BoolSetting) {
            createBoolWidget(context);
        } else if (setting instanceof EnumSetting<?>) {
            createEnumWidget(context);
        } else if (isTextInputSetting(setting)) {
            createEditBox(context);
        } else {
            createReadOnlyWidget(context);
        }
    }

    private void createBoolWidget(WidgetCreationContext ctx) {
        BoolSetting boolSetting = (BoolSetting) ctx.setting;
        Checkbox.Builder builder = Checkbox.builder(Component.empty(), font)
                .pos(ctx.x, ctx.y)
                .selected(Boolean.TRUE.equals(boolSetting.get()))
                .onValueChange((checkbox, value) -> {
                    boolSetting.set(value);
                    scheduleRebuild();
                });

        if (ctx.hasDescription()) {
            builder.tooltip(ctx.createTooltip());
        }

        screen.addWidget(builder.build());
    }

    private void createEnumWidget(WidgetCreationContext ctx) {
        EnumSetting<?> enumSetting = (EnumSetting<?>) ctx.setting;
        Enum<?> current = (Enum<?>) enumSetting.get();

        if (current == null) return;

        List<Enum<?>> values = Arrays.asList(current.getDeclaringClass().getEnumConstants());
        CycleButton.Builder<Enum<?>> builder = CycleButton.<Enum<?>>builder(
                value -> Component.literal(value.name()),
                () -> (Enum<?>) enumSetting.get()
        ).withValues(values);

        CycleButton<Enum<?>> cycle = builder.create(
                ctx.x, ctx.y - 1, ctx.width, 20,
                Component.literal(ctx.setting.name),
                (button, value) -> ((Setting) enumSetting).set(value)
        );

        if (ctx.hasDescription()) {
            cycle.setTooltip(ctx.createTooltip());
        }

        screen.addWidget(cycle);
    }

    private void createEditBox(WidgetCreationContext ctx) {
        EditBox box = new EditBox(font, ctx.x, ctx.y - 1, ctx.width, 20,
                Component.literal(ctx.setting.name));
        box.setValue(ValueFormatter.format(ctx.setting.get()));
        box.setMaxLength(256);
        box.setResponder(ctx.setting::parse);

        if (ctx.hasDescription()) {
            box.setTooltip(ctx.createTooltip());
        }

        screen.addWidget(box);
    }

    private void createReadOnlyWidget(WidgetCreationContext ctx) {
        String value = ValueFormatter.format(ctx.setting.get());
        String trimmed = ValueFormatter.trim(value, ctx.width);

        Button info = Button.builder(
                Component.literal(trimmed),
                btn -> screen.setStatus(ctx.setting.name + ": " + value)
        ).bounds(ctx.x, ctx.y - 1, ctx.width, 20).build();

        if (ctx.hasDescription()) {
            info.setTooltip(ctx.createTooltip());
        }

        screen.addWidget(info);
    }

    private boolean isTextInputSetting(Setting<?> setting) {
        return setting instanceof IntSetting
                || setting instanceof DoubleSetting
                || setting instanceof StringSetting
                || setting instanceof BlockPosSetting
                || setting instanceof DoubleRangeSetting
                || setting instanceof ColorSetting
                || setting instanceof BlockListSetting
                || setting instanceof DirectionListSetting;
    }

    private void scheduleRebuild() {
        screen.minecraft().execute(() -> {
            if (screen.minecraft().screen == screen) {
                screen.refreshUI();
            }
        });
    }

    private record WidgetCreationContext(Setting<?> setting, int x, int y, int width) {
        boolean hasDescription() {
            return setting.description != null && !setting.description.isBlank();
        }

        Tooltip createTooltip() {
            return Tooltip.create(Component.literal(setting.description));
        }
    }
}