/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.EnumMethods;

public enum BoosterMode implements EnumMethods<BoosterMode> {
    BOOSTER_OFF, ACTIVATE_WITH_A_OR_B, BOOSTER_ALWAYS_ON;

    @Override
    public Class<BoosterMode> enumClass() {
        return BoosterMode.class;
    }
}
