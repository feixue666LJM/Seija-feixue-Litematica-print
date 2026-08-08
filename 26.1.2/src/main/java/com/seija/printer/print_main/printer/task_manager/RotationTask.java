/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.seija.printer.print_main.printer.task_manager;


import com.seija.printer.print_main.printer.util.records.RotationData;

public class RotationTask extends Task{
    public RotationData rdata;

    public RotationTask(Runnable task, RotationData rdata) {
        super(task);
        this.rdata = rdata;
    }
}
