/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_HuntingTimer;
import de.amr.pacmanfx.arcade.ms_pacman.actors.*;
import de.amr.pacmanfx.arcade.pacman.Arcade_LevelData;
import de.amr.pacmanfx.arcade.pacman.actors.Inky;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.ArcadeHouse;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
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

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap, new ArcadeMsPacMan_HuntingTimer());
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(150);

        final MsPacMan msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(msPacMan);

        final Blinky blinky = ArcadeMsPacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        blinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_1_RED)));

        final Pinky pinky = ArcadeMsPacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        pinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_2_PINK)));

        final Inky inky = ArcadeMsPacMan_ActorFactory.createInky();
        inky.setHome(house);
        inky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_3_CYAN)));

        final Sue sue = ArcadeMsPacMan_ActorFactory.createSue();
        sue.setHome(house);
        sue.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, sue);

        newGameLevel.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        newGameLevel.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter().setEnabled(levelNumber < 8);

        return newGameLevel;
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[new Random().nextInt(levelNumbers.length)];
        mapSelector().setSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        final GameLevel demoLevel = createLevel(levelNumber, true);
        demoLevel.pac().setImmune(false);
        demoLevel.pac().setUsingAutopilot(true);
        demoLevel.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter().setEnabled(false);
        scoreManager().score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        demoLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        setGameLevel(demoLevel);
        playStateMachine().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public Arcade_LevelData levelData(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            return levelData(1);
        }
        return super.levelData(gameLevel);
    }
}