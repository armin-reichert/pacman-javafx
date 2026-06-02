/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.flow.GameControlFlow;
import de.amr.pacmanfx.flow.StateMachineGameControlFlow;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.io.IOException;

import static de.amr.basics.math.Vector2i.vec2_int;
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
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = vec2_int(10, 15);

    protected GameControlFlow gameFlow;
    protected CoinMechanism coinMechanism;

    protected HeadsUpDisplay hud;
    protected GateKeeper gateKeeper;
    protected Steering automaticSteering;
    protected Steering demoLevelSteering;
    protected Arcade_ActorSpeedControl actorSpeedControl;

    protected Arcade_GameModel(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);
        hud = new HeadsUpDisplay(coinMechanism);

        gameFlow = new StateMachineGameControlFlow("Arcade Pac-Man Games Control Flow", this);
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }

        actorSpeedControl = new Arcade_ActorSpeedControl();
        setCollisionStrategy(CollisionStrategy.SAME_TILE);
    }

    @Override
    public GameControlFlow flow() {
        return gameFlow;
    }

    @Override
    public ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public void eatPellet(GameLevel level, Vector2i tile) {
        super.eatPellet(level, tile);
        level.entities().pac().setRestingTicks(rules().restingTicksForPellet());
        checkRedGhostCruiseElroyActivation(level);
    }

    @Override
    public void eatEnergizer(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(rules().pointsForEnergizer(), level.number());
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());

        final Pac pac = level.entities().pac();
        pac.setRestingTicks(rules().restingTicksForEnergizer());
        checkRedGhostCruiseElroyActivation(level);

        level.killedGhostsForCurrentEnergizer().clear();

        if (!isLevelCompleted()) {
            empowerPac(pac, level);
        }
    }

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(Globals.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroyState().setMode(ElroyState.Mode.ONE);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroyState().setMode(ElroyState.Mode.TWO);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }

    protected void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    // Game interface

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public void init() {
        lives().setInitialCount(3);
        prepareNewGame();
    }

    @Override
    public void prepareNewGame() {
        score.reset();
        try {
            highScore.load();
            highScore.setEnabled(true);
        } catch (IOException x) {
            Logger.error(x, "Error loading high-score file {}", highScore.file().getAbsolutePath());
        }
        gateKeeper.reset();
        levelProperty().set(null);
        lives().setCount(lives().initialCount());
        levelCounter().clear();
        setPlayingLevel(false);
    }

    @Override
    public void startNewGame(long tick) {
        if (tick == 1) {
            prepareNewGame();
            buildNormalLevel(1);
            flow().publishGameEvent(new GameStartedEvent(this));
        }
        else if (tick == 2) {
            startLevel();
        }
        else if (tick == Arcade_GameState.TICK_NEW_GAME_SHOW_GUYS) {
            final GameLevel level = optGameLevel().orElseThrow();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.TICK_NEW_GAME_START_HUNTING) {
            setPlayingLevel(true);
            flow().enterState(Arcade_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    public void continuePlayingLevel(long tick) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (tick == 1) {
            makeReadyForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            showLevelMessage(level, GameLevelMessageType.READY);
        }
        else if (tick == 60) {
            flow().publishGameEvent(new GameContinuedEvent(this));
        }
        else if (tick == Arcade_GameState.TICK_RESUME_HUNTING) {
            flow().enterState(Arcade_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    public boolean canStartNewGame() { return !coinMechanism.isEmpty(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    public void doPacManDying(Pac pac, long tick) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (tick == 1) {
            gateKeeper.resetCounterAndSetEnabled(true);
            level.huntingTimer().stop();
            pac.animations().stopSelected();
            pac.powerTimer().stop();
            pac.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero.");
            pac.setSpeed(0);
            pac.setDead(true);
            level.entities().ghosts().forEach(ghost -> ghost.onPacKilled(level));
            flow().publishGameEvent(new StopAllSoundsEvent(this));
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.entities().ghosts().forEach(Ghost::hide);
            pac.animations().select(ArcadePacMan_AnimationID.PAC_DYING);
            pac.animations().resetSelected();
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_START_ANIMATION) {
            pac.animations().playSelected();
            flow().publishGameEvent(new PacDyingEvent(this, pac));
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_PAC_DEAD) {
            flow().publishGameEvent(new PacDeadEvent(this, pac));
        }
        else {
            level.blinking().doTick();
            pac.update(level);
        }
    }

    @Override
    public void onEatGhost(GameLevel level, Ghost eatenGhost) {
        final int killedBefore = level.killedGhostsForCurrentEnergizer().size();
        final int points = rules().pointsForGhost(killedBefore);

        scorePoints(points, level.number());
        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        eatenGhost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        eatenGhost.animations().selectAtFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        level.killedGhostsForCurrentEnergizer().add(eatenGhost);
        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());
        flow().publishGameEvent(new GhostEatenEvent(this, eatenGhost));
    }

    @Override
    public void onGameOver() {
        if (!coinMechanism.isEmpty()) {
            coinMechanism.consumeCoin(); //TODO not sure if coin should be consumed after game is over
        }
        setPlayingLevel(false);
        final GameLevel level = optGameLevel().orElseThrow();
        showLevelMessage(level, GameLevelMessageType.GAME_OVER);
        try {
            updateHighScore();
        } catch (IOException x) {
            Logger.error(x, "Error updating high-score file {}", highScore.file().getAbsolutePath());
        }
        Logger.info("Game ended with level number {}", level.number());
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        level.setCutSceneNumber(rules().cutSceneNumberAfterLevel(levelNumber).orElse(0));
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        levelProperty().set(level);
        flow().publishGameEvent(new LevelCreatedEvent(this, level));
    }

    @Override
    public void buildDemoLevel() {
        int levelNumber = 1;
        final GameLevel level = createLevel(levelNumber, true);
        level.setCutSceneNumber(0);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(levelNumber);
        demoLevelSteering.init();
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);

        levelProperty().set(level);

        flow().publishGameEvent(new LevelCreatedEvent(this, level));
    }

    @Override
    public void startDemoLevel(long tick) {
        if (tick == 1) {
            buildDemoLevel();
        }
        else if (tick == 2) {
            startLevel();
        }
        else if (tick == 3) {
            // Now, actor animations are available, show them
            final GameLevel level = optGameLevel().orElseThrow();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.TICK_RESUME_HUNTING) {
            flow().enterState(Arcade_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    public void startLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        level.recordStartTime(System.currentTimeMillis());
        makeReadyForPlaying(level);
        if (level.isDemoLevel()) {
            showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            score().setEnabled(false);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showLevelMessage(level, GameLevelMessageType.READY);
            levelCounter().update(level.number(), level.bonusSymbolCode(0));
            score().setEnabled(true);
            updateCheats(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        flow().publishGameEvent(new LevelStartedEvent(this, level));
    }

    @Override
    public void startNextLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        buildNormalLevel(level.number() + 1);
        startLevel();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }
}