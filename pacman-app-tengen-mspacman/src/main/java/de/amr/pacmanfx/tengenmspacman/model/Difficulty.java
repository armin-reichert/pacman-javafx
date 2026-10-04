/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.EnumMethods;

public enum Difficulty implements EnumMethods<Difficulty> {
    NORMAL, EASY, HARD, CRAZY;

    @Override
    public Class<Difficulty> enumClass() {
        return Difficulty.class;
    }
}
