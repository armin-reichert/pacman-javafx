/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Globals_Core;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals_Core.tile;
import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    protected Arcade_GameModel() {
        actorSpeedControl = new Arcade_ActorSpeedControl();
    }

    @Override
    public void eatPellet(GameContext gameContext, GameLevel level, Vector2i tile) {
        super.eatPellet(gameContext, level, tile);
        level.entities().pac().setRestingTicks(gameContext.rules().restingTicksForPellet());
        checkRedGhostCruiseElroyActivation(level);
    }

    @Override
    public void eatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(gameContext, gameContext.rules().pointsForEnergizer(), level.number());
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());

        final Pac pac = level.entities().pac();
        pac.setRestingTicks(gameContext.rules().restingTicksForEnergizer());
        checkRedGhostCruiseElroyActivation(level);

        level.clearGhostKillChain();

        startPacPowerMode(gameContext, level, pac);
    }

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(Globals_Core.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroy().setBoost(Elroy.Boost.MEDIUM);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroy().setBoost(Elroy.Boost.LARGE);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }

    // Game interface

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber) {
        final GameLevel level = createLevel(gameContext, levelNumber, false);
        level.setCutSceneNumber(gameContext.rules().cutSceneNumberAfterLevel(levelNumber).orElse(0));
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        setLevel(level);
        gameContext.flow().publishGameEvent(new LevelCreatedEvent(gameContext, level));
    }

    @Override
    public void buildDemoLevel(GameContext gameContext) {
        int levelNumber = 1;
        final GameLevel level = createLevel(gameContext, levelNumber, true);
        level.setCutSceneNumber(0);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(levelNumber);
        demoLevelSteering.init();
        levelCounter.setEnabled(true);
        score.setLevelNumber(levelNumber);

        setLevel(level);
        gameContext.flow().publishGameEvent(new LevelCreatedEvent(gameContext, level));
    }

    //TODO remove tick parameter, introduce game state
    @Override
    public void startDemoLevel(GameContext gameContext, long tick) {
        if (tick == 1) {
            buildDemoLevel(gameContext);
        }
        else if (tick == 2) {
            startLevel(gameContext);
        }
        else if (tick == 3) {
            // Now, actor animations are available, show them
            final GameLevel level = optGameLevel().orElseThrow();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.Timing.TICK_RESUME_HUNTING) {
            gameContext.flow().enterState(Arcade_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    public void startLevel(GameContext gameContext) {
        final GameLevel level = optGameLevel().orElseThrow();
        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(level);
        if (level.isDemoLevel()) {
            showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            score().setEnabled(false);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showLevelMessage(level, GameLevelMessageType.READY);
            levelCounter.update(level.number(), level.bonusSymbolCode(0));
            score.setEnabled(true);
            cheats.update(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        gameContext.flow().publishGameEvent(new LevelStartedEvent(gameContext, level));
    }
}