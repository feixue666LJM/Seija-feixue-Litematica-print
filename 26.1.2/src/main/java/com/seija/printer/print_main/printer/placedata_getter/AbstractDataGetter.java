/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.seija.printer.print_main.printer.placedata_getter;

import com.seija.printer.print_main.modules.Printer;
import com.seija.printer.print_main.printer.util.records.DirData;
import com.seija.printer.print_main.printer.util.records.PlaceData;
import com.seija.printer.settings.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDataGetter {
    public Printer pri = Printer.getINSTANCE();
    public abstract PlaceData getData(BlockState needState, DirData dirData);
    public abstract boolean isSuitable(BlockState needState,BlockPos pos);
    public Setting[] getSettings(){
        return new Setting[]{};
    };
}
