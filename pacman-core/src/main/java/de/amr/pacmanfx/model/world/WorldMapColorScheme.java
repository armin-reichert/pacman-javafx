/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.world;

import java.util.Map;

public record WorldMapColorScheme(String fill, String stroke, String door, String pellet) {

    public Map<String, String> toColorMap() {
        return Map.of(
            "fill",   (fill),
            "stroke", (stroke),
            "door",   (door),
            "pellet", (pellet)
        );
    }
}