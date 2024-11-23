/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenRenderer.COLOR_MAPS;

public class MazeFlashing {

    private static final Map<String, Color> BLACK_WHITE_COLOR_MAP = Map.of(
        "fill",   Color.valueOf(NES.Palette.color(0x0f)),
        "stroke", Color.valueOf(NES.Palette.color(0x20)),
        "door",   Color.valueOf(NES.Palette.color(0x0f)),
        "food",   Color.valueOf(NES.Palette.color(0x0f))
    );

    private final List<Map<String, Color>> colorMaps = new ArrayList<>();
    private boolean running;
    private long startTick;
    private int currentIndex;
    private boolean highlightPhase;

    public MazeFlashing(Map<String, Object> mapConfig, int numFlashes) {
        NES_ColorScheme nesColorScheme = (NES_ColorScheme) mapConfig.get("nesColorScheme");
        boolean randomColorScheme = (boolean) mapConfig.get("randomColorScheme");
        Map<String, Color> worldMapColorMap = COLOR_MAPS.get(nesColorScheme);
        colorMaps.clear();
        for (int i = 0; i < numFlashes; ++i) {
            colorMaps.add(randomColorScheme ? randomColorMap() : worldMapColorMap);
            colorMaps.add(BLACK_WHITE_COLOR_MAP);
        }
        startTick = -1;
        running = false;
    }

    private Map<String, Color> randomColorMap() {
        NES_ColorScheme nesColorScheme = NES_ColorScheme.random();
        // ignore color schemes with black fill color
        while (nesColorScheme.fillColor().equals(NES.Palette.color(0x0f))) {
            nesColorScheme = NES_ColorScheme.random();
        }
        return COLOR_MAPS.get(nesColorScheme);
    }

    public Map<String, Color> currentColorMap() {
        return highlightPhase ? BLACK_WHITE_COLOR_MAP : colorMaps.get(currentIndex);
    }

    public void startAt(long tick) {
        running = true;
        startTick = tick;
        currentIndex = 0;
        Logger.debug("Maze flashing started at tick {}", startTick);
    }

    public boolean isRunning() {
        return running;
    }

    public void update(long tick) {
        int phaseTicks = 10; // TODO: how many ticks really?
        // single flash phase complete?
        long flashingTicksSoFar = tick - startTick;
        if (flashingTicksSoFar > 0 && flashingTicksSoFar % phaseTicks == 0) {
            if (currentIndex < colorMaps.size() - 1) {
                ++currentIndex;
                Logger.debug("Maze flashing index changes to {} at tick {}", currentIndex, tick);
            }
        }
        highlightPhase = flashingTicksSoFar % (2 * phaseTicks) == 1;
    }
}