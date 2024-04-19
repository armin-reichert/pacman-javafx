/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.actors.CreatureMovement.followTarget;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariants implements GameModel {

    MS_PACMAN {

        /**
         * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 1-4
            {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 5+
        };

        long huntingDuration(int phaseIndex) {
            return HUNTING_DURATIONS[levelNumber <= 4 ? 0 : 1][phaseIndex];
        }

        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        @Override
        public void createAndStartLevel(int levelNumber, boolean demoLevel) {
            checkLevelNumber(levelNumber);
            this.levelNumber = levelNumber;
            this.demoLevel = demoLevel;

            world = createMsPacManWorld(mapNumberMsPacMan(levelNumber));

            initGhostHouseAccess();

            pac = new Pac("Ms. Pac-Man");
            pac.reset();
            pac.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            pac.setPowerFadingTicks(PAC_POWER_FADING_TICKS); // not sure about duration
            pac.setAutopilot(new RuleBasedPacSteering(this));
            pac.setUseAutopilot(demoLevel);

            ghosts = new Ghost[] {
                new Ghost(RED_GHOST,    "Blinky"),
                new Ghost(PINK_GHOST,   "Pinky"),
                new Ghost(CYAN_GHOST,   "Inky"),
                new Ghost(ORANGE_GHOST, "Sue")
            };
            ghosts().forEach(ghost -> {
                ghost.reset();
                ghost.setHouse(world.house());
                ghost.setFrightenedBehavior(this::frightenedGhostBehaviour);
                ghost.setRevivalPosition(GHOST_REVIVAL_POSITIONS[ghost.id()]);
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE / (float) FPS);
            });

            huntingPhaseIndex = 0;
            huntingTimer.resetIndefinitely();
            numGhostsKilledInLevel = 0;
            cruiseElroyState = 0;

            nextBonusIndex = -1;
            bonusSymbols.clear();
            bonusSymbols.add(nextBonusSymbol());
            bonusSymbols.add(nextBonusSymbol());
            bonus = null;

            score.setLevelNumber(levelNumber);

            // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
            // (also inside a level) whenever a bonus score is reached. At least that's what I was told.
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (levelNumber <= 7) {
                levelCounter.add(bonusSymbols.getFirst());
                if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                    levelCounter.removeFirst();
                }
            }

            Logger.info("Level {} created", levelNumber);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            letsGetReadyToRumble();

            if (demoLevel) {
                pac.show();
                ghosts().forEach(Ghost::show);
            } else {
                pac.hide();
                ghosts().forEach(Ghost::hide);
            }

            Logger.info("Level {} started", levelNumber);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        /**
         * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
         * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
         * only the scatter target of Blinky and Pinky would have been affected. Who knows?
         */
        @Override
        public void huntingBehaviour(Ghost ghost) {
            if (scatterPhase().isPresent() && scatterPhase().get() == 0
                && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
                roam(ghost, world, huntingSpeedPercentage(ghost), pseudoRandomDirection());
            } else {
                if (chasingPhase().isPresent() || ghost.id() == RED_GHOST && cruiseElroyState() > 0) {
                    followTarget(ghost, world, chasingTarget(ghost.id()), huntingSpeedPercentage(ghost));
                } else {
                    followTarget(ghost, world, ghostScatterTarget(ghost.id()), huntingSpeedPercentage(ghost));
                }
            }
        }

        @Override
        boolean isBonusReached() {
            return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
        }

        /**
         * <p>Got this information from
         * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
         * </p>
         * <p><em>
         * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
         * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
         * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
         * spawn.</em></p>
         *
         * <p><b>Dying while a fruit is on screen causes it to immediately disappear and never return.
         * (TODO: what does never mean here? For the rest of the game?)</b></p>
         *
         * <p><em>
         * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
         * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
         * the following table:
         * </em></p>
         *
         * <table>
         * <tr>
         *   <th>Cherry</th>
         *   <th>Strawberry</th>
         *   <th>Peach</th>
         *   <th>Pretzel</th>
         *   <th>Apple</th>
         *   <th>Pear</th>
         *   <th>Banana</th>
         * </tr>
         * <tr align="right">
         *   <td>5/32</td>
         *   <td>5/32</td>
         *   <td>5/32</td>
         *   <td>5/32</td>
         *   <td>4/32</td>
         *   <td>4/32</td>
         *   <td>4/32</td>
         * </tr>
         * </table>
         */
        byte nextBonusSymbol() {
            if (levelNumber <= 7) {
                return (byte) (levelNumber - 1);
            }
            int choice = randomInt(0, 320);
            if (choice <  50) return 0; // 5/32 probability
            if (choice < 100) return 1; // 5/32
            if (choice < 150) return 2; // 5/32
            if (choice < 200) return 3; // 5/32
            if (choice < 240) return 4; // 4/32
            if (choice < 280) return 5; // 4/32
            else              return 6; // 4/32
        }

        void createNextBonus() {
            if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
                Logger.info("Previous bonus is still active, skip this one");
                return;
            }
            createMovingBonus(bonusSymbols.get(nextBonusIndex), RND.nextBoolean());
            publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }

        final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

        /**
         * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the
         * house and finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * TODO: This is not the exact behavior as in the original Arcade game.
         **/
        void createMovingBonus(byte symbol, boolean leftToRight) {
            var houseEntry = tileAt(world.house().door().entryPosition());
            var houseEntryOpposite = houseEntry.plus(0, world.house().size().y() + 1);
            var entryPortal = world.portals().get(RND.nextInt(world.portals().size()));
            var exitPortal  = world.portals().get(RND.nextInt(world.portals().size()));

            var route = List.of(
                np(leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd()),
                np(houseEntry),
                np(houseEntryOpposite),
                np(houseEntry),
                np(leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0))
            );

            bonus = new MovingBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            ((MovingBonus) bonus).setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            // pass *copy* of list because route gets modified when moving!
            ((MovingBonus) bonus).setRoute(new ArrayList<>(route), leftToRight);
            bonus.setEdible(TickTimer.INDEFINITE);
            Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        }
    },

    /**
     * All about the Pac-Man Arcade game (1980)  can be found in:
     * <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>.
     */
    PACMAN {

        final int[][] HUNTING_DURATIONS = { // Hunting duration (in ticks) of chase and scatter phases.
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1}, // Level 1
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1}, // Levels 2-4
            {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1}, // Levels 5+
        };

        long huntingDuration(int phaseIndex) {
            int duration = switch (levelNumber) {
                case 1       -> HUNTING_DURATIONS[0][phaseIndex];
                case 2, 3, 4 -> HUNTING_DURATIONS[1][phaseIndex];
                default      -> HUNTING_DURATIONS[2][phaseIndex];
            };
            return duration != -1 ? duration : TickTimer.INDEFINITE;
        }

        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-pacman.xml");

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        @Override
        public void createAndStartLevel(int levelNumber, boolean demoLevel) {
            checkLevelNumber(levelNumber);
            this.levelNumber = levelNumber;
            this.demoLevel = demoLevel;

            world = createPacManWorld();
            initGhostHouseAccess();

            pac = new Pac("Pac-Man");
            pac.reset();
            pac.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            pac.setPowerFadingTicks(PAC_POWER_FADING_TICKS); // not sure about duration
            if (demoLevel) {
                pac.setAutopilot(new RouteBasedSteering(List.of(ArcadeWorld.PACMAN_DEMO_LEVEL_ROUTE)));
                pac.setUseAutopilot(true);
            } else {
                pac.setAutopilot(new RuleBasedPacSteering(this));
                pac.setUseAutopilot(false);
            }

            ghosts = new Ghost[] {
                new Ghost(RED_GHOST,    "Blinky"),
                new Ghost(PINK_GHOST,   "Pinky"),
                new Ghost(CYAN_GHOST,   "Inky"),
                new Ghost(ORANGE_GHOST, "Clyde")
            };
            var forbidden = new HashMap<Vector2i, List<Direction>>();
            var up = List.of(UP);
            ArcadeWorld.PACMAN_RED_ZONE.forEach(tile -> forbidden.put(tile, up));
            ghosts().forEach(ghost -> {
                ghost.reset();
                ghost.setHouse(world.house());
                ghost.setFrightenedBehavior(this::frightenedGhostBehaviour);
                ghost.setRevivalPosition(GHOST_REVIVAL_POSITIONS[ghost.id()]);
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE / (float) FPS);
                ghost.setForbiddenMoves(forbidden);
            });

            huntingPhaseIndex = 0;
            huntingTimer.resetIndefinitely();
            numGhostsKilledInLevel = 0;
            cruiseElroyState = 0;

            nextBonusIndex = -1;
            bonusSymbols.clear();
            bonusSymbols.add(nextBonusSymbol());
            bonusSymbols.add(nextBonusSymbol());
            bonus = null;

            score.setLevelNumber(levelNumber);
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            levelCounter.add(bonusSymbols.getFirst());
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                levelCounter.removeFirst();
            }

            Logger.info("Level {} created ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            letsGetReadyToRumble();
            if (demoLevel) {
                pac.show();
                ghosts().forEach(Ghost::show);
            } else {
                pac.hide();
                ghosts().forEach(Ghost::hide);
            }
            Logger.info("Level {} started ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        @Override
        public void huntingBehaviour(Ghost ghost) {
            byte relSpeed = huntingSpeedPercentage(ghost);
            if (chasingPhase().isPresent() || ghost.id() == RED_GHOST && cruiseElroyState() > 0) {
                followTarget(ghost, world, chasingTarget(ghost.id()), relSpeed);
            } else {
                followTarget(ghost, world, ghostScatterTarget(ghost.id()), relSpeed);
            }
        }

        @Override
        boolean isBonusReached() {
            return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
        }

        final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {-1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        byte nextBonusSymbol() {
            return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
        }

        final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

        void createNextBonus() {
            byte symbol = bonusSymbols.get(nextBonusIndex);
            bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            bonus.entity().setPosition(ArcadeWorld.BONUS_POSITION);
            bonus.setEdible(randomInt(9 * FPS, 10 * FPS));
            publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    };

    // --- Common to all variants --------------------------------------------------------------------------------------

    final GameLevel[] LEVELS = {
        /* 1*/ new GameLevel( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0),
        /* 2*/ new GameLevel( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1),
        /* 3*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0),
        /* 4*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0),
        /* 5*/ new GameLevel(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2),
        /* 6*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0),
        /* 7*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 8*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 9*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3),
        /*10*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0),
        /*11*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0),
        /*12*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0),
        /*13*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3),
        /*14*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0),
        /*15*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*16*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*17*/ new GameLevel(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3),
        /*18*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*19*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*20*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*21*/ new GameLevel( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0)
    };

    final Direction[] GHOST_DIRECTIONS_ON_START = {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP};

    final Vector2f[] GHOST_POSITIONS_ON_START = {
        ArcadeWorld.HOUSE_ENTRY_POSITION,
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_LEFT_SEAT,
        ArcadeWorld.HOUSE_RIGHT_SEAT
    };

    final Vector2f[] GHOST_REVIVAL_POSITIONS = {
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_LEFT_SEAT,
        ArcadeWorld.HOUSE_RIGHT_SEAT
    };

    final byte  POINTS_PELLET = 10;
    final byte  POINTS_ENERGIZER = 50;
    final short POINTS_ALL_GHOSTS_IN_LEVEL = 12_000;
    final short EXTRA_LIFE_SCORE = 10_000;
    final byte  LEVEL_COUNTER_MAX_SYMBOLS = 7;
    final short PAC_POWER_FADING_TICKS = 2 * FPS; // unsure
    final short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure
    final byte  RESTING_TICKS_PELLET = 1;
    final byte  RESTING_TICKS_ENERGIZER = 3;
    final byte  PPS_GHOST_INSIDE_HOUSE = 30; // correct?
    final byte  PPS_GHOST_RETURNING_HOME = 120; // correct?

    final List<Byte> bonusSymbols = new ArrayList<>();
    final List<Byte> levelCounter = new LinkedList<>();
    final Score score = new Score();
    final Score highScore = new Score();

    Pac pac;
    Ghost[] ghosts;
    Bonus bonus;
    World world;

    int levelNumber;
    boolean demoLevel;
    boolean playing;
    byte initialLives = 3;
    byte lives;

    final Pulse blinking = new Pulse(10, false);
    final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    int huntingPhaseIndex;
    byte numGhostsKilledInLevel;
    byte nextBonusIndex; // -1=no bonus, 0=first, 1=second
    byte cruiseElroyState;

    /** Ghost house access control */
    static class HouseAccessData {
        static final byte NO_DOT_LIMIT = -1;
        byte[] globalDotLimits;
        byte[] privateDotLimits;
        int[] dotCounters;
        int pacStarvingLimit;
        int globalDotCounter;
        boolean globalDotCounterEnabled;
    }
    final HouseAccessData houseAccess = new HouseAccessData();

    @Override
    public Pac pac() {
        return pac;
    }

    @Override
    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    @Override
    public Stream<Ghost> ghosts(GhostState... states) {
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    @Override
    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public Pulse blinking() {
        return blinking;
    }

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param phaseIndex hunting phase index (0..7)
     */
    @Override
    public void startHuntingPhase(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + phaseIndex);
        }
        huntingPhaseIndex = phaseIndex;
        huntingTimer.reset(huntingDuration(phaseIndex));
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            phaseIndex, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    abstract long huntingDuration(int phaseIndex);

    abstract boolean isBonusReached();

    @Override
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    @Override
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    @Override
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    @Override
    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    @Override
    public int levelNumber() {
        return levelNumber;
    }

    @Override
    public boolean isDemoLevel() {
        return demoLevel;
    }

    @Override
    public byte cruiseElroyState() {
        return cruiseElroyState;
    }

    /**
     * @param cruiseElroyState Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    void setCruiseElroyState(int cruiseElroyState) {
        if (cruiseElroyState < -2 || cruiseElroyState > 2) {
            throw new IllegalArgumentException(
                "Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
        }
        this.cruiseElroyState = (byte) cruiseElroyState;
        Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
    }

    void enableCruiseElroyState(boolean enabled) {
        if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
            cruiseElroyState = (byte) (-cruiseElroyState);
            Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
        }
    }

    SimulationStepEventLog eventLog() {
        return GameController.it().eventLog();
    }

    Direction pseudoRandomDirection() {
        int rnd = Globals.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }

    void frightenedGhostBehaviour(Ghost ghost) {
        roam(ghost, world, frightenedGhostRelSpeed(ghost), pseudoRandomDirection());
    }

    Vector2i ghostScatterTarget(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST    -> SCATTER_TILE_NE;
            case PINK_GHOST   -> SCATTER_TILE_NW;
            case CYAN_GHOST   -> SCATTER_TILE_SE;
            case ORANGE_GHOST -> SCATTER_TILE_SW;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost(ORANGE_GHOST).tile().euclideanDistance(pac.tile()) < 8
                ? ArcadeWorld.SCATTER_TILE_SW
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    byte frightenedGhostRelSpeed(Ghost ghost) {
        return world.isTunnel(ghost.tile())
            ? level().ghostSpeedTunnelPercentage() : level().ghostSpeedFrightenedPercentage();
    }

    byte huntingSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return level().ghostSpeedTunnelPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
            return level().elroy1SpeedPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
            return level().elroy2SpeedPercentage();
        }
        return level().ghostSpeedPercentage();
    }

    @Override
    public GameLevel level() {
        return levelNumber != 0 ? LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)] : null;
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public int initialLives() {
        return initialLives;
    }

    @Override
    public void setInitialLives(int lives) {
        initialLives = (byte) lives;
    }

    @Override
    public int lives() {
        return lives;
    }

    @Override
    public void addLives(int lives) {
        this.lives += (byte) lives;
    }

    @Override
    public void loseLife() {
        if (lives == 0) {
            throw new IllegalArgumentException("No life left to loose :-(");
        }
        --lives;
    }

    @Override
    public List<Byte> levelCounter() {
        return levelCounter;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public void scorePoints(int points) {
        if (demoLevel) {
            return;
        }
        int oldScore = score.points();
        int newScore = oldScore + points;
        score.setPoints(newScore);
        if (newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }
        if (oldScore < EXTRA_LIFE_SCORE && newScore >= EXTRA_LIFE_SCORE) {
            addLives(1);
            publishGameEvent(GameEventType.EXTRA_LIFE_WON);
        }
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.loadFromFile(highScoreFile());
        if (highScore.points() > oldHighScore.points()) {
            highScore.saveToFile(highScoreFile(), String.format("%s High Score", name()));
        }
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void reset() {
        levelNumber = 0;
        playing = false;
        lives = initialLives;
        score.reset();
        Logger.info("Game model ({}) reset", this);
    }

    @Override
    public void letsGetReadyToRumble() {
        pac.reset();
        pac.setPosition(ArcadeWorld.PAC_POSITION);
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        pac.resetAnimation();
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(GHOST_POSITIONS_ON_START[ghost.id()]);
            ghost.setMoveAndWishDir(GHOST_DIRECTIONS_ON_START[ghost.id()]);
            ghost.setState(LOCKED);
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    @Override
    public void onPacDying() {
        huntingTimer().stop();
        Logger.info("Hunting timer stopped");
        resetGlobalDotCounterAndSetEnabled(true);
        enableCruiseElroyState(false);
        pac.die();
    }

    @Override
    public void onLevelCompleted() {
        blinking.setStartPhase(Pulse.OFF);
        blinking.reset();
        pac.freeze();
        ghosts().forEach(Ghost::hide);
        bonus().ifPresent(Bonus::setInactive);
        huntingTimer().stop();
        Logger.info("Hunting timer stopped");
        Logger.trace("Game level {} completed.", levelNumber);
    }

    @Override
    public void doLevelTestStep(GameState testState) {
        if (levelNumber > 20) {
            GameController.it().restart(GameState.BOOT);
            return;
        }
        if (testState.timer().tick() > 2 * FPS) {
            blinking.tick();
            ghosts().forEach(ghost -> ghost.update(this));
            bonus().ifPresent(bonus -> bonus.update(this));
        }
        if (testState.timer().atSecond(1.0)) {
            letsGetReadyToRumble();
            pac.show();
            ghosts().forEach(Ghost::show);
        } else if (testState.timer().atSecond(2)) {
            blinking.setStartPhase(Pulse.ON);
            blinking.restart();
        } else if (testState.timer().atSecond(2.5)) {
            nextBonusIndex += 1;
            createNextBonus();
        } else if (testState.timer().atSecond(4.5)) {
            bonus().ifPresent(bonus -> bonus.setEaten(60));
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else if (testState.timer().atSecond(6.5)) {
            bonus().ifPresent(Bonus::setInactive); // needed?
            nextBonusIndex += 1;
            createNextBonus();
        } else if (testState.timer().atSecond(7.5)) {
            bonus().ifPresent(bonus -> bonus.setEaten(60));
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else if (testState.timer().atSecond(8.5)) {
            pac.hide();
            ghosts().forEach(Ghost::hide);
            blinking.stop();
            blinking.setStartPhase(Pulse.ON);
            blinking.reset();
        } else if (testState.timer().atSecond(9.5)) {
            testState.setProperty("mazeFlashing", true);
            blinking.setStartPhase(Pulse.OFF);
            blinking.restart(2 * level().numFlashes());
        } else if (testState.timer().atSecond(12.0)) {
            testState.timer().restartIndefinitely();
            pac.freeze();
            ghosts().forEach(Ghost::hide);
            bonus().ifPresent(Bonus::setInactive);
            testState.setProperty("mazeFlashing", false);
            blinking.reset();
            createAndStartLevel(levelNumber + 1, false);
        }
    }

    // Bonus Management

    @Override
    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    abstract void createNextBonus();

    // Main logic

    @Override
    public boolean isLevelComplete() {
        return world.uneatenFoodCount() == 0;
    }

    @Override
    public boolean isPacManKilled() {
        var killers = ghosts(HUNTING_PAC).filter(pac::sameTile).toList();
        if (!killers.isEmpty() && !GameController.it().isPacImmune()) {
            eventLog().pacDied = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean areGhostsKilled() {
        return !eventLog().killedGhosts.isEmpty();
    }

    @Override
    public void doHuntingStep() {
        checkFoodEaten();
        updateGhosts();
        updatePac();
        updateBonus();
        updateHuntingTimer();
        blinking.tick();
        var prey = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
        if (!prey.isEmpty()) {
            killGhosts(prey);
        }
    }

    void checkFoodEaten() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog().foodFoundTile = pacTile;
            pac.onStarvingEnd();
            if (world.isEnergizerTile(pacTile)) {
                eventLog().energizerFound = true;
                pac.setRestingTicks(RESTING_TICKS_ENERGIZER);
                pac.victims().clear();
                scorePoints(POINTS_ENERGIZER);
                Logger.info("Scored {} points for eating energizer", POINTS_ENERGIZER);
                if (level().pacPowerSeconds() > 0) {
                    eventLog().pacGetsPower = true;
                    huntingTimer().stop();
                    Logger.info("Hunting timer stopped");
                    pac.powerTimer().restartSeconds(level().pacPowerSeconds());
                    // TODO do already frightened ghosts reverse too?
                    ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
                    ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
                    publishGameEvent(GameEventType.PAC_GETS_POWER);
                }
            } else {
                pac.setRestingTicks(RESTING_TICKS_PELLET);
                scorePoints(POINTS_PELLET);
            }
            updateDotCount();
            world.eatFoodAt(pacTile);
            if (world.uneatenFoodCount() == level().elroy1DotsLeft()) {
                setCruiseElroyState(1);
            } else if (world.uneatenFoodCount() == level().elroy2DotsLeft()) {
                setCruiseElroyState(2);
            }
            if (isBonusReached()) {
                nextBonusIndex += 1;
                eventLog().bonusIndex = nextBonusIndex;
                createNextBonus();
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    void updatePac() {
        pac.update(this);
        if (pac.powerTimer().remaining() == PAC_POWER_FADING_TICKS) {
            eventLog().pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (pac.powerTimer().hasExpired()) {
            pac.powerTimer().stop();
            pac.powerTimer().resetIndefinitely();
            huntingTimer().start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog().pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    void updateBonus() {
        if (bonus == null) {
            return;
        }
        if (bonus.state() == Bonus.STATE_EDIBLE && pac.sameTile(bonus.entity())) {
            bonus.setEaten(BONUS_POINTS_SHOWN_TICKS);
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog().bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    void updateHuntingTimer( ) {
        if (huntingTimer().hasExpired()) {
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
            startHuntingPhase(huntingPhaseIndex + 1);
        } else {
            huntingTimer().advance();
        }
    }

    void updateGhosts() {
        // Important: Ghosts must be in order RED, PINK, CYAN, ORANGE!
        Ghost prisoner = ghosts(LOCKED).findFirst().orElse(null);
        if (prisoner != null) {
            String releaseInfo = checkReleaseOf(prisoner);
            if (releaseInfo != null) {
                eventLog().releasedGhost = prisoner;
                eventLog().ghostReleaseInfo = releaseInfo;
                if (prisoner.insideHouse(world.house())) {
                    prisoner.setState(LEAVING_HOUSE);
                } else {
                    prisoner.setMoveAndWishDir(LEFT);
                    prisoner.setState(HUNTING_PAC);
                }
                if (prisoner.id() == ORANGE_GHOST && cruiseElroyState() < 0) {
                    enableCruiseElroyState(true);
                    Logger.trace("Cruise elroy mode re-enabled because {} exits house", prisoner.name());
                }
            }
        }
        ghosts().forEach(ghost -> ghost.update(this));
    }

    @Override
    public void killGhosts(List<Ghost> prey) {
        if (!prey.isEmpty()) {
            prey.forEach(this::killGhost);
            if (numGhostsKilledInLevel == 16) {
                int points = POINTS_ALL_GHOSTS_IN_LEVEL;
                scorePoints(points);
                Logger.info("Scored {} points for killing all ghosts at level {}", points, levelNumber);
            }
        }
    }

    void killGhost(Ghost ghost) {
        byte[] multiple = { 2, 4, 8, 16 };
        int killedSoFar = pac.victims().size();
        int points = 100 * multiple[killedSoFar];
        scorePoints(points);
        ghost.eaten(killedSoFar);
        pac.victims().add(ghost);
        eventLog().killedGhosts.add(ghost);
        numGhostsKilledInLevel += 1;
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    // Ghost house access

    /**
     * From the Pac-Man dossier:
     * <p>
     * Commonly referred to as the ghost house or monster pen, this cordoned-off area in the center of the maze is the
     * domain of the four ghosts and off-limits to Pac-Man.
     * </p>
     * <p>
     * Whenever a level is completed or a life is lost, the ghosts are returned to their starting positions in and around
     * the ghost house before play continues—Blinky is always located just above and outside, while the other three are
     * placed inside: Inky on the left, Pinky in the middle, and Clyde on the right.
     * The pink door on top is used by the ghosts to enter or exit the house. Once a ghost leaves, however, it cannot
     * reenter unless it is first captured by Pac-Man—then the disembodied eyes can return home to be revived.
     * Since Blinky is already on the outside after a level is completed or a life is lost, the only time he can get
     * inside the ghost house is after Pac-Man captures him, and he immediately turns around to leave once revived.
     * That's about all there is to know about Blinky's behavior in terms of the ghost house, but determining when the
     * other three ghosts leave home is an involved process based on several variables and conditions.
     * The rest of this section will deal with them exclusively. Accordingly, any mention of “the ghosts” below refers t
     * o Pinky, Inky, and Clyde, but not Blinky.
     * </p>
     * <p>
     * The first control used to evaluate when the ghosts leave home is a personal counter each ghost retains for
     * tracking the number of dots Pac-Man eats. Each ghost's “dot counter” is reset to zero when a level begins and can
     * only be active when inside the ghost house, but only one ghost's counter can be active at any given time regardless
     * of how many ghosts are inside. The order of preference for choosing which ghost's counter to activate is:
     * Pinky, then Inky, and then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its
     * dot counter increased by one. Each ghost also has a “dot limit” associated with his counter, per level.
     * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its dot counter is
     * deactivated (but not reset). The most-preferred ghost still waiting inside the house (if any) activates its timer
     * at this point and begins counting dots.
     * </p>
     * <p>
     * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level begins.
     * For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This results in Pinky exiting
     * immediately which, in turn, activates Inky's dot counter. His counter must then reach or exceed 30 dots before
     * he can leave the house. Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and
     * starts counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's dot limit
     * is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the house as soon as the level
     * begins from now on. Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
     * and will leave the ghost house immediately at the start of every level.
     * </p>
     * <p>
     * Whenever a life is lost, the system disables (but does not reset) the ghosts' individual dot counters and uses
     * a global dot counter instead. This counter is enabled and reset to zero after a life is lost, counting the number
     * of dots eaten from that point forward. The three ghosts inside the house must wait for this special counter to
     * tell them when to leave. Pinky is released when the counter value is equal to 7 and Inky is released when it
     * equals 17. The only way to deactivate the counter is for Clyde to be inside the house when the counter equals 32;
     * otherwise, it will keep counting dots even after the ghost house is empty. If Clyde is present at the
     * appropriate time, the global counter is reset to zero and deactivated, and the ghosts' personal dot limits are
     * re-enabled and used as before for determining when to leave the house (including Clyde who is still in the house
     * at this time).
     * </p>
     * <p>
     * If dot counters were the only control, Pac-Man could simply stop eating dots early on and keep the ghosts
     * trapped inside the house forever. Consequently, a separate timer control was implemented to handle this case by
     * tracking the amount of time elapsed since Pac-Man has last eaten a dot. This timer is always running but gets
     * reset to zero each time a dot is eaten. Anytime Pac-Man avoids eating dots long enough for the timer to reach
     * its limit, the most-preferred ghost waiting in the ghost house (if any) is forced to leave immediately, and
     * the timer is reset to zero. The same order of preference described above is used by this control as well.
     * The game begins with an initial timer limit of four seconds, but lowers to it to three seconds starting with
     * level five.
     * </p>
     * <p>
     * The more astute reader may have already noticed there is subtle flaw in this system resulting in a way to
     * keep Pinky, Inky, and Clyde inside the ghost house for a very long time after eating them.
     * The trick involves having to sacrifice a life in order to reset and enable the global dot counter,
     * and then making sure Clyde exits the house before that counter is equal to 32.
     * This is accomplished by avoiding eating dots and waiting for the timer limit to force Clyde out.
     * Once Clyde is moving for the exit, start eating dots again until at least 32 dots have been consumed since
     * the life was lost. Now head for an energizer and gobble up some ghosts. Blinky will leave the house immediately
     * as usual, but the other three ghosts will remain “stuck” inside as long as Pac-Man continues eating dots with
     * sufficient frequency as not to trigger the control timer. Why does this happen?
     * The key lies in how the global dot counter works—it cannot be deactivated if Clyde is outside the house when
     * the counter has a value of 32. By letting the timer force Clyde out before 32 dots are eaten, the global dot
     * counter will keep counting dots instead of deactivating when it reaches 32. Now when the ghosts are eaten by
     * Pac-Man and return home, they will still be using the global dot counter to determine when to leave.
     * As previously described, however, this counter's logic only checks for three values: 7, 17, and 32, and
     * once those numbers are exceeded, the counter has no way to release the ghosts associated with them.
     * The only control left to release the ghosts is the timer which can be easily avoided by eating a dot every
     * so often to reset it.
     * </p>
     * </pre>
     */
    void initGhostHouseAccess() {
        houseAccess.globalDotLimits = new byte[] {HouseAccessData.NO_DOT_LIMIT, 7, 17, HouseAccessData.NO_DOT_LIMIT};
        houseAccess.privateDotLimits = new byte[] {0, 0, 0, 0};
        if (levelNumber == 1) {
            houseAccess.privateDotLimits[CYAN_GHOST] = 30;
            houseAccess.privateDotLimits[ORANGE_GHOST] = 60;
        } else if (levelNumber == 2) {
            houseAccess.privateDotLimits[ORANGE_GHOST] = 50;
        }
        houseAccess.dotCounters = new int[] {0, 0, 0, 0};
        houseAccess.globalDotCounter = 0;
        houseAccess.globalDotCounterEnabled = false;
        houseAccess.pacStarvingLimit = levelNumber < 5 ? 240 : 180; // 4 sec : 3 sec
    }

    String checkReleaseOf(Ghost prisoner) {
        byte id = prisoner.id();
        if (id == RED_GHOST) {
            return "Red ghost gets released unconditionally";
        }
        // check private dot counter first (if enabled)
        if (!houseAccess.globalDotCounterEnabled && houseAccess.dotCounters[id] >= houseAccess.privateDotLimits[id]) {
            return String.format("Private dot counter reached limit (%d)", houseAccess.privateDotLimits[id]);
        }
        // check global dot counter
        if (houseAccess.globalDotLimits[id] != HouseAccessData.NO_DOT_LIMIT && houseAccess.globalDotCounter >= houseAccess.globalDotLimits[id]) {
            return String.format("Global dot counter reached limit (%d)", houseAccess.globalDotLimits[id]);
        }
        // check Pac-Man starving time
        if (pac.starvingTicks() >= houseAccess.pacStarvingLimit) {
            pac.onStarvingEnd();
            return String.format("%s reached starving limit (%d ticks)", pac.name(), houseAccess.pacStarvingLimit);
        }
        return null;
    }

    void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
        houseAccess.globalDotCounter = 0;
        houseAccess.globalDotCounterEnabled = enabled;
        Logger.trace("Global dot counter set to 0 and {}", enabled ? "enabled" : "disabled");
    }

    void updateDotCount() {
        if (houseAccess.globalDotCounterEnabled) {
            if (ghost(ORANGE_GHOST).inState(LOCKED) && houseAccess.globalDotCounter == 32) {
                Logger.trace("{} inside house when global counter reached 32", ghost(ORANGE_GHOST).name());
                resetGlobalDotCounterAndSetEnabled(false);
            } else {
                houseAccess.globalDotCounter++;
                Logger.trace("Global dot counter = {}", houseAccess.globalDotCounter);
            }
        } else {
            ghosts(LOCKED).filter(ghost -> ghost.insideHouse(world.house())).findFirst().ifPresent(ghost -> {
                houseAccess.dotCounters[ghost.id()]++;
                Logger.trace("{} dot counter = {}", ghost.name(), houseAccess.dotCounters[ghost.id()]);
            });
        }
    }

    // Game Event Support

    final List<GameEventListener> gameEventListeners = new ArrayList<>();

    @Override
    public void addGameEventListener(GameEventListener listener) {
        checkNotNull(listener);
        gameEventListeners.add(listener);
    }

    @Override
    public void publishGameEvent(GameEventType type) {
        publishGameEvent(new GameEvent(type, this));
    }

    @Override
    public void publishGameEvent(GameEventType type, Vector2i tile) {
        publishGameEvent(new GameEvent(type, this, tile));
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        Logger.trace("Publish game event: {}", event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}