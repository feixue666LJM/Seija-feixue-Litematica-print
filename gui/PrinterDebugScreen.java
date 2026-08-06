/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.gui;

import com.kijinseija.seija_printer.print_main.InitClass;
import com.kijinseija.seija_printer.print_main.modules.ClientModule;
import com.kijinseija.seija_printer.print_main.modules.ItemSearcher;
import com.kijinseija.seija_printer.print_main.modules.Printer;
import com.kijinseija.seija_printer.print_main.printer.task_manager.RotationManager;
import com.kijinseija.seija_printer.print_main.printer.util.RenderUtil;
import com.kijinseija.seija_printer.settings.PrinterSettings;
import com.kijinseija.seija_printer.settings.core.BlockPosSetting;
import com.kijinseija.seija_printer.settings.core.BlockListSetting;
import com.kijinseija.seija_printer.settings.core.BoolSetting;
import com.kijinseija.seija_printer.settings.core.ColorSetting;
import com.kijinseija.seija_printer.settings.core.DoubleSetting;
import com.kijinseija.seija_printer.settings.core.EnumSetting;
import com.kijinseija.seija_printer.settings.core.IntSetting;
import com.kijinseija.seija_printer.settings.core.Setting;
import com.kijinseija.seija_printer.settings.core.SettingColor;
import com.kijinseija.seija_printer.settings.core.SettingGroup;
import com.kijinseija.seija_printer.settings.core.StringSetting;
import com.kijinseija.seija_printer.settings.obj.DoubleRange;
import com.kijinseija.seija_printer.settings.impl.DoubleRangeSetting;
import com.kijinseija.seija_printer.settings.impl.DirectionListSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Standalone settings and runtime diagnostics screen.
 *
 * <p>The screen deliberately uses only vanilla client widgets.  It can be
 * opened while in a world ({@link #shouldPause()} returns {@code false}),
 * so module ticks and rendering continue while values are being inspected.</p>
 */
public final class PrinterDebugScreen extends Screen {
    private static final int ROW_HEIGHT = 28;
    private static final int GROUP_HEIGHT = 22;
    private static final int PANEL_MARGIN = 12;
    private static final int BOTTOM_BAR_HEIGHT = 36;

    private final Screen parent;
    private final InitClass runtime;
    private final List<SettingRow> rows = new ArrayList<>();

    private int selectedModule;
    private int scrollOffset;
    private int contentHeight;
    private String status = "";

    public PrinterDebugScreen(Screen parent, InitClass runtime) {
        super(Text.translatable("screen.seija_printer.debug.title"));
        this.parent = parent;
        this.runtime = runtime;
        this.selectedModule = 0;
    }

    /** Convenience entry point for key mappings and other client integrations. */
    public static void open(MinecraftClient client, InitClass runtime) {
        if (client != null) client.setScreen(new PrinterDebugScreen(client.currentScreen, runtime));
    }

    @Override
    protected void init() {
        clearChildren();
        rows.clear();

        List<ClientModule> modules = modules();
        if (selectedModule >= modules.size()) selectedModule = Math.max(0, modules.size() - 1);

        int panelLeft = panelLeft();
        int panelRight = panelRight();
        int panelTop = panelTop();
        int panelBottom = panelBottom();

        addModuleTabs(modules, panelLeft, 28);

        ClientModule module = currentModule();
        int actionY = 54;
        int actionWidth = Math.min(112, Math.max(84, (panelRight - panelLeft - 18) / 5));
        addDrawableChild(ButtonWidget.builder(module == null
                ? Text.translatable("screen.seija_printer.debug.no_module")
                : Text.translatable(module.isActive()
                    ? "screen.seija_printer.debug.disable"
                    : "screen.seija_printer.debug.enable"), button -> {
            if (module != null) {
                module.toggle();
                status = module.name + (module.isActive() ? " enabled" : " disabled");
                clearAndInit();
            }
        }).dimensions(panelLeft, actionY, actionWidth, 20).build());

        if (module instanceof ItemSearcher searcher) {
            int searchActionY = 78;
            int searchActionWidth = (panelRight - panelLeft - 6) / 2;
            addDrawableChild(ButtonWidget.builder(Text.translatable("screen.seija_printer.debug.start_analysis"), button -> {
                if (!searcher.isActive()) searcher.activate();
                searcher.startAnalysis();
                status = "Analysis started";
            }).dimensions(panelLeft, searchActionY, searchActionWidth, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.translatable("screen.seija_printer.debug.print_list"), button -> {
                searcher.printItemList();
                status = "Material list sent to chat";
            }).dimensions(panelLeft + searchActionWidth + 6, searchActionY, searchActionWidth, 20).build());
        }

        int saveX = panelRight - 2 * 76 - 6;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.seija_printer.debug.save"), button -> {
            saveSettings();
            status = "Settings saved";
        }).dimensions(saveX, actionY, 76, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.seija_printer.debug.reset_all"), button -> {
            if (module != null) {
                module.settings.reset();
                status = "Settings reset";
                clearAndInit();
            }
        }).dimensions(panelRight - 76, actionY, 76, 20).build());

        if (module != null) buildSettingRows(module, panelLeft, panelRight, panelTop, panelBottom);
        if (contentHeight <= panelBottom - panelTop) scrollOffset = 0;
        else scrollOffset = Math.min(scrollOffset, maxScroll(panelTop, panelBottom));
    }

    private void addModuleTabs(List<ClientModule> modules, int left, int y) {
        int x = left;
        int available = Math.max(100, panelRight() - left);
        int width = Math.max(96, Math.min(170, available / Math.max(1, modules.size())));
        for (int i = 0; i < modules.size(); i++) {
            ClientModule module = modules.get(i);
            final int index = i;
            ButtonWidget tab = ButtonWidget.builder(Text.literal(module.name), button -> {
                selectedModule = index;
                scrollOffset = 0;
                status = "";
                clearAndInit();
            }).dimensions(x, y, width - 4, 20).build();
            if (!module.description.isBlank()) {
                tab.setTooltip(Tooltip.of(Text.literal(module.description)));
            }
            tab.active = selectedModule != i;
            addDrawableChild(tab);
            x += width;
        }
    }

    private void buildSettingRows(ClientModule module, int left, int right, int top, int bottom) {
        int y = top + 6 - scrollOffset;
        int labelWidth = Math.max(90, Math.min(260, (right - left) * 40 / 100));
        int controlX = left + labelWidth;
        int resetX = right - 56;
        int controlWidth = Math.max(72, resetX - controlX - 8);

        for (SettingGroup group : module.settings.values()) {
            if (group.settings.stream().noneMatch(Setting::isVisible)) continue;
            rows.add(SettingRow.group(group.name, y));
            y += GROUP_HEIGHT;

            for (Setting<?> setting : group.settings) {
                if (!setting.isVisible()) continue;
                int rowY = y;
                rows.add(SettingRow.setting(setting, rowY));
                if (isWithinPanel(rowY, top, bottom)) {
                    addSettingWidget(setting, controlX, rowY, controlWidth);
                    addDrawableChild(ButtonWidget.builder(Text.translatable("screen.seija_printer.debug.reset"), button -> {
                        setting.reset();
                        status = setting.name + " reset";
                        clearAndInit();
                    }).dimensions(resetX, rowY - 1, 54, 20).build());
                }
                y += ROW_HEIGHT;
            }
        }
        contentHeight = Math.max(0, y + scrollOffset - top);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addSettingWidget(Setting<?> setting, int x, int y, int width) {
        Text label = Text.literal(setting.name);
        String description = setting.description;
        Tooltip tooltip = description == null || description.isBlank()
            ? null : Tooltip.of(Text.literal(description));

        if (setting instanceof BoolSetting boolSetting) {
            CheckboxWidget checkbox = new CheckboxWidget(x, y - 1, width, 20, Text.empty(), Boolean.TRUE.equals(boolSetting.get())) {
                @Override
                public void onPress() {
                    super.onPress();
                    boolSetting.set(isChecked());
                    client.execute(() -> {
                    if (client.currentScreen == PrinterDebugScreen.this) clearAndInit();
                    });
                }
            };
            if (tooltip != null) checkbox.setTooltip(tooltip);
            addDrawableChild(checkbox);
            return;
        }

        if (setting instanceof EnumSetting<?> enumSetting) {
            Enum<?> current = (Enum<?>) enumSetting.get();
            if (current != null) {
                List<Enum<?>> values = Arrays.asList(current.getDeclaringClass().getEnumConstants());
                CyclingButtonWidget.Builder<Enum<?>> builder = CyclingButtonWidget.<Enum<?>>builder(
                    value -> Text.literal(value.name())
                ).values(values).initially((Enum<?>) enumSetting.get());
                CyclingButtonWidget<Enum<?>> cycle = builder.build(x, y - 1, width, 20, label,
                    (button, value) -> ((Setting) enumSetting).set(value));
                if (tooltip != null) cycle.setTooltip(tooltip);
                addDrawableChild(cycle);
                return;
            }
        }

        if (setting instanceof IntSetting || setting instanceof DoubleSetting
            || setting instanceof StringSetting || setting instanceof BlockPosSetting
            || setting instanceof DoubleRangeSetting || setting instanceof ColorSetting
            || setting instanceof BlockListSetting || setting instanceof DirectionListSetting) {
            TextFieldWidget box = new TextFieldWidget(textRenderer, x, y - 1, width, 20, label);
            box.setText(formatValue(setting.get()));
            box.setMaxLength(256);
            box.setChangedListener(setting::parse);
            if (tooltip != null) box.setTooltip(tooltip);
            addDrawableChild(box);
            return;
        }

        // Complex settings (block lists, colors, nested settings) remain
        // intentionally read-only here; the reset button still works.
        if (tooltip != null) {
            // The tooltip is attached to a compact button so read-only values
            // are still discoverable without introducing a custom widget.
            ButtonWidget info = ButtonWidget.builder(Text.literal(trimValue(formatValue(setting.get()), width)), button -> {
                status = setting.name + ": " + formatValue(setting.get());
            }).dimensions(x, y - 1, width, 20).tooltip(tooltip).build();
            addDrawableChild(info);
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal(trimValue(formatValue(setting.get()), width)), button -> {
                status = setting.name + ": " + formatValue(setting.get());
            }).dimensions(x, y - 1, width, 20).build());
        }
    }

    private static String formatValue(Object value) {
        if (value == null) return "<unset>";
        if (value instanceof BlockPos pos) return pos.getX() + " " + pos.getY() + " " + pos.getZ();
        if (value instanceof DoubleRange range) return range.value1 + " " + range.value2;
        if (value instanceof SettingColor color) {
            return color.r + "," + color.g + "," + color.b + "," + color.a + "," + color.rainbow;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(entry -> {
                if (entry instanceof Block block) return Registries.BLOCK.getId(block).toString();
                if (entry instanceof Direction direction) return direction.asString();
                return String.valueOf(entry);
            }).reduce((left, right) -> left + "," + right).orElse("");
        }
        if (value instanceof Enum<?> enumValue) return enumValue.name();
        return String.valueOf(value);
    }

    private static String trimValue(String value, int width) {
        int max = Math.max(12, width / 7);
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(1, max - 3)) + "...";
    }

    @Override
    public void renderBackground(MatrixStack graphics) {
        super.renderBackground(graphics);

        int left = panelLeft();
        int right = panelRight();
        int top = panelTop();
        int bottom = panelBottom();

        DrawableHelper.fill(graphics, left, top, right, bottom, 0x990B0F14);
        DrawableHelper.drawHorizontalLine(graphics, left, right - 1, top, 0xFF59616B);
        DrawableHelper.drawHorizontalLine(graphics, left, right - 1, bottom - 1, 0xFF59616B);
        DrawableHelper.drawVerticalLine(graphics, left, top, bottom - 1, 0xFF59616B);
        DrawableHelper.drawVerticalLine(graphics, right - 1, top, bottom - 1, 0xFF59616B);
    }

    @Override
    public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
        // Screen.render() applies the blurred background before drawing widgets.
        // Draw labels afterwards so they never become part of the blur pass.
        super.render(graphics, mouseX, mouseY, delta);

        int left = panelLeft();
        int right = panelRight();
        int top = panelTop();
        int bottom = panelBottom();

        DrawableHelper.drawCenteredTextWithShadow(graphics, textRenderer, title, width / 2, 8, 0xFFFFFFFF);

        ClientModule module = currentModule();
        if (!status.isBlank()) DrawableHelper.drawTextWithShadow(graphics, textRenderer, Text.literal(status), left + 8, bottom + 9, 0xFFE0C46C);
        DrawableHelper.drawTextWithShadow(graphics, textRenderer, Text.literal(diagnostics()), left + 8, bottom + 21, 0xFF9FAAB5);

        int labelWidth = Math.max(90, Math.min(260, (right - left) * 40 / 100));
        for (SettingRow row : rows) {
            if (row.group()) {
                if (isWithinPanel(row.y(), top, bottom)) {
                    DrawableHelper.drawTextWithShadow(graphics, textRenderer, Text.literal(row.groupName()), left + 8, row.y() + 5, 0xFF76B9FF);
                    DrawableHelper.drawHorizontalLine(graphics, left + 8, right - 8, row.y() + 18, 0x664A6176);
                }
            } else if (isWithinPanel(row.y(), top, bottom)) {
                String label = textRenderer.trimToWidth(row.setting().name, Math.max(20, labelWidth - 12), true);
                DrawableHelper.drawTextWithShadow(graphics, textRenderer, Text.literal(label), left + 8, row.y() + 5, 0xFFE8E8E8);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseY >= panelTop() && mouseY <= panelBottom() && contentHeight > panelBottom() - panelTop()) {
            int step = amount > 0 ? -ROW_HEIGHT : ROW_HEIGHT;
            int next = Math.max(0, Math.min(maxScroll(panelTop(), panelBottom()), scrollOffset + step));
            if (next != scrollOffset) {
                scrollOffset = next;
                clearAndInit();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void close() {
        saveSettings();
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void saveSettings() {
        if (runtime != null) PrinterSettings.getINSTANCE().saveModules(runtime.modules());
        else PrinterSettings.getINSTANCE().save(Printer.getINSTANCE().settings);
    }

    private String diagnostics() {
        String world = client.world == null ? "world: none" : "world: loaded";
        String player = client.player == null ? "player: none" : "player: " + client.player.getGameProfile().getName();
        String queue = "rotation: " + RotationManager.INSTANCE.taskSize();
        String render = "render: " + RenderUtil.renderList.size();
        ClientModule module = currentModule();
        String moduleState = module == null ? "module: none"
            : "module: " + (module.isActive() ? "active" : "inactive");
        if (module instanceof ItemSearcher searcher) {
            return trimText(moduleState + " | " + world + " | " + player + " | " + queue + " | " + render
                + " | materials: " + searcher.materialCounts().size(), panelRight() - panelLeft() - 16);
        }
        return trimText(moduleState + " | " + world + " | " + player + " | " + queue + " | " + render,
            panelRight() - panelLeft() - 16);
    }

    private String trimText(String text, int pixelWidth) {
        int maxChars = Math.max(20, pixelWidth / 6);
        if (text.length() <= maxChars) return text;
        return text.substring(0, Math.max(1, maxChars - 3)) + "...";
    }

    private List<ClientModule> modules() {
        if (runtime != null) return runtime.modules();
        return List.of(Printer.getINSTANCE());
    }

    private ClientModule currentModule() {
        List<ClientModule> modules = modules();
        return modules.isEmpty() ? null : modules.get(Math.max(0, Math.min(selectedModule, modules.size() - 1)));
    }

    private int panelLeft() {
        int panelWidth = Math.min(640, Math.max(260, width - PANEL_MARGIN * 2));
        return (width - panelWidth) / 2;
    }

    private int panelRight() {
        int panelWidth = Math.min(640, Math.max(260, width - PANEL_MARGIN * 2));
        return (width + panelWidth) / 2;
    }

    private int panelTop() {
        return currentModule() instanceof ItemSearcher ? 106 : 82;
    }

    private int panelBottom() {
        return Math.max(panelTop() + 40, height - BOTTOM_BAR_HEIGHT);
    }

    private int maxScroll(int top, int bottom) {
        return Math.max(0, contentHeight - (bottom - top));
    }

    private static boolean isWithinPanel(int y, int top, int bottom) {
        return y + ROW_HEIGHT >= top && y <= bottom - 2;
    }

    private record SettingRow(String groupName, Setting<?> setting, int y, boolean group) {
        static SettingRow group(String name, int y) {
            return new SettingRow(name, null, y, true);
        }

        static SettingRow setting(Setting<?> setting, int y) {
            return new SettingRow("", setting, y, false);
        }
    }
}
