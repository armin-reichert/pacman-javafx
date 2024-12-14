/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import javafx.scene.paint.Color;

import java.util.Map;

public record WorldMapColoring(Color fill, Color stroke, Color door, Color pellet) {

    public WorldMapColoring(String fillValue, String strokeValue, String doorValue, String pelletValue) {
        this(Color.valueOf(fillValue), Color.valueOf(strokeValue), Color.valueOf(doorValue), Color.valueOf(pelletValue));
    }

    public WorldMapColoring(Map<String, String> sourceMap) {
        this(sourceMap.get("fill"), sourceMap.get("stroke"), sourceMap.get("door"), sourceMap.get("pellet"));
    }

    public WorldMapColoring(NES_ColorScheme ncs) {
        this(ncs.fillColor(), ncs.strokeColor(), ncs.strokeColor(), ncs.pelletColor());
    }
}