/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.print_main.printer.util.records;

import com.kijinseija.seija_printer.print_main.modules.Printer;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record DirDataI(BlockPos placePos, List<Direction> dirs) {
    private static Printer pri = Printer.getINSTANCE();


}
