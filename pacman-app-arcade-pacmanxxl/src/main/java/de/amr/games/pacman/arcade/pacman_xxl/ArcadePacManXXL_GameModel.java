/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>).
 */
public class ArcadePacManXXL_GameModel extends ArcadePacMan_GameModel {

    static final List<Map<String, String>> COLOR_MAPS = List.of(
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
        //TODO ugly:
        builtinMaps.clear();
        loadBuiltinMaps("maps/masonic_%d.world", 8);
        updateCustomMaps();
    }

    @Override
    protected void populateLevel(WorldMap worldMap) {
        GameWorld world = new GameWorld(worldMap);

        // House can be at non-default position!
        if (!worldMap.hasProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MIN_TILE, formatTile(vec_2i(10, 15)));
        }
        if (!worldMap.hasProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MAX_TILE, formatTile(vec_2i(17, 19)));
        }
        Vector2i minTile = worldMap.getTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
        Vector2i maxTile = worldMap.getTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
        world.createArcadeHouse(minTile.x(), minTile.y(), maxTile.x(), maxTile.y());

        var pac = new Pac();
        pac.setName("Pac-Man");
        pac.setWorld(world);
        pac.reset();

        var ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.clyde() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setWorld(world);
            ghost.setRevivalPosition(world.ghostPosition(ghost.id()));
            ghost.reset();
        });
        ghosts[RED_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST)); // middle house position

        level.setWorld(world);
        level.setPac(pac);
        level.setGhosts(ghosts);
        level.setBonusSymbol(0, computeBonusSymbol());
        level.setBonusSymbol(1, computeBonusSymbol());
    }

    @Override
    public void configureNormalLevel() {
        levelCounterEnabled = true;

        WorldMap worldMap = selectWorldMap(level.number);
        Map<String, String> colorMap;
        if (builtinMaps.contains(worldMap)) {
            colorMap = COLOR_MAPS.get(randomInt(0, COLOR_MAPS.size()));
        } else {
            colorMap = Map.of(
                    "fill",   worldMap.getStringPropertyOrElse(LayerID.TERRAIN, PROPERTY_COLOR_WALL_FILL, "000000"),
                    "stroke", worldMap.getStringPropertyOrElse(LayerID.TERRAIN, PROPERTY_COLOR_WALL_STROKE, "0000ff"),
                    "door",   worldMap.getStringPropertyOrElse(LayerID.TERRAIN, PROPERTY_COLOR_DOOR, "00ffff"),
                    "pellet", worldMap.getStringPropertyOrElse(LayerID.FOOD, PROPERTY_COLOR_FOOD, "ffffff")
            );
        }
        worldMap.setConfigValue("colorMap", colorMap);

        level.setNumFlashes(levelData(level.number).numFlashes());
        level.setIntermissionNumber(ArcadePacMan_GameModel.intermissionNumberAfterLevel(level.number));
        populateLevel(worldMap);
        level.pac().setAutopilot(autopilot);
        setCruiseElroy(0);
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    protected WorldMap selectWorldMap(int levelNumber) {
        return switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS ->
                    levelNumber <= builtinMaps.size()
                            ? new WorldMap(builtinMaps.get(levelNumber - 1))
                            : new WorldMap(builtinMaps.get(randomInt(0, builtinMaps.size())));
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield levelNumber <= maps.size()
                        ? new WorldMap(maps.get(levelNumber - 1))
                        : new WorldMap(maps.get(randomInt(0, maps.size())));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield new WorldMap(maps.get(randomInt(0, maps.size())));
            }
        };
    }

    @Override
    public void configureDemoLevel() {
        configureNormalLevel();
        levelCounterEnabled = false;
        demoLevelSteering = new RuleBasedPacSteering(this);
        setDemoLevelBehavior();
    }

    @Override
    public void activateNextBonus() {
        level.advanceNextBonus();
        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        StaticBonus staticBonus = new StaticBonus(symbol, ArcadePacMan_GameModel.BONUS_VALUE_FACTORS[symbol] * 100);
        level.setBonus(staticBonus);
        // in a non-Arcade style custom map, the bonus position must be taken from the terrain map
        if (level.world().map().hasProperty(LayerID.TERRAIN, PROPERTY_POS_BONUS)) {
            Vector2i bonusTile = level.world().map().getTileProperty(PROPERTY_POS_BONUS, new Vector2i(13, 20));
            staticBonus.actor().setPosition(halfTileRightOf(bonusTile));
        } else {
            staticBonus.actor().setPosition(ArcadePacMan_GameModel.BONUS_POS);
        }
        staticBonus.setEdible(bonusEdibleTicks());
        publishGameEvent(GameEventType.BONUS_ACTIVATED, staticBonus.actor().tile());
    }

}