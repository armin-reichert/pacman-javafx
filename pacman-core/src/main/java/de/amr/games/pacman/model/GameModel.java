/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.RouteBasedSteering;
import de.amr.games.pacman.lib.RuleBasedSteering;
import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkLevelNumber;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Pac-Man / Ms. Pac-Man game model.
 *
 * @author Armin Reichert
 */
public class GameModel {

    public static final byte RED_GHOST = 0;
    public static final byte PINK_GHOST = 1;
    public static final byte CYAN_GHOST = 2;
    public static final byte ORANGE_GHOST = 3;

    /** Game loop frequency. */
    public static final short FPS = 60;

    /** Pixels/tick at 100% relative speed. */
    public static final float SPEED_PX_100_PERCENT = 1.25f;
    public static final float SPEED_PX_INSIDE_HOUSE = 0.5f; // correct?
    public static final float SPEED_PX_RETURNING_TO_HOUSE = 2.0f; // correct?
    public static final byte MAX_CREDIT = 99;
    public static final byte LEVEL_COUNTER_MAX_SYMBOLS = 7;
    public static final byte RESTING_TICKS_NORMAL_PELLET = 1;
    public static final byte RESTING_TICKS_ENERGIZER = 3;
    public static final byte POINTS_NORMAL_PELLET = 10;
    public static final byte POINTS_ENERGIZER = 50;
    public static final short POINTS_ALL_GHOSTS_KILLED_IN_LEVEL = 12_000;
    public static final short[] POINTS_GHOSTS_SEQUENCE = {200, 400, 800, 1600};
    public static final short EXTRA_LIFE_SCORE = 10000;
    public static final short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure
    public static final short PAC_POWER_FADES_TICKS = 2 * FPS - 1; // unsure

