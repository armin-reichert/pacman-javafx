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
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.actors.CreatureMovement.followTarget;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariants implements GameModel, EnumMethodMixin<GameVariants> {

    MS_PACMAN {

        final String PAC_NAME = "Ms. Pac-Man";
        final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };
        final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50}; // * 100
        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");

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

        @Override
        public String pacName() {
            return PAC_NAME;
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        @Override
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
            checkLevelNumber(levelNumber);
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

        @Override
        public int bonusValue(byte symbol) {
            return BONUS_VALUE_FACTORS[symbol] * 100;
        }

        @Override
        public Optional<Bonus> createNextBonus(World world, Bonus existingBonus, int bonusIndex, byte symbol) {
            if (existingBonus != null && existingBonus.state() != Bonus.STATE_INACTIVE) {
                Logger.info("Previous bonus is still active, skip this one");
                return Optional.empty();
            }
            var bonus = createMovingBonus(world, symbol, RND.nextBoolean());
            bonus.setEdible(TickTimer.INDEFINITE);
            return Optional.of(bonus);
        }

        /**
         * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the
         * house and finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * TODO: This is not the exact behavior as in the original Arcade game.
         **/
        private Bonus createMovingBonus(World world, byte symbol, boolean leftToRight) {
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

            var movingBonus = new MovingBonus(symbol, bonusValue(symbol));
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

        @Override
        public void createAndStartLevel(int levelNumber) {
            checkLevelNumber(levelNumber);
            var data = new GameLevel.Data(levelNumber, false, RAW_LEVEL_DATA[levelDataIndex(levelNumber)]);
            var level = new GameLevel(data, createMsPacManWorld(mapNumberMsPacMan(levelNumber)));
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
            var data = new GameLevel.Data(1, true, RAW_LEVEL_DATA[0]);
            var level = new GameLevel(data, createMsPacManWorld(1));
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

        /**
         * <p>
         * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say,
         * the original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man
         * but because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who knows?
         * </p>
         */
        @Override
        public void huntingBehaviour(Ghost ghost, GameLevel level) {
            if (level.scatterPhase().isPresent() && (ghost.id() == GameModel.RED_GHOST || ghost.id() == GameModel.PINK_GHOST)) {
                roam(ghost, level.world(), level.huntingSpeedPercentage(ghost), level.pseudoRandomDirection());
            } else {
                PACMAN.huntingBehaviour(ghost, level);
            }
        }
    },

    PACMAN {

        final String PAC_NAME = "Pac-Man";
        final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };
        final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {7 /* default */, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
        final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50}; // * 100
        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-pacman.xml");

        // Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
        final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1}, // Level 1
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1}, // Levels 2-4
            {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1}, // Levels 5+
        };

        @Override
        public String pacName() {
            return PAC_NAME;
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        @Override
        public boolean isBonusReached(GameLevel level) {
            return level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
        }

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        @Override
        public byte nextBonusSymbol(int levelNumber) {
            return BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber < 13 ? levelNumber : 0];
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
            return BONUS_VALUE_FACTORS[symbol] * 100;
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
            var data = new GameLevel.Data(levelNumber, false, RAW_LEVEL_DATA[levelDataIndex(levelNumber)]);
            var level = new GameLevel(data, createPacManWorld());
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
            var data = new GameLevel.Data(1, true, RAW_LEVEL_DATA[0]);
            var level = new GameLevel(data, createPacManWorld());
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

        @Override
        public void huntingBehaviour(Ghost ghost, GameLevel level) {
            byte relSpeed = level.huntingSpeedPercentage(ghost);
            if (level.chasingPhase().isPresent() || ghost.id() == GameModel.RED_GHOST && level.cruiseElroyState() > 0) {
                followTarget(ghost, level.world(), level.chasingTarget(ghost.id()), relSpeed);
            } else {
                followTarget(ghost, level.world(), level.ghostScatterTarget(ghost.id()), relSpeed);
            }
        }
    };

    // Common to all variants

    final byte[][] RAW_LEVEL_DATA = {
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

    int levelDataIndex(int levelNumber) {
        return Math.min(levelNumber - 1, RAW_LEVEL_DATA.length - 1);
    }

    final List<Byte> levelCounter = new LinkedList<>();
    final Score score = new Score();
    final Score highScore = new Score();

    GameLevel level;
    boolean playing;
    short initialLives = 3;
    short lives;

    // Why does the default implementation return NULL as soon as the enum classes have methods?
    @Override
    public GameVariants[] enumValues() {
        return new GameVariants[] {GameVariants.MS_PACMAN, GameVariants.PACMAN};
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
        level = null;
        lives = initialLives;
        Logger.info("Game model ({}) reset", this);
    }

    @Override
    public void setLevel(GameLevel level) {
        this.level = level;
        if (level != null) {
            score.setLevelNumber(level.number());
        }
    }

    @Override
    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    @Override
    public short initialLives() {
        return initialLives;
    }

    @Override
    public void setInitialLives(short initialLives) {
        this.initialLives = initialLives;
    }

    @Override
    public int lives() {
        return lives;
    }

    @Override
    public void addLives(int lives) {
        this.lives += (short) lives;
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
    public void addSymbolToLevelCounter(byte symbol) {
        levelCounter.add(symbol);
        if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
            levelCounter.removeFirst();
        }
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public void scorePoints(int levelNumber, int points) {
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