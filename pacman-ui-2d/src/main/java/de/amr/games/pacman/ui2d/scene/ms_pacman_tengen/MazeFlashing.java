/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManTengenGame;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.model.ms_pacman_tengen.MsPacManTengenGameMapConfig.COLOR_MAPS;

public class MazeFlashing {

    private static final Map<String, Color> BLACK_WHITE_COLOR_MAP = Map.of(
        "fill",   Color.valueOf(NES.Palette.color(0x0f)),
        "stroke", Color.valueOf(NES.Palette.color(0x20)),
        "door",   Color.valueOf(NES.Palette.color(0x0f)),
        "food",   Color.valueOf(NES.Palette.color(0x0f))
    );

    private static Map<String, Color> mapStringToColorValues(Map<String, String> colorScheme) {
        Map<String, Color> colorMap = new HashMap<>();
        for (String key : colorScheme.keySet()) {
            colorMap.put(key, Color.valueOf(colorScheme.get(key)));
        }
        return colorMap;
    }

    private final List<Map<String, Color>> colorSchemes = new ArrayList<>();
    private long startTick;
    private int currentIndex;
    private boolean highlightPhase;

    public void init(MsPacManTengenGame game) {
        GameLevel level = game.level().orElseThrow();
        NES_ColorScheme currentScheme = (NES_ColorScheme) level.mapConfig().get("nesColorScheme");
        Map<String, Color> currentColorMap = mapStringToColorValues(COLOR_MAPS.get(currentScheme));
        boolean random = game.mapConfigMgr().isRandomColorSchemeUsed(game.mapCategory(), level.number);
        colorSchemes.clear();
        for (int i = 0; i < game.numFlashes(); ++i) {
            colorSchemes.add(random ? randomColorfulScheme() : currentColorMap);
            colorSchemes.add(BLACK_WHITE_COLOR_MAP);
        }
        startTick = -1;
    }

    private Map<String, Color> randomColorfulScheme() {
        NES_ColorScheme nesColorScheme = NES_ColorScheme.random();
        // ignore color schemes with black fill color
        while (nesColorScheme.fillColor().equals(NES.Palette.color(0x0f))) {
            nesColorScheme = NES_ColorScheme.random();
        }
        return mapStringToColorValues(COLOR_MAPS.get(nesColorScheme));
    }

    public Map<String, Color> currentColorScheme() {
        return highlightPhase ? BLACK_WHITE_COLOR_MAP : colorSchemes.get(currentIndex);
    }

    public void update(long t) {
        int phaseTicks = 10; // TODO: how many ticks really?
        if (startTick == -1) { // not running yet
            startTick = t;
            currentIndex = 0;
            Logger.info("Maze flashing started at tick {}", startTick);
        }
        // single flash phase complete?
        long flashingTicksSoFar = t - startTick;
        if (flashingTicksSoFar > 0 && flashingTicksSoFar % phaseTicks == 0) {
            if (currentIndex < colorSchemes.size() - 1) {
                ++currentIndex;
                Logger.info("Maze flashing index changes to {} at tick {}", currentIndex, t);
            }
        }
        highlightPhase = flashingTicksSoFar % (2 * phaseTicks) == 1;
    }
}