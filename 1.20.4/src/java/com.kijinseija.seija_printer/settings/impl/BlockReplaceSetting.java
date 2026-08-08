/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kijinseija.seija_printer.settings.core.IVisible;
import com.kijinseija.seija_printer.settings.core.Setting;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockReplaceSetting extends Setting<HashMap<List<Block>, List<Block>>> {


    private BlockReplaceSetting(String name, String description, HashMap<List<Block>, List<Block>> defaultValue, Consumer<HashMap<List<Block>, List<Block>>> onChanged, Consumer<Setting<HashMap<List<Block>, List<Block>>>> onModuleActivated, IVisible visible) {
        super(name, description, copy(defaultValue), onChanged, onModuleActivated, visible);
        value = copy(defaultValue);
    }

    private static HashMap<List<Block>, List<Block>> copy(Map<List<Block>, List<Block>> source) {
        HashMap<List<Block>, List<Block>> copy = new LinkedHashMap<>();
        if (source == null) return copy;
        for (Map.Entry<List<Block>, List<Block>> entry : source.entrySet()) {
            List<Block> key = entry.getKey() == null ? new ArrayList<>() : new ArrayList<>(entry.getKey());
            List<Block> val = entry.getValue() == null ? null : new ArrayList<>(entry.getValue());
            copy.put(key, val);
        }
        return copy;
    }

    @Override
    protected void resetImpl() {
        value = copy(defaultValue);
    }

    @Override
    protected HashMap<List<Block>, List<Block>> parseImpl(String str) {
        return new LinkedHashMap<>(0);
    }

    @Override
    protected boolean isValueValid(HashMap<List<Block>, List<Block>> value) {
        return true;
    }


    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();//总列表 存储键值对
        for (Map.Entry<List<Block>, List<Block>> blockEntry : get().entrySet()) {
            List<Block> keyBlocks = blockEntry.getKey();
            List<Block> valueBlocks = blockEntry.getValue();

            if (valueBlocks == null)
                continue;

            //单个
            NbtCompound entryTag = new NbtCompound();//单个键值对标签

            NbtList keyBlockList = new NbtList();
            for (Block block : keyBlocks) {
                keyBlockList.add(NbtString.of(Registries.BLOCK.getId(block).toString()));
            }//填写key
            entryTag.put("keyBlocks", keyBlockList);

            NbtList valueBlockList = new NbtList();
            for (Block block : valueBlocks) {
                valueBlockList.add(NbtString.of(Registries.BLOCK.getId(block).toString()));
            }//填valueBlock
            entryTag.put("valBlocks", valueBlockList);

            valueTag.add(entryTag);

        }
        tag.put("value", valueTag);


        return tag;
    }

    @Override
    protected HashMap<List<Block>, List<Block>> load(NbtCompound tag) {
        if (!tag.contains("value", NbtElement.LIST_TYPE)) return copy(defaultValue);

        HashMap<List<Block>, List<Block>> entries = new LinkedHashMap<>();
        NbtList entryListTag = tag.getList("value", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < entryListTag.size(); i++) {
            NbtCompound entryTag = entryListTag.getCompound(i);
            NbtList keyBlocksTag = entryTag.getList("keyBlocks", NbtElement.STRING_TYPE);
            ArrayList<Block> keyList = new ArrayList<>();
            for (NbtElement tagI : keyBlocksTag) {
                Block block = Registries.BLOCK.get(new Identifier(tagI.asString()));
                keyList.add(block);
            }
            NbtList valBlocksTag = entryTag.getList("valBlocks", NbtElement.STRING_TYPE);
            ArrayList<Block> valList = new ArrayList<>();
            for (NbtElement tagI : valBlocksTag) {
                Block block = Registries.BLOCK.get(new Identifier(tagI.asString()));
                valList.add(block);
            }
            entries.put(keyList, valList);
        }

        return entries;
    }

    @Override
    public JsonElement toJson() {
        JsonArray entries = new JsonArray();
        for (Map.Entry<List<Block>, List<Block>> entry : get().entrySet()) {
            if (entry.getValue() == null) continue;

            JsonObject jsonEntry = new JsonObject();
            jsonEntry.add("keyBlocks", blocksToJson(entry.getKey()));
            jsonEntry.add("valBlocks", blocksToJson(entry.getValue()));
            entries.add(jsonEntry);
        }
        return entries;
    }

    @Override
    public boolean fromJson(JsonElement json) {
        if (json == null || !json.isJsonArray()) return false;

        HashMap<List<Block>, List<Block>> entries = new LinkedHashMap<>();
        try {
            for (JsonElement element : json.getAsJsonArray()) {
                if (!element.isJsonObject()) return false;
                JsonObject entry = element.getAsJsonObject();
                entries.put(blocksFromJson(entry.getAsJsonArray("keyBlocks")), blocksFromJson(entry.getAsJsonArray("valBlocks")));
            }
        } catch (RuntimeException ignored) {
            return false;
        }
        return set(entries);
    }

    private static JsonArray blocksToJson(List<Block> blocks) {
        JsonArray json = new JsonArray();
        for (Block block : blocks) json.add(new JsonPrimitive(Registries.BLOCK.getId(block).toString()));
        return json;
    }

    private static List<Block> blocksFromJson(JsonArray json) {
        if (json == null) throw new IllegalArgumentException("Missing block list");
        List<Block> blocks = new ArrayList<>();
        for (JsonElement element : json) {
            blocks.add(Registries.BLOCK.get(new Identifier(element.getAsString())));
        }
        return blocks;
    }

    public static class Builder extends SettingBuilder<BlockReplaceSetting.Builder, HashMap<List<Block>, List<Block>>, BlockReplaceSetting> {
        public Builder() {
            super(new LinkedHashMap<>());
        }

        @Override
        public BlockReplaceSetting build() {
            return new BlockReplaceSetting(name, description, copy(defaultValue), onChanged, onModuleActivated, visible);
        }


    }
}
