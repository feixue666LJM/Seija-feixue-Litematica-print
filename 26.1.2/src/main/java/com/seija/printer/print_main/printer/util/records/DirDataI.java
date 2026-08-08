/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.seija.printer.print_main.printer.util.records;

import java.util.List;

import com.seija.printer.print_main.modules.Printer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record DirDataI(BlockPos placePos, List<Direction> dirs) {
    private static Printer pri = Printer.getINSTANCE();


}