    private static final byte[][] LEVEL_DATA = {
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

    private static int dataRow(int levelNumber) {
        return (levelNumber - 1) < LEVEL_DATA.length ? (levelNumber - 1) : (LEVEL_DATA.length - 1);
    }

    // Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
    private static final int[][] HUNTING_DURATIONS_PACMAN = {
        {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, -1}, // Level 1
        {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS, 1, -1}, // Level 2-4
        {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS, 1, -1}, // Level 5+
    };

    /**
     * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
     *
     * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
     * @see <a href=" https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
     */
    private static final int[][] HUNTING_DURATIONS_MS_PACMAN = {
        {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 1-4
        {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 5+
    };

    public int[] huntingDurations(int levelNumber) {
        checkLevelNumber(levelNumber);
        return switch (variant) {
            case MS_PACMAN -> HUNTING_DURATIONS_MS_PACMAN[levelNumber <= 4 ? 0 : 1];
            case PACMAN -> switch (levelNumber) {
                case 1 -> HUNTING_DURATIONS_PACMAN[0];
                case 2, 3, 4 -> HUNTING_DURATIONS_PACMAN[1];
                default -> HUNTING_DURATIONS_PACMAN[2];
            };
        };
    }

    // Ms. Pac-Man bonus #3 is an orange, not a peach! (Found in official Arcade machine manual)

    public static final byte MS_PACMAN_CHERRIES = 0;
    public static final byte MS_PACMAN_STRAWBERRY = 1;
    public static final byte MS_PACMAN_ORANGE = 2;
    public static final byte MS_PACMAN_PRETZEL = 3;
    public static final byte MS_PACMAN_APPLE = 4;
    public static final byte MS_PACMAN_PEAR = 5;
    public static final byte MS_PACMAN_BANANA = 6;

    public static final byte[] BONUS_VALUES_MS_PACMAN = {1, 2, 5, 7, 10, 20, 50}; // * 100

    public static final byte PACMAN_CHERRIES = 0;
    public static final byte PACMAN_STRAWBERRY = 1;
    public static final byte PACMAN_PEACH = 2;
    public static final byte PACMAN_APPLE = 3;
    public static final byte PACMAN_GRAPES = 4;
    public static final byte PACMAN_GALAXIAN = 5;
    public static final byte PACMAN_BELL = 6;
    public static final byte PACMAN_KEY = 7;

    public static final byte[] BONUS_VALUES_PACMAN = {1, 3, 5, 7, 10, 20, 30, 50}; // * 100

    private static final File HIGHSCORE_FILE_PACMAN = new File(System.getProperty("user.home"), "highscore-pacman.xml");
    private static final File HIGHSCORE_FILE_MS_PACMAN = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");


    private final GameVariant variant;
    private final List<Byte> levelCounter;
    private final Score score;
    private final Score highScore;
    private GameLevel level;
    private short initialLives;
    private short lives;
    private boolean playing;
    private boolean scoringEnabled;

    public GameModel(GameVariant variant) {
        checkGameVariant(variant);
        this.variant = variant;
        levelCounter = new LinkedList<>();
        score = new Score();
        highScore = new Score();
        initialLives = 3;
    }

    /**
     * Resets the game and deletes the current level. Credit, immunity and scores remain unchanged.
     */
    public void reset() {
        level = null;
        lives = initialLives;
        playing = false;
        scoringEnabled = true;
        Logger.info("Game model ({}) reset", variant);
    }

    /**
     * Starts new game level with the given number.
     *
     * @param levelNumber level number (starting at 1)
     */
    public void createAndStartLevel(int levelNumber) {
        checkLevelNumber(levelNumber);
        World world = switch (variant) {
            case MS_PACMAN -> createMsPacManWorld(mapNumberMsPacMan(levelNumber));
            case PACMAN -> createPacManWorld();
        };
        level = new GameLevel(this, world, levelNumber, LEVEL_DATA[dataRow(levelNumber)], false);

        if (levelNumber == 1) {
            levelCounter.clear();
        }
        // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
        // (also inside the same level) whenever a bonus is earned. That's what I was told.
        if (variant == GameVariant.PACMAN || levelNumber <= 7) {
            levelCounter.add(level.bonusSymbol(0));
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                levelCounter.removeFirst();
            }
        }
        score.setLevelNumber(levelNumber);

        Logger.info("Level {} created ({})", levelNumber, variant);
        publishGameEvent(this, GameEventType.LEVEL_CREATED);

        // at this point the animations of Pac-Man and the ghosts must have been created!
        level.letsGetReadyToRumble(false);

        Logger.info("Level {} started ({})", levelNumber, variant);
        publishGameEvent(this, GameEventType.LEVEL_STARTED);
    }

    /**
     * Creates the demo game level ("attract mode").
     */
    public void startDemoLevel() {
        scoringEnabled = false;
        switch (variant) {
            case MS_PACMAN -> {
                var world = createMsPacManWorld(1);
                level = new GameLevel(this, world, 1, LEVEL_DATA[0], true);
                // TODO this is not the exact behavior from the Arcade game
                level.pac().setSteering(new RuleBasedSteering());
            }
            case PACMAN -> {
                var world = createPacManWorld();
                level = new GameLevel(this, world, 1, LEVEL_DATA[0], true);
                // TODO this is not the exact behavior from the Arcade game
                level.pac().setSteering(new RouteBasedSteering(List.of(ArcadeWorld.PACMAN_DEMO_LEVEL_ROUTE)));
            }
        }
        Logger.info("Demo level created ({})", variant);
        publishGameEvent(this, GameEventType.LEVEL_CREATED);

        score.setLevelNumber(level.number());
        level.letsGetReadyToRumble(true);
        Logger.info("Demo Level {} started ({})", level.number(), variant);

        publishGameEvent(this, GameEventType.LEVEL_STARTED);
    }

    public void removeLevel() {
        level = null;
    }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public GameVariant variant() {
        return variant;
    }

    /**
     * @return number of maze (not map) used in level, 1-based.
     */
    public int mazeNumber(int levelNumber) {
        return variant == GameVariant.MS_PACMAN ? ArcadeWorld.mazeNumberMsPacMan(levelNumber) : 1;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public short initialLives() {
        return initialLives;
    }

    public void setInitialLives(short initialLives) {
        this.initialLives = initialLives;
    }

    public short lives() {
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
        return Collections.unmodifiableList(levelCounter);
    }

    public void clearLevelCounter() {
        levelCounter.clear();
    }

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public void scorePoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Scored points value must not be negative but is: " + points);
        }
        if (level == null) {
            throw new IllegalStateException("Cannot score points: No game level exists");
        }
        if (!scoringEnabled) {
            return;
        }
        var oldScore = score.points();
        var newScore = oldScore + points;
        score.setPoints(newScore);
        if (newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(level.number());
            highScore.setDate(LocalDate.now());
        }
        if (oldScore < EXTRA_LIFE_SCORE && newScore >= EXTRA_LIFE_SCORE) {
            lives += 1;
            publishGameEvent(this, GameEventType.EXTRA_LIFE_WON);
        }
    }

    private File highScoreFile() {
        return switch (variant) {
            case MS_PACMAN -> HIGHSCORE_FILE_MS_PACMAN;
            case PACMAN -> HIGHSCORE_FILE_PACMAN;
        };
    }

    private static void loadScore(Score score, File file) {
        try (var in = new FileInputStream(file)) {
            var p = new Properties();
            p.loadFromXML(in);
            var points = Integer.parseInt(p.getProperty("points"));
            var levelNumber = Integer.parseInt(p.getProperty("level"));
            var date = LocalDate.parse(p.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
            score.setPoints(points);
            score.setLevelNumber(levelNumber);
            score.setDate(date);
            Logger.info("Score loaded. File: '{}' Points: {} Level: {}", file.getAbsolutePath(), score.points(),
                score.levelNumber());
        } catch (Exception x) {
            Logger.error("Score could not be loaded. File '{}' Reason: {}", file, x.getMessage());
        }
    }

    public void loadHighScore() {
        loadScore(highScore, highScoreFile());
    }

    public void updateHighScore() {
        var file = highScoreFile();
        var savedHiscore = new Score();
        loadScore(savedHiscore, file);
        if (highScore.points() > savedHiscore.points()) {
            var p = new Properties();
            p.setProperty("points", String.valueOf(highScore.points()));
            p.setProperty("level", String.valueOf(highScore.levelNumber()));
            p.setProperty("date", highScore.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            p.setProperty("url", "https://github.com/armin-reichert/pacman-basic");
            try (var out = new FileOutputStream(file)) {
                p.storeToXML(out, String.format("%s High Score", variant));
                Logger.info("High Score saved to '{}' Points: {} Level: {}", file, highScore.points(), highScore.levelNumber());
            } catch (Exception x) {
                Logger.error("High Score could not be saved to '{}': {}", file, x.getMessage());
            }
        }
    }
}