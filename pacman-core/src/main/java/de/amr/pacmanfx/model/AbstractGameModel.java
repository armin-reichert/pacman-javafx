/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.flow.GameFlow;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals.halfTileRightOf;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface providing the core simulation logic shared by all Pac-Man variants.
 */
public abstract class AbstractGameModel implements GameModel {

    // Common properties

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    // Common data

    protected CoinMechanism coinMechanism;

    protected final Score score;

    protected GameFlow flow;

    protected ActorSpeedControl actorSpeedControl;

    protected GateKeeper gateKeeper;

    protected HeadsUpDisplay hud;

    protected PersistentScore highScore;

    protected GameRules rules;

    protected GameCheats cheats;

    protected PacManLives lives;

    protected LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected Steering automaticSteering;

    protected Steering demoLevelSteering;

    // Constructor

    protected AbstractGameModel() {
        coinMechanism = CoinMechanism.OUT_OF_SERVICE;
        score = new Score();
        lives = new PacManLivesImpl();
        hud = new HeadsUpDisplay();
        cheats = new DefaultCheatsImpl();

        score.pointsProperty().addListener((_, oldScore, newScore)
            -> handleScoreChange(oldScore.intValue(), newScore.intValue()));
    }

    protected void handleScoreChange(int oldScore, int newScore) {
        if (rules.isExtraLifeAwarded(oldScore, newScore)) {
            lives.add(1);
            flow.publishGameEvent(new SpecialScoreEvent(flow.context(), newScore));
        }
    }

    /* -------------------------------------------------------------------------
     * GameModel interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public GameFlow flow() {
        return flow;
    }

    public GameRules rules() {
        return rules;
    }

    @Override
    public GameCheats cheats() {
        return cheats;
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

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
    public HeadsUpDisplay hud() {
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
        prepareNewGame();
    }

    @Override
    public void prepareNewGame() {
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

        level.set(null);
        setPlaying(false);
    }

    @Override
    public boolean canStartNewGame() {
        return !coinMechanism.isEmpty();
    }

    @Override
    public boolean canContinueOnGameOver() {
        return false;
    }

    @Override
    public void onGameOver(GameLevel level) {
        if (!coinMechanism.isEmpty()) {
            coinMechanism.consumeCoin(); //TODO not sure if coin should be consumed after game is over
        }
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
    public abstract void activateNextBonus(GameLevel level);

    // Level related

    @Override
    public abstract GameLevel createLevel(int levelNumber, boolean demoLevel);

    @Override
    public abstract void buildNormalLevel(int levelNumber);

    @Override
    public abstract void buildDemoLevel();

    @Override
    public void setLevel(GameLevel level) {
        this.level.set(level);
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(level.get());
    }

    @Override
    public abstract void startLevel();

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

    //TODO remove tick, introduce new game state
    @Override
    public abstract void startDemoLevel(long tick);

    @Override
    public boolean isDemoLevelRunning() {
        return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
    }

    @Override
    public void startNextLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        if (level.number() < rules.lastLevelNumber()) {
            buildNormalLevel(level.number() + 1);
            startLevel();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", rules.lastLevelNumber());
        }
    }

    @Override
    public void onStartLevelPlaying(GameLevel level) {
        // Clear "READY!" message. "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        level.entities().pac().animations().playSelected();
        level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());

        final HuntingTimer huntingTimer = level.huntingTimer();
        huntingTimer.startFirstPhase(rules, level.number());
        flow.publishGameEvent(new HuntingPhaseStartedEvent(
            flow.context(),
            huntingTimer.phaseIndex(),
            huntingTimer.currentHuntingPhase())
        );
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
    public void eatPellet(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        scorePoints(rules.pointsForPellet(), level.number());
        if (gateKeeper != null) {
            gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        }
    }

    @Override
    public void onEatGhost(GameLevel level, Ghost eatenGhost) {
        final int killedBefore = level.killedGhostsForCurrentEnergizer().size();
        final int points = rules.pointsForGhost(killedBefore);

        scorePoints(points, level.number());
        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        eatenGhost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        eatenGhost.animations().selectAtFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        level.killedGhostsForCurrentEnergizer().add(eatenGhost);
        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());

        flow.publishGameEvent(new GhostEatenEvent(flow().context(), eatenGhost));
    }

    public void startPacPowerMode(GameLevel level, Pac pac) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            flow.publishGameEvent(new PacGetsPowerEvent(flow.context(), pac));
        }
    }

    public void updatePacPowerMode(GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                flow.publishGameEvent(new PacPowerFadesEvent(flow.context(), pac));
            } else if (pac.powerTimer().hasExpired()) {
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.killedGhostsForCurrentEnergizer().clear();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                flow.publishGameEvent(new PacLostPowerEvent(flow.context(), pac));
            }
        }
    }



    /* -------------------------------------------------------------------------
     * Cheating
     * ---------------------------------------------------------------------- */

    public class DefaultCheatsImpl extends GameCheats {

        @Override
        public void update(GameLevel level) {
            if (level.isDemoLevel() || !level.game().isPlaying()) {
                return;
            }
            final Pac pac = level.entities().pac();
            pac.immuneProperty().set(isPacImmune());
            pac.usingAutopilotProperty().set(isPacUsingAutopilot());
            if (isPacImmune() || isPacUsingAutopilot()) {
                notifyCheatUsed();
            }
        }

        public void handleCheatDetected() {
            if (highScore != null) {
                highScore.setEnabled(false);
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Utility methods
     * ---------------------------------------------------------------------- */

    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
        }
    }

    /* -------------------------------------------------------------------------
     * Score management
     * ---------------------------------------------------------------------- */

    public void createHighScore(File highScoreFile) {
        requireNonNull(highScoreFile);
        //TODO more checks
        highScore = new PersistentScore(highScoreFile);
    }

    protected void scorePoints(int points, int levelNumber) {
        if (!score.isEnabled()) {
            return;
        }
        final int oldScore = score.points();
        final int newScore = oldScore + points;

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

    public void eatBonus(GameLevel level, Bonus bonus) {
        scorePoints(bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(rules.eatenBonusDisplaySeconds());
        flow.publishGameEvent(new BonusEatenEvent(flow.context(), bonus));
    }
}
