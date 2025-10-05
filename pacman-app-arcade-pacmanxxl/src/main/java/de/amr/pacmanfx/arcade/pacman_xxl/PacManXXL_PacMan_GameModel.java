/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.*;
import de.amr.pacmanfx.arcade.pacman.actors.*;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_PacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File highScoreFile) {
        super(gameContext, mapSelector, highScoreFile);
        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        demoLevelSteering = new RuleBasedPacSteering(gameContext);
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.getWorldMapCopy(levelNumber);

        Vector2i houseMinTile = worldMap.terrainLayer().getTileProperty(POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            houseMinTile = ARCADE_MAP_HOUSE_MIN_TILE;
            Logger.warn("No house min tile found in map, using {}", houseMinTile);
            worldMap.terrainLayer().propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));
        }
        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        worldMap.terrainLayer().setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap);
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(90);

        final Pac pacMan = new PacMan();
        pacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(pacMan);

        final Blinky blinky = new Blinky();
        final Pinky pinky = new Pinky();
        final Inky inky = new Inky();
        final Clyde clyde = new Clyde();

        blinky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_1_RED)));
        pinky .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_2_PINK)));
        inky  .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_3_CYAN)));
        clyde .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = worldMap.terrainLayer().tiles()
                .filter(tile -> worldMap.terrainLayer().get(tile) == TerrainTile.ONE_WAY_DOWN.$)
                .toList();
        newGameLevel.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOLS_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        newGameLevel.setBonusSymbol(0, symbol);
        newGameLevel.setBonusSymbol(1, symbol);

        levelCounter().setEnabled(true);

        return newGameLevel;
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int randomIndex = randomInt(0, DEMO_LEVEL_NUMBERS.length);
        int levelNumber = DEMO_LEVEL_NUMBERS[randomIndex];
        scoreManager().score().setLevelNumber(levelNumber);
        mapSelector().setSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        final GameLevel demoLevel = createLevel(levelNumber, true);
        demoLevel.pac().setImmune(false);
        demoLevel.pac().setUsingAutopilot(true);
        demoLevel.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter().setEnabled(false);
        huntingTimer().reset();
        gateKeeper.setLevelNumber(levelNumber);
        demoLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));
        setGameLevel(demoLevel);
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public Arcade_LevelData levelData(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            return levelData(1);
        }
        return super.levelData(gameLevel);
    }
}