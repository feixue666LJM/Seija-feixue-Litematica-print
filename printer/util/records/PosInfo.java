/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.print_main.printer.util.records;

import com.kijinseija.seija_printer.settings.core.Color;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public record PosInfo(BlockPos pos, Direction torchDir, Vec3d clickVec,boolean isPlaceMode, long timestamp, Color renderColor) {
    }
