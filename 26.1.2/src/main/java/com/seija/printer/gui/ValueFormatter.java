package com.seija.printer.gui;

import com.seija.printer.settings.core.SettingColor;
import com.seija.printer.settings.obj.DoubleRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

public final class ValueFormatter {

    private ValueFormatter() {}

    public static String format(Object value) {
        if (value == null) return "<unset>";

        if (value instanceof BlockPos pos) {
            return formatBlockPos(pos);
        }
        if (value instanceof DoubleRange range) {
            return formatDoubleRange(range);
        }
        if (value instanceof SettingColor color) {
            return formatColor(color);
        }
        if (value instanceof Collection<?> collection) {
            return formatCollection(collection);
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        return String.valueOf(value);
    }

    public static String trim(String value, int pixelWidth) {
        int maxChars = Math.max(12, pixelWidth / 7);
        if (value.length() <= maxChars) return value;
        return value.substring(0, Math.max(1, maxChars - 3)) + "...";
    }

    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String formatDoubleRange(DoubleRange range) {
        return range.value1 + " " + range.value2;
    }

    private static String formatColor(SettingColor color) {
        return color.r + "," + color.g + "," + color.b + "," + color.a + "," + color.rainbow;
    }

    private static String formatCollection(Collection<?> collection) {
        return collection.stream()
                .map(ValueFormatter::formatCollectionEntry)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static String formatCollectionEntry(Object entry) {
        if (entry instanceof Block block) {
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        }
        if (entry instanceof Direction direction) {
            return direction.getName();
        }
        return String.valueOf(entry);
    }
}