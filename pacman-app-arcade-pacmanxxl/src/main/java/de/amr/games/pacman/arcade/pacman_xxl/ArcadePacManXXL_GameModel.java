/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.randomInt;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class ArcadePacManXXL_GameModel extends ArcadePacMan_GameModel {

    static final List<Map<String, String>> MAP_COLORINGS = List.of(
        Map.of("fill", "#359c9c", "stroke", "#85e2ff", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c2b853", "stroke", "#ffeace", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#86669c", "stroke", "#f6c4e0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#ed0a04", "stroke", "#f0b4cd", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#2067c1", "stroke", "#65e5bb", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c55994", "stroke", "#f760c0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#12bc76", "stroke", "#ade672", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#5036d9", "stroke", "#5f8bcf", "door", "#fcb5ff", "pellet", "#feb8ae")
    );

    public ArcadePacManXXL_GameModel(File userDir) {
        super(userDir);
        scoreManager.setHighScoreFile(new File(userDir, "highscore-pacman_xxl.xml"));
        builtinMaps.clear(); // super class constructor adds Pac-Man Aracde map
        loadBuiltinMaps("maps/masonic_%d.world", 8);
        updateCustomMaps();
    }

    @Override
    protected WorldMap selectWorldMap(int levelNumber) {
        WorldMap template = switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS ->
                    levelNumber <= builtinMaps.size()
                            ? builtinMaps.get(levelNumber - 1)
                            : builtinMaps.get(randomInt(0, builtinMaps.size()));
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield levelNumber <= maps.size()
                        ? maps.get(levelNumber - 1)
                        : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield maps.get(randomInt(0, maps.size()));
            }
        };

        WorldMap worldMap = new WorldMap(template);
        Map<String, String> mapColoring = builtinMaps.contains(template) ? randomMapColoring() : coloringFromMap(template);
        worldMap.setConfigValue("colorMap", mapColoring);

        return worldMap;
    }

    private Map<String, String> randomMapColoring() {
        return MAP_COLORINGS.get(randomInt(0, MAP_COLORINGS.size()));
    }

    private Map<String, String> coloringFromMap(WorldMap template) {
        return Map.of(
            "fill",   template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_WALL_FILL,   "000000"),
            "stroke", template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_WALL_STROKE, "0000ff"),
            "door",   template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_DOOR,        "00ffff"),
            "pellet", template.getPropertyOrDefault(LayerID.FOOD, PROPERTY_COLOR_FOOD,           "ffffff"));
    }

    @Override
    public void configureDemoLevel() {
        configureNormalLevel();
        levelCounterEnabled = false;
        demoLevelSteering = new RuleBasedPacSteering(this); // super class uses predefined steering
        setDemoLevelBehavior();
    }
}