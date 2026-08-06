/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.kijinseija.seija_printer.settings.core.IVisible;
import com.kijinseija.seija_printer.settings.core.Setting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Direction;

public class DirectionListSetting extends Setting<List<Direction>> {
    public DirectionListSetting(String name, String description, List<Direction> defaultValue, Consumer<List<Direction>> onChanged, Consumer<Setting<List<Direction>>> onModuleActivated, IVisible visible) {
        super(name, description, new ArrayList<>(defaultValue), onChanged, onModuleActivated, visible);
        value = new ArrayList<>(defaultValue);
    }

    @Override
    protected List<Direction> parseImpl(String str) {
        String[] values = str.split(",");
        List<Direction> dirs = new ArrayList<>(values.length);
        for (String s : values) {
            Direction dir = Direction.byName(s);
            if (dir != null) dirs.add(dir);
        }
        return dirs;
    }

    @Override
    protected boolean isValueValid(List<Direction> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (Direction dir : get()) {
            valueTag.add(NbtString.of(dir.getName()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public List<Direction> load(NbtCompound tag) {
        if (!tag.contains("value", NbtElement.LIST_TYPE)) return new ArrayList<>(defaultValue);

        List<Direction> directions = new ArrayList<>();
        NbtList valueTag = tag.getList("value", NbtElement.STRING_TYPE);
        for (NbtElement tagI : valueTag) {
            Direction dir = Direction.byName(tagI.asString());
            if (dir != null)
                directions.add(dir);
        }

        return directions;
    }

    @Override
    public JsonElement toJson() {
        JsonArray json = new JsonArray();
        for (Direction direction : get()) json.add(new JsonPrimitive(direction.getName()));
        return json;
    }

    @Override
    public boolean fromJson(JsonElement json) {
        if (json == null || !json.isJsonArray()) return false;
        List<Direction> directions = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray()) {
            Direction direction = Direction.byName(element.getAsString());
            if (direction == null) return false;
            directions.add(direction);
        }
        return set(directions);
    }

    public static class Builder extends SettingBuilder<Builder, List<Direction>, DirectionListSetting> {

        public Builder() {
            super(new ArrayList<>(0));
        }

        @Override
        public Builder defaultValue(List<Direction> map) {
            this.defaultValue = map == null ? new ArrayList<>() : new ArrayList<>(map);
            return this;
        }

        public DirectionListSetting.Builder defaultValue(Direction... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        @Override
        public DirectionListSetting build() {
            return new DirectionListSetting(name, description, new ArrayList<>(defaultValue), onChanged, onModuleActivated, visible);
        }
    }
}
