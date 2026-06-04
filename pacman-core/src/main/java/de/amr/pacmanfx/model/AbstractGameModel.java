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
import de.amr.pacmanfx.flow.GameControlFlow;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface providing the core simulation logic shared by all Pac-Man variants.
 */
public abstract class AbstractGameModel implements GameModel {

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    // common model data

    protected final SimulationStep simStep = new SimulationStep();

    protected CoinMechanism coinMechanism = CoinMechanism.OUT_OF_SERVICE;

    protected final Score score = new Score();

    protected GameControlFlow flow;

    protected ActorSpeedControl actorSpeedControl;

    protected GateKeeper gateKeeper;

    protected HeadsUpDisplay hud;

    protected PersistentScore highScore;

    protected final PacManLives lives = new PacManLivesImpl();

    protected GameRules rules;

    protected LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected Steering automaticSteering;

    protected Steering demoLevelSteering;

    private CollisionStrategy collisionStrategy = DEFAULT_COLLISION_STRATEGY;

    // Constructor

    protected AbstractGameModel() {
        hud = new HeadsUpDisplay();

        score.pointsProperty().addListener((_, oldScore, newScore)
            -> handleScoreChange(oldScore.intValue(), newScore.intValue()));

        cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                handleCheatDetected();
            }
        });
    }

    /* -------------------------------------------------------------------------
     * Game interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public GameControlFlow flow() {
        return flow;
    }

    public GameRules rules() {
        return rules;
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
    public SimulationStep simulationStep() {
        return simStep;
    }

    @Override
    public void init() {
        mapSelector.loadMapPrototypes();
        lives().setInitialCount(3);
        hud.all(false);
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
        levelCounter.clear();
        setPlaying(false);
    }

    @Override
    public boolean canStartNewGame() { return !coinMechanism.isEmpty(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

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

    public abstract void activateNextBonus(GameLevel level);

    // Level related

    public abstract GameLevel createLevel(int levelNumber, boolean demoLevel);

    public abstract void buildNormalLevel(int levelNumber);

    public abstract void buildDemoLevel();

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(level.get());
    }

    public abstract void startLevel();

    @Override
    public void makeReadyForPlaying(GameLevel level) {
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

    public abstract void startDemoLevel(long tick);

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
        huntingTimer.startFirstPhase(rules(), level.number());
        flow().publishGameEvent(new HuntingPhaseStartedEvent(this, huntingTimer.phaseIndex(), huntingTimer.currentHuntingPhase()));
    }

    @Override
    public void doLevelPlaying(GameLevel level) {
        doHuntingStep(level);
        if (gateKeeper() != null) {
            gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        updateCheats(level);
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
    public CollisionStrategy collisionStrategy() {
        return collisionStrategy;
    }

    @Override
    public void setCollisionStrategy(CollisionStrategy strategy) {
        this.collisionStrategy = requireNonNull(strategy);
    }

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
    public boolean hasPacManBeenKilled() {
        return simStep.pacKiller != null;
    }

    @Override
    public boolean hasGhostBeenKilled() {
        return !simStep.ghostsKilled.isEmpty();
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

        flow().publishGameEvent(new GhostEatenEvent(this, eatenGhost));
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

    protected void handleScoreChange(int oldScore, int newScore) {
        if (rules().isExtraLifeAwarded(oldScore, newScore)) {
            simStep.extraLifeWon = true;
            simStep.extraLifeScore = newScore;
        }
        if (simStep.extraLifeWon) {
            lives().add(1);
            flow().publishGameEvent(new SpecialScoreEvent(this, newScore));
        }
    }

    /* -------------------------------------------------------------------------
     * Cheating
     * ---------------------------------------------------------------------- */

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final BooleanProperty pacImmune = new SimpleBooleanProperty(false);

    private final BooleanProperty pacUsingAutopilot = new SimpleBooleanProperty(false);

    @Override
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    @Override
    public BooleanProperty pacImmuneProperty() {
        return pacImmune;
    }

    @Override
    public boolean isPacImmune() {
        return pacImmuneProperty().get();
    }

    @Override
    public boolean isPacUsingAutopilot() {
        return pacUsingAutopilotProperty().get();
    }

    @Override
    public BooleanProperty pacUsingAutopilotProperty() {
        return pacUsingAutopilot;
    }

    protected void handleCheatDetected() {
        highScore.setEnabled(false);
    }

    /* -------------------------------------------------------------------------
     * Main simulation step
     * ---------------------------------------------------------------------- */

    protected void doHuntingStep(GameLevel level) {
        level.heartbeat().triggerPulse();

        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);

        boolean quitHunting;
        if (isCollisionDoubleChecked()) {
            quitHunting = evalCollisions(level, pac, ghosts, bonus);
            if (!quitHunting) {
                level.entities().forEach(e -> e.update(level));
                quitHunting = evalCollisions(level, pac, ghosts, bonus);
            }
        } else {
            level.entities().forEach(e -> e.update(level));
            quitHunting = evalCollisions(level, pac, ghosts, bonus);
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped!");
            return;
        }

        checkFoodFound(level, pac);
        checkBonusFound(level);

        if (!rules.isLevelCompleted(level)) {
            updatePacPower(level, pac);
            level.huntingTimer().update(rules(), level.number());
        }
    }

    private boolean evalCollisions(GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        detectCollisions(level, pac, ghosts, bonus);
        if (!simStep.ghostsCollidingWithPac.isEmpty()) {
            // Is Pac getting killed after the collision with a ghost?
            // He might stay alive if immune or in level's safe phase!
            checkPacKilled(level, pac);
            if (hasPacManBeenKilled()) {
                return true;
            }
            else {
                // Frightened ghosts get killed when colliding with Pac
                simStep.ghostsCollidingWithPac.stream()
                    .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                    .forEach(simStep.ghostsKilled::add);
                // More than one ghost might have been killed in this step
                simStep.ghostsKilled.forEach(ghost -> onEatGhost(level, ghost));
                if (hasGhostBeenKilled()) {
                    return true;
                }
            }

            // If collision happened while teleporting (horizontally), move collided actors into visible world
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
                if (pac.moveDir() == Direction.LEFT) {
                    pac.setX(hPortal.rightBorderEntryTile().x() * TS + HTS);
                } else if (pac.moveDir() == Direction.RIGHT) {
                    pac.setX(hPortal.leftBorderEntryTile().x() * TS - HTS);
                }
                // Not sure if colliding ghosts should also be moved back to light
                //simStep.ghostsCollidingWithPac.forEach(ghost -> ghost.setX(pac.x()));
                Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
            });
        }

        return false;
    }

    /**
     * Checks whether Pac-Man has entered a tile containing food (pellet or energizer)
     * and triggers the corresponding variant-specific behavior.
     *
     * @param level the current level
     * @param pac   Pac-Man
     */
    private void checkFoodFound(GameLevel level, Pac pac) {
        if (simStep.foodTile == null) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().markFoodEatenAt(simStep.foodTile);
            pac.endStarving();
            if (simStep.energizerFound) {
                eatEnergizer(level, simStep.foodTile);
            } else {
                eatPellet(level, simStep.foodTile);
            }
            if (rules().isBonusAwarded(level)) {
                activateNextBonus(level);
                simStep.bonusIndex = level.currentBonusIndex();
            }
            flow().publishGameEvent(new PacEatsFoodEvent(this, pac, simStep.energizerFound, false));
        }
    }

    protected void empowerPac(Pac pac, GameLevel level) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            simStep.pacGotPower = true;
            flow().publishGameEvent(new PacGetsPowerEvent(this, pac));
        }
    }

    /**
     * Checks whether Pac-Man has collided with an edible bonus item and, if so, triggers
     * the variant-specific bonus handling.
     *
     * @param level the current level
     */
    private void checkBonusFound(GameLevel level) {
        if (simStep.edibleBonus != null) {
            eatBonus(level, simStep.edibleBonus);
        }
    }

    // Collision behavior is controlled by the current collision strategy.
    // The original Arcade games use tile-based collision which can lead to missed collisions
    // by passing through!
    private void detectCollisions(GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {

        // Ghosts colliding with Pac?
        simStep.ghostsCollidingWithPac.clear();
        ghosts.stream().filter(ghost -> collisionStrategy().collide(pac, ghost))
            .forEach(simStep.ghostsCollidingWithPac::add);

        simStep.edibleBonus = null;
        if (bonus != null && bonus.state() == BonusState.EDIBLE && collisionStrategy().collide(pac, bonus)) {
            simStep.edibleBonus = bonus;
        }

        final Vector2i pacTile = pac.computeTile();
        if (level.worldMap().foodLayer().hasFoodAtTile(pacTile)) {
            simStep.foodTile = pacTile;
            simStep.energizerFound = level.worldMap().foodLayer().isEnergizerTile(pacTile);
        }
    }

    /**
     * Checks if Pac-Man gets killed by a collision with an attacking ghost.
     *
     * <p>In attract mode (demo level), there is a time interval at the beginning when Pac-Man is safe.
     * This is to avoid having Pac-Man getting killed too early in demo mode.
     * In contrast to the original Arcade games, the demo mode is not fixed but uses random ghost moves so it
     * cannot be predicted how long the demo mode runs.</p>
     *
     * <p>In normal mode, Pac-Man can be made immune against ghost attacks using a cheat command.
     * In this case, Pac-Man is safe against ghost attacks too.</p>
     *
     * @param level the game level
     * @param pac   the Pac
     */
    protected void checkPacKilled(GameLevel level, Pac pac) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(level) || !demoLevel && pac.isImmune()) {
            return;
        }
        simStep.pacKiller = simStep.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null);
    }

    /**
     * Updates Pac-Man's power mode. Power starts fading after some time. When this happens, the ghosts start flashing
     * and when the power timer expires, they take their normal color again and continue chasing Pac-Man.
     *
     * @param level the game level
     * @param pac the Pac-Man
     */
    protected void updatePacPower(GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                simStep.pacStartsLosingPower = true;
                flow().publishGameEvent(new PacPowerFadesEvent(this, pac));
            } else if (pac.powerTimer().hasExpired()) {
                simStep.pacLostPower = true;
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.killedGhostsForCurrentEnergizer().clear();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                flow().publishGameEvent(new PacLostPowerEvent(this, pac));
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Cheat management
     * ---------------------------------------------------------------------- */

    public void clearCheats() {
        cheatUsed.set(false);
        pacImmune.set(false);
        pacUsingAutopilot.set(false);
    }

    public void updateCheats(GameLevel level) {
        if (level.isDemoLevel() || !level.game().isPlaying()) {
            return;
        }
        final Pac pac = level.entities().pac();
        pac.immuneProperty().set(isPacImmune());
        pac.usingAutopilotProperty().set(isPacUsingAutopilot());
        if (isPacImmune() || isPacUsingAutopilot()) {
            cheatUsed.set(true);
        }
    }

    /* -------------------------------------------------------------------------
     * Score management
     * ---------------------------------------------------------------------- */

    public void setHighScoreFile(File highScoreFile) {
        requireNonNull(highScoreFile);
        highScore = new PersistentScore(highScoreFile);
    }

    protected void scorePoints(int points, int levelNumber) {
        if (!score.isEnabled()) {
            return;
        }
        final int oldScore = score.points();
        final int newScore = oldScore + points;
        if (highScore.isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    protected void updateHighScore() {
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







    /**
     * @return property controlling whether collisions are double-checked each tick
     */
    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    /**
     * @return {@code true} if collisions are double-checked each tick
     */
    public Boolean isCollisionDoubleChecked() {
        return collisionDoubleCheckedProperty().get();
    }

    /**
     * Enables or disables double collision checking.
     *
     * @param doubleChecked {@code true} to enable double-checking
     */
    public void setCollisionDoubleChecked(boolean doubleChecked) {
        collisionDoubleCheckedProperty().set(doubleChecked);
    }

    /**
     * @return the level property
     */
    public ObjectProperty<GameLevel> levelProperty() {
        return level;
    }

    /**
     * Called when Pac-Man eats an energizer.
     *
     * @param level the current level
     * @param tile  the tile containing the energizer
     */
    public abstract void eatEnergizer(GameLevel level, Vector2i tile);

    public void eatBonus(GameLevel level, Bonus bonus) {
        scorePoints(bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(rules().eatenBonusDisplaySeconds());
        flow().publishGameEvent(new BonusEatenEvent(this, bonus));
    }

    /**
     * Determines whether Pac-Man is safe from being killed during demo mode.
     *
     * @param demoLevel the demo level
     * @return {@code true} if Pac-Man cannot be killed at this moment
     */
    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);


}
