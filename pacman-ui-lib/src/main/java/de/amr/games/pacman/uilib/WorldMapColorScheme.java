/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.scene.paint.Color;

public record WorldMapColorScheme(Color fill, Color stroke, Color door, Color pellet) {

    public WorldMapColorScheme(String fillSpec, String strokeSpec, String doorSpec, String pelletSpec) {
        this(Color.web(fillSpec), Color.web(strokeSpec), Color.web(doorSpec), Color.web(pelletSpec));
    }
}