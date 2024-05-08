/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.*;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.actors.GhostState.*;

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
        final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
        final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

        final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

        {
            initialLives = 3;
            highScoreFileName = "highscore-ms_pacman.xml";
            reset();
            Logger.info("Game variant {} initialized.", this);
        }

        @Override
        public World createWorld(int mapNumber) {
            return switch (mapNumber) {
                case 1 -> createArcadeWorld("/maps/mspacman_1.terrain", "/maps/mspacman_1.food");
                case 2 -> createArcadeWorld("/maps/mspacman_2.terrain", "/maps/mspacman_2.food");
                case 3 -> createArcadeWorld("/maps/mspacman_3.terrain", "/maps/mspacman_3.food");
                case 4 -> createArcadeWorld("/maps/mspacman_4.terrain", "/maps/mspacman_4.food");
                default -> throw new IllegalArgumentException("Ms. Pac-Man map number must be in 1-4, is: " + mapNumber);
            };
        }

        /**
         * In Ms. Pac-Man, there are 4 maps used by the 6 mazes. Up to level 13, the mazes are:
         * <ul>
         * <li>Maze #1: pink maze, white dots (level 1-2)
         * <li>Maze #2: light blue maze, yellow dots (level 3-5)
         * <li>Maze #3: orange maze, red dots (level 6-9)
         * <li>Maze #4: dark blue maze, white dots (level 10-13)
         * </ul>
         * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
         * <ul>
         * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
         * <li>Maze #6: orange maze, white dots (same map as maze #4)
         * </ul>
         * <p>
         */
        public int mazeNumber(int levelNumber) {
            checkLevelNumber(levelNumber);
            return switch (levelNumber) {
                case 1, 2 -> 1;
                case 3, 4, 5 -> 2;
                case 6, 7, 8, 9 -> 3;
                case 10, 11, 12, 13 -> 4;
                default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;  // alternate between maze #5 and #6 every 4th level
            };
        }

        public int mapNumber(int levelNumber) {
            checkLevelNumber(levelNumber);
            // from level 14, alternate between map #3 and #4 every 4th level
            return levelNumber <= 13 ? mazeNumber(levelNumber) : mazeNumber(levelNumber) - 2;
        }

        @Override
        long huntingTicks(int levelNumber, int phaseIndex) {
            long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }

        @Override
        void buildRegularLevel(int levelNumber) {
            this.levelNumber = checkLevelNumber(levelNumber);
            populateLevel(createWorld(mapNumber(levelNumber)));
            pac.setName("Ms. Pac-Man");
            pac.setAutopilot(new RuleBasedPacSteering(this));
            pac.setUseAutopilot(false);
            ghosts[ORANGE_GHOST].setName("Sue");
        }

        @Override
        void buildDemoLevel() {
            byte[] levelNumbers = {1, 3, 6, 10, 14, 18}; // these numbers cover all 6 available mazes
            levelNumber = levelNumbers[randomInt(0, levelNumbers.length)];
            populateLevel(createWorld(mapNumber(levelNumber)));
            pac.setName("Ms. Pac-Man");
            pac.setAutopilot(new RuleBasedPacSteering(this));
            pac.setUseAutopilot(true);
            ghosts[ORANGE_GHOST].setName("Sue");
        }

        /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
         */
        @Override
        void updateLevelCounter() {
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (!demoLevel && levelNumber < 8) {
                levelCounter.add(bonusSymbols[0]);
                if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                    levelCounter.removeFirst();
                }
            }
        }

        /**
         * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
         * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
         * only the scatter target of Blinky and Pinky would have been affected. Who knows?
         */
        @Override
        public void letGhostHunt(Ghost ghost) {
            byte speed = huntingSpeedPct(ghost);
            if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
                ghost.roam(speed);
            } else {
                // even phase: scattering, odd phase: chasing
                boolean chasing = isOdd(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
                ghost.followTarget(chasing ? chasingTarget(ghost) : scatterTarget(ghost), speed);
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
        @Override
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

        /**
         * Bonus symbol enters the world at a random portal, walks to the house entry, takes a tour around the
         * house and finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * Note: This is not the exact behavior from the original Arcade game.
         **/
        @Override
        public void createNextBonus() {
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

            byte symbol = bonusSymbols[nextBonusIndex];
            var movingBonus = new MovingBonus(world, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            movingBonus.setRoute(route, leftToRight);
            movingBonus.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
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

        static final NavPoint[] ARCADE_MAP_DEMO_LEVEL_ROUTE = {
            np(12, 26), np(9, 26), np(12, 32), np(15, 32), np(24, 29), np(21, 23),
            np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17),
            np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8),
            np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8),
            np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17),
            np(21, 17), np(21, 23), np(21, 26), np(24, 29), /* avoid moving up: */ np(26, 29),
            np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26),
            np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29),
            np(15, 32), np(12, 32), np(3, 29), np(6, 23)
        };

        final int[] HUNTING_TICKS_1      = {420, 1200, 420, 1200, 300,  1200, 300, -1};
        final int[] HUNTING_TICKS_2_TO_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
        final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 300, 1200, 300, 62262,   1, -1};

        final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {-1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
        final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

        {
            initialLives = 3;
            highScoreFileName = "highscore-pacman.xml";
            reset();
            Logger.info("Game variant {} initialized.", this);
        }

        @Override
        public  World createWorld(int mapNumber) {
            World world = createArcadeWorld("/maps/pacman.terrain", "/maps/pacman.food");
            world.setDemoLevelRoute(List.of(ARCADE_MAP_DEMO_LEVEL_ROUTE));
            List<Direction> up = List.of(UP);
            Map<Vector2i, List<Direction>> fp = new HashMap<>();
            Stream.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26)).forEach(tile -> fp.put(tile, up));
            world.setForbiddenPassages(fp);
            world.setBonusPosition(halfTileRightOf(13, 20));
            return world;
        }

        @Override
        public int mapNumber(int levelNumber) {
            return 1;
        }

        @Override
        public int mazeNumber(int levelNumber) {
            return 1;
        }

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
        void buildRegularLevel(int levelNumber) {
            this.levelNumber = checkLevelNumber(levelNumber);
            populateLevel(createWorld(1));
            pac.setName("Pac-Man");
            pac.setAutopilot(new RuleBasedPacSteering(this));
            pac.setUseAutopilot(false);
        }

        @Override
        void buildDemoLevel() {
            levelNumber = 1;
            populateLevel(createWorld(1));
            pac.setAutopilot(world.getDemoLevelRoute().isEmpty()
                ? new RuleBasedPacSteering(this)
                : new RouteBasedSteering(world.getDemoLevelRoute()));
            pac.setUseAutopilot(true);
        }

        @Override
        void updateLevelCounter() {
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (!demoLevel) {
                levelCounter.add(bonusSymbols[0]);
                if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                    levelCounter.removeFirst();
                }
            }
        }

        @Override
        public void letGhostHunt(Ghost ghost) {
            byte speed = huntingSpeedPct(ghost);
            // even phase: scattering, odd phase: chasing
            boolean chasing = isOdd(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
            ghost.followTarget(chasing ? chasingTarget(ghost) : scatterTarget(ghost), speed);
        }

        @Override
        public boolean isPacManKillingIgnored() {
            return false;
        }

        @Override
        boolean isBonusReached() {
            return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
        }

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        @Override
        byte computeBonusSymbol() {
            return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
        }

        @Override
        public void createNextBonus() {
            nextBonusIndex += 1;
            byte symbol = bonusSymbols[nextBonusIndex];
            bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
            bonus.entity().setPosition(world.bonusPosition());
            bonus.setEdible(randomInt(540, 600));
            publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    };

    // --- Common to all variants --------------------------------------------------------------------------------------

    static final GameLevel[] LEVELS = {
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

    static GameLevel level(int levelNumber) {
        return LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)];
    }

    static final byte    POINTS_PELLET = 10;
    static final byte    POINTS_ENERGIZER = 50;
    static final short   POINTS_ALL_GHOSTS_IN_LEVEL = 12_000;
    static final short   EXTRA_LIFE_SCORE = 10_000;
    static final byte    LEVEL_COUNTER_MAX_SYMBOLS = 7;
    static final byte    PAC_POWER_FADING_TICKS = 120; // unsure
    static final byte    BONUS_POINTS_SHOWN_TICKS = 120; // unsure
    static final byte    RESTING_TICKS_PELLET = 1;
    static final byte    RESTING_TICKS_ENERGIZER = 3;
    static final byte    PPS_GHOST_INSIDE_HOUSE = 30; // correct?
    static final byte    PPS_GHOST_RETURNING_HOME = 120; // correct?
    static final short[] KILLED_GHOST_VALUES = { 200, 400, 800, 1600 };

    final Pulse          blinking = new Pulse(10, Pulse.OFF);
    final byte[]         bonusSymbols = new byte[2];
    final List<Byte>     levelCounter = new ArrayList<>();
    final TickTimer      huntingTimer = new TickTimer("HuntingTimer");
    final TickTimer      powerTimer = new TickTimer("PacPowerTimer");
    final List<Ghost>    victims = new ArrayList<>();
    final Score          score = new Score();
    final Score          highScore = new Score();
    final GateKeeper     gateKeeper = new GateKeeper();

    String               highScoreFileName;
    int                  levelNumber; // 1=first level
    boolean              demoLevel;
    long                 levelStartTime;
    boolean              playing;
    int                  initialLives;
    int                  lives;

    byte                 huntingPhaseIndex;
    byte                 cruiseElroy;
    byte                 numGhostsKilledInLevel;
    byte                 nextBonusIndex; // -1=no bonus, 0=first, 1=second

    Pac                  pac;
    Ghost[]              ghosts;
    Bonus                bonus;
    World                world;

    SimulationStepEventLog eventLog;

    abstract void buildRegularLevel(int levelNumber);

    abstract void buildDemoLevel();

    abstract byte computeBonusSymbol();

    abstract long huntingTicks(int levelNumber, int phaseIndex);

    abstract boolean isBonusReached();

    abstract void updateLevelCounter();

    House createArcadeHouse() {
        var house = new House();
        house.setSize(v2i(8, 5));
        house.setDoor(new Door(v2i(13, 15), v2i(14, 15)));
        return house;
    }

    void validateArcadeMapSize(TileMap map) {
        if (map.numCols() != ARCADE_MAP_TILES_X || map.numRows() != ARCADE_MAP_TILES_Y) {
            throw new IllegalArgumentException(
                String.format("Arcade map must have %d columns and %d rows but has %d columns and %d rows",
                    ARCADE_MAP_TILES_X, ARCADE_MAP_TILES_Y, map.numCols(), map.numRows()));
        }
    }

    World createArcadeWorld(String terrainMapURL, String foodMapURL) {
        var terrainMap = TileMap.fromURL(getClass().getResource(terrainMapURL), Tiles.TERRAIN_TILES_END);
        validateArcadeMapSize(terrainMap);
        var foodMap = TileMap.fromURL(getClass().getResource(foodMapURL), Tiles.FOOD_TILES_END);
        validateArcadeMapSize(foodMap);

        var world = new World(terrainMap, foodMap);
        world.setHouse(createArcadeHouse());
        world.house().setTopLeftTile(v2i(10, 15));
        world.setPacPosition(halfTileRightOf(13, 26));
        world.setGhostPositions(new Vector2f[] {
            halfTileRightOf(13, 14), // red ghost
            halfTileRightOf(13, 17), // pink ghost
            halfTileRightOf(11, 17), // cyan ghost
            halfTileRightOf(15, 17)  // orange ghost
        });
        world.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        world.setGhostScatterTiles(new Vector2i[] {
            v2i(25,  0), // near right-upper corner
            v2i( 2,  0), // near left-upper corner
            v2i(27, 34), // near right-lower corner
            v2i( 0, 34)  // near left-lower corner
        });
        return world;
    }

    void clearLevel() {
        levelNumber = 0;
        levelStartTime = 0;
        huntingPhaseIndex = 0;
        huntingTimer.resetIndefinitely();
        numGhostsKilledInLevel = 0;
        cruiseElroy = 0;
        bonus = null;
        nextBonusIndex = -1;
        Arrays.fill(bonusSymbols, (byte)-1);
        pac = null;
        ghosts = null;
        world = null;
        blinking.stop();
        blinking.reset();
    }

    void populateLevel(World world) {
        this.world = world;
        bonusSymbols[0] = computeBonusSymbol();
        bonusSymbols[1] = computeBonusSymbol();

        pac = new Pac(world);
        pac.setName("Pac-Man");
        pac.reset();
        pac.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST,    world),
            new Ghost(PINK_GHOST,   world),
            new Ghost(CYAN_GHOST,   world),
            new Ghost(ORANGE_GHOST, world)
        };
        ghosts[RED_GHOST]   .setName("Blinky");
        ghosts[PINK_GHOST]  .setName("Pinky");
        ghosts[CYAN_GHOST]  .setName("Inky");
        ghosts[ORANGE_GHOST].setName("Clyde");

        ghosts[RED_GHOST]   .setRevivalPosition(world.ghostPosition(PINK_GHOST));
        ghosts[PINK_GHOST]  .setRevivalPosition(world.ghostPosition(PINK_GHOST));
        ghosts[CYAN_GHOST]  .setRevivalPosition(world.ghostPosition(CYAN_GHOST));
        ghosts[ORANGE_GHOST].setRevivalPosition(world.ghostPosition(ORANGE_GHOST));

        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
            ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME * SEC_PER_TICK);
            ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE * SEC_PER_TICK);
        });
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
    public List<Ghost> victims() {
        return victims;
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
        huntingPhaseIndex = (byte) phaseIndex;
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
        return Optional.of(level(levelNumber));
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
    public void reset() {
        playing = false;
        lives = initialLives;
        clearLevel();
        score.reset();
    }

    @Override
    public void createRegularLevel(int levelNumber) {
        clearLevel();
        demoLevel = false;
        buildRegularLevel(levelNumber);
        score.setLevelNumber(levelNumber);
        Logger.info("Level {} created", levelNumber);
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void createDemoLevel() {
        clearLevel();
        demoLevel = true;
        buildDemoLevel();
        Logger.info("Demo Level created");
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void startLevel() {
        updateLevelCounter();
        gateKeeper.init(levelNumber);
        letsGetReadyToRumble();
        levelStartTime = System.currentTimeMillis();
        Logger.info("{} started ({})", demoLevel ? "Demo Level" : "Level " + levelNumber, this);
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void letsGetReadyToRumble() {
        pac.reset();
        pac.setPosition(world.pacPosition());
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        pac.animations().ifPresent(Animations::resetSelected);
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

    @Override
    public SimulationStepEventLog eventLog() {
        return eventLog;
    }

    @Override
    public void clearEventLog() {
        eventLog = new SimulationStepEventLog();
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

    byte huntingSpeedPct(Ghost ghost) {
        GameLevel level = level(levelNumber);
        if (world.isTunnel(ghost.tile())) {
            return level.ghostSpeedTunnelPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return level.elroy1SpeedPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return level.elroy2SpeedPct();
        }
        return level.ghostSpeedPct();
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
        initialLives = lives;
    }

    @Override
    public int lives() {
        return lives;
    }

    @Override
    public void addLives(int deltaLives) {
        lives += deltaLives;
    }

    @Override
    public void loseLife() {
        if (lives == 0) {
            Logger.error("No life left to lose :-(");
            return;
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
        File file = new File(System.getProperty("user.home"), highScoreFileName);
        highScore.loadFromFile(file);
        Logger.info("Highscore loaded. File: '{}', {} points, level {}",
            file, highScore.points(), highScore.levelNumber());
    }

    @Override
    public void updateHighScore() {
        File file = new File(System.getProperty("user.home"), highScoreFileName);
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
        huntingTimer.stop();
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
        huntingTimer.stop();
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

    @Override
    public boolean isLevelComplete() {
        return world.uneatenFoodCount() == 0;
    }

    @Override
    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    @Override
    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    @Override
    public void doHuntingStep() {
        blinking.tick();
        checkForFood();
        unlockGhosts();
        ghosts().forEach(ghost -> ghost.update(this));
        pac.update(this);
        if (bonus != null) updateBonus();
        updatePacPower();
        updateHuntingTimer();
        ghosts(FRIGHTENED).filter(pac::sameTile).forEach(this::killGhost);
        eventLog.pacKilled = ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
    }

    void checkForFood() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog.foodFoundTile = pacTile;
            pac.onStarvingEnd();
            if (world.isEnergizerTile(pacTile)) {
                eventLog.energizerFound = true;
                pac.setRestingTicks(RESTING_TICKS_ENERGIZER);
                victims.clear();
                scorePoints(POINTS_ENERGIZER);
                Logger.info("Scored {} points for eating energizer", POINTS_ENERGIZER);
                if (level(levelNumber).pacPowerSeconds() > 0) {
                    eventLog.pacGetsPower = true;
                    huntingTimer.stop();
                    Logger.info("Hunting timer stopped");
                    int seconds = level(levelNumber).pacPowerSeconds();
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
            if (world.uneatenFoodCount() == level(levelNumber).elroy1DotsLeft()) {
                cruiseElroy = 1;
            } else if (world.uneatenFoodCount() == level(levelNumber).elroy2DotsLeft()) {
                cruiseElroy = 2;
            }
            if (isBonusReached()) {
                createNextBonus();
                eventLog.bonusIndex = nextBonusIndex;
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    void updatePacPower() {
        powerTimer.advance();
        if (powerTimer.remaining() == PAC_POWER_FADING_TICKS) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            victims.clear();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
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
            eventLog.bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    void updateHuntingTimer( ) {
        huntingTimer.advance();
        if (huntingTimer.hasExpired()) {
            Logger.info("Hunting timer expired, tick={}", huntingTimer.tick());
            startHuntingPhase(huntingPhaseIndex + 1);
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
        }
    }

    void unlockGhosts() {
        Ghost blinky = ghost(RED_GHOST);
        if (blinky.inState(LOCKED)) {
            if (blinky.insideHouse(world.house())) {
                blinky.setMoveAndWishDir(Direction.UP);
                blinky.setState(LEAVING_HOUSE);
            } else {
                blinky.setMoveAndWishDir(LEFT);
                blinky.setState(HUNTING_PAC);
            }
        }
        // Ghosts in order PINK, CYAN, ORANGE!
        Ghost prisoner = ghosts(LOCKED).findFirst().orElse(null);
        if (prisoner != null) {
            String releaseInfo = gateKeeper.checkReleaseOf(this, prisoner);
            if (releaseInfo != null) {
                eventLog.releasedGhost = prisoner;
                eventLog.ghostReleaseInfo = releaseInfo;
                prisoner.setMoveAndWishDir(Direction.UP);
                prisoner.setState(LEAVING_HOUSE);
                if (prisoner.id() == ORANGE_GHOST && cruiseElroyState() < 0) {
                    Logger.trace("Re-enable cruise elroy mode because {} exits house:", prisoner.name());
                    setCruiseElroyEnabled(true);
                }
            }
        }
    }

    @Override
    public void killGhost(Ghost ghost) {
        eventLog.killedGhosts.add(ghost);
        int killedSoFar = victims.size();
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
        victims.add(ghost);
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
        Logger.trace("Game event: {}", event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}