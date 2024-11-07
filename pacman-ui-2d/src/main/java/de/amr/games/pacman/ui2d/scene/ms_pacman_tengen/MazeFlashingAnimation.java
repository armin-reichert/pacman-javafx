/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.MapConfigurationManager;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.model.ms_pacman_tengen.MapConfigurationManager.randomMapColorScheme;

public class MazeFlashingAnimation {

    private static final Map<String, Color> HIGHLIGHT_COLOR_SCHEME = mapToColors(MapConfigurationManager.HIGHLIGHT_COLOR_SCHEME);

    private static Map<String, Color> mapToColors(Map<String, String> colorScheme) {
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

    public void init(TengenMsPacManGame game) {
        Map<String, Color> currentScheme = mapToColors(game.currentMapConfig().colorScheme());
        boolean random = game.mapConfigMgr().isRandomColorSchemeUsed(game.mapCategory(),
            game.level().get().number);
        colorSchemes.clear();
        for (int i = 0; i < game.numFlashes(); ++i) {
            colorSchemes.add(random ? randomColorfulScheme() : currentScheme);
            colorSchemes.add(HIGHLIGHT_COLOR_SCHEME);
        }
        startTick = -1;
    }

    private Map<String, Color> randomColorfulScheme() {
        var colorScheme = randomMapColorScheme().get();
        // skip color schemes with black fill color
        while (colorScheme.get("fill").equals(NES.Palette.color(0x0f))) {
            colorScheme = randomMapColorScheme().get();
        }
        return mapToColors(colorScheme);
    }

    public Map<String, Color> currentColorScheme() {
        return highlightPhase ? HIGHLIGHT_COLOR_SCHEME : colorSchemes.get(currentIndex);
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
