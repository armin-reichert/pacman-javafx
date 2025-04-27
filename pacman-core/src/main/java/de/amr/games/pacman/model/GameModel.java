/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common base class of all Pac-Man game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    private final BooleanProperty cutScenesEnabledPy = new SimpleBooleanProperty(true);
    private final IntegerProperty initialLivesPy = new SimpleIntegerProperty(3);
    private final IntegerProperty livesPy = new SimpleIntegerProperty(0);
    private final BooleanProperty playingPy = new SimpleBooleanProperty(false);
    private final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(false);

    protected ScoreManager scoreManager;
    protected GameLevel level;

    protected GameModel() {
        scoreManager = new ScoreManager();
        scoreManager.setOnExtraLifeWon(score -> {
            eventsThisFrame().setExtraLifeWon();
            eventsThisFrame().setExtraLifeScore(score);
            addLives(1);
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.EXTRA_LIFE_WON);
        });
    }

    public abstract void init();

    public abstract void resetEverything();

    public abstract void resetForStartingNewGame();

    public abstract boolean canStartNewGame();

    public abstract boolean continueOnGameOver();

    public abstract boolean isOver();

    public abstract void endGame();

    public abstract void onPacKilled();

    public abstract void killGhost(Ghost ghost);

    public abstract void activateNextBonus();

    protected abstract void setActorBaseSpeed(int levelNumber);

    public abstract float ghostAttackSpeed(Ghost ghost);

    public abstract float ghostFrightenedSpeed(Ghost ghost);

    public abstract float ghostSpeedInsideHouse(Ghost ghost);

    public abstract float ghostSpeedReturningToHouse(Ghost ghost);

    public abstract float ghostTunnelSpeed(Ghost ghost);

    public abstract float pacNormalSpeed();

    public abstract long pacDyingTicks();

    public abstract long pacPowerTicks();

    public abstract long pacPowerFadingTicks();

    public abstract float pacPowerSpeed();

    public abstract long gameOverStateTicks();

    public abstract LevelData createLevelData(int levelNumber);

    public abstract void buildLevel(int levelNumber, LevelData data);

    public abstract void buildDemoLevel();

    public abstract void assignDemoLevelBehavior(Pac pac);

    protected abstract boolean isPacManKillingIgnored();

    protected abstract boolean isBonusReached();

    protected abstract byte computeBonusSymbol(int levelNumber);

    protected abstract void onEnergizerEaten(Vector2i tile);

    protected abstract void onPelletEaten(Vector2i tile);

    public abstract MapSelector mapSelector();

    public abstract HuntingTimer huntingTimer();

    protected Optional<GateKeeper> gateKeeper() { return Optional.empty(); }

    protected SimulationStepEvents eventsThisFrame() { return THE_GAME_CONTROLLER.eventsThisFrame(); }

    public int lastLevelNumber() { return Integer.MAX_VALUE; }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public abstract <T extends LevelCounter> T levelCounter();

    public ScoreManager scoreManager() {
        return scoreManager;
    }

    public BooleanProperty cutScenesEnabledProperty() { return cutScenesEnabledPy; }

    public IntegerProperty initialLivesProperty() { return initialLivesPy; }

    public IntegerProperty livesProperty() { return livesPy; }

    public BooleanProperty playingProperty() { return playingPy; }

    public boolean isPlaying() { return playingProperty().get(); }

    public BooleanProperty scoreVisibleProperty() { return scoreVisiblePy; }

    public boolean isScoreVisible() { return scoreVisibleProperty().get(); }

    public void loseLife() {
        int lives = livesProperty().get();
        if (lives > 0) {
            livesProperty().set(lives - 1);
        } else {
            Logger.error("Cannot lose life, no lives left");
        }
    }

    public void addLives(int lives) {
        livesProperty().set(livesProperty().get() + lives);
    }

    public void startNewGame() {
        resetForStartingNewGame();
        createLevel(1, createLevelData(1));
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.GAME_STARTED);
    }

    public final void createLevel(int levelNumber, LevelData data) {
        buildLevel(levelNumber, data);
        level.setDemoLevel(false);
        scoreManager.setLevelNumber(levelNumber);
        scoreManager.setScoreEnabled(!level.isDemoLevel());
        scoreManager.setHighScoreEnabled(!level.isDemoLevel());
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.showMessage(level.isDemoLevel() ? GameLevel.Message.GAME_OVER : GameLevel.Message.READY);
        letsGetReadyToRumble();
        setActorBaseSpeed(level.number());
        levelCounter().update(level);
        Logger.info("{} started", level.isDemoLevel() ? "Demo Level" : "Level " + level.number());
        // Note: This event is very important because it triggers the creation of the actor animations!
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_STARTED);
    }

    public void startNextLevel() {
        if (level.number() < lastLevelNumber()) {
            createLevel(level.number() + 1, createLevelData(level.number() + 1));
            startLevel();
            showPacAndGhosts();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber());
        }
    }

    /**
     * Sets each guy to his start position and resets him to his initial state. The guys are all initially invisible!
     */
    public void letsGetReadyToRumble() {
        level.pac().reset(); // initially invisible!
        level.pac().setPosition(level.pacStartPosition());
        level.pac().setMoveAndWishDir(Direction.LEFT);
        level.pac().powerTimer().resetIndefiniteTime();
        level.ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(level.ghostStartPosition(ghost.id()));
            ghost.setMoveAndWishDir(level.ghostStartDirection(ghost.id()));
            ghost.setState(LOCKED);
        });
        level.blinking().setStartPhase(Pulse.ON); // Energizers are visible when ON
        level.blinking().reset();

        //TODO this is dubious as actor animations are not always created at this point in time
        initActorAnimationState();
    }

    protected void initActorAnimationState() {
        level.pac().selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void showPacAndGhosts() {
        level.pac().show();
        level.ghosts().forEach(Ghost::show);
    }

    public void hidePacAndGhosts() {
        level.pac().hide();
        level.ghosts().forEach(Ghost::hide);
    }

    /**
     * Returns the chasing target tile for the given chaser.
     *
     * @param ghostID the chasing ghost's ID
     * @param level the game level
     * @param buggy if overflow bug from Arcade game is simulated
     * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
     */
    protected Vector2i chasingTargetTile(byte ghostID, GameLevel level, boolean buggy) {
        return switch (ghostID) {
            // Blinky (red ghost) attacks Pac-Man directly
            case RED_GHOST_ID -> level.pac().tile();

            // Pinky (pink ghost) ambushes Pac-Man
            case PINK_GHOST_ID -> level.pac().tilesAhead(4, buggy);

            // Inky (cyan ghost) attacks from opposite side as Blinky
            case CYAN_GHOST_ID -> level.pac().tilesAhead(2, buggy).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());

            // Clyde/Sue (orange ghost) attacks directly or retreats towards scatter target if Pac is near
            case ORANGE_GHOST_ID -> level.ghost(ORANGE_GHOST_ID).tile().euclideanDist(level.pac().tile()) < 8
                ? level.ghostScatterTile(ORANGE_GHOST_ID) : level.pac().tile();

            default -> throw GameException.invalidGhostID(ghostID);
        };
    }

    public void endLevel() {
        Logger.info("Level complete, stop hunting timer");
        huntingTimer().stop();

        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();

        level.pac().freeze();
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero");

        level.bonus().ifPresent(Bonus::setInactive);

        // when cheating, there might still be remaining food
        level.worldMap().tiles().filter(level::hasFoodAt).forEach(level::registerFoodEatenAt);

        Logger.trace("Game level {} completed.", level.number());
    }

    public boolean isLevelComplete() {
        return level.uneatenFoodCount() == 0;
    }

    public boolean hasPacManBeenKilled() {
        return eventsThisFrame().pacKilledTile() != null;
    }

    public boolean haveGhostsBeenKilled() {
        return !eventsThisFrame().killedGhosts().isEmpty();
    }

    public void startHunting() {
        level.pac().startAnimation();
        level.ghosts().forEach(Ghost::startAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(level.number());
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep() {
        huntingTimer().update(level.number());
        level.blinking().tick();
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.unlockGhosts(level, eventsThisFrame()));
        checkForFood();
        level.pac().update(this);
        updatePacPower();
        checkPacKilled();
        if (!hasPacManBeenKilled()) {
            level.ghosts().forEach(ghost -> ghost.update(this));
            level.ghosts(FRIGHTENED).filter(ghost -> areActorsColliding(ghost, level.pac())).forEach(this::killGhost);
            if (!haveGhostsBeenKilled()) {
                level.bonus().ifPresent(this::updateBonus);
            }
        }
    }

    private void checkPacKilled() {
        level.ghosts(HUNTING_PAC)
            .filter(ghost -> areActorsColliding(level.pac(), ghost))
            .findFirst().ifPresent(killer -> {
                boolean killed;
                if (level.isDemoLevel()) {
                    killed = !isPacManKillingIgnored();
                } else {
                    killed = !level.pac().isImmune();
                }
                if (killed) {
                    eventsThisFrame().setPacKiller(killer);
                    eventsThisFrame().setPacKilledTile(killer.tile());
                }
            });
    }

    private void checkForFood() {
        Vector2i tile = level.pac().tile();
        if (level.hasFoodAt(tile)) {
            level.pac().endStarving();
            if (level.isEnergizerPosition(tile)) {
                eventsThisFrame().setFoundEnergizerAtTile(tile);
                onEnergizerEaten(tile);
            } else {
                onPelletEaten(tile);
            }
            level.registerFoodEatenAt(tile);
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (isBonusReached()) {
                activateNextBonus();
                eventsThisFrame().setBonusIndex(level.nextBonusIndex());
            }
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    protected boolean areActorsColliding(Actor2D either, Actor2D other) {
        return either.sameTile(other);
    }

    private void updatePacPower() {
        final TickTimer timer = level.pac().powerTimer();
        timer.doTick();
        if (level.pac().isPowerFadingStarting(this)) {
            eventsThisFrame().setPacStartsLosingPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (timer.hasExpired()) {
            timer.stop();
            timer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer restarted because Pac-Man lost power");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventsThisFrame().setPacLostPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_LOST_POWER);
        }
    }

    private void updateBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && areActorsColliding(level.pac(), bonus.actor())) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scoreManager.scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventsThisFrame().setBonusEatenTile(bonus.actor().tile());
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }
}