/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameModel implements EnumMethods<GameModel> {

    MS_PACMAN {

        private static final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");
        private static final byte[] BONUS_VALUE_BY_100 = {1, 2, 5, 7, 10, 20, 50}; // * 100
        private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };

        /**
         * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href=" https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        private static final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 1-4
            {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 5+
        };

        @Override
        public String pacName() {
            return "Ms. Pac-Man";
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        public boolean isBonusReached(GameLevel level) {
            return level.world().eatenFoodCount() == 64 || level.world().eatenFoodCount() == 176;
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
        @Override
        public byte nextBonusSymbol(int levelNumber) {
            return switch (levelNumber) {
                case 1 -> 0; // Cherries
                case 2 -> 1; // Strawberry
                case 3 -> 2; // Orange (not peach!)
                case 4 -> 3; // Pretzel (a Brez'n, Herrgottsakra!)
                case 5 -> 4; // Apple
                case 6 -> 5; // Pear
                case 7 -> 6; // Banana
                default -> {
                    int random = randomInt(0, 320);
                    if (random <  50) yield 0; // 5/32
                    if (random < 100) yield 1; // 5/32
                    if (random < 150) yield 2; // 5/32
                    if (random < 200) yield 3; // 5/32
                    if (random < 240) yield 4; // 4/32
                    if (random < 280) yield 5; // 4/32
                    else              yield 6; // 4/32
                }
            };
        }

        @Override
        public int bonusValue(byte symbol) {
            return BONUS_VALUE_BY_100[symbol] * 100;
        }

        @Override
        public Optional<Bonus> createNextBonus(World world, Bonus existingBonus, int bonusIndex, byte symbol) {
            if (existingBonus != null && existingBonus.state() != Bonus.STATE_INACTIVE) {
                Logger.info("Previous bonus is still active, skip this one");
                return Optional.empty();
            }
            var bonus = createMovingBonus(world, symbol, bonusValue(symbol), RND.nextBoolean());
            bonus.setEdible(TickTimer.INDEFINITE);
            return Optional.of(bonus);
        }

        /**
         * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the house and
         * finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * TODO: This is not the exact behavior as in the original Arcade game.
         **/
        private Bonus createMovingBonus(World world, byte symbol, int points, boolean leftToRight) {
            var houseEntry = tileAt(world.house().door().entryPosition());
            var houseEntryOpposite= houseEntry.plus(0, world.house().size().y() + 1);
            var entryPortal = world.portals().get(RND.nextInt(world.portals().size()));
            var exitPortal  = world.portals().get(RND.nextInt(world.portals().size()));

            var route = List.of(
                np(leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd()),
                np(houseEntry),
                np(houseEntryOpposite),
                np(houseEntry),
                np(leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0))
            );

            var movingBonus = new MovingBonus(symbol, points);
            movingBonus.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            // pass copy of list because route gets modified
            movingBonus.setRoute(new ArrayList<>(route), leftToRight);
            Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
            return movingBonus;
        }


        @Override
        public int[] huntingDurations(int levelNumber) {
            return HUNTING_DURATIONS[levelNumber <= 4 ? 0 : 1];
        }

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        private World createWorld(int levelNumber) {
            return createMsPacManWorld(mapNumberMsPacMan(levelNumber));
        }

        @Override
        public void createAndStartLevel(int levelNumber) {
            checkLevelNumber(levelNumber);
            var level = new GameLevel(levelData(levelNumber, false), createWorld(levelNumber));
            setLevel(level);
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (levelNumber <= 7) {
                // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
                // (also inside a level) whenever a bonus score is reached. At least that's what I was told.
                addSymbolToLevelCounter(level.bonusSymbol(0));
            }
            level.pac().setAutopilot(new RuleBasedPacSteering(level));
            Logger.info("Level {} created ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            level.letsGetReadyToRumble(false);
            Logger.info("Level {} started ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        @Override
        public void createAndStartDemoLevel() {
            var level = new GameLevel(levelData(1, true), createWorld(1));
            setLevel(level);
            level.pac().setAutopilot(new RuleBasedPacSteering(level));
            level.pac().setUseAutopilot(true);
            Logger.info("Demo level created ({})", this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts have been created!
            level.letsGetReadyToRumble(true);
            Logger.info("Demo Level started ({})", this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

    },

    PACMAN {

        private static final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-pacman.xml");
        private static final byte[] BONUS_VALUE_BY_100 = {1, 3, 5, 7, 10, 20, 30, 50}; // * 100
        private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };

        // Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
        private static final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1}, // Level 1
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1}, // Levels 2-4
            {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1}, // Levels 5+
        };

        @Override
        public String pacName() {
            return "Pac-Man";
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        public boolean isBonusReached(GameLevel level) {
            return level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
        }

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        public byte nextBonusSymbol(int levelNumber) {
            return switch (levelNumber) {
                case 1 ->      0; // Cherries
                case 2 ->      1; // Strawberry;
                case 3, 4 ->   2; // peach
                case 5, 6 ->   3; // Apple;
                case 7, 8 ->   4; // Grapes;
                case 9, 10 ->  5; // Galaxian
                case 11, 12 -> 6; // Bell
                default ->     7; // Key
            };
        }

        @Override
        public Optional<Bonus> createNextBonus(World world, Bonus existingBonus, int bonusIndex, byte symbol) {
            var bonus = new StaticBonus(symbol, bonusValue(symbol));
            bonus.entity().setPosition(ArcadeWorld.BONUS_POSITION);
            bonus.setEdible(randomInt(9 * FPS, 10 * FPS));
            return Optional.of(bonus);
        }

        @Override
        public int bonusValue(byte symbol) {
            return BONUS_VALUE_BY_100[symbol] * 100;
        }

        @Override
        public int[] huntingDurations(int levelNumber) {
            return switch (levelNumber) {
                case 1       -> HUNTING_DURATIONS[0];
                case 2, 3, 4 -> HUNTING_DURATIONS[1];
                default      -> HUNTING_DURATIONS[2];
            };
        }

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        @Override
        public void createAndStartLevel(int levelNumber) {
            checkLevelNumber(levelNumber);
            var level = new GameLevel(levelData(levelNumber, false), createPacManWorld());
            setLevel(level);
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            addSymbolToLevelCounter(level.bonusSymbol(0));
            level.pac().setAutopilot(new RuleBasedPacSteering(level));
            Logger.info("Level {} created ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            level.letsGetReadyToRumble(false);
            Logger.info("Level {} started ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        @Override
        public void createAndStartDemoLevel() {
            var level = new GameLevel(levelData(1, true), createPacManWorld());
            setLevel(level);
            level.pac().setAutopilot(new RouteBasedSteering(List.of(ArcadeWorld.PACMAN_DEMO_LEVEL_ROUTE)));
            level.pac().setUseAutopilot(true);
            Logger.info("Demo level created ({})", this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts have been created!
            level.letsGetReadyToRumble(true);
            Logger.info("Demo Level started ({})", this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }
    };

    public static final byte RED_GHOST    = 0;
    public static final byte PINK_GHOST   = 1;
    public static final byte CYAN_GHOST   = 2;
    public static final byte ORANGE_GHOST = 3;

    /** Game loop frequency. */
    public static final byte FPS = 60;

    /** Base speed of creatures (Pac-Man, ghosts, moving bonus). */
    public static final byte PPS_AT_100_PERCENT = 75; // base speed (at 100% relative speed) in pixels/sec
    public static final byte PPS_GHOST_INHOUSE = 38; // correct?
    public static final short PPS_GHOST_RETURNING_HOME = 150; // correct?
    public static final byte MAX_CREDIT = 99;
    public static final byte LEVEL_COUNTER_MAX_SYMBOLS = 7;
    public static final byte RESTING_TICKS_NORMAL_PELLET = 1;
    public static final byte RESTING_TICKS_ENERGIZER = 3;
    public static final byte POINTS_NORMAL_PELLET = 10;
    public static final byte POINTS_ENERGIZER = 50;
    public static final short POINTS_ALL_GHOSTS_KILLED_IN_LEVEL = 12_000;
    public static final short EXTRA_LIFE_SCORE = 10_000;
    public static final short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure
    public static final short PAC_POWER_FADING_TICKS = 2 * FPS; // unsure

    protected static final byte[][] RAW_LEVEL_DATA = {
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
    };

    public static GameLevel.Data levelData(int levelNumber, boolean demoLevel) {
        checkLevelNumber(levelNumber);
        int index = Math.min(levelNumber - 1, RAW_LEVEL_DATA.length - 1);
        return new GameLevel.Data(levelNumber, demoLevel, RAW_LEVEL_DATA[index]);
    }

    protected final List<GameEventListener> gameEventListeners = new ArrayList<>();
    protected final List<Byte> levelCounter = new LinkedList<>();
    protected final Score score = new Score();
    protected final Score highScore = new Score();

    protected GameLevel level;
    protected boolean playing;
    protected short initialLives = 3;
    protected short lives;

    public abstract String pacName();

    public abstract String ghostName(byte id);

    public abstract boolean isBonusReached(GameLevel level);

    public abstract byte nextBonusSymbol(int levelNumber);

    public abstract int bonusValue(byte symbol);

    public List<Byte> supplyBonusSymbols(int levelNumber) {
        return List.of(nextBonusSymbol(levelNumber), nextBonusSymbol(levelNumber));
    }

    public abstract Optional<Bonus> createNextBonus(World world, Bonus bonus, int bonusIndex, byte symbol);

    public abstract int[] huntingDurations(int levelNumber);

    public abstract File highScoreFile();

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    /**
     * Starts new game level with the given number.
     *
     * @param levelNumber level number (starting at 1)
     */
    public abstract void createAndStartLevel(int levelNumber);

    /**
     * Creates and starts the demo game level ("attract mode"). Behavior of the ghosts is different from the original
     * Arcade game because they do not follow a predetermined path but change their direction randomly when frightened.
     * In Pac-Man variant, Pac-Man at least follows the same path as in the Arcade game, but in Ms. Pac-Man game, she
     * does not behave as in the Arcade game but hunts the ghosts using some goal-driven algorithm.
     */
    public abstract void createAndStartDemoLevel();

    // Why does the default implementation returns NULL as soon as the enum classes have methods?
    @Override
    public GameModel[] enumValues() {
        return new GameModel[] {GameModel.MS_PACMAN, GameModel.PACMAN};
    }

    /**
     * Resets the game and deletes the current level. Credit, immunity and scores remain unchanged.
     */
    public void reset() {
        level = null;
        lives = initialLives;
        Logger.info("Game model ({}) reset", this);
    }

    public void setLevel(GameLevel level) {
        this.level = level;
        if (level != null) {
            score.setLevelNumber(level.number());
        }
    }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public short initialLives() {
        return initialLives;
    }

    public void setInitialLives(short initialLives) {
        this.initialLives = initialLives;
    }

    public int lives() {
        return lives;
    }

    public void addLives(short lives) {
        this.lives += lives;
    }

    public void loseLife() {
        if (lives == 0) {
            throw new IllegalArgumentException("No life left to loose :-(");
        }
        --lives;
    }

    public List<Byte> levelCounter() {
        return levelCounter;
    }

    public void addSymbolToLevelCounter(byte symbol) {
        levelCounter.add(symbol);
        if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
            levelCounter.removeFirst();
        }
    }

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public void updateHighScore() {
        var file = highScoreFile();
        var oldHighScore = new Score();
        oldHighScore.loadFromFile(file);
        if (highScore.points() > oldHighScore.points()) {
            highScore.saveToFile(file, String.format("%s High Score", name()));
        }
    }

    public void addListener(GameEventListener listener) {
        checkNotNull(listener);
        gameEventListeners.add(listener);
    }

    public void publishGameEvent(GameEventType type) {
        publish(new GameEvent(type, this));
    }

    public void publishGameEvent(GameEventType type, Vector2i tile) {
        publish(new GameEvent(type, this, tile));
    }

    public void publish(GameEvent event) {
        Logger.trace("Publish game event: {}", event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}