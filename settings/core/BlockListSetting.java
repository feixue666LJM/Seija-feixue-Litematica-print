/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.settings.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockListSetting extends Setting<List<Block>> {
    protected BlockListSetting(
        String name,
        String description,
        List<Block> defaultValue,
        Consumer<List<Block>> onChanged,
        Consumer<Setting<List<Block>>> onModuleActivated,
        IVisible visible
    ) {
        super(name, description, copy(defaultValue), onChanged, onModuleActivated, visible);
        value = copy(defaultValue);
    }

    private static List<Block> copy(List<Block> blocks) {
        return blocks == null ? new ArrayList<>() : new ArrayList<>(blocks);
    }

    @Override
    protected List<Block> parseImpl(String text) {
        List<Block> blocks = new ArrayList<>();
        if (text == null || text.isBlank()) return blocks;

        for (String value : text.split(",")) {
            try {
                blocks.add(Registries.BLOCK.get(new Identifier(value.trim())));
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return blocks;
    }

    @Override
    protected boolean isValueValid(List<Block> value) {
        return value != null && value.stream().noneMatch(java.util.Objects::isNull);
    }

    @Override
    protected void resetImpl() {
        value = copy(defaultValue);
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList list = new NbtList();
        for (Block block : value) list.add(NbtString.of(Registries.BLOCK.getId(block).toString()));
        tag.put("value", list);
        return tag;
    }

    @Override
    protected List<Block> load(NbtCompound tag) {
        if (!tag.contains("value", NbtElement.LIST_TYPE)) return copy(defaultValue);

        List<Block> blocks = new ArrayList<>();
        for (NbtElement valueTag : tag.getList("value", NbtElement.STRING_TYPE)) {
            String id = valueTag.asString();
            if (!id.isEmpty()) blocks.add(Registries.BLOCK.get(new Identifier(id)));
        }
        return blocks;
    }

    @Override
    public JsonElement toJson() {
        JsonArray json = new JsonArray();
        for (Block block : value) json.add(new JsonPrimitive(Registries.BLOCK.getId(block).toString()));
        return json;
    }

    @Override
    public boolean fromJson(JsonElement json) {
        if (json == null || !json.isJsonArray()) return false;
        List<Block> blocks = new ArrayList<>();
        try {
            for (JsonElement element : json.getAsJsonArray()) {
                blocks.add(Registries.BLOCK.get(new Identifier(element.getAsString())));
            }
        } catch (RuntimeException ignored) {
            return false;
        }
        return set(blocks);
    }

    public static class Builder extends SettingBuilder<Builder, List<Block>, BlockListSetting> {
        public Builder() {
            super(new ArrayList<>());
        }

        @Override
        public Builder defaultValue(List<Block> blocks) {
            defaultValue = copy(blocks);
            return this;
        }

        public Builder defaultValue(Block... blocks) {
            return defaultValue(blocks == null ? List.of() : Arrays.asList(blocks));
        }

        @Override
        public BlockListSetting build() {
            return new BlockListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
