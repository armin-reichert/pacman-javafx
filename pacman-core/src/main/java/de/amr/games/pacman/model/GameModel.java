/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.model.actors.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common base class of all Pac-Man game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    /**
     * Directory where application stores high scores and other stuff (user home directory).
     */
    public static final File HOME_DIR;

    /**
     * Directory where custom maps are looked for (<code>user_home_directory/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR;

    static {
        HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");
        if (HOME_DIR.mkdir()) {
            Logger.info("Home directory '{}' created", HOME_DIR);
        }
        CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");
        if (CUSTOM_MAP_DIR.mkdir()) {
            Logger.info("Created custom map directory {}", CUSTOM_MAP_DIR);
        }
    }

    // Ghost IDs
    public static final byte RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;
    public static final short POINTS_ALL_GHOSTS_EATEN_IN_LEVEL = 12_000;
    public static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = { 2, 4, 8, 16 }; // factor * 100 = value

    protected final MapSelector  mapSelector;
    protected final List<Byte>   levelCounter = new ArrayList<>();
    protected final GateKeeper   gateKeeper = new GateKeeper();
    protected final ScoreManager scoreManager = new ScoreManager();
    protected final HuntingTimer huntingTimer;

    protected GameLevel          level;
    protected long               levelStartTime;
    protected int                lastLevelNumber;
    protected boolean            levelCounterEnabled;
    protected boolean            playing;
    protected boolean            simulateOverflowBug;
    protected boolean            cutScenesEnabled;
    protected int                initialLives;
    protected int                lives;
    protected boolean            demoLevel;

    protected SimulationStepLog  eventLog;

    protected GameModel(MapSelector mapSelector, HuntingTimer huntingTimer) {
        this.mapSelector = assertNotNull(mapSelector);
        this.huntingTimer = assertNotNull(huntingTimer);
    }

    public abstract void         init();
    public abstract void         resetEverything();
    public abstract void         resetForStartingNewGame();
    public abstract boolean      canStartNewGame();
    public abstract boolean      continueOnGameOver();
    public abstract boolean      isOver();
    public abstract void         endGame();
    public abstract void         onPacKilled();
    public abstract void         killGhost(Ghost ghost);
    public abstract void         activateNextBonus();

    protected abstract void      setActorBaseSpeed(int levelNumber);

    public abstract float        ghostAttackSpeed(Ghost ghost);
    public abstract float        ghostFrightenedSpeed(Ghost ghost);
    public abstract float        ghostSpeedInsideHouse(Ghost ghost);
    public abstract float        ghostSpeedReturningToHouse(Ghost ghost);
    public abstract float        ghostTunnelSpeed(Ghost ghost);

    public abstract float        pacNormalSpeed();
    public abstract long         pacPowerTicks();
    public abstract long         pacPowerFadingTicks();
    public abstract float        pacPowerSpeed();

    public abstract long         gameOverStateTicks();

    protected abstract GameLevel makeNormalLevel(int levelNumber);
    protected abstract GameLevel makeDemoLevel();

    public abstract void assignDemoLevelBehavior(GameLevel demoLevel);

    protected abstract boolean   isPacManKillingIgnored();
    protected abstract boolean   isBonusReached();
    protected abstract byte      computeBonusSymbol(int levelNumber);

    protected abstract void      onPelletOrEnergizerEaten(Vector2i tile, int remainingFoodCount, boolean energizer);
    protected abstract void      onGhostReleased(Ghost ghost);

    public final MapSelector mapSelector() { return mapSelector; }

    public final HuntingTimer huntingTimer() { return huntingTimer; }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public final int lastLevelNumber() { return lastLevelNumber; }

    public int initialLives() {
        return initialLives;
    }

    public void setInitialLives(int lives) {
        initialLives = lives;
    }

    public int lives() {
        return lives;
    }

    public void addLives(int deltaLives) {
        lives += deltaLives;
    }

    public void loseLife() {
        if (lives == 0) {
            Logger.error("No life left to lose :-(");
            return;
        }
        --lives;
    }

    public List<Byte> levelCounter() {
        return levelCounter;
    }

    public ScoreManager scoreManager() {
        return scoreManager;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setDemoLevel(boolean demoLevel) {
        this.demoLevel = demoLevel;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    public void setCutScenesEnabled(boolean cutScenesEnabled) {
        this.cutScenesEnabled = cutScenesEnabled;
    }

    public boolean isCutScenesEnabled() {
        return cutScenesEnabled;
    }

    public void startNewGame() {
        resetForStartingNewGame();
        createNormalLevel(1);
        publishGameEvent(GameEventType.GAME_STARTED);
    }

    public void createNormalLevel(int levelNumber) {
        setDemoLevel(false);
        level = makeNormalLevel(levelNumber);
        scoreManager.setLevelNumber(levelNumber);
        huntingTimer.reset();
        updateLevelCounter();
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    public void startNextLevel() {
        int nextLevelNumber = level.number + 1;
        if (nextLevelNumber <= lastLevelNumber) {
            createNormalLevel(nextLevelNumber);
            startLevel();
            showGuys();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    public void createDemoLevel() {
        setDemoLevel(true);
        level = makeDemoLevel();
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    protected void updateLevelCounter() {
        if (level.number == 1) {
            levelCounter.clear();
        }
        if (levelCounterEnabled) {
            levelCounter.add(level.bonusSymbol(0));
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounter.removeFirst();
            }
        }
    }

    public void startLevel() {
        gateKeeper.setLevelNumber(level.number);
        scoreManager.setLevelNumber(level.number);
        scoreManager.setScoreEnabled(!isDemoLevel());
        scoreManager.setHighScoreEnabled(!isDemoLevel());
        letsGetReadyToRumble();
        setActorBaseSpeed(level.number);
        level.showMessage(GameLevel.Message.READY);
        Logger.info("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        levelStartTime = System.currentTimeMillis();
        Logger.info("{} started", isDemoLevel() ? "Demo Level" : "Level " + level.number);
        level.ghosts().forEach(ghost -> Logger.info("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    /**
     * Sets each guy to his start position and resets him to his initial state. The guys are all initially invisible!
     */
    public void letsGetReadyToRumble() {
        level.pac().reset(); // invisible!
        level.pac().setPosition(level.pacPosition());
        level.pac().setMoveAndWishDir(Direction.LEFT);
        level.ghosts().forEach(ghost -> {
            ghost.reset(); // invisible!
            ghost.setPosition(level.ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(level.ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
        });
        initActorAnimations();
        level.powerTimer().resetIndefiniteTime();
        level.blinking().setStartPhase(Pulse.ON); // Energizers are visible when ON
        level.blinking().reset();
    }

    protected void initActorAnimations() {
        level.pac().selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void showGuys() {
        level.pac().show();
        level.ghosts().forEach(Actor2D::show);
    }

    public void hideGuys() {
        level.pac().hide();
        level.ghosts().forEach(Actor2D::hide);
    }

    public SimulationStepLog eventLog() {
        return eventLog;
    }

    public void clearEventLog() {
        eventLog = new SimulationStepLog();
    }

    protected Vector2i scatterTarget(Ghost ghost) {
        return level.ghostScatterTile(ghost.id());
    }

    /**
     * See this <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">explanation</a>.
     */
    protected Vector2i chasingTarget(Ghost ghost) {
        return switch (ghost.id()) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> level.pac().tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> level.pac().tilesAhead(4, simulateOverflowBug);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> level.pac().tilesAhead(2, simulateOverflowBug).scaled(2).minus(level.ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost.tile().euclideanDist(level.pac().tile()) < 8 ? scatterTarget(ghost) : level.pac().tile();
            default -> throw GameException.illegalGhostID(ghost.id());
        };
    }

    public void onLevelCompleted() {
        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();
        level.pac().freeze();
        level.bonus().ifPresent(Bonus::setInactive);
        // when cheating, there might still be food
        level.worldMap().tiles()
            .filter(level::hasFoodAt)
            .forEach(level::registerFoodEatenAt);
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        level.powerTimer().stop();
        level.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", level.number);
    }

    public boolean isLevelComplete() {
        return level.uneatenFoodCount() == 0;
    }

    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    public void startHunting() {
        level.pac().startAnimation();
        level.ghosts().forEach(Ghost::startAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer.startHunting(level.number);
        publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep() {
        Pac pac = level.pac();
        updateHuntingTimer();
        level.blinking().tick();
        gateKeeper.unlockGhosts(level, this::onGhostReleased, eventLog);
        checkForFood(level, pac);
        pac.update(this);
        updatePacPower();
        checkPacKilled();
        if (!eventLog.pacKilled) {
            level.ghosts().forEach(ghost -> ghost.update(this));
            level.ghosts(FRIGHTENED).filter(level.pac()::sameTile).forEach(this::killGhost);
            if (eventLog.killedGhosts.isEmpty()) {
                level.bonus().ifPresent(this::updateBonus);
            }
        }
    }

    private void updateHuntingTimer() {
        if (huntingTimer.hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}",
                huntingTimer.phaseIndex(), huntingTimer.phaseType(), huntingTimer.tickCount());
            huntingTimer.startNextPhase(level.number);
        } else {
            huntingTimer.doTick();
        }
    }

    private void checkPacKilled() {
        boolean pacMeetsKiller = level.ghosts(HUNTING_PAC).anyMatch(level.pac()::sameTile);
        if (isDemoLevel()) {
            eventLog.pacKilled = pacMeetsKiller && !isPacManKillingIgnored();
        } else {
            eventLog.pacKilled = pacMeetsKiller && !level.pac().isImmune();
        }
    }

    private void checkForFood(GameLevel level, Pac pac) {
        Vector2i tile = pac.tile();
        if (level.hasFoodAt(tile)) {
            eventLog.foodFoundTile = tile;
            eventLog.energizerFound = level.isEnergizerPosition(tile);
            level.registerFoodEatenAt(tile);
            onPelletOrEnergizerEaten(tile, level.uneatenFoodCount(), eventLog.energizerFound);
            pac.endStarving();
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            pac.starve();
        }
    }

    protected void processEatenEnergizer() {
        level.victims().clear(); // ghosts eaten using this energizer
        long powerTicks = pacPowerTicks();
        if (powerTicks > 0) {
            Logger.info("Power: {} ticks ({0.00} sec)", powerTicks, powerTicks / 60.0);
            eventLog.pacGetsPower = true;
            huntingTimer.stop();
            level.powerTimer().restartTicks(pacPowerTicks());
            Logger.info("Hunting paused, power timer restarted, duration={} ticks", level.powerTimer().durationTicks());
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseASAP);
            publishGameEvent(GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseASAP);
        }
    }

    private void updatePacPower() {
        level.powerTimer().doTick();
        if (level.powerTimer().remainingTicks() == pacPowerFadingTicks()) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (level.powerTimer().hasExpired()) {
            level.powerTimer().stop();
            level.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean isPowerFading() {
        return level.powerTimer().isRunning() && level.powerTimer().remainingTicks() <= pacPowerFadingTicks();
    }

    public boolean isPowerFadingStarting() {
        return level.powerTimer().isRunning() && level.powerTimer().remainingTicks() == pacPowerFadingTicks()
            || level.powerTimer().durationTicks() < pacPowerFadingTicks() && level.powerTimer().tickCount() == 1;
    }

    private void updateBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && level.pac().sameTile(bonus.actor())) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scoreManager.scorePoints(this, bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog.bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    // Game Event Support

    private final List<GameEventListener> gameEventListeners = new ArrayList<>();

    public void addGameEventListener(GameEventListener listener) {
        assertNotNull(listener);
        if (!gameEventListeners.contains(listener)) {
            gameEventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", this, listener);
        } else {
            Logger.warn("{}: Game event listener already registered: {}", this, listener);
        }
    }

    public void removeGameEventListener(GameEventListener listener) {
        assertNotNull(listener);
        boolean removed = gameEventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", this, listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", this, listener);
        }
    }

    public void publishGameEvent(GameEvent event) {
        assertNotNull(event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    public void publishGameEvent(GameEventType type) {
        assertNotNull(type);
        publishGameEvent(new GameEvent(type, this));
    }

    public void publishGameEvent(GameEventType type, Vector2i tile) {
        assertNotNull(type);
        publishGameEvent(new GameEvent(type, this, tile));
    }
}