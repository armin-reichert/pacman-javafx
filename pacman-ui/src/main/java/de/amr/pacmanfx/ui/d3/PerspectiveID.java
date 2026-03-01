/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

public enum PerspectiveID {
    DRONE, TOTAL, TRACK_PLAYER, NEAR_PLAYER;

    public PerspectiveID prev() {
        return values()[ordinal() == 0 ? values().length - 1 : ordinal() - 1];
    }

    public PerspectiveID next() {
        return values()[ordinal() < values().length - 1 ? ordinal() + 1 : 0];
    }
}
