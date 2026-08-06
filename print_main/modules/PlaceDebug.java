/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.print_main.modules;

import com.kijinseija.seija_printer.utils.player.ChatUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

public class PlaceDebug extends ClientModule {

    public PlaceDebug() {
        super("PlaceDebug", "Test");

    }
    public void onPacket(PlayerInteractBlockC2SPacket packet){
        if (packet != null) {
            BlockHitResult bhr = packet.getBlockHitResult();

            ChatUtils.sendMsg(Text.of(bhr.getBlockPos()+"  Side:"+bhr.getSide()+" Vec:"+bhr.getPos()+"Type:"+bhr.getType()));
        }
    }
}
