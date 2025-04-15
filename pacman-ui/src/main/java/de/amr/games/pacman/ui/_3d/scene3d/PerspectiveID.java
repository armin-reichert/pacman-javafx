/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.scene3d;

// Note: Be careful when changing these IDs because they are used to compose resource bundle keys
public enum PerspectiveID {
    DRONE, TOTAL, TRACK_PLAYER, NEAR_PLAYER;

    public PerspectiveID prev() {
        return values()[ordinal() == 0 ? values().length - 1 : ordinal() - 1];
    }

    public PerspectiveID next() {
        return values()[ordinal() < values().length - 1 ? ordinal() + 1 : 0];
    }
}