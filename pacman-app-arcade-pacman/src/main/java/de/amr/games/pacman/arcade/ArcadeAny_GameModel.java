/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.ActorSpeedControl;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 */
public abstract class ArcadeAny_GameModel extends GameModel {

    // Level settings as specified in the "Pac-Man dossier"
    public static final byte[][] LEVEL_DATA = {
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
    };

    protected static final byte PELLET_VALUE = 10;
    protected static final byte ENERGIZER_VALUE = 50;
    protected static final int POINTS_ALL_GHOSTS_EATEN_IN_LEVEL = 12_000;
    protected static final int EXTRA_LIFE_SCORE = 10_000;
    protected static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // points = value * 100

    protected MapSelector mapSelector;
    protected LevelCounter levelCounter;
    protected HuntingTimer huntingTimer;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;

    public class ArcadeActorSpeedControl implements ActorSpeedControl {
        @Override
        public void setActorBaseSpeed(GameLevel level) {
            level.pac().setBaseSpeed(1.25f);
            level.ghosts().forEach(ghost -> ghost.setBaseSpeed(1.25f));
            Logger.debug("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
            level.ghosts().forEach(ghost -> Logger.debug("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
        }

        @Override
        public float pacNormalSpeed(GameLevel level) {
            byte pct = level.data().pacSpeedPercentage();
            return pct > 0 ? pct * 0.01f * level.pac().baseSpeed() : level.pac().baseSpeed();
        }

        @Override
        public float pacPowerSpeed(GameLevel level) {
            byte pct = level.data().pacSpeedPoweredPercentage();
            return pct > 0 ? pct * 0.01f * level.pac().baseSpeed() : pacNormalSpeed(level);
        }

        @Override
        public float ghostAttackSpeed(GameLevel level, Ghost ghost) {
            if (level.isTunnel(ghost.tile())) {
                return level.data().ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
            }
            if (ghost.id() == RED_GHOST_ID && cruiseElroy == 1) {
                return level.data().elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
            }
            if (ghost.id() == RED_GHOST_ID && cruiseElroy == 2) {
                return level.data().elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
            }
            return level.data().ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
        }

        @Override
        public float ghostSpeedInsideHouse(GameLevel level, Ghost ghost) {
            return 0.5f;
        }

        @Override
        public float ghostSpeedReturningToHouse(GameLevel level, Ghost ghost) {
            return 2;
        }

        @Override
        public float ghostFrightenedSpeed(GameLevel level, Ghost ghost) {
            float pct = level.data().ghostSpeedFrightenedPercentage();
            return pct > 0 ? pct * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
        }

        @Override
        public float ghostTunnelSpeed(GameLevel level, Ghost ghost) {
            return level.data().ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
        }
    }

    @Override
    protected Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends LevelCounter> T levelCounter() {
        return (T) levelCounter;
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public void init() {
        initialLivesProperty().set(3);
        mapSelector.loadAllMaps(this);
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
    }

    @Override
    public void prepareForNewGame() {
        playingProperty().set(false);
        livesProperty().set(initialLivesProperty().get());
        level = null;
        cruiseElroy = 0;
        levelCounter().reset();
        loadHighScore();
        resetScore();
        gateKeeper.reset();
        huntingTimer.reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        buildNormalLevel(1);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !THE_COIN_MECHANISM.isEmpty(); }

    @Override
    public boolean continueOnGameOver() { return false; }

    @Override
    public boolean isOver() {
        return livesProperty().get() == 0;
    }

    // Actors

    protected byte cruiseElroy;
    protected ActorSpeedControl speedControl = new ArcadeActorSpeedControl();

    @Override
    public ActorSpeedControl speedControl() {
        return speedControl;
    }

    @Override
    public long pacPowerTicks(GameLevel level) {
        return level != null ? 60 * level.data().pacPowerSeconds() : 0;
    }

    private void updateBlinkyCruiseElroy() {
        if (level.uneatenFoodCount() == level.data().elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (level.uneatenFoodCount() == level.data().elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
    }

    protected void setBlinkyCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    /**
     * Returns the chasing target tile for the given ghost.
     * @param level the game level
     * @param ghostID the chasing ghost's ID
     * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
     */
    protected Vector2i chasingTargetTile(GameLevel level, byte ghostID) {
        return switch (ghostID) {
            // Blinky (red ghost) attacks Pac-Man directly
            case RED_GHOST_ID -> level.pac().tile();

            // Pinky (pink ghost) ambushes Pac-Man
            case PINK_GHOST_ID -> level.pac().tilesAhead(4, true);

            // Inky (cyan ghost) attacks from opposite side as Blinky
            case CYAN_GHOST_ID -> level.pac().tilesAhead(2, true).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());

            // Clyde/Sue (orange ghost) attacks directly or retreats towards scatter target if Pac is near
            case ORANGE_GHOST_ID -> level.ghost(ORANGE_GHOST_ID).tile().euclideanDist(level.pac().tile()) < 8
                    ? level.ghostScatterTile(ORANGE_GHOST_ID) : level.pac().tile();

            default -> throw GameException.invalidGhostID(ghostID);
        };
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        gateKeeper.resetCounterAndSetEnabled(true);
        setBlinkyCruiseElroyEnabled(false);
        level.pac().die();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        THE_SIMULATION_STEP.killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        if (level.victims().size() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_EATEN_IN_LEVEL;
            scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number());
        }
    }

    // Food handling

    @Override
    protected void onPelletEaten(Vector2i tile) {
        scorePoints(PELLET_VALUE);
        level.pac().setRestingTicks(1);
        updateBlinkyCruiseElroy();
    }

    @Override
    protected void onEnergizerEaten(Vector2i tile) {
        scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        level.pac().setRestingTicks(3);
        Logger.info("Resting 3 ticks");
        updateBlinkyCruiseElroy();
        level.victims().clear();
        long powerTicks = pacPowerTicks(level);
        if (powerTicks > 0) {
            huntingTimer().stop();
            Logger.info("Hunting Pac-Man stopped as he got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / TICKS_PER_SECOND);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseAtNextOccasion);
            THE_SIMULATION_STEP.setPacGotPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);
        }
    }

    @Override
    public void onGameEnding() {
        playingProperty().set(false);
        if (!THE_COIN_MECHANISM.isEmpty()) {
            THE_COIN_MECHANISM.consumeCoin();
        }
        updateHighScore();
        level.showMessage(GameLevel.Message.GAME_OVER);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    protected LevelData createLevelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        level.setDemoLevel(false);
        setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        createLevel(1);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
        setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(1));
        huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }

    @Override
    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.makeReadyForPlaying();
        initAnimationOfPacManAndGhosts();
        speedControl.setActorBaseSpeed(level);
        levelCounter().update(level);
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.Message.GAME_OVER);
            score().setEnabled(false);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());

        } else {
            level.showMessage(GameLevel.Message.READY);
            score().setEnabled(true);
            highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(level.number() + 1);
        startLevel();
        level.showPacAndGhosts();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }
}