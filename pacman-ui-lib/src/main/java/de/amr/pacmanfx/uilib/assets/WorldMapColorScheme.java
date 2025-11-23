/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public record WorldMapColorScheme(Color fill, Color stroke, Color door, Color pellet) {

    public WorldMapColorScheme(String fillSpec, String strokeSpec, String doorSpec, String pelletSpec) {
        this(Color.web(fillSpec), Color.web(strokeSpec), Color.web(doorSpec), Color.web(pelletSpec));
    }

    public Map<String, String> toColorMap() {
        return Map.of(
            "fill",   formatColorHex(fill),
            "stroke", formatColorHex(stroke),
            "door",   formatColorHex(door),
            "pellet", formatColorHex(pellet)
        );
    }
}