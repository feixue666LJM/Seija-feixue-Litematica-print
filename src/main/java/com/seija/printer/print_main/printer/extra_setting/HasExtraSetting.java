/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.seija.printer.print_main.printer.extra_setting;


import com.seija.printer.settings.core.SettingGroup;
import com.seija.printer.settings.core.Settings;

public interface HasExtraSetting {
    SettingGroup getSettingGroup(Settings settings);
}
