/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.world.ArcadeWorld;
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

/**
 * Pac-Man / Ms. Pac-Man game model.
 *
 * @author Armin Reichert
 */
public class GameModel {

    public static final byte RED_GHOST    = 0;
    public static final byte PINK_GHOST   = 1;
    public static final byte CYAN_GHOST   = 2;
    public static final byte ORANGE_GHOST = 3;

    /** Game loop frequency. */
    public static final short FPS = 60;

    /** Base speed of creatures (Pac-Man, ghosts, moving bonus). */
    public static final float SPEED_AT_100_PERCENT = 1.25f;
    public static final float SPEED_GHOST_INSIDE_HOUSE = 0.5f; // correct?
    public static final float SPEED_GHOST_RETURNING_TO_HOUSE = 2.0f; // correct?
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

    private static final byte[][] RAW_LEVEL_DATA = {
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

    public static GameLevelData levelData(int levelNumber) {
        int index = Math.min(levelNumber - 1, RAW_LEVEL_DATA.length - 1);
        return new GameLevelData(RAW_LEVEL_DATA[index]);
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

    private static final File HIGHSCORE_DIRECTORY = new File(System.getProperty("user.home"));
    private static final File HIGHSCORE_PACMAN    = new File(HIGHSCORE_DIRECTORY, "highscore-pacman.xml");
    private static final File HIGHSCORE_MS_PACMAN = new File(HIGHSCORE_DIRECTORY, "highscore-ms_pacman.xml");

    private static File highScoreFile(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN -> HIGHSCORE_MS_PACMAN;
            case PACMAN    -> HIGHSCORE_PACMAN;
        };
    }

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

    public void setLevel(GameLevel level) {
        this.level = level;
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

    public void incrementLevelCounter(byte symbol) {
        levelCounter.add(symbol);
        if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
            levelCounter.removeFirst();
        }
    }

    public void setScoringEnabled(boolean scoringEnabled) {
        this.scoringEnabled = scoringEnabled;
    }

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public int pointsForKillingGhost(int index) {
        short[] points = { 200, 400, 800, 1600 };
        return points[index];
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
        int oldScore = score.points();
        int newScore = oldScore + points;
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

    public void loadHighScore() {
        loadScore(highScore, highScoreFile(variant));
    }

    public void updateHighScore() {
        var file = highScoreFile(variant);
        var oldHighScore = new Score();
        loadScore(oldHighScore, file);
        if (highScore.points() > oldHighScore.points()) {
            saveScore(highScore, file, String.format("%s High Score", variant));
        }
    }

    private static void loadScore(Score score, File file) {
        try (var in = new FileInputStream(file)) {
            var p = new Properties();
            p.loadFromXML(in);
            score.setPoints(Integer.parseInt(p.getProperty("points")));
            score.setLevelNumber(Integer.parseInt(p.getProperty("level")));
            score.setDate(LocalDate.parse(p.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE));
            Logger.info("Score loaded from file '{}'. Points: {} Level: {}",
                file, score.points(), score.levelNumber());
        } catch (Exception x) {
            Logger.error("Score could not be loaded from file '{}'. Error: {}", file, x.getMessage());
        }
    }

    private static void saveScore(Score score, File file, String description) {
        try (var out = new FileOutputStream(file)) {
            var p = new Properties();
            p.setProperty("points", String.valueOf(score.points()));
            p.setProperty("level",  String.valueOf(score.levelNumber()));
            p.setProperty("date",   score.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            p.setProperty("url",    "https://github.com/armin-reichert/pacman-javafx");
            p.storeToXML(out, description);
            Logger.info("Saved '{}' to file '{}'. Points: {} Level: {}",
                description, file, score.points(), score.levelNumber());
        } catch (Exception x) {
            Logger.error("Score could not be saved to file '{}'. Error: {}", file, x.getMessage());
        }
    }
}