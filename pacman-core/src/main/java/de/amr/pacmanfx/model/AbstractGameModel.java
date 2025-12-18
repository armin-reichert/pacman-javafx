/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all Pac-Man game models. Any logic implemented here should be identical in all Pac-Man game variants.
 */
public abstract class AbstractGameModel implements Game {

    public static final CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final BooleanProperty immune = new SimpleBooleanProperty(false);

    private final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    private final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    protected final GameControl gameControl;

    protected final SimulationStep simStep = new SimulationStep();

    protected AbstractGameModel(GameControl gameControl) {
        this.gameControl = requireNonNull(gameControl);
        gameControl.stateMachine().setContext(this);
        gameControl.stateMachine().addStateChangeListener(
            (oldState, newState) -> publishGameEvent(new GameStateChangeEvent(this, oldState, newState)));

        cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated && highScore.isEnabled()) {
                highScore.setEnabled(false);
            }
        });

        lifeCountProperty().addListener((_, _, nv) -> {
            if (nv.intValue() < 0) {
                throw new IllegalArgumentException("Life count cannot be set to negative value " + nv.intValue());
            }
        });

        score.pointsProperty().addListener((_, oldScore, newScore) -> {
            // has extra life score line been crossed?
            for (int scoreLine : extraLifeScores) {
                if (oldScore.intValue() < scoreLine && newScore.intValue() >= scoreLine) {
                    simStep.extraLifeWon = true;
                    simStep.extraLifeScore = scoreLine;
                    break;
                }
            }
            if (simStep.extraLifeWon) {
                addLives(1);
                publishGameEvent(new GameEvent(this, GameEvent.Type.SPECIAL_SCORE_REACHED));
            }
        });
    }

    public ObjectProperty<GameLevel> levelProperty() {
        return level;
    }

    // To be implemented by subclasses

    /**
     * Called when Pac finds a normal (non-energizer) pellet.

     * @param level the game level
     * @param tile the pellet tile
     */
    protected abstract void eatPellet(GameLevel level, Vector2i tile);

    /**
     * Called when Pac finds an energizer pellet.

     * @param level the game level
     * @param tile the energizer tile
     */
    protected abstract void eatEnergizer(GameLevel level, Vector2i tile);

    /**
     * Called when Pac collides with a bonus actor.

     * @param level the game level
     * @param bonus the bonus actor
     */
    protected abstract void eatBonus(GameLevel level, Bonus bonus);

    /**
     * @param demoLevel the running demo level
     * @return {@code true} if Pac can currently not be killed in this demo level
     */
    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    // These methods are public such that info panel can call them:

    /**
     * @param level the game level
     * @param ghost a ghost
     * @return the attack speed (pixel/frame) of this ghost
     */
    public abstract float ghostSpeedAttacking(GameLevel level, Ghost ghost);

    /**
     * @param level the game level
     * @return the speed (pixel/frame) of frightened ghosts
     */
    public abstract float ghostSpeedWhenFrightened(GameLevel level);

    /**
     * @param levelNumber the game level number (1, 2, ...)
     * @return the speed (pixel/frame) of a ghost inside a tunnel leading to some portal
     */
    public abstract float ghostSpeedTunnel(int levelNumber);

    // Game interface

    @Override
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    @Override
    public BooleanProperty immuneProperty() {
        return immune;
    }

    @Override
    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    @Override
    public final GameControl control() {
        return gameControl;
    }

    @Override
    public SimulationStep simulationStep() {
        return simStep;
    }

    @Override
    public GameLevel level() {
        return level.get();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(level.get());
    }

    @Override
    public IntegerProperty lifeCountProperty() {
        return lifeCount;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public int lifeCount() {
        return lifeCountProperty().get();
    }

    @Override
    public void addLives(int n) {
        lifeCountProperty().set(lifeCount() + n);
    }

    @Override
    public CollisionStrategy collisionStrategy() {
        return collisionStrategy.get();
    }

    @Override
    public void setCollisionStrategy(CollisionStrategy strategy) {
        requireNonNull(strategy);
        collisionStrategy.set(strategy);
    }

    @Override
    public boolean isPlaying() { return playing.get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return simStep.pacKiller != null;
    }

    @Override
    public boolean hasGhostBeenKilled() {
        return !simStep.killedGhosts.isEmpty();
    }

    @Override
    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    @Override
    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }

    @Override
    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    @Override
    public void setInitialLifeCount(int count) {
        initialLifeCount.set(count);
    }

    @Override
    public boolean isLevelCompleted() {
        return level().worldMap().foodLayer().uneatenFoodCount() == 0;
    }

    @Override
    public void startHunting(GameLevel level) {
        level.huntingTimer().startFirstPhase(level.number());

        //TODO move into UI layer?
        level.blinking().setStartState(Pulse.State.ON);
        level.blinking().restart();
        level.pac().optAnimationManager().ifPresent(AnimationManager::play);
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));

        publishGameEvent(GameEvent.Type.HUNTING_PHASE_STARTED);
    }

    @Override
    public void onLevelCompleted() {
        final GameLevel level = level();
        final Pac pac = level.pac();

        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.optBonus().ifPresent(Bonus::setInactive);

        pac.setSpeed(0);
        //TODO move animation-related stuff into UI layer?
        pac.stopAnimation();
        pac.selectAnimation(CommonAnimationID.ANIM_PAC_FULL);
        level.blinking().setStartState(Pulse.State.OFF);
        level.blinking().reset();
        level.ghosts().forEach(Ghost::stopAnimation);
    }

    // GameEventManager implementation

    private final Set<GameEventListener> eventListeners = new HashSet<>();

    @Override
    public void addGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void removeGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    @Override
    public void publishGameEvent(GameEvent.Type type) {
        requireNonNull(type);
        publishGameEvent(new GameEvent(this, type));
    }

    @Override
    public void publishGameEvent(GameEvent.Type type, Vector2i tile) {
        requireNonNull(type);
        publishGameEvent(new GameEvent(this, type, tile));
    }

    // other stuff

    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
        }
    }

    /**
     * The main logic step of all Pac-Man games. Checks if Pac-Man collides with a ghost or finds food or a bonus.
     * Collision with a ghost either kills the ghost and earns points (in case Pac-Man has power) or kills Pac-Man and
     * loses a life. When Pac-Man finds an energizer pellet he enters power mode and is able to kill ghosts. The duration
     * of the power mode varies between levels.
     *
     * @param level the game level
     */
    protected void doHuntingStep(GameLevel level) {
        final Pac pac = level.pac();

        // Compute and record collisions between actors and Pac-Man/food collisions
        detectCollisions(level);

        if (!simStep.ghostsCollidingWithPac.isEmpty()) {
            // Is Pac getting killed after collision with ghost? He might stay alive if immune or in level's safe phase!
            checkPacKilled(level);
            if (hasPacManBeenKilled()) {
                return; // game state will change, so quit this hunting step now
            }

            // Ghost(s) killed?
            simStep.ghostsCollidingWithPac.stream().filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(simStep.killedGhosts::add);
            if (hasGhostBeenKilled()) {
                // just in case more than one ghost is killed during the same simulation step
                simStep.killedGhosts.forEach(this::onEatGhost);
                return; // game state will change, so quit this hunting step now
            }
        }

        // Did Pac enter a tile containing a pellet or an energizer?
        if (simStep.foodTile == null) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().registerFoodEatenAt(simStep.foodTile);
            pac.endStarving();
            if (simStep.energizerFound) {
                eatEnergizer(level, simStep.foodTile);
            } else {
                eatPellet(level, simStep.foodTile);
            }
            if (isBonusReached(level)) {
                activateNextBonus(level);
                simStep.bonusIndex = level.currentBonusIndex();
            }
            publishGameEvent(GameEvent.Type.PAC_FOUND_FOOD, simStep.foodTile);
        }

        if (simStep.edibleBonus != null) {
            eatBonus(level, simStep.edibleBonus);
        }

        pac.tick(this);
        level.ghosts().forEach(ghost -> ghost.tick(this));
        level.optBonus().ifPresent(bonus -> bonus.tick(this));
        level.blinking().tick();

        if (!isLevelCompleted()) {
            updatePacPower(level);
            level.huntingTimer().update(level.number());
        }
    }

    public void onEnergizerEaten(GameLevel level) {
        level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(MovingActor::requestTurnBack);
        level.energizerVictims().clear();
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            level.pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            simStep.pacGotPower = true;
            publishGameEvent(GameEvent.Type.PAC_GETS_POWER);
        }
    }

    private void detectCollisions(GameLevel level) {
        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Pac pac = level.pac();
        final Vector2i pacTile = pac.tile();

        // Ghosts colliding with Pac? While teleportation takes place, collisions are disabled. (Not sure what the
        // original Arcade game does). Collision behavior is controlled by the current collision strategy. The original
        // Arcade games use tile-based collision which can lead to missed collisions by passing through.
        simStep.ghostsCollidingWithPac = level.ghosts()
            .filter(ghost -> !terrainLayer.isTileInPortalSpace(ghost.tile()))
            .filter(ghost -> collisionStrategy().collide(pac, ghost))
            .toList();

        simStep.edibleBonus = level.optBonus()
            .filter(bonus -> bonus.state() == BonusState.EDIBLE)
            .filter(bonus -> collisionStrategy().collide(pac, bonus))
            .orElse(null);

        if (foodLayer.hasFoodAtTile(pacTile)) {
            simStep.foodTile = pacTile;
            simStep.energizerFound = foodLayer.isEnergizerTile(pacTile);
        }
    }

    /**
     * Checks if Pac-Man gets killed by a collision with an attacking ghost.
     * <p>In attract mode (demo level), there is a time interval at the beginning when Pac-Man is safe.
     * This is to avoid having Pac-Man getting killed too early in demo mode.
     * In contrast to the original Arcade games, the demo mode is not fixed but uses random ghost moves so it
     * cannot be predicted how long the demo mode runs.
     * <p>
     * In normal mode, Pac-Man can be made immune against ghost attacks using a cheat command.
     * In this case, Pac-Man is safe against ghost attacks too.
     *
     * @param level the game level
     */
    protected void checkPacKilled(GameLevel level) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(level) || !demoLevel && level.pac().isImmune()) {
            return;
        }
        simStep.pacKiller = simStep.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null);
    }

    /**
     * Updates the power of Pac-Man. Power starts fading after some time. When this happens, the ghosts start flashing
     * and when the power timer expires, they take their normal color again and continue chasing Pac-Man.
     *
     * @param level the game level
     */
    protected void updatePacPower(GameLevel level) {
        final Pac pac = level.pac();
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                simStep.pacStartsLosingPower = true;
                publishGameEvent(GameEvent.Type.PAC_STARTS_LOSING_POWER);
            } else if (pac.powerTimer().hasExpired()) {
                simStep.pacLostPower = true;
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.energizerVictims().clear();
                level.huntingTimer().start();
                level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                publishGameEvent(GameEvent.Type.PAC_LOST_POWER);
            }
        }
    }

    protected void clearCheatingProperties() {
        immuneProperty().set(false);
        usingAutopilotProperty().set(false);
        cheatUsedProperty().set(false);
    }

    protected void updateCheatingProperties(GameLevel level) {
        level.pac().immuneProperty().bind(immuneProperty());
        level.pac().usingAutopilotProperty().bind(usingAutopilotProperty());
        cheatUsedProperty().set(immune() || usingAutopilot());
    }

    // ScoreManager

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Score score = new Score();
    private final Score highScore = new Score();
    private File highScoreFile;
    private Set<Integer> extraLifeScores = Set.of();

    protected void setHighScoreFile(File highScoreFile) {
        this.highScoreFile = requireNonNull(highScoreFile);
    }

    protected void setExtraLifeScores(Integer... scores) {
        extraLifeScores = Set.of(scores);
    }

    protected void scorePoints(int points) {
        if (!score.isEnabled()) {
            return;
        }
        int oldScore = score.points(), newScore = oldScore + points;
        if (highScore().isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(score.levelNumber());
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    protected void loadHighScore() {
        highScore.read(highScoreFile);
        Logger.info("High Score loaded from file '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
    }

    protected void updateHighScore() {
        var oldHighScore = Score.createFromFile(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            try {
                final String dateTime = FORMATTER.format(LocalDateTime.now());
                highScore.save(highScoreFile, "High Score updated at %s".formatted(dateTime));
            } catch (IOException x) {
                Logger.error("High Score file could not be saved: '{}'", highScoreFile);
            }
        }
    }

    public void saveHighScore() {
        try {
            new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
        } catch (IOException x) {
            Logger.error("High Score could not be saved to file '{}'", highScoreFile);
            Logger.error(x);
        }
    }
}