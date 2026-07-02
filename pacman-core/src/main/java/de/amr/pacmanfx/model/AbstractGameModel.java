/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.level.LevelCounter;
import de.amr.pacmanfx.model.lives.PacManLives;
import de.amr.pacmanfx.model.lives.PacManLivesImpl;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface providing the core simulation logic shared by all Pac-Man variants.
 */
public abstract class AbstractGameModel implements GameModel {

    // Common properties

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    // Common data

    protected final Score score;

    protected ActorSpeedControl actorSpeedControl;

    protected GateKeeper gateKeeper;

    protected HUDState hud;

    protected PersistentScore highScore;

    protected GameLevel currentLevel;

    protected PacManLives lives;

    protected LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected Steering automaticSteering;

    protected Steering demoLevelSteering;

    // Constructor

    protected AbstractGameModel() {
        score = new Score();
        lives = new PacManLivesImpl();
        hud = new HUDState();
    }

    public void setMapSelector(WorldMapSelector mapSelector) {
        this.mapSelector =  requireNonNull(mapSelector);
    }

    /* -------------------------------------------------------------------------
     * GameModel interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public PacManLives lives() {
        return lives;
    }

    @Override
    public ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public HUDState hudState() {
        return hud;
    }

    @Override
    public LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public PersistentScore highScore() {
        return highScore;
    }

    @Override
    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    // Lifecycle

    @Override
    public void init() {
        mapSelector.loadMapPrototypes();
        lives.setInitialCount(3);
        hud.hide();
        resetForNewGame();
    }

    @Override
    public void resetForNewGame() {
        lives.setCount(lives.initialCount());
        score.reset();
        if (highScore != null) {
            try {
                highScore.load();
                highScore.setEnabled(true);
            } catch (IOException x) {
                Logger.error(x, "Error loading high-score file {}", highScore.file().getAbsolutePath());
            }
        } else {
            Logger.error("No high-score file has been assigned");
        }
        gateKeeper.reset();
        levelCounter.clear();

        setLevel(null);
        setPlaying(false);
    }

    @Override
    public boolean canStartNewGame(GameContext gameContext) {
        return !gameContext.coinMechanism().isEmpty();
    }

    @Override
    public boolean canContinueOnGameOver() {
        return false;
    }

    @Override
    public void onGameOver(GameContext gameContext, GameLevel level) {
        updateHighScore();
        setPlaying(false);
        showLevelMessage(level, GameLevelMessageType.GAME_OVER);
    }

    @Override
    public boolean isPlaying() {
        return playing.get();
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public abstract void activateNextBonus(GameContext gameContext, GameLevel level);

    // Level related

    @Override
    public abstract GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel);

    @Override
    public abstract void buildNormalLevel(GameContext gameContext, int levelNumber);

    @Override
    public abstract void buildDemoLevel(GameContext gameContext);

    @Override
    public void setLevel(GameLevel level) {
        currentLevel = level;
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(currentLevel);
    }

    @Override
    public abstract void startLevel(GameContext gameContext);

    @Override
    public void prepareLevelForPlaying(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = terrain.optHouse().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.reset(); // initially invisible!
        pac.setPosition(terrain.pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetToIndefiniteDuration();
        pac.animations().resetSelected();

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            final Direction direction = house.ghostStartDirection(ghost.personality());
            ghost.setMoveDir(direction);
            ghost.setWishDir(direction);
            ghost.setState(GhostState.LOCKED);
            ghost.animations().resetSelected();
        });

        level.heartbeat().setStartState(Pulse.State.ON); // Energizers are visible when ON
        level.heartbeat().reset();
    }

    @Override
    public void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    @Override
    public boolean isDemoLevelRunning() {
        return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
    }

    @Override
    public void startNextLevel(GameContext gameContext) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (level.number() < gameContext.rules().lastLevelNumber()) {
            buildNormalLevel(gameContext, level.number() + 1);
            startLevel(gameContext);
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", gameContext.rules().lastLevelNumber());
        }
    }

    @Override
    public void onLevelCompleted(GameLevel level) {
        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_FULL);
        pac.setSpeed(0);
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().stopSelected();
            //TODO check in emulator if ghost animation is reset to normal
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.setSpeed(0);
        });
        level.optBonus().ifPresent(Bonus::setInactive);
    }

    // Actor related

    @Override
    public void eatPellet(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        scorePoints(gameContext, gameContext.rules().pointsForPellet(), level.number());
        if (gateKeeper != null) {
            gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        }
    }

    @Override
    public void eatBonus(GameContext gameContext, GameLevel level, Bonus bonus) {
        scorePoints(gameContext, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(gameContext.rules().eatenBonusDisplaySeconds());
        gameContext.flow().publishGameEvent(new BonusEatenEvent(gameContext, bonus));
    }

    @Override
    public void onEatGhost(GameContext gameContext, GameLevel level, Ghost eatenGhost) {
        final int killedBefore = level.ghostKillChainSize();
        final int points = gameContext.rules().pointsForGhost(killedBefore);

        scorePoints(gameContext, points, level.number());
        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        eatenGhost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        eatenGhost.animations().selectAndSetFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());

        gameContext.flow().publishGameEvent(new GhostEatenEvent(gameContext, eatenGhost));
    }

    public void startPacPowerMode(GameContext gameContext, GameLevel level, Pac pac) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            gameContext.flow().publishGameEvent(new PacGetsPowerEvent(gameContext, pac));
        }
    }

    public void updatePacPowerMode(GameContext gameContext, GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                gameContext.flow().publishGameEvent(new PacPowerFadesEvent(gameContext, pac));
            } else if (pac.powerTimer().hasExpired()) {
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.clearGhostKillChain();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                gameContext.flow().publishGameEvent(new PacLostPowerEvent(gameContext, pac));
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Score management
     * ---------------------------------------------------------------------- */

    public void setHighScoreFile(File file) {
        highScore = new PersistentScore(file);
    }

    protected void scorePoints(GameContext gameContext, int points, int levelNumber) {
        if (!score.isEnabled()) {
            return;
        }
        final int oldScore = score.points();
        final int newScore = oldScore + points;

        if (gameContext.rules().isExtraLifeAwarded(oldScore, newScore)) {
            lives.add(1);
            gameContext.flow().publishGameEvent(new SpecialScoreEvent(gameContext, newScore));
        }

        if (highScore != null && highScore.isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }

        score.setPoints(newScore);
    }

    protected void updateHighScore() {
        if (highScore == null) {
            Logger.error("Cannot update high-score, no high-score file has been assigned");
            return;
        }
        final PersistentScore savedHighScore = new PersistentScore(highScore.file());
        try {
            savedHighScore.load();
            if (highScore.points() > savedHighScore.points()) {
                highScore.save();
            }
        } catch (IOException x) {
            Logger.error(x, "Could not update high-score");
        }
    }
}
