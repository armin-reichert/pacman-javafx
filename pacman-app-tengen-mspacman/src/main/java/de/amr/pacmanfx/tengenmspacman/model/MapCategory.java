/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.EnumMethods;

public enum MapCategory implements EnumMethods<MapCategory> {
    ARCADE, MINI, BIG, STRANGE;

    @Override
    public Class<MapCategory> enumClass() {
        return MapCategory.class;
    }
}
