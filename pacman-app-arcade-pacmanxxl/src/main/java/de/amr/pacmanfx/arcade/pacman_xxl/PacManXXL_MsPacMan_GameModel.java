/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.Random;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_MsPacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File highScoreFile) {
        super(gameContext, mapSelector, highScoreFile);
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public void createLevel(int levelNumber) {
        final WorldMap worldMap = mapSelector.getWorldMap(levelNumber);

        Vector2i houseMinTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            houseMinTile = ARCADE_MAP_HOUSE_MIN_TILE;
            Logger.warn("No house min tile found in map, using {}", houseMinTile);
            worldMap.terrainLayer().propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));
        }
        final ArcadeHouse house = new ArcadeHouse(houseMinTile);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap, house);
        newGameLevel.setGameOverStateTicks(150);

        final MsPacMan msPacMan = new MsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(msPacMan);

        final Blinky blinky = new Blinky();
        final Pinky pinky = new Pinky();
        final Inky inky = new Inky();
        final Sue sue = new Sue();

        blinky.setStartPosition(halfTileRightOf(worldMap.getTerrainTileProperty(POS_GHOST_1_RED)));
        pinky .setStartPosition(halfTileRightOf(worldMap.getTerrainTileProperty(POS_GHOST_2_PINK)));
        inky  .setStartPosition(halfTileRightOf(worldMap.getTerrainTileProperty(POS_GHOST_3_CYAN)));
        sue   .setStartPosition(halfTileRightOf(worldMap.getTerrainTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, sue);

        newGameLevel.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        newGameLevel.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        setGameLevel(newGameLevel);

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        setLevelCounterEnabled(levelNumber < 8);
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[new Random().nextInt(levelNumbers.length)];
        mapSelector().setSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        gameLevel().setDemoLevel(true);
        gameLevel().pac().setImmune(false);
        gameLevel().pac().setUsingAutopilot(true);
        gameLevel().pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        setLevelCounterEnabled(false);
        huntingTimer().reset();
        scoreManager().score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        gameLevel().optHouse().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    protected ArcadeLevelData levelData(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            return levelData(1);
        }
        return super.levelData(gameLevel);
    }
}