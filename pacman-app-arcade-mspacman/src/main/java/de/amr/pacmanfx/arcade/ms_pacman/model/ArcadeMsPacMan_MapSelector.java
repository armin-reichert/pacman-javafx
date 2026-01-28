/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.UIConfig;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;

// Though there are 6 maps in Ms. Pac-Man, we only use world maps 1-4 (maps 5 and 6 are the same as 3 and 4 using a
// different color scheme.) and store the color map index (0-5) in the map configuration instead.
// The 2D renderer uses that index to extract the corresponding map image from the sprite sheet.
// The 3D renderer uses that index and the color values in MAP_COLOR_SCHEMES to create the 3D materials.
public class ArcadeMsPacMan_MapSelector implements WorldMapSelector {

    private static final int PROTOTYPES_COUNT = 4;
    private static final String PROTOTYPES_PATH = "/de/amr/pacmanfx/arcade/ms_pacman/maps/mspacman_%d.world";

    /** Colors used by the six Ms. Pac-Man Arcade maps. */
    public static final WorldMapColorScheme[] MAP_COLOR_SCHEMES = {
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new WorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new WorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
    };

    private static int mapNumber(int levelNumber) {
        return switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
    }

    private static int colorMapIndex(int levelNumber) {
        return switch (levelNumber) {
            case 1, 2 -> 0;
            case 3, 4, 5 -> 1;
            case 6, 7, 8, 9 -> 2;
            case 10, 11, 12, 13 -> 3;
            default -> (levelNumber - 14) % 8 < 4 ? 4 : 5;
        };
    }

    private List<WorldMap> mapPrototypes = List.of();

    @Override
    public void loadMapPrototypes() {
        try {
            mapPrototypes = WorldMapSelector.loadMaps(getClass(), PROTOTYPES_PATH, PROTOTYPES_COUNT);
        } catch (IOException x) {
            Logger.error("Could not open world map");
            throw new RuntimeException(x);
        } catch (WorldMapParseException x) {
            Logger.error("Could not parse world map");
            throw new RuntimeException(x);
        }
    }

    /**
     * <p>In Ms. Pac-Man, there are 4 maps (mapNumber=1..4) and 6 color schemes (colorIndex=0..5).
     * </p>
     * <ul>
     * <li>Levels 1-2: (mapNumber=1, colorIndex=0): pink wall fill, white dots
     * <li>Levels 3-5: (mapNumber=2, colorIndex=1)): light blue wall fill, yellow dots
     * <li>Levels 6-9: (mapNumber=3, colorIndex=2): orange wall fill, red dots
     * <li>Levels 10-13: (mapNumber=4, colorIndex=3): blue wall fill, white dots
     * </ul>
     * For levels 14 and later, alternates every 4th level between:
     * <ul>
     * <li>(mapNumber=3, colorIndex=4): pink wall fill, cyan dots
     * <li>(mapNumber=4, colorIndex=5): orange wall fill, white dots
     * </ul>
     * <p>
     *
     * @param levelNumber level number (starts at 1)
     * @param args (unused)
     */
    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) {
        requireValidLevelNumber(levelNumber);
        if (mapPrototypes.isEmpty()) {
            loadMapPrototypes();
        }
        final int mapNumber = mapNumber(levelNumber);
        final WorldMap prototype = mapPrototypes.get(mapNumber - 1);

        final WorldMap worldMap = new WorldMap(prototype);
        worldMap.setConfigValue(UIConfig.ConfigKey.MAP_NUMBER, mapNumber);
        worldMap.setConfigValue(UIConfig.ConfigKey.COLOR_MAP_INDEX, colorMapIndex(levelNumber));
        return worldMap;
    }
}