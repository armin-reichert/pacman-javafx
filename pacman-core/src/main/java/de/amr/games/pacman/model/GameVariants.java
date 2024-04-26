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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.CreatureMovement.followTarget;
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
        final int[] HUNTING_TICKS_1_TO_4 = {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1};
        final int[] HUNTING_TICKS_5_PLUS = {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1};

        @Override
        long huntingTicks(int levelNumber, int phaseIndex) {
            long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }

        @Override
        public String highScoreFileName() {
            return "highscore-ms_pacman.xml";
        }

        @Override
        void buildLevel(int levelNumber, boolean demoLevel) {
            this.demoLevel = demoLevel;
            if (demoLevel) {
                byte[] levelNumbers = {1, 3, 6, 10, 14, 18}; // these numbers cover all 6 available mazes
                this.levelNumber = levelNumbers[randomInt(0, levelNumbers.length)];
                Logger.info("Demo Level maze number: {}", mazeNumberMsPacMan(levelNumber));
            } else {
                this.levelNumber = checkLevelNumber(levelNumber);
            }

            world = createMsPacManWorld(mapNumberMsPacMan(this.levelNumber));
            bonusSymbols.add(computeBonusSymbol());
            bonusSymbols.add(computeBonusSymbol());

            pac = new Pac("Ms. Pac-Man");
            pac.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            pac.setAutopilot(new RuleBasedPacSteering(this));
            pac.setUseAutopilot(demoLevel);

            ghosts = new Ghost[] {
                new Ghost(RED_GHOST,    "Blinky"),
                new Ghost(PINK_GHOST,   "Pinky"),
                new Ghost(CYAN_GHOST,   "Inky"),
                new Ghost(ORANGE_GHOST, "Sue")
            };
            ghosts().forEach(ghost -> {
                ghost.setHouse(world.house());
                ghost.setFrightenedBehavior(coward -> coward.roam(world, frightenedGhostSpeedPct(coward)));
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE / (float) FPS);
            });
            ghosts[RED_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST));
            ghosts[PINK_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST));
            ghosts[CYAN_GHOST].setRevivalPosition(world.ghostPosition(CYAN_GHOST));
            ghosts[ORANGE_GHOST].setRevivalPosition(world.ghostPosition(ORANGE_GHOST));

            // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
            // (also inside a level) whenever a bonus score is reached. At least that's what I was told.
            if (this.levelNumber == 1) {
                levelCounter.clear();
            }
            if (!demoLevel && this.levelNumber <= 7) {
                addSymbolToLevelCounter(bonusSymbols.getFirst());
            }
        }

        /**
         * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
         * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
         * only the scatter target of Blinky and Pinky would have been affected. Who knows?
         */
        @Override
        public void huntingBehaviour(Ghost ghost) {
            if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
                ghost.roam(world, huntingSpeedPct(ghost));
            } else {
                Vector2i targetTile = chasingPhase().isPresent() || ghost.id() == RED_GHOST && cruiseElroy > 0
                    ? chasingTarget(ghost) : scatterTarget(ghost);
                followTarget(ghost, world, targetTile, huntingSpeedPct(ghost));
            }
        }

        @Override
        public boolean isPacManKillingIgnored() {
            float levelRunningSeconds = (System.currentTimeMillis() - levelStartTime) / 1000f;
            if (demoLevel && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
                Logger.info("Pac-Man killing ignored, demo level running for {} seconds", levelRunningSeconds);
                return true;
            }
            return false;
        }

        @Override
        boolean isBonusReached() {
            return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
        }

        /**
         * <p>Got this information from
         * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
         * </p>
         * <p style="font-style:italic">
         * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
         * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
         * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
         * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
         * (TODO: what does never mean here? For the rest of the game?).
         * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
         * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
         * the following table:
         *
         * <table>
         * <tr align="left">
         *   <th>Cherry</th><th>Strawberry</th><th>Peach</th><th>Pretzel</th><th>Apple</th><th>Pear&nbsp;</th><th>Banana</th>
         * </tr>
         * <tr align="right">
         *     <td>5/32</td><td>5/32</td><td>5/32</td><td>5/32</td><td>4/32</td><td>4/32</td><td>4/32</td>
         * </tr>
         * </table>
         * </p>
         */
        byte computeBonusSymbol() {
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

        final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

        /**
         * Bonus symbol enters the world at a random portal, walks to the house entry, takes a tour around the
         * house and finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * Note: This is not the exact behavior from the original Arcade game.
         **/
        @Override
        void createNextBonus() {
            if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
                Logger.info("Previous bonus is still active, skip this one");
                return;
            }
            nextBonusIndex += 1;

            boolean leftToRight = RND.nextBoolean();
            var houseEntry = tileAt(world.house().door().entryPosition());
            var houseEntryOpposite = houseEntry.plus(0, world.house().size().y() + 1);
            var entryPortal = world.portals().get(RND.nextInt(world.portals().size()));
            var exitPortal  = world.portals().get(RND.nextInt(world.portals().size()));
            List<NavPoint> route = Stream.of(
                leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
                houseEntry,
                houseEntryOpposite,
                houseEntry,
                leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
            ).map(NavPoint::np).toList();

            byte symbol = bonusSymbols.get(nextBonusIndex);
            var movingBonus = new MovingBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            movingBonus.setRoute(route, leftToRight);
            movingBonus.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
            bonus = movingBonus;
            bonus.setEdible(TickTimer.INDEFINITE);
            publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    },

    /**
     * All about the Pac-Man Arcade game (1980)  can be found in:
     * <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>.
     */
    PACMAN {

        final int[] HUNTING_TICKS_1      = {7*FPS, 20*FPS, 7*FPS, 20*FPS, 5*FPS,   20*FPS, 5*FPS, -1};
        final int[] HUNTING_TICKS_2_TO_4 = {7*FPS, 20*FPS, 7*FPS, 20*FPS, 5*FPS, 1033*FPS, 1,     -1};
        final int[] HUNTING_TICKS_5_PLUS = {5*FPS, 20*FPS, 5*FPS, 20*FPS, 5*FPS, 1037*FPS, 1,     -1};

        @Override
        long huntingTicks(int levelNumber, int phaseIndex) {
            long ticks = switch (levelNumber) {
                case 1       -> HUNTING_TICKS_1[phaseIndex];
                case 2, 3, 4 -> HUNTING_TICKS_2_TO_4[phaseIndex];
                default      -> HUNTING_TICKS_5_PLUS[phaseIndex];
            };
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }

        @Override
        public String highScoreFileName() {
            return "highscore-pacman.xml";
        }

        @Override
        void buildLevel(int levelNumber, boolean demoLevel) {

            this.levelNumber = checkLevelNumber(levelNumber);
            this.demoLevel = demoLevel;

            world = createPacManWorld();
            bonusSymbols.add(computeBonusSymbol());
            bonusSymbols.add(computeBonusSymbol());

            pac = new Pac("Pac-Man");
            pac.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
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
            ghosts().forEach(ghost -> {
                ghost.setHouse(world.house());
                ghost.setForbiddenPassages(world.forbiddenPassages());
                ghost.setFrightenedBehavior(coward -> coward.roam(world, frightenedGhostSpeedPct(coward)));
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE / (float) FPS);
            });
            ghosts[RED_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST));
            ghosts[PINK_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST));
            ghosts[CYAN_GHOST].setRevivalPosition(world.ghostPosition(CYAN_GHOST));
            ghosts[ORANGE_GHOST].setRevivalPosition(world.ghostPosition(ORANGE_GHOST));

            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (!demoLevel) {
                addSymbolToLevelCounter(bonusSymbols.getFirst());
            }
        }

        @Override
        public void huntingBehaviour(Ghost ghost) {
            Vector2i targetTile = chasingPhase().isPresent() || ghost.id() == RED_GHOST && cruiseElroyState() > 0
                ? chasingTarget(ghost) : scatterTarget(ghost);
             followTarget(ghost, world, targetTile, huntingSpeedPct(ghost));
        }

        @Override
        public boolean isPacManKillingIgnored() {
            return false;
        }

        @Override
        boolean isBonusReached() {
            return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
        }

        final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {-1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        byte computeBonusSymbol() {
            return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
        }

        final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

        @Override
        void createNextBonus() {
            nextBonusIndex += 1;
            byte symbol = bonusSymbols.get(nextBonusIndex);
            bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            bonus.entity().setPosition(world.bonusPosition());
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
    final short[] KILLED_GHOST_VALUES = { 200, 400, 800, 1600 };

    final Pulse blinking = new Pulse(10, false);
    final List<Byte> bonusSymbols = new ArrayList<>(2);
    final List<Byte> levelCounter = new ArrayList<>();
    final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    final TickTimer powerTimer = new TickTimer("PacPowerTimer");
    final Score score = new Score();
    final Score highScore = new Score();

    final GateKeeper gateKeeper = new GateKeeper();

    int levelNumber; // 1=first level
    boolean demoLevel;
    long levelStartTime;
    boolean playing;
    byte initialLives;
    byte lives;

    int huntingPhaseIndex;
    byte cruiseElroy;
    byte numGhostsKilledInLevel;
    byte nextBonusIndex; // -1=no bonus, 0=first, 1=second

    Pac pac;
    Ghost[] ghosts;
    Bonus bonus;
    World world;

    GameVariants() {
        initialLives = 3;
        reset();
    }

    abstract void buildLevel(int levelNumber, boolean demoLevel);

    abstract long huntingTicks(int levelNumber, int phaseIndex);

    abstract boolean isBonusReached();

    GameLevel levelData(int levelNumber) {
        return LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)];
    }

    void clearLevel() {
        levelNumber = 0;
        demoLevel = false;
        levelStartTime = 0;
        huntingPhaseIndex = 0;
        huntingTimer.resetIndefinitely();
        numGhostsKilledInLevel = 0;
        cruiseElroy = 0;
        bonus = null;
        nextBonusIndex = -1;
        bonusSymbols.clear();
        pac = null;
        ghosts = null;
        bonus = null;
        world = null;
        blinking.stop();
        blinking.reset();
    }

    void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

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
        checkNotNull(states);
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    @Override
    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public TickTimer powerTimer() {
        return powerTimer;
    }

    @Override
    public Pulse blinking() {
        return blinking;
    }

    @Override
    public void startHuntingPhase(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + phaseIndex);
        }
        huntingPhaseIndex = phaseIndex;
        huntingTimer.reset(huntingTicks(levelNumber, huntingPhaseIndex));
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            huntingPhaseIndex, currentHuntingPhaseName(),
            huntingTimer.duration(), (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }
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
    public Optional<GameLevel> level() {
        if (levelNumber == 0) {
            return Optional.empty();
        }
        return Optional.of(levelData(levelNumber));
    }

    @Override
    public boolean isDemoLevel() {
        return demoLevel;
    }

    @Override
    public byte cruiseElroyState() {
        return cruiseElroy;
    }

    @Override
    public void startLevel() {
        letsGetReadyToRumble();
        levelStartTime = System.currentTimeMillis();
        Logger.info("{}Level {} started ({})", demoLevel ? "Demo " : "", levelNumber, this);
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void reset() {
        playing = false;
        lives = initialLives;
        clearLevel();
        levelCounter.clear();
        score.reset();
    }

    @Override
    public void createLevel(int levelNumber, boolean demoLevel) {
        clearLevel();
        buildLevel(levelNumber, demoLevel);
        score.setLevelNumber(levelNumber);
        gateKeeper.init(levelNumber);
        Logger.info("{}Level {} created", demoLevel ? "Demo " : "", levelNumber);
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void letsGetReadyToRumble() {
        pac.reset();
        pac.setPosition(world.pacPosition());
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        pac.resetAnimation();
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(world.ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(world.ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    @Override
    public void makeGuysVisible(boolean visible) {
        pac.setVisible(visible);
        ghosts().forEach(ghost -> ghost.setVisible(visible));
    }

    void addSymbolToLevelCounter(byte symbol) {
        levelCounter.add(symbol);
        if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
            levelCounter.removeFirst();
        }
    }

    SimulationStepEventLog eventLog() {
        return GameController.it().eventLog();
    }

    Vector2i scatterTarget(Ghost ghost) {
        return world.ghostScatterTile(ghost.id());
    }

    Vector2i chasingTarget(Ghost ghost) {
        return switch (ghost.id()) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost.tile().euclideanDistance(pac.tile()) < 8 ? scatterTarget(ghost) : pac.tile();
            default -> throw new IllegalGhostIDException(ghost.id());
        };
    }

    byte frightenedGhostSpeedPct(Ghost ghost) {
        return world.isTunnel(ghost.tile())
            ? levelData(levelNumber).ghostSpeedTunnelPct() : levelData(levelNumber).ghostSpeedFrightenedPct();
    }

    byte huntingSpeedPct(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return levelData(levelNumber).ghostSpeedTunnelPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return levelData(levelNumber).elroy1SpeedPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return levelData(levelNumber).elroy2SpeedPct();
        }
        return levelData(levelNumber).ghostSpeedPct();
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

    public void loadHighScore() {
        File file = new File(System.getProperty("user.home"), highScoreFileName());
        highScore().loadFromFile(file);
        Logger.info("Highscore loaded. File: '{}', {} points, level {}",
            file, highScore().points(), highScore().levelNumber());
    }

    @Override
    public void updateHighScore() {
        File file = new File(System.getProperty("user.home"), highScoreFileName());
        var oldHighScore = new Score();
        oldHighScore.loadFromFile(file);
        if (highScore.points() > oldHighScore.points()) {
            highScore.saveToFile(file, String.format("%s High Score", name()));
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
    public void onPacDying() {
        huntingTimer().stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
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
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", levelNumber);
    }

    @Override
    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    abstract void createNextBonus();

    @Override
    public boolean isLevelComplete() {
        return world.uneatenFoodCount() == 0;
    }

    @Override
    public boolean isPacManKilled() {
        if (GameController.it().isPacImmune()) {
            return false;
        }
        eventLog().pacKilled = ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
        return eventLog().pacKilled;
    }

    @Override
    public boolean areGhostsKilled() {
        return !eventLog().killedGhosts.isEmpty();
    }

    @Override
    public void doHuntingStep() {
        blinking.tick();
        checkForFood();
        updateGhosts();
        pac.update(this);
        if (bonus != null) updateBonus();
        updatePacPower();
        updateHuntingTimer();
        ghosts(FRIGHTENED).filter(pac::sameTile).forEach(this::killGhost);
    }

    void checkForFood() {
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
                if (levelData(levelNumber).pacPowerSeconds() > 0) {
                    eventLog().pacGetsPower = true;
                    huntingTimer().stop();
                    Logger.info("Hunting timer stopped");
                    int seconds = levelData(levelNumber).pacPowerSeconds();
                    powerTimer.restartSeconds(seconds);
                    Logger.info("Power timer restarted to {} seconds", seconds);
                    // TODO do already frightened ghosts reverse too?
                    ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
                    ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
                    publishGameEvent(GameEventType.PAC_GETS_POWER);
                }
            } else {
                pac.setRestingTicks(RESTING_TICKS_PELLET);
                scorePoints(POINTS_PELLET);
            }
            gateKeeper.onPelletOrEnergizerEaten(this);
            world.eatFoodAt(pacTile);
            if (world.uneatenFoodCount() == levelData(levelNumber).elroy1DotsLeft()) {
                cruiseElroy = 1;
            } else if (world.uneatenFoodCount() == levelData(levelNumber).elroy2DotsLeft()) {
                cruiseElroy = 2;
            }
            if (isBonusReached()) {
                createNextBonus();
                eventLog().bonusIndex = nextBonusIndex;
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    void updatePacPower() {
        powerTimer.advance();
        if (powerTimer.remaining() == PAC_POWER_FADING_TICKS) {
            eventLog().pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            pac.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog().pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    @Override
    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
    }

    @Override
    public boolean isPowerFadingStarting() {
        return powerTimer.isRunning() && powerTimer.remaining() == PAC_POWER_FADING_TICKS
            || powerTimer.duration() < PAC_POWER_FADING_TICKS && powerTimer.tick() == 1;
    }

    void updateBonus() {
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
        if (huntingTimer.hasExpired()) {
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
            startHuntingPhase(huntingPhaseIndex + 1);
        } else {
            huntingTimer.advance();
        }
    }

    void updateGhosts() {
        // Important: Ghosts must be in order RED, PINK, CYAN, ORANGE!
        Ghost prisoner = ghosts(LOCKED).findFirst().orElse(null);
        if (prisoner != null) {
            String releaseInfo = gateKeeper.checkReleaseOf(this, prisoner);
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
                    Logger.trace("Re-enable cruise elroy mode because {} exits house:", prisoner.name());
                    setCruiseElroyEnabled(true);
                }
            }
        }
        ghosts().forEach(ghost -> ghost.update(this));
    }

    @Override
    public void killGhost(Ghost ghost) {
        eventLog().killedGhosts.add(ghost);
        int killedSoFar = pac.victims().size();
        int points = KILLED_GHOST_VALUES[killedSoFar];
        ghost.eaten(killedSoFar);
        scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        numGhostsKilledInLevel += 1;
        if (numGhostsKilledInLevel == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_IN_LEVEL;
            scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, levelNumber);
        }
        pac.victims().add(ghost);
    }

    // Level test

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
            createNextBonus();
        } else if (testState.timer().atSecond(4.5)) {
            bonus().ifPresent(bonus -> bonus.setEaten(60));
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else if (testState.timer().atSecond(6.5)) {
            bonus().ifPresent(Bonus::setInactive); // needed?
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
            blinking.restart(2 * levelData(levelNumber).numFlashes());
        } else if (testState.timer().atSecond(12.0)) {
            testState.timer().restartIndefinitely();
            pac.freeze();
            ghosts().forEach(Ghost::hide);
            bonus().ifPresent(Bonus::setInactive);
            testState.setProperty("mazeFlashing", false);
            blinking.reset();
            createLevel(levelNumber + 1, false);
            startLevel();
            makeGuysVisible(true);
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