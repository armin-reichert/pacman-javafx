/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_HuntingTimer;
import de.amr.pacmanfx.arcade.pacman.Arcade_LevelData;
import de.amr.pacmanfx.arcade.pacman.actors.*;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.ArcadeHouse;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
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
        final WorldMap worldMap = mapSelector.provideWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        Vector2i houseMinTile = terrain.getTileProperty(POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            houseMinTile = ARCADE_MAP_HOUSE_MIN_TILE;
            Logger.warn("No house min tile found in map, using {}", houseMinTile);
            terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));
        }
        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap, new ArcadePacMan_HuntingTimer());
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(90);

        final Pac pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(pacMan);

        final Blinky blinky = ArcadePacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        blinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_1_RED)));

        final Pinky pinky = ArcadePacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        pinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_2_PINK)));

        final Inky inky = ArcadePacMan_ActorFactory.createInky();
        inky.setHome(house);
        inky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_3_CYAN)));

        final Clyde clyde = ArcadePacMan_ActorFactory.createClyde();
        clyde.setHome(house);
        clyde.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = terrain.tiles()
                .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
                .toList();
        newGameLevel.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOL_CODES_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
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
        gateKeeper.setLevelNumber(levelNumber);
        demoLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));
        setGameLevel(demoLevel);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }

    @Override
    public Arcade_LevelData levelData(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            return levelData(1);
        }
        return super.levelData(gameLevel);
    }
}