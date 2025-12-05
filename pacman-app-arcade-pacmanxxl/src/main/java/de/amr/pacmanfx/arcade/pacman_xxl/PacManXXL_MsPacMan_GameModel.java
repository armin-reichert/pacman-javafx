/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_HuntingTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.*;
import de.amr.pacmanfx.arcade.pacman.model.actors.Inky;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.Random;

import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_MsPacMan_GameModel(CoinMechanism coinMechanism, MapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, mapSelector, highScoreFile);
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
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

        final int numFlashes = levelData(levelNumber).numFlashes();
        final GameLevel level = new GameLevel(this, levelNumber, worldMap, new ArcadeMsPacMan_HuntingTimer(), numFlashes);
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData(levelNumber).secPacPower());
        level.setPacPowerFadingSeconds(0.5f * numFlashes); //TODO correct?

        final MsPacMan msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        level.setPac(msPacMan);

        final Blinky blinky = ArcadeMsPacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        setGhostStartPosition(blinky, terrain.getTileProperty(POS_GHOST_1_RED));

        final Pinky pinky = ArcadeMsPacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = ArcadeMsPacMan_ActorFactory.createInky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Sue sue = ArcadeMsPacMan_ActorFactory.createSue();
        sue.setHome(house);
        setGhostStartPosition(sue, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setGhosts(blinky, pinky, inky, sue);

        level.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        level.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[new Random().nextInt(levelNumbers.length)];
        mapSelector().setSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        final GameLevel level = createLevel(levelNumber, true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter().setEnabled(false);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?

        gameLevelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }
}