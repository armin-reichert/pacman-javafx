/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.scene.common.LevelCompleteAnimation;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In Tengen Ms. Pac-Man, maze flashing animations in levels 28-31 (non-Arcade mazes) use
 * different colors, not just switching to black-and-white.
 */
public class LevelCompleteAnimationTengen extends LevelCompleteAnimation {

    private final List<NES_ColorScheme> colorSchemes = new ArrayList<>();

    public LevelCompleteAnimationTengen(Map<String, Object> mapConfig, int numFlashes, int highlightPhaseDuration) {
        super(numFlashes, highlightPhaseDuration);
        var nesColorScheme = (NES_ColorScheme) mapConfig.get("nesColorScheme");
        var randomColorScheme = (boolean) mapConfig.get("randomColorScheme");
        for (int i = 0; i < numFlashes; ++i) {
            NES_ColorScheme previous = i > 0 ? colorSchemes.get(i-1) : null;
            colorSchemes.add(randomColorScheme ? chooseRandomColorScheme(previous) : nesColorScheme);
        }
    }

    private NES_ColorScheme chooseRandomColorScheme(NES_ColorScheme previous) {
        NES_ColorScheme nesColorScheme = NES_ColorScheme.random();
        // avoid repetitions and ignore color schemes with black fill color
        while (nesColorScheme == previous || nesColorScheme.fillColor().equals(NES.Palette.color(0x0f))) {
            nesColorScheme = NES_ColorScheme.random();
        }
        return nesColorScheme;
    }

    public Color currentFillColor() {
        return Color.valueOf(isInHighlightPhase() ? NES.Palette.color(0x0f) : colorSchemes.get(flashingIndex()).fillColor());
    }

    public Color currentStrokeColor() {
        return Color.valueOf(isInHighlightPhase() ? NES.Palette.color(0x20) : colorSchemes.get(flashingIndex()).strokeColor());
    }
}