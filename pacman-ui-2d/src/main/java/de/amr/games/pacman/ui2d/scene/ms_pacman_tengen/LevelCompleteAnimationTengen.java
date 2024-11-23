package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.scene.common.LevelCompleteAnimation;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenRenderer.COLOR_MAPS;

public class LevelCompleteAnimationTengen extends LevelCompleteAnimation {

    private static final Map<String, Color> BLACK_WHITE_COLOR_MAP = Map.of(
        "fill",   Color.valueOf(NES.Palette.color(0x0f)),
        "stroke", Color.valueOf(NES.Palette.color(0x20)),
        "door",   Color.valueOf(NES.Palette.color(0x0f)),
        "food",   Color.valueOf(NES.Palette.color(0x0f))
    );

    private final List<Map<String, Color>> colorMaps = new ArrayList<>();

    public LevelCompleteAnimationTengen(Map<String, Object> mapConfig, int numFlashes, int highlightPhaseDuration) {
        super(numFlashes, highlightPhaseDuration);
        NES_ColorScheme nesColorScheme = (NES_ColorScheme) mapConfig.get("nesColorScheme");
        boolean randomColorScheme = (boolean) mapConfig.get("randomColorScheme");
        Map<String, Color> selectedColorMap = COLOR_MAPS.get(nesColorScheme);
        colorMaps.clear();
        for (int i = 0; i < numFlashes; ++i) {
            colorMaps.add(randomColorScheme ? chooseRandomColorMap() : selectedColorMap);
        }
    }

    private Map<String, Color> chooseRandomColorMap() {
        NES_ColorScheme nesColorScheme = NES_ColorScheme.random();
        // ignore color schemes with black fill color
        while (nesColorScheme.fillColor().equals(NES.Palette.color(0x0f))) {
            nesColorScheme = NES_ColorScheme.random();
        }
        return COLOR_MAPS.get(nesColorScheme);
    }

    public Map<String, Color> currentColorMap() {
        return isHighlightMaze() ? BLACK_WHITE_COLOR_MAP : colorMaps.get(flashingIndex());
    }
}
