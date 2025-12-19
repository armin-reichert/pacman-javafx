/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_HuntingTimer;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.model.actors.*;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_PacMan_GameModel(CoinMechanism coinMechanism, MapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, mapSelector, highScoreFile);
        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        demoLevelSteering = new RuleBasedPacSteering();
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final LevelData levelData = levelData(levelNumber);
        final WorldMap worldMap = mapSelector.selectWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        Vector2i houseMinTile = terrain.getTileProperty(POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            houseMinTile = ARCADE_MAP_HOUSE_MIN_TILE;
            Logger.warn("No house min tile found in map, using {}", houseMinTile);
            terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));
        }
        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, new ArcadePacMan_HuntingTimer(), levelData.numFlashes());
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        final Pac pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAutomaticSteering(automaticSteering);
        level.setPac(pacMan);

        final Blinky blinky = ArcadePacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        setGhostStartPosition(blinky, terrain.getTileProperty(POS_GHOST_1_RED));

        final Pinky pinky = ArcadePacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = ArcadePacMan_ActorFactory.createInky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Clyde clyde = ArcadePacMan_ActorFactory.createClyde();
        clyde.setHome(house);
        setGhostStartPosition(clyde, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        level.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOL_CODES_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        level.setBonusSymbol(0, symbol);
        level.setBonusSymbol(1, symbol);

        setLevelCounterEnabled(true);

        return level;
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int randomIndex = randomInt(0, DEMO_LEVEL_NUMBERS.length);
        int levelNumber = DEMO_LEVEL_NUMBERS[randomIndex];
        score().setLevelNumber(levelNumber);
        mapSelector().setSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);

        final GameLevel level = createLevel(levelNumber, true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        setLevelCounterEnabled(false);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }
}