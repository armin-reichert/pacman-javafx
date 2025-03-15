/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import javafx.scene.paint.Color;

import java.util.Map;

public record WorldMapColoring(Color fill, Color stroke, Color door, Color pellet) {

    public WorldMapColoring(String fillValue, String strokeValue, String doorValue, String pelletValue) {
        this(Color.valueOf(fillValue), Color.valueOf(strokeValue), Color.valueOf(doorValue), Color.valueOf(pelletValue));
    }

    public WorldMapColoring(NES_ColorScheme ncs) {
        this(ncs.fillColor(), ncs.strokeColor(), ncs.strokeColor(), ncs.pelletColor());
    }
}